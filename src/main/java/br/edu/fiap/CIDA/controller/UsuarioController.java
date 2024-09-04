package br.edu.fiap.CIDA.controller;

import br.edu.fiap.CIDA.entity.Arquivo;
import br.edu.fiap.CIDA.entity.Auth;
import br.edu.fiap.CIDA.entity.TipoDocumento;
import br.edu.fiap.CIDA.entity.Usuario;
import br.edu.fiap.CIDA.repository.ArquivoRepository;
import br.edu.fiap.CIDA.repository.UsuarioRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Controller
public class UsuarioController {

    @Autowired
    UsuarioRepository repo;
    @Autowired
    private ArquivoRepository arquivoRepository;
    
    private static final String BASE_FOLDER = "file_server/arquivos_empresas/";
    private final String ARQUIVO_FOLDER = System.getProperty("user.dir") + "/" + BASE_FOLDER;

    @GetMapping("/")
    public String index() {
        return "index"; // Nome do arquivo "index.html" sem a extensão, localizado em "src/main/resources/templates"
    }

    @GetMapping("/teste")
    public String retornarPagina() {
        return "pagina"; // Nome do arquivo "new_user.html" sem a extensão
    }

    @GetMapping("/novo_usuario")
    public ModelAndView novoUsuario() {

        ModelAndView mv = new ModelAndView("new_user");
        mv.addObject("tipoDoc", TipoDocumento.values());
        mv.addObject("usuario", new Usuario());

        return mv;
    }
    @PostMapping("/usuario")
    public ModelAndView insereUsuario(@Valid Usuario user, BindingResult bindingResult) {
        // Verifica se existem erros de validação
        if (bindingResult.hasErrors()) {
            // Retorna a página de criação de usuário com os erros
            return new ModelAndView("new_user");
        } else {
            // Cria o objeto de autenticação
            var auth = Auth.builder()
                    .email(user.getAuth_user().getEmail())
                    .hashSenha(user.getAuth_user().getHashSenha())
                    .ultimoLogin(LocalDateTime.now())
                    .build();

            // Associa o objeto Auth ao usuário
            user.setAuth_user(auth);
            user.setDataCriacao(LocalDateTime.now()); // Define a data de criação como a data atual

            // Salva o usuário
            repo.save(user);
            System.out.println("Direcionando para user_success");

            var mv = new ModelAndView("user_success");
            mv.addObject("usuario",user);

            return mv;
        }
    }

    @GetMapping("/{id}/enviar_arquivo")
    public ModelAndView novoArquivo(@PathVariable Long id) {
        Optional<Usuario> user = repo.findById(id);

        if (user.isPresent()) {
            Usuario usuario = user.get();
            var arq = new Arquivo();
            arq.setUsuario(usuario);

            ModelAndView mv = new ModelAndView("enviar_arquivo");
            mv.addObject("arquivo", arq);
            mv.addObject("usuario", usuario);  // Adiciona o usuário ao modelo
            System.out.println("Id Usuario: " + id);
            System.out.println(arq);
            return mv;
        } else {
            // Caso o usuário não seja encontrado
            return new ModelAndView("error").addObject("message", "Usuário não encontrado");
        }
    }
    @Transactional
    @PostMapping(value = "/{id}/arquivo/upload")
    public ModelAndView uploadArquivo(@RequestPart("file") MultipartFile file,
//                                      BindingResult bindingResult,
                                      @PathVariable("id") Long id,
                                      @RequestParam String url) {
        // Obtém o usuário a partir do ID
        Optional<Usuario> usuarioOptional = repo.findById(id);
        System.out.println(usuarioOptional);
        System.out.println(usuarioOptional.isEmpty());

        // Verifica se o usuário existe
        if (usuarioOptional.isEmpty()) {
            return new ModelAndView("error").addObject("message", "Usuário não encontrado.");
        }

//        // Verifica se há erros de validação
//        if (bindingResult.hasErrors()) {
//            return new ModelAndView("enviar_arquivo");
//        }

        // Obtém a extensão do arquivo
        String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());

        // Lista de extensões permitidas
        List<String> textExtensions = Arrays.asList("txt", "doc", "docx", "rtf", "odt", "csv", "tsv", "json", "xml", "yaml", "yml", "java", "py", "js", "html", "css", "md", "ini", "conf", "properties", "pdf", "sql");

        // Verifica se o tipo de conteúdo é texto ou se a extensão está na lista de extensões permitidas
        if ((file.getContentType() != null && extension != null && textExtensions.contains(extension.toLowerCase()))) {
            try {
                // Cria o objeto Arquivo
                Arquivo arquivo = Arquivo.builder()
                        .nome(file.getOriginalFilename())
                        .extensao(extension)
                        .url(url)
                        .dataUpload(LocalDateTime.now())
                        .tamanho(file.getSize())
                        .usuario(usuarioOptional.get())
                        .build();

                // Salva o objeto Arquivo no banco de dados
                arquivoRepository.save(arquivo);
                
                Path destination = Paths
                        .get(ARQUIVO_FOLDER)
                        .resolve(arquivo.getNome())
                        .normalize()
                        .toAbsolutePath();
                try {
                    if (!Files.exists(Path.of( ARQUIVO_FOLDER ))) Files.createDirectories(Path.of ( ARQUIVO_FOLDER ));
                    Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    System.err.println( "[ IOEXCEPTION ][  ARQUIVO - UPLOAD  ] -  ERRO NO UPLOAD DO ARQUIVO:  " + e.getMessage() );
//                    log.debug("[ IOEXCEPTION ][  ARQUIVO - UPLOAD  ] -  ERRO NO UPLOAD DO ARQUIVO:  " + e.getMessage());
                    
                }

                return new ModelAndView("upload_success").addObject("arquivo", arquivo);

            } catch (Exception e) {
                e.printStackTrace();
                return new ModelAndView("error").addObject("message", "Erro ao salvar o arquivo.");
            }
        } else {
            return new ModelAndView("error").addObject("message", "Tipo de arquivo não permitido.");
        }
    }

    @GetMapping("/login")
    public String login() {
        return "login"; // nome da página de login (login.html)
    }


}
