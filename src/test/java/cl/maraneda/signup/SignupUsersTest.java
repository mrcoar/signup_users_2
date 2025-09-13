package cl.maraneda.signup;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class SignupUsersTest {
    @Test
    public void contextLoads() {
        /*Nothing*/
    }

    @Test
    public void mainApplicationTest(){
        SignupUsers.main(new String[]{});
    }
}
