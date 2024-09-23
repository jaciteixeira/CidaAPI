package br.edu.fiap.CIDA.dto.request;

import br.edu.fiap.CIDA.entity.TipoDocumento;
import jakarta.validation.constraints.*;

public record UsuarioUpdateRequest(

        @NotEmpty(message = "Telefone não pode estar em branco")
        String telefone,
        @NotNull(message = "Selecione o tipo de documento")
        TipoDocumento tipoDoc,
        @NotEmpty(message = "Numero do documento não pode estar em branco")
        String numeroDocumento
) {}
