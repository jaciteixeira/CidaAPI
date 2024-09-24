package br.edu.fiap.CIDA.controller;

import br.edu.fiap.CIDA.dto.request.UsuarioRequest;
import br.edu.fiap.CIDA.entity.Arquivo;
import br.edu.fiap.CIDA.entity.Auth;
import br.edu.fiap.CIDA.entity.TipoDocumento;
import br.edu.fiap.CIDA.entity.Usuario;
import br.edu.fiap.CIDA.repository.ArquivoRepository;
import br.edu.fiap.CIDA.repository.AuthRepository;
import br.edu.fiap.CIDA.repository.UsuarioRepository;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDateTime;
import java.util.*;

@Controller
@RestController
public class UsuarioController {

    @Autowired
    UsuarioRepository repo;
    @Autowired
    AuthRepository repoAuth;
    @Autowired
    ArquivoRepository arquivoRepo;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/")
    public ModelAndView index(HttpSession session) {
        if (session.getAttribute("usuario") != null) {
            return new ModelAndView("redirect:/home");
        }
        return new ModelAndView("index");
    }

    @GetMapping("/teste")
    public String retornarPagina() {
        return "pagina";
    }

    @GetMapping("/novo_usuario")
    public ModelAndView retornaViewNewUser(HttpSession session) {

        if (session.getAttribute("usuarioRequest") != null) {
            return new ModelAndView("redirect:/home");
        }

        ModelAndView mv = new ModelAndView("new_user");
        mv.addObject("tipoDoc", TipoDocumento.values());
        mv.addObject("usuarioRequest", new UsuarioRequest("", "", "", null, ""));

        return mv;
    }

    @PostMapping("/usuario")
    public ModelAndView insereUsuario(@Valid UsuarioRequest userRequest,
                                      BindingResult bindingResult,
                                      HttpSession session) {

        String prefix = "cida-container-";
        String uuidPart = UUID.randomUUID().toString().replaceAll("-", "").toLowerCase();

        String containerName = prefix + uuidPart;

        if (containerName.length() > 63) {
            containerName = containerName.substring(0, 63);
        }

        var authUser = Auth.builder()
                .email(userRequest.email())
                .hashSenha(passwordEncoder.encode(userRequest.senha()))
                .ultimoLogin(LocalDateTime.now())
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

        if (bindingResult.hasErrors()) {
            var mv = new ModelAndView("new_user");
            mv.addObject("tipoDoc", TipoDocumento.values());
            return mv;
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
        session.setAttribute("usuario", user);

        return new ModelAndView("redirect:/home");
    }


    @GetMapping("/home")
    public ModelAndView transfToHome(HttpSession session) {
        if (session.getAttribute("usuario") == null) {
            return new ModelAndView("redirect:/login"); // Redireciona para home se estiver logado
        }

        Usuario user = (Usuario) session.getAttribute("usuario");

        ModelAndView mv = new ModelAndView("home");
        mv.addObject("user", user);
        return mv;
    }

    @GetMapping("/login")
    public ModelAndView loginPage(HttpSession session) {
        if (session.getAttribute("usuario") != null) {
            return new ModelAndView("redirect:/home");
        }
        return new ModelAndView("login");
    }


    @PostMapping("/login")
    public ModelAndView login(@RequestParam String email, @RequestParam String password, HttpSession session) {
        boolean isAuthenticated = authenticate(email, password);

        if (isAuthenticated) {
            Auth authUser = repoAuth.findByEmail(email).get();
            Usuario usuario = repo.findByAuthUser(authUser);
            session.setAttribute("usuario", usuario);
            usuario.getAuthUser().setUltimoLogin(LocalDateTime.now());
            repo.save(usuario);
            return new ModelAndView("redirect:/home");
        } else {
            ModelAndView mv = new ModelAndView("login");
            mv.addObject("error", "Credenciais inválidas");
            return mv;
        }
    }

    @GetMapping("/{id}/atualizar-usuario")
    public ModelAndView retornaViewatualizaUsuario(@PathVariable Long id,
                                                   HttpSession session) {
        Usuario usuario = repo.findById(id).orElse(null);

//        if (usuario == null) {
//            return new ModelAndView("redirect:/login");
//        }

        Usuario usuarioSessao = (Usuario) session.getAttribute("usuario");
        if (usuarioSessao == null || !usuarioSessao.getId().equals(id)) {
            return new ModelAndView("redirect:/login");
        }

        ModelAndView mv = new ModelAndView("profile_update")
                .addObject("tipoDoc", TipoDocumento.values())
                .addObject("usuario", usuario)
                .addObject("usuarioRequest", new UsuarioRequest("", "", "", null, ""));

        return mv;
    }


    @PostMapping("/{id}/atualizar-usuario")
    public ModelAndView atualizaUsuario(@PathVariable Long id,
                                        UsuarioRequest userRequest,
                                        BindingResult bindingResult,
                                        HttpSession session) {

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

        session.setAttribute("usuario", usuarioAtual);
        return new ModelAndView("redirect:/home");
    }


    @PostMapping("/{id}/remover-conta")
    public ModelAndView removeUsuario(@PathVariable Long id,
                                      HttpSession session) {

        Usuario usuarioAtual = (Usuario) session.getAttribute("usuario");

        if (usuarioAtual == null) {
            return new ModelAndView("redirect:/login");
        }

        try {

            var listArquivos = arquivoRepo.findByUsuario(repo.findById(id).get());
            for (Arquivo arquivo: listArquivos){
                arquivoRepo.deleteById(arquivo.getId());
            }
            repo.deleteById(id);
            session.invalidate();
        } catch (Exception e) {
            ModelAndView mv = new ModelAndView("index");
            mv.addObject("error", "Erro ao remover o usuário");
            return mv;
        }

        return new ModelAndView("redirect:/");
    }


    public boolean authenticate(String email, String rawPassword) {
        Auth authUser = repoAuth.findByEmail(email).get();
        if (authUser != null) {
            return passwordEncoder.matches(rawPassword, authUser.getHashSenha());
        }
        return false;
    }

}
