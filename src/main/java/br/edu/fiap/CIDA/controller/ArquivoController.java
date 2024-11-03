package br.edu.fiap.CIDA.controller;

import br.edu.fiap.CIDA.entity.Arquivo;
import br.edu.fiap.CIDA.entity.Usuario;
import br.edu.fiap.CIDA.rabbitmq.RabbitMQConfig;
import br.edu.fiap.CIDA.repository.ArquivoRepository;
import br.edu.fiap.CIDA.repository.UsuarioRepository;
import br.edu.fiap.CIDA.service.AzureBlobService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Controller
@RestController
public class ArquivoController {

    @Autowired
    UsuarioRepository usuarioRepo;
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

            return new ModelAndView("enviar_arquivo").addObject("arquivo", arquivo);
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
        Optional<Usuario> usuarioOpt = usuarioRepo.findById(id);
        Usuario user = (Usuario) session.getAttribute("usuario");

        if (user == null) {
            return new ModelAndView("login");
        }

        String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());

        if (file.getContentType() != null && verificaFormato(extension)) {
            try {
                Arquivo arquivo = Arquivo.builder()
                        .nome(file.getOriginalFilename())
                        .extensao(extension)
                        .url(url)
                        .dataUpload(LocalDateTime.now())
                        .tamanho(file.getSize())
                        .usuario(usuarioOpt.orElse(null))
                        .build();

                azureBlobService.uploadFile(file, user.getNomeContainer());
                arquivoRepo.save(arquivo);

                return new ModelAndView("redirect:/{id}/arquivos")
                        .addObject("idUsuario", id)
                        .addObject("usuario", usuarioOpt);
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
        Usuario user = (Usuario) session.getAttribute("usuario");

        if (user != null) {
            Optional<Usuario> userOp = usuarioRepo.findById(idUsuario);

            if (userOp.isPresent()) {
                Usuario usuarioEncontrado = userOp.get();
                List<Arquivo> arquivos = arquivoRepo.findByUsuario(usuarioEncontrado);

                return new ModelAndView("lista_arquivos")
                        .addObject("arquivos", arquivos)
                        .addObject("usuario", usuarioEncontrado);
            }
        }
        return new ModelAndView("error").addObject("message", "Usuário não encontrado.");
    }

    @GetMapping("/{id}/detalhes-arquivo")
    public ModelAndView detalhesArquivo(@PathVariable("id") Long id) {
        Optional<Arquivo> arquivo = arquivoRepo.findById(id);

        return arquivo.map(arquivoFound -> new ModelAndView("detalhes_arquivo").addObject("arquivo", arquivoFound))
                .orElseGet(() -> new ModelAndView("error").addObject("message", "Arquivo não encontrado."));
    }

    @GetMapping("{id}/atualizar-arquivo")
    public ModelAndView retornaAtualizarArquivo(@PathVariable("id") Long id) {
        Optional<Arquivo> arquivoOpt = arquivoRepo.findById(id);

        return arquivoOpt.map(arquivo -> new ModelAndView("atualizar_arquivo").addObject("arquivo", arquivo))
                .orElseGet(() -> new ModelAndView("error").addObject("message", "Arquivo não encontrado."));
    }

    @PostMapping("{id}/atualizar-arquivo")
    public ModelAndView atualizarArquivo(@PathVariable("id") Long id,
                                         @RequestPart("file") MultipartFile file,
                                         @Valid Arquivo arquivoRecebido,
                                         BindingResult result) {
        Optional<Arquivo> arquivoOpt = arquivoRepo.findById(id);

        if (result.hasErrors()) {
            return new ModelAndView("atualizar_arquivo")
                    .addObject("id", id)
                    .addObject("arquivo", arquivoRecebido);
        }

        if (arquivoOpt.isPresent()) {
            String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());
            Arquivo arquivo = arquivoOpt.get();

            if (verificaFormato(extension)) {
                try {
                    arquivo.setNome(file.getOriginalFilename());
                    arquivo.setTamanho(file.getSize());
                    arquivo.setExtensao(extension);
                    arquivo.setDataUpload(LocalDateTime.now());
                    arquivo.setUrl(arquivoRecebido.getUrl());
                    azureBlobService.uploadFile(file, arquivo.getUsuario().getNomeContainer());
                    arquivoRepo.save(arquivo);

                    return new ModelAndView("redirect:/{idUsuario}/arquivos")
                            .addObject("idUsuario", arquivo.getUsuario().getId());
                } catch (RuntimeException e) {
                    return new ModelAndView("error").addObject("message", "Erro ao atualizar o arquivo.");
                }
            } else {
                result.rejectValue("error", null, "Tipo de arquivo não permitido.");
                return new ModelAndView("atualizar_arquivo")
                        .addObject("id", id)
                        .addObject("arquivo", arquivoRecebido);
            }
        }

        return new ModelAndView("error").addObject("message", "Arquivo não encontrado.");
    }

    @GetMapping("{id}/remover-arquivo")
    public ModelAndView removerArquivo(@PathVariable Long id) {
        Optional<Arquivo> arquivoOpt = arquivoRepo.findById(id);

        if (arquivoOpt.isPresent()) {
            arquivoRepo.deleteById(id);
            // Considerando que você deseja redirecionar para a lista de arquivos do usuário associado ao arquivo
            return new ModelAndView("redirect:/{idUsuario}/arquivos")
                    .addObject("idUsuario", arquivoOpt.get().getUsuario().getId());
        }

        return new ModelAndView("error").addObject("message", "Arquivo não encontrado.");
    }

    private boolean verificaFormato(String extension) {
        List<String> textExtensions = Arrays.asList("txt", "doc", "docx", "rtf", "csv", "xml", "pdf");
        return extension != null && textExtensions.contains(extension.toLowerCase());
    }
}
