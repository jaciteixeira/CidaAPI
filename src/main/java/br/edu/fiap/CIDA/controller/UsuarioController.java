package br.edu.fiap.CIDA.controller;

import br.edu.fiap.CIDA.entity.Auth;
import br.edu.fiap.CIDA.entity.TipoDocumento;
import br.edu.fiap.CIDA.entity.Usuario;
import br.edu.fiap.CIDA.repository.UsuarioRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Controller
public class UsuarioController {

    @Autowired
    UsuarioRepository repo;

    @GetMapping("/")
    public String index() {
        return "index"; // Nome do arquivo "index.html" sem a extensão, localizado em "src/main/resources/templates"
    }

    @GetMapping("/teste")
    public String retornarPagina() {
        return "new_user"; // Nome do arquivo "new_user.html" sem a extensão
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
            System.out.println(user);

            // Retorna para a página de sucesso
            return new ModelAndView("user_success");
        }
    }
}
