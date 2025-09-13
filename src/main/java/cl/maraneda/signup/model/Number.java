package cl.maraneda.signup.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;


@Entity
@Table(name = "NUMBERS")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Number {
    @Id
    @Column(name="number", nullable=false)
    private Integer phoneNumber;

    @Column(name="citycode", nullable=false)
    private Integer cityCode;

    @Column(name="countrycode", nullable=false)
    private Integer countryCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="USER_ID", referencedColumnName = "ID", nullable=false)
    private User user;
}
