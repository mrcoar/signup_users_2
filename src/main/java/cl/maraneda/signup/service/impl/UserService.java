package cl.maraneda.signup.service.impl;

import cl.maraneda.signup.exceptions.UserAlreadyExistsException;
import cl.maraneda.signup.dto.UserDTO;
import cl.maraneda.signup.model.User;
import cl.maraneda.signup.repository.UserRepository;
import cl.maraneda.signup.service.IUserService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService implements IUserService {
    private transient final UserRepository repository;
    private transient final ModelMapper mapper;

    @Autowired
    public UserService(UserRepository ur, ModelMapper mm){
        repository = ur;
        mapper = mm;
    }

    @Override
    public UserDTO addUser(UserDTO userDTO) throws UserAlreadyExistsException{
        if(repository.existsByEmail(userDTO.getEmail())){
            throw new UserAlreadyExistsException("The user already exists");
        }
        User user = mapper.map(userDTO, User.class);
        user.setIsActive(Boolean.TRUE);
        User saved = repository.save(user);
        return mapper.map(saved, UserDTO.class);
    }

    @Override
    public UserDTO searchUser(String email, String password) {
        User u = repository.findByEmailAndPassword(email, password);
        return u != null ?
               UserDTO.builder()
                       .id(u.getId())
                       .name(u.getName())
                       .email(u.getEmail())
                       .password(u.getPassword())
                       .created(u.getCreated())
                       .lastLogin(u.getLastLogin())
                       .isActive(u.getIsActive())
                      .build()
               : null;
    }

    @Override
    public Boolean existsByEmail(String email) {
        return repository.existsByEmail(email);
    }

    @Override
    public UserDTO updateUser(UserDTO userDTO) {
        User user = mapper.map(userDTO, User.class);
        User saved = repository.save(user);
        return mapper.map(saved, UserDTO.class);
    }

    public void deleteByUserMailPrefix(String prefix){
        repository.deleteByUserEmailPrefix(prefix);
    }
}
