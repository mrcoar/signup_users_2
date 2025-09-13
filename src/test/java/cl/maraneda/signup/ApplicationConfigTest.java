package cl.maraneda.signup;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * @author FullStack Labs
 * @version 1.0
 * @since 2022-10
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = ApplicationConfig.class)
public class ApplicationConfigTest {
    @Autowired
    transient ApplicationConfig applicationConfig;
    @Test
    void modelMapperTest() {
        ModelMapper mapper = applicationConfig.modelMapper();
        Assertions.assertThat(mapper).isInstanceOf(ModelMapper.class);
    }

}
