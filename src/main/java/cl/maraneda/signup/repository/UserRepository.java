package cl.maraneda.signup.repository;

import cl.maraneda.signup.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    @Query("SELECT u FROM User u WHERE u.email=:email AND u.password=:password")
    public User findByEmailAndPassword(@Param("email")String email, @Param("password")String password);

    public Boolean existsByEmail(String email);

    @Transactional
    @Modifying
    @Query("DELETE FROM User u WHERE u.email LIKE :prefix || '%'")
    public void deleteByUserEmailPrefix(@Param("prefix") String prefix);

    public User findByEmailAndToken(String email, String token);
}
