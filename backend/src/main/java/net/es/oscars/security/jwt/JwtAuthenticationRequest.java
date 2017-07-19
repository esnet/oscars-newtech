package net.es.oscars.security.jwt;


import lombok.Data;

import java.io.Serializable;

@Data
public class  JwtAuthenticationRequest implements Serializable {

    private static final long serialVersionUID = -8445943548965154778L;


    private String username;
    private String password;

}