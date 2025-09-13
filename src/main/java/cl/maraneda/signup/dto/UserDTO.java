package cl.maraneda.signup.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.Date;

@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Data
public class UserDTO {
    protected String id;
    protected String name;
    protected String email;
    protected String password;
    protected Date created;
    protected Date lastLogin;
    protected Boolean isActive;
    protected String token;
}
