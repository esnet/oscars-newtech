package net.es.oscars;

import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = CoreUnitTestConfiguration.class)
@TestPropertySource(locations = "classpath:testing.properties")
public abstract class AbstractCoreTest {


}