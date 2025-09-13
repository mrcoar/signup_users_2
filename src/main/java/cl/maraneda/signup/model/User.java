package cl.maraneda.signup.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.Email;
import javax.validation.constraints.Pattern;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "USER")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @Column(name="id", nullable=false, length = 40)
    private String id;

    @Column(name="name", length = 100)
    private String name;

    @Column(name="email", length = 100, nullable = false)
    @Email(message = "Invalid email format")
    private String email;

    @Column(name="password", length=100, nullable = false)
    private String password;

    @Column(name="created")
    @Temporal(TemporalType.TIMESTAMP)
    @CreationTimestamp
    private Date created;

    @Column(name="last_login")
    @Temporal(TemporalType.TIMESTAMP)
    @UpdateTimestamp
    private Date lastLogin;

    @Column(name = "is_active", columnDefinition = "BIT(1) NOT NULL DEFAULT 1")
    private Boolean isActive;

    @Column(name = "token", length = 400, nullable = false)
    @Pattern(regexp = "^[A-Za-z0-9\\-_]+\\.[A-Za-z0-9\\-_]+\\.[A-Za-z0-9\\-_]+$",
             message="The token must be a valid signed JWT")
    private String token;

    @OneToMany(mappedBy="user", fetch=FetchType.LAZY)
    private List<Number> numbers;
}
