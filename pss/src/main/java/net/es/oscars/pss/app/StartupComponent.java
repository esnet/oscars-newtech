package net.es.oscars.pss.app;

import net.es.oscars.pss.app.exc.StartupException;

public interface StartupComponent {
    void startup() throws StartupException;
}
