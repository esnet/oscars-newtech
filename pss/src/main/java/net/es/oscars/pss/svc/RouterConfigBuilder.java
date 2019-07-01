package net.es.oscars.pss.svc;


import net.es.oscars.dto.pss.cmd.Command;
import net.es.oscars.dto.pss.cmd.CommandType;
import net.es.oscars.dto.pss.cmd.GeneratedCommands;
import net.es.oscars.dto.topo.DeviceModel;
import net.es.oscars.pss.beans.ConfigException;
import net.es.oscars.pss.beans.PssProfile;
import net.es.oscars.pss.beans.UrnMappingException;
import net.es.oscars.pss.prop.PssProps;
import net.es.oscars.pss.prop.RancidProps;
import net.es.oscars.pss.rancid.RancidArguments;
import net.es.oscars.pss.rest.BackendServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RouterConfigBuilder {
    private PssProps pssProps;
    private BackendServer bes;
    private UrnMappingService ums;

    @Autowired
    public RouterConfigBuilder(PssProps pssProps,
                               BackendServer bes,
                               UrnMappingService ums) {
        this.pssProps = pssProps;
        this.ums = ums;
        this.bes = bes;
    }


    public RancidArguments controlPlaneCheck(String deviceUrn, DeviceModel model, String profile)
            throws ConfigException, UrnMappingException  {
        String routerConfig;
        switch (model) {
            case ALCATEL_SR7750:
                routerConfig = "echo \"OSCARS PSS control plane check\"";
                break;
            case JUNIPER_MX:
            case JUNIPER_EX:
                routerConfig = "show chassis hardware";
                break;
            default:
                throw new ConfigException("unknown model");
        }

        return buildRouterConfig(routerConfig, deviceUrn, model, profile);
    }

    public RancidArguments getConfig(String deviceUrn, DeviceModel model, String profile)
            throws ConfigException, UrnMappingException  {
        String routerConfig;

        switch (model) {
            case ALCATEL_SR7750:
                routerConfig = "admin display-config\n";
                break;
            case JUNIPER_MX:
            case JUNIPER_EX:
                routerConfig = "show configuration | display xml\n";
                break;
            default:
                throw new ConfigException("unknown model");
        }

        return buildRouterConfig(routerConfig, deviceUrn, model, profile);

    }

    public RancidArguments build(Command command)
            throws ConfigException, UrnMappingException  {

        GeneratedCommands cmds = bes.commands(command.getConnectionId(), command.getDevice(), command.getProfile());
        String routerConfig = cmds.getGenerated().get(CommandType.BUILD);
        if (routerConfig == null) {
            throw new ConfigException("Null generated commands for BUILD");
        }
        return buildRouterConfig(routerConfig, command.getDevice(), command.getModel(), command.getProfile());
    }

    public RancidArguments dismantle(Command command) throws ConfigException, UrnMappingException  {
        GeneratedCommands cmds = bes.commands(command.getConnectionId(), command.getDevice(), command.getProfile());
        String routerConfig = cmds.getGenerated().get(CommandType.DISMANTLE);
        if (routerConfig == null) {
            throw new ConfigException("Null generated commands for BUILD");
        }


        return buildRouterConfig(routerConfig, command.getDevice(), command.getModel(), command.getProfile());
    }


    public RancidArguments buildRouterConfig(String routerConfig, String deviceUrn, DeviceModel model, String profileName)
            throws ConfigException, UrnMappingException {
        String execPath;

        PssProfile pssProfile = PssProfile.find(pssProps, profileName);


        RancidProps props = pssProfile.getRancid();

        String cloginrc = props.getCloginrc();
        String dir = props.getDir();
        String router = ums.getRouterAddress(deviceUrn, pssProfile);


        switch (model) {
            case ALCATEL_SR7750:
                execPath = dir + "/alulogin";
                break;
            case JUNIPER_MX:
            case JUNIPER_EX:
                execPath = dir + "/jlogin";
                break;
            default:
                throw new ConfigException("unknown model");
        }

        return RancidArguments.builder()
                .cloginrc(cloginrc)
                .executable(execPath)
                .routerConfig(routerConfig)
                .router(router)
                .build();

    }

}
