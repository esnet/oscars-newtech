package net.es.oscars.app;

import net.es.oscars.app.exc.StartupException;

public interface StartupComponent {
    void startup() throws StartupException;
}
