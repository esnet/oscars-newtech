package net.es.oscars.ext;

import com.ullink.slack.simpleslackapi.ChannelHistoryModule;
import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import com.ullink.slack.simpleslackapi.impl.ChannelHistoryModuleFactory;
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.app.StartupComponent;
import net.es.oscars.app.exc.StartupException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
@Slf4j
public class SlackConnector implements StartupComponent {
    @Value("${slack.token}")
    private String slackToken;

    @Value("${slack.channel}")
    private String slackChannel;

    @Value("${slack.enable}")
    private Boolean enable;

    private SlackSession session;
    private ChannelHistoryModule channelHistoryModule;

    private SlackChannel channel;
    public void startup() throws StartupException {
        if (!enable) {
            return;
        }
        this.session = SlackSessionFactory.createWebSocketSlackSession(slackToken);
        try {

            session.connect();
            channel = session.findChannelByName(slackChannel);

            //build a channelHistory module from the slack session
            channelHistoryModule = ChannelHistoryModuleFactory.createChannelHistoryModule(session);

        } catch (IOException ex) {
            log.error(ex.getMessage(), ex);
            throw new StartupException(ex.getMessage());

        }

    }

    public void sendMessage(String message) {
        if (!enable) {
            return;
        }

        SlackChannel channel = session.findChannelByName(slackChannel);
        session.sendMessage(channel, message );
    }

    public List<SlackMessagePosted> fetchLastMessagesFromChannelHistory(Integer howMany) {

        return channelHistoryModule.fetchHistoryOfChannel(channel.getId(),howMany);
    }
}
