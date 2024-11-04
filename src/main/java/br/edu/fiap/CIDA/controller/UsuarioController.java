package br.edu.fiap.CIDA.controller;

import br.edu.fiap.CIDA.dto.request.UsuarioRequest;
import br.edu.fiap.CIDA.entity.*;
import br.edu.fiap.CIDA.repository.ArquivoRepository;
import br.edu.fiap.CIDA.repository.AuthRepository;
import br.edu.fiap.CIDA.repository.RoleRepository;
import br.edu.fiap.CIDA.repository.UsuarioRepository;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.springframework.ai.openai.OpenAiChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class UsuarioController {

    @Autowired
    UsuarioRepository usuarioRepository;
    @Autowired
    AuthRepository authRepository;
    @Autowired
    ArquivoRepository arquivoRepo;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private OpenAiChatClient openAiChatClient;

    @GetMapping("/")
    public String index(HttpSession session) {
        if (session.getAttribute("usuarioRequest") != null) {
            return "home";
        }
        return "index";
    }

    @GetMapping("/teste")
    public String retornarPagina() {
        return "pagina";
    }

    @GetMapping("/novo_usuario")
    public ModelAndView retornaViewNewUser(HttpSession session) {
        if (session.getAttribute("usuarioRequest") != null) {
            return new ModelAndView("home");
        }

        ModelAndView mv = new ModelAndView("new_user");
        mv.addObject("tipoDoc", TipoDocumento.values());
        mv.addObject("usuarioRequest", new UsuarioRequest("", "", "", null, ""));

        return mv;
    }

    @PostMapping("/usuario")
    public ModelAndView insereUsuario(@Valid UsuarioRequest userRequest,
                                      BindingResult bindingResult,
                                      RedirectAttributes redirectAttributes) {
        Optional<Auth> auth = authRepository.findByEmail(userRequest.email());
        if(auth.isPresent()) {
            bindingResult.rejectValue("email", "error.email", "E-mail já cadastrado.");
            var mv = new ModelAndView("new_user");
            mv.addObject("tipoDoc", TipoDocumento.values());
            return mv;
        }

        String prefix = "cida-container-";
        String uuidPart = UUID.randomUUID().toString().replaceAll("-", "").toLowerCase();

        String containerName = prefix + uuidPart;

        if (containerName.length() > 63) {
            containerName = containerName.substring(0, 63);
        }


        String ROLE = (userRequest.email().contains("@opengroup")) ? "ROLE_ADMIN" : "ROLE_USER";

        Role roleUser = roleRepository.findByName(ROLE)
                .orElseThrow(() -> new IllegalArgumentException("Role não encontrada"));

        Set<Role> roles = new HashSet<>();
        roles.add(roleUser);

        var authUser = Auth.builder()
                .email(userRequest.email())
                .hashSenha(passwordEncoder.encode(userRequest.senha()))
                .ultimoLogin(LocalDateTime.now())
                .roles(roles)
                .build();
        var user = Usuario.builder()
                .authUser(authUser)
                .telefone(userRequest.telefone())
                .tipoDoc(userRequest.tipoDoc())
                .numeroDocumento(userRequest.numeroDocumento())
                .dataCriacao(LocalDateTime.now())
                .nomeContainer(containerName)
                .status(Status.ATIVO)
                .build();

        if (user.getAuthUser() != null) {
            Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
            Set<ConstraintViolation<Auth>> authViolations = validator.validate(user.getAuthUser());
            authViolations.forEach(violation ->
                    bindingResult.rejectValue("auth_user." + violation.getPropertyPath(), null, violation.getMessage())
            );
        }

        if (bindingResult.hasErrors()) {
            var mv = new ModelAndView("new_user");
            mv.addObject("tipoDoc", TipoDocumento.values());
            return mv;
        }

        try {
            usuarioRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            String errorMessage = "Erro ao salvar o usuário: " + e.getMostSpecificCause().getMessage();
            redirectAttributes.addFlashAttribute("error", errorMessage);

            var mv = new ModelAndView("new_user");
            mv.addObject("tipoDoc", TipoDocumento.values());
            return mv;
        }
        redirectAttributes.addFlashAttribute("successMessage", "Cadastrado com Sucesso");
        return new ModelAndView("redirect:/login");
    }

    @GetMapping("/home")
    public ModelAndView transfToHome(HttpSession session) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        String username = auth.getName();
        var authUser = authRepository.findByEmail(username);
        var user = usuarioRepository.findByAuthUser(authUser.get());

        ModelAndView mv = new ModelAndView("home");
        mv.addObject("user", user);

        List<String> roles = user.getAuthUser().getRoles().stream()
                .map(Role::getName)  // Pega apenas o nome de cada Role
                .collect(Collectors.toList());  // Coleta em uma lista de strings

        mv.addObject("roles", roles);
        session.setAttribute("roles", roles);
        session.setAttribute("usuario", user);
        return mv;
    }

    @GetMapping("/login")
    public String login(HttpSession session) {
        if (session.getAttribute("usuario") != null ) {
            return "redirect:/home";
        }
        return "login";
    }

    @GetMapping("/{id}/atualizar-usuario")
    public ModelAndView retornaViewatualizaUsuario(@PathVariable Long id,
                                                   HttpSession session) {
        Usuario usuario = usuarioRepository.findById(id).orElse(null);

        Usuario usuarioSessao = (Usuario) session.getAttribute("usuario");
        if (usuarioSessao == null || !usuarioSessao.getId().equals(id)) {
            return new ModelAndView("redirect:/login");
        }

        return new ModelAndView("profile_update")
                .addObject("tipoDoc", TipoDocumento.values())
                .addObject("usuario", usuario)
                .addObject("usuarioRequest", new UsuarioRequest("", "", "", null, ""));

    }

    @PostMapping("/{id}/atualizar-usuario")
    public ModelAndView atualizaUsuario(@PathVariable Long id,
                                        UsuarioRequest userRequest,
                                        BindingResult bindingResult,
                                        HttpSession session) {

        Usuario usuarioAtual = usuarioRepository.findById(id).orElse(null);
        System.out.println(userRequest);
        System.out.println(usuarioAtual);
        System.out.println(bindingResult);

        if (bindingResult.hasErrors()) {
            ModelAndView mv = new ModelAndView("profile_update")
                    .addObject("id", id)
                    .addObject("tipoDoc", TipoDocumento.values())
                    .addObject("usuario", usuarioAtual);
            return mv;
        }

        usuarioAtual.setTelefone(userRequest.telefone());
        usuarioAtual.setTipoDoc(userRequest.tipoDoc());
        usuarioAtual.setNumeroDocumento(userRequest.numeroDocumento());

        try {
            usuarioRepository.save(usuarioAtual);
        } catch (DataIntegrityViolationException e) {
            String errorMessage = "Erro ao atualizar o usuário: " + e.getMostSpecificCause().getMessage();
            bindingResult.reject("error.usuario", errorMessage);

            ModelAndView mv = new ModelAndView("profile_update")
                    .addObject("tipoDoc", TipoDocumento.values())
                    .addObject("usuario", usuarioAtual);
            return mv;
        }
        session.setAttribute("usuario", usuarioAtual);
        return new ModelAndView("redirect:/home");
    }

    @PostMapping("/{id}/remover")
    public String removerUsuario(@PathVariable Long id,
                                       HttpSession session,
                                       RedirectAttributes redirectAttributes) {
        Usuario usuarioAtual = (Usuario) session.getAttribute("usuario");

        if (usuarioAtual == null) {
            return "redirect:/login";
        }
        try {
            var usuario = usuarioRepository.findById(id).orElse(null);
            if (usuario != null) {
                usuario.setStatus(Status.INATIVO);
                usuarioRepository.save(usuario);
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Erro ao excluir conta. "+e.getMessage());
            return "/home";
        }
        return "redirect:/logout";
    }


    @PostMapping("/{id}/remover-conta")
    public ModelAndView removeUsuario(@PathVariable Long id,
                                      HttpSession session,
                                      RedirectAttributes redirectAttributes) {
        Usuario usuarioAtual = (Usuario) session.getAttribute("usuario");
        if (usuarioAtual == null) {
            return new ModelAndView("redirect:/login");
        }
        try {
            var user = usuarioRepository.findById(id).orElse(null);
            if(user != null && user.getStatus() == Status.INATIVO) {

                var listArquivos = arquivoRepo.findByUsuario(usuarioRepository.findById(id).get());
                for (Arquivo arquivo: listArquivos){
                    arquivoRepo.deleteById(arquivo.getId());
                }
                usuarioRepository.deleteById(id);
                redirectAttributes.addFlashAttribute("message", "Usuário removido da base de dados!");
            } else {
                redirectAttributes.addFlashAttribute("message", "Usuário ativo no sistema");
            }
        } catch (Exception e) {
            ModelAndView mv = new ModelAndView("index");
            mv.addObject("error", "Erro ao remover o usuário");
            return mv;
        }
        return new ModelAndView("redirect:/all/usuarios");
    }

    @GetMapping("/acesso_negado")
    public String retornaAcessoNegado() {
        return"acesso_negado";
    }

    @GetMapping("/all/usuarios")
    public ModelAndView retornaViewListaUsuarios(HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            return new ModelAndView("redirect:/login");
        }
        List<Usuario> usuarios = usuarioRepository.findAll();
        Iterator<Usuario> iterator = usuarios.iterator();
        while (iterator.hasNext()) {
            Usuario u = iterator.next();
            if (u.getAuthUser().getEmail().contains("@opengroup")) {
                iterator.remove();
            }
        }
        ModelAndView mv = new ModelAndView("lista_usuarios")
                .addObject("usuarios", usuarios);
        return mv;
    }

    @GetMapping("/{id}/ativar-conta")
    public String ativarConta(@PathVariable Long id){
        usuarioRepository.findById(id).ifPresent(usuario -> {
            usuario.setStatus(Status.ATIVO);
            usuarioRepository.save(usuario);
        });
        return "redirect:/all/usuarios";
    }

    @GetMapping("/chat_gpt")
    public String retornaFormChatGPT() {
        return "chat_gpt";
    }

    @PostMapping("/pergunta_chat_gpt")
    public String enviarPerguntaChatGPT(@RequestParam(name = "pergunta") String pergunta, Model model) {
        String resposta = openAiChatClient.call(pergunta);
        model.addAttribute("var", resposta);
        return "chat_gpt";
    }
}
