package cl.maraneda.signup.controller;

import cl.maraneda.signup.exceptions.UserAlreadyExistsException;
import cl.maraneda.signup.Util;
import cl.maraneda.signup.dto.CreatedUserDTO;
import cl.maraneda.signup.dto.ErrorDTO;
import cl.maraneda.signup.dto.InputUserDTO;
import cl.maraneda.signup.dto.UserDTO;
import cl.maraneda.signup.service.impl.NumberService;
import cl.maraneda.signup.service.impl.UserService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.InvalidKeyException;
import io.jsonwebtoken.security.WeakKeyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.RollbackException;
import javax.validation.ConstraintViolationException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private transient UserService userService;

    @Autowired
    private transient NumberService numberService;

    @PutMapping(value="/sign-up", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> addUser(@RequestBody InputUserDTO juserDTO){
        if(juserDTO.getEmail()==null || juserDTO.getEmail().isBlank()){
            return new ResponseEntity<>(
                ErrorDTO.builder().detail("Email is required to sign up the user").build(),
                HttpStatus.BAD_REQUEST);
        }
        if(juserDTO.getPassword()==null || juserDTO.getPassword().isBlank()){
            return new ResponseEntity<>(
                ErrorDTO.builder().detail("Password is required to sign up the user").build(),
                HttpStatus.BAD_REQUEST);
        }

        if(juserDTO.getName()==null || juserDTO.getName().isBlank()){
            return new ResponseEntity<>(
                    ErrorDTO.builder().detail("Name is required to sign up the user").build(),
                    HttpStatus.BAD_REQUEST);
        }

        if(!Util.PASSWORD_PATTERN.matcher(juserDTO.getPassword()).matches()){
            return new ResponseEntity<>(
                ErrorDTO.builder().detail(
                    "The password must contain between 8 and 12 characters." +
                    "Also, it must contain exactly 1 upper case letter, " +
                    "exactly 2 digits and the rest of the characters must be " +
                    "lower case characters. The digits don't need to be consecutives."
                ).build(),
                HttpStatus.BAD_REQUEST);
        }
        if(juserDTO.getPhones()==null || juserDTO.getPhones().isEmpty()){
            return new ResponseEntity<>(
                ErrorDTO.builder().detail(
                    "The user must have at least one phone number"
                ).build(),
                HttpStatus.BAD_REQUEST);
        }
        try{
            String name = Optional.ofNullable(juserDTO.getName()).orElse("Unnamed User");
            String jws =
                Jwts.builder()
                    .subject(name)
                    .issuer(name)
                    .claims(Map.of(
                        "name", name,
                        "email", juserDTO.getEmail(),
                        "password", juserDTO.getPassword(),
                        "lastLogin", new Date()))
                    .issuedAt(new Date())
                    .signWith(Util.getJWTSignatureFromString(juserDTO.getEmail()))
                    .compact();
            //Store the user
            UserDTO user =
                UserDTO.builder()
                       .id(UUID.randomUUID().toString())
                       .name(juserDTO.getName())
                       .email(juserDTO.getEmail())
                       .password(
                           Base64.getEncoder().encodeToString(juserDTO.getPassword().getBytes())
                       )
                       .token(jws)
                       .build();
            UserDTO saved = userService.addUser(user);
            CreatedUserDTO createdUserDTO =
                CreatedUserDTO
                    .builder()
                    .id(saved.getId())
                    .name(saved.getName())
                    .email(saved.getEmail())
                    .created(saved.getCreated())
                    .lastLogin(saved.getLastLogin())
                    .isActive(saved.getIsActive())
                    .password(juserDTO.getPassword())
                    .token(jws)
                    .build();
            //Store the numbers
            numberService.addAll(juserDTO.getPhones(), saved);
            createdUserDTO.setPhones(juserDTO.getPhones());
            return new ResponseEntity<>(createdUserDTO, HttpStatus.CREATED);
        }catch(UserAlreadyExistsException e){
            return new ResponseEntity<>(
                ErrorDTO.builder().detail(e.getMessage()).build(),
                HttpStatus.FORBIDDEN);
        }catch(TransactionSystemException e){
            if(e.getCause()!=null &&
               e.getCause() instanceof RollbackException &&
               e.getCause().getCause() != null &&
               e.getCause().getCause() instanceof ConstraintViolationException &&
               e.getCause().getCause().getMessage().contains("Invalid email format")){
                return new ResponseEntity<>(
                    ErrorDTO.builder().detail(
                        "The inputted email has an invalid format. " +
                        "The valid format is <name>@<domain>.<ext>." +
                        "Refer to RFC-5322 for more details"
                    ).build(),
                    HttpStatus.BAD_REQUEST);
            }else{
                return new ResponseEntity<>(
                    ErrorDTO.builder().detail(
                        "Internal transaction error: " + e.getMessage()
                    ).build(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }catch(InvalidKeySpecException e){
            return new ResponseEntity<>(
                ErrorDTO.builder()
                        .detail("Internal error when generating secret for the JWT signature.").build(),
                HttpStatus.INTERNAL_SERVER_ERROR);
        }catch(NoSuchAlgorithmException e){
            return new ResponseEntity<>(
                ErrorDTO.builder()
                        .detail("Unsupported algorithm used to generate secret key.").build(),
                HttpStatus.INTERNAL_SERVER_ERROR);
        }catch(WeakKeyException e){
            return new ResponseEntity<>(
                ErrorDTO.builder()
                        .detail("Generated secret key not strong enough to sign JWT.").build(),
                HttpStatus.INTERNAL_SERVER_ERROR);
        }catch(InvalidKeyException e){
            return new ResponseEntity<>(
                ErrorDTO.builder()
                        .detail("Generated invalid secret key.").build(),
                HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
