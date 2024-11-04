package br.edu.fiap.CIDA.controller;

import br.edu.fiap.CIDA.dto.request.UsuarioRequest;
import br.edu.fiap.CIDA.entity.Arquivo;
import br.edu.fiap.CIDA.entity.Auth;
import br.edu.fiap.CIDA.entity.Role;
import br.edu.fiap.CIDA.entity.TipoDocumento;
import br.edu.fiap.CIDA.entity.Usuario;
import br.edu.fiap.CIDA.repository.ArquivoRepository;
import br.edu.fiap.CIDA.repository.AuthRepository;
import br.edu.fiap.CIDA.repository.RoleRepository;
import br.edu.fiap.CIDA.repository.UsuarioRepository;
import br.edu.fiap.CIDA.service.UsuarioUserDetailsService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class UsuarioController {

    @Autowired
    UsuarioRepository repo;
    @Autowired
    AuthRepository repoAuth;
    @Autowired
    ArquivoRepository arquivoRepo;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/teste")
    public String retornarPagina() {
        return "pagina";
    }

    @GetMapping("/novo_usuario")
    public ModelAndView retornaViewNewUser() {

        ModelAndView mv = new ModelAndView("new_user");
        mv.addObject("tipoDoc", TipoDocumento.values());
        mv.addObject("usuarioRequest", new UsuarioRequest("", "", "", null, ""));

        return mv;
    }

    @PostMapping("/usuario")
    public ModelAndView insereUsuario(@Valid UsuarioRequest userRequest,
                                      BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            var mv = new ModelAndView("new_user");
            mv.addObject("tipoDoc", TipoDocumento.values());
            return mv;
        } else {


            Optional<Auth> auth = repoAuth.findByEmail(userRequest.email());
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
                    .build();

            if (user.getAuthUser() != null) {
                Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
                Set<ConstraintViolation<Auth>> authViolations = validator.validate(user.getAuthUser());
                authViolations.forEach(violation ->
                        bindingResult.rejectValue("auth_user." + violation.getPropertyPath(), null, violation.getMessage())
                );
            }


            try {
                repo.save(user);

            } catch (DataIntegrityViolationException e) {
                String errorMessage = "Erro ao salvar o usuário: " + e.getMostSpecificCause().getMessage();
                bindingResult.reject("error.usuario", errorMessage);

                var mv = new ModelAndView("new_user");
                mv.addObject("tipoDoc", TipoDocumento.values());
                return mv;
            }

            return new ModelAndView("redirect:/home");
        }
    }


    @GetMapping("/home")
    public ModelAndView transfToHome(HttpSession session) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        String email = auth.getName();

        Auth autenticacao = repoAuth.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        Usuario usuario  = repo.findByAuthUser(autenticacao);

        session.setAttribute("usuario", usuario);

        ModelAndView mv = new ModelAndView("home");

        mv.addObject("usuario", usuario);
        return mv;
    }

        @GetMapping("/login")
        public String login () {
            return "login";
        }



//    @PostMapping("/login")
//    public ModelAndView login(@RequestParam String email, @RequestParam String password) {
//        boolean isAuthenticated = authenticate(email, password);
//        System.out.println("DENTRO DE /login");
//
//        if (isAuthenticated) {
//            Auth authUser = repoAuth.findByEmail(email).get();
//            Usuario usuario = repo.findByAuthUser(authUser);
//            usuario.getAuthUser().setUltimoLogin(LocalDateTime.now());
//            repo.save(usuario);
//            return new ModelAndView("redirect:/home");
//        } else {
//            ModelAndView mv = new ModelAndView("login");
//            mv.addObject("error", "Credenciais inválidas");
//            System.out.println(mv);
//            return mv;
//        }
//    }

    @GetMapping("/{id}/atualizar-usuario")
    public ModelAndView retornaViewatualizaUsuario(@PathVariable Long id) {
        Usuario usuario = repo.findById(id).orElse(null);

        return new ModelAndView("profile_update")
                .addObject("tipoDoc", TipoDocumento.values())
                .addObject("usuario", usuario)
                .addObject("usuarioRequest", new UsuarioRequest("", "", "", null, ""));

    }


    @PostMapping("/{id}/atualizar-usuario")
    public ModelAndView atualizaUsuario(@PathVariable Long id,
                                        UsuarioRequest userRequest,
                                        BindingResult bindingResult) {

        Usuario usuarioAtual = repo.findById(id).orElse(null);
        System.out.println(userRequest);
        System.out.println(usuarioAtual);
        System.out.println(bindingResult);

//        if (usuarioAtual == null) {
//            return new ModelAndView("redirect:/login");
//        }

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
            repo.save(usuarioAtual);
        } catch (DataIntegrityViolationException e) {
            String errorMessage = "Erro ao atualizar o usuário: " + e.getMostSpecificCause().getMessage();
            bindingResult.reject("error.usuario", errorMessage);

            ModelAndView mv = new ModelAndView("profile_update")
                    .addObject("tipoDoc", TipoDocumento.values())
                    .addObject("usuario", usuarioAtual);
            return mv;
        }

        return new ModelAndView("redirect:/home");
    }


    @PostMapping("/{id}/remover-conta")
    public ModelAndView removeUsuario(@PathVariable Long id) {

        try {

            var listArquivos = arquivoRepo.findByUsuario(repo.findById(id).get());
            for (Arquivo arquivo: listArquivos){
                arquivoRepo.deleteById(arquivo.getId());
            }
            repo.deleteById(id);
        } catch (Exception e) {
            ModelAndView mv = new ModelAndView("index");
            mv.addObject("error", "Erro ao remover o usuário");
            return mv;
        }

        return new ModelAndView("redirect:/");
    }


//    public boolean authenticate(String email, String rawPassword) {
//        Auth authUser = repoAuth.findByUsername(email).get();
//        if (authUser != null) {
//            return passwordEncoder.matches(rawPassword, authUser.getHashSenha());
//        }
//        return false;
//    }

}
