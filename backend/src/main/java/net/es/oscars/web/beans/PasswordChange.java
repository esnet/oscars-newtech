package net.es.oscars.web.beans;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor(suppressConstructorProperties=true)
public class PasswordChange {
    private String oldPassword;
    private String newPassword;

}
