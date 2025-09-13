package cl.maraneda.signup.repository;

import cl.maraneda.signup.model.Number;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface NumberRepository extends JpaRepository<Number, Integer> {
    public List<Number> findByUserId(@Param("id")String id);

    public int countByUserId(@Param("id")String id);

    @Transactional
    @Modifying
    @Query("DELETE FROM Number n WHERE n.user.id IN (SELECT u.id FROM User u WHERE u.email LIKE CONCAT(:prefix,'%'))")
    public void deleteByUserEmailPrefix(@Param("prefix") String prefix);
}
