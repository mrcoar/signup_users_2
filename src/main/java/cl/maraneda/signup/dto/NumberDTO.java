package cl.maraneda.signup.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Data
public class NumberDTO {
    private Integer phoneNumber;
    private Integer cityCode;
    private Integer countryCode;
}
