package cl.maraneda.signup.service;

import cl.maraneda.signup.exceptions.UserAlreadyExistsException;
import cl.maraneda.signup.dto.UserDTO;

public interface IUserService {
    public UserDTO addUser(UserDTO user) throws UserAlreadyExistsException;
    public UserDTO searchUser(String email, String password);
    public Boolean existsByEmail(String email);
    public UserDTO updateUser(UserDTO user);
}
