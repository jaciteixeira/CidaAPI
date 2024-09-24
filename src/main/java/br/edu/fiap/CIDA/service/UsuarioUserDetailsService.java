package br.edu.fiap.CIDA.service;

import java.util.stream.Collectors;

import br.edu.fiap.CIDA.entity.Auth;
import br.edu.fiap.CIDA.repository.AuthRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


@Service
public class UsuarioUserDetailsService implements UserDetailsService {

    @Autowired
    private AuthRepository uRep;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        Auth resgatado = uRep.findByEmail(email).orElseThrow(
                () -> new UsernameNotFoundException("Usuário " + email + " não encontrado na base de dados"));

        return new User(resgatado.getEmail(), resgatado.getHashSenha(), resgatado.getRoles()
                .stream().map(role -> new SimpleGrantedAuthority(role.getNome())).collect(Collectors.toList()));
    }

}