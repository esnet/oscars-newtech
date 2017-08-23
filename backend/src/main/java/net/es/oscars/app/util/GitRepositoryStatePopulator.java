package net.es.oscars.app.util;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.app.StartupComponent;
import net.es.oscars.app.exc.StartupException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Properties;

@Slf4j
@Service
@Data
public class GitRepositoryStatePopulator implements StartupComponent {
    GitRepositoryState gitRepositoryState;

    @Autowired
    public GitRepositoryStatePopulator() {
    }

    public void startup() throws StartupException {
//        if (gitRepositoryState == null)
        if (true)
        {
            Properties properties = new Properties();
            try {
                properties.load(getClass().getClassLoader().getResourceAsStream("git.properties"));
            }
            catch (IOException e) {
                throw new StartupException(e.getMessage());
            }
            gitRepositoryState = new GitRepositoryState(properties);
        }
    }
}
