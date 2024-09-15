package br.edu.fiap.CIDA.dto.request;

import br.edu.fiap.CIDA.entity.TipoDocumento;
import jakarta.validation.constraints.*;

public record UsuarioRequest(
        @NotBlank(message = "O email é obrigatório!")
        @Email(message = "Email inválido!")
        String email,
        @Pattern(regexp = "(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%^&*_=+-]).{8,16}", message = "A senha deve seguir o padrão: ")
        @NotEmpty(message = "Senha é obrigatório!")
        String senha,

        @NotEmpty(message = "Telefone não pode estar em branco")
        String telefone,
        @NotNull(message = "Selecione o tipo de documento")
        TipoDocumento tipoDoc,
        @NotEmpty(message = "Numero do documento não pode estar em branco")
        String numeroDocumento
) {}
