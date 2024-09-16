package br.edu.fiap.CIDA.controller;

import br.edu.fiap.CIDA.entity.Arquivo;
import br.edu.fiap.CIDA.entity.Usuario;
import br.edu.fiap.CIDA.repository.ArquivoRepository;
import br.edu.fiap.CIDA.repository.UsuarioRepository;
import br.edu.fiap.CIDA.service.AzureBlobService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
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

//        List<String> textExtensions = Arrays.asList("txt", "doc", "docx", "rtf", "csv", "xml", "pdf");

        if ((file.getContentType() != null && verificaFormato(extension))) {
            try {
                Arquivo arquivo = Arquivo.builder()
                        .nome(file.getOriginalFilename())
                        .extensao(extension)
                        .url(url)
                        .dataUpload(LocalDateTime.now())
                        .tamanho(file.getSize())
                        .usuario(usuarioOpt.get())
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
        System.out.println(idUsuario);
        Usuario user = (Usuario) session.getAttribute("usuario");
        System.out.println(user);

        var userOp = UsuarioRepo.findById(idUsuario);

        if (user != null || userOp.isPresent()) {
            Usuario usuarioEncontrado = userOp.get();

            List<Arquivo> arquivos = arquivoRepo.findByUsuario(usuarioEncontrado);

            ModelAndView mv = new ModelAndView("lista_arquivos");

            mv.addObject("arquivos", arquivos);
            mv.addObject("usuario", usuarioEncontrado);

            return mv;
        }

        return new ModelAndView("error");
    }

    @GetMapping("/{id}/detalhes-arquivo")
    public ModelAndView detalhesArquivo(@PathVariable("id") Long id){

        var arquivo = arquivoRepo.findById(id);
        if (arquivo.isPresent()){
            return new  ModelAndView("detalhes_arquivo").addObject("arquivo",arquivo.get());
        }
        return new ModelAndView("error").addObject("message", "Arquivo não encontrado.");
    }

    @GetMapping("{id}/atualizar-arquivo")
    public ModelAndView retornaAtualizarArquivo(@PathVariable("id") Long id){
        var arquivo = arquivoRepo.findById(id).get();
        System.out.println(arquivo);
        return new ModelAndView("atualizar_arquivo")
                .addObject("arquivo", arquivo);
    }

    @PostMapping("{id}/atualizar-arquivo")
    public ModelAndView atualizarArquivo(@PathVariable("id") Long id,
                                         MultipartFile file,
                                         @Valid Arquivo arquivoRecebido,
                                         BindingResult result){

        var arquivoOpt = arquivoRepo.findById(id);

        if(result.hasErrors()) {
            ModelAndView mv = new ModelAndView("atualiza_arquivo");
            mv.addObject("id", id);
            mv.addObject("arquivo", arquivoOpt);
            return mv;
        }else {
            String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());
            var arquivo = arquivoOpt.get();
            if (arquivoOpt.isPresent() && verificaFormato(extension)){
                try {
                    arquivo.setNome(file.getOriginalFilename());
                    arquivo.setTamanho(file.getSize());
                    arquivo.setExtensao(StringUtils.getFilenameExtension(file.getOriginalFilename()));
                    arquivo.setDataUpload(LocalDateTime.now());
                    arquivo.setUrl(arquivoRecebido.getUrl());
                    azureBlobService.uploadFile(file, arquivo.getUsuario().getNomeContainer());
                    arquivoRepo.save(arquivo);

                    return new ModelAndView("redirect:/{idUsuario}/arquivos")
                            .addObject("idUsuario", id);
                }catch (RuntimeException e){
                    return new ModelAndView("error").addObject("message", "Erro ao atualizar o arquivo.");
                }
            }
            else {
                result.rejectValue("error", null, "Tipo de arquivo não permitido.");
                return new ModelAndView("redirect:/{id}/atualizar-arquivo")
                        .addObject("idUsuario", id);
            }
        }
    }

    private boolean verificaFormato(String extension){
        List<String> textExtensions = Arrays.asList("txt", "doc", "docx", "rtf", "csv", "xml", "pdf");
        return extension != null && textExtensions.contains(extension.toLowerCase());
    }


}
