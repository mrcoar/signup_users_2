package cl.maraneda.signup.service;

import cl.maraneda.signup.dto.NumberDTO;
import cl.maraneda.signup.dto.UserDTO;

import java.util.List;

public interface INumberService {
    public void addAll(List<NumberDTO> numbers, UserDTO userDTO);
    public List<NumberDTO> getByUser(String userId);
}
