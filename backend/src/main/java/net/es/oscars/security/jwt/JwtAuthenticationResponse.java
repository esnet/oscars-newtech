package net.es.oscars.security.jwt;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;


@Data
@Builder
public class JwtAuthenticationResponse implements Serializable {

    private static final long serialVersionUID = 1250166508152483573L;

    private final String token;

    private final boolean admin;

}