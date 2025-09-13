package cl.maraneda.signup.dto;

import cl.maraneda.signup.model.Number;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.List;

@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Data
@EqualsAndHashCode(callSuper = true)
public class CreatedUserDTO extends UserDTO{
    private List<NumberDTO> phones;
}
