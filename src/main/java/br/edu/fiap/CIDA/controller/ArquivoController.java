package br.edu.fiap.CIDA.controller;

import br.edu.fiap.CIDA.entity.Arquivo;
import br.edu.fiap.CIDA.entity.Usuario;
import br.edu.fiap.CIDA.repository.ArquivoRepository;
import br.edu.fiap.CIDA.repository.UsuarioRepository;
import br.edu.fiap.CIDA.service.AzureBlobService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Controller
public class ArquivoController {

    @Autowired
    UsuarioRepository UsuarioRepo;
    @Autowired
    private ArquivoRepository arquivoRepo;
    @Autowired
    private AzureBlobService azureBlobService;

    @GetMapping("/{id}/enviar_arquivo")
    public ModelAndView novoArquivo(@PathVariable("id") Long id, HttpSession session) {
        Usuario user = (Usuario) session.getAttribute("usuario");

        if (user != null) {
            var arquivo = new Arquivo();
            arquivo.setUsuario(user);

            ModelAndView mv = new ModelAndView("enviar_arquivo");
            mv.addObject("arquivo", arquivo);
            return mv;
        } else {
            return new ModelAndView("error").addObject("message", "Usuário não encontrado");
        }
    }

    @Transactional
    @PostMapping(value = "/{id}/arquivo/upload")
    public ModelAndView uploadArquivo(@RequestPart("file") MultipartFile file,
                                      @PathVariable("id") Long id,
                                      @RequestParam String url,
                                      HttpSession session) {
        Optional<Usuario> usuarioOpt = UsuarioRepo.findById(id);

        Usuario user = (Usuario) session.getAttribute("usuario");

        if (user == null) {
            return new ModelAndView("login");
        }

        String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());

        List<String> textExtensions = Arrays.asList("txt", "doc", "docx", "rtf", "csv", "xml", "pdf");

        if ((file.getContentType() != null && extension != null && textExtensions.contains(extension.toLowerCase()))) {
            try {
                Arquivo arquivo = Arquivo.builder()
                        .nome(file.getOriginalFilename()+ "-" + UUID.randomUUID().toString())
                        .extensao(extension)
                        .url(user.getNomeContainer())
                        .dataUpload(LocalDateTime.now())
                        .tamanho(file.getSize())
                        .usuario(usuarioOpt.get())
                        .build();


                azureBlobService.uploadFile(file, user.getNomeContainer());

                arquivoRepo.save(arquivo);

                return new ModelAndView("redirect: /1/arquivos").addObject("usuario", user);

            } catch (Exception e) {
                e.printStackTrace();
                return new ModelAndView("error").addObject("message", "Erro ao salvar o arquivo.");
            }
        } else {
            return new ModelAndView("error").addObject("message", "Tipo de arquivo não permitido.");
        }
    }

    @GetMapping("/{idUsuario}/arquivos")
    public ModelAndView listarArquivos(@PathVariable("idUsuario") Long idUsuario, HttpSession session) {
        // Recupera o usuário da sessão
        Usuario user = (Usuario) session.getAttribute("usuario");

        // Procura o usuário no repositório
        var userOp = UsuarioRepo.findById(idUsuario);

        // Verifica se o usuário da sessão não é nulo e o usuário com o id existe no banco de dados
        if (user != null || userOp.isPresent()) {
            // Obtém o objeto Usuario do Optional
            Usuario usuarioEncontrado = userOp.get();

            // Busca os arquivos relacionados ao usuário encontrado
            List<Arquivo> arquivos = arquivoRepo.findByUsuario(usuarioEncontrado);

            // Cria o ModelAndView para renderizar a página
            ModelAndView mv = new ModelAndView("lista_arquivos");

            // Adiciona a lista de arquivos e o usuário ao modelo
            mv.addObject("arquivos", arquivos);
            mv.addObject("usuario", usuarioEncontrado);

            System.out.println(arquivos);  // Apenas para debug
            return mv;
        }

        // Se o usuário não estiver presente ou não for encontrado, retorna a página de erro
        return new ModelAndView("error");
    }

}
