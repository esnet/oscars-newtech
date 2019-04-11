package net.es.oscars.pss.svc;


import net.es.oscars.dto.pss.cmd.Command;
import net.es.oscars.dto.pss.cmd.CommandType;
import net.es.oscars.dto.pss.cmd.GeneratedCommands;
import net.es.oscars.dto.topo.enums.DeviceModel;
import net.es.oscars.pss.beans.ConfigException;
import net.es.oscars.pss.beans.PssProfile;
import net.es.oscars.pss.beans.UrnMappingException;
import net.es.oscars.pss.prop.PssProps;
import net.es.oscars.pss.prop.RancidProps;
import net.es.oscars.pss.rancid.RancidArguments;
import net.es.oscars.pss.rest.BackendProxy;
import net.es.oscars.pss.rest.BackendServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RouterConfigBuilder {
    private PssProps pssProps;
    private AluCommandGenerator acg;
    private MxCommandGenerator mcg;
    private ExCommandGenerator ecg;
    private BackendServer bes;
    private UrnMappingService ums;

    @Autowired
    public RouterConfigBuilder(PssProps pssProps,
                               BackendServer bes,
                               AluCommandGenerator acg,
                               MxCommandGenerator mcg,
                               ExCommandGenerator ecg,
                               UrnMappingService ums) {
        this.pssProps = pssProps;
        this.acg = acg;
        this.mcg = mcg;
        this.ecg = ecg;
        this.ums = ums;
        this.bes = bes;
    }

    public String generate(Command command) throws ConfigException, UrnMappingException  {
        String result = "";
        switch (command.getType()) {
            case CONFIG_STATUS:
                break;
            case OPERATIONAL_STATUS:
                result = opStatus(command).getRouterConfig();
                break;
            case CONTROL_PLANE_STATUS:
                result = controlPlaneCheck(command.getDevice(), command.getModel(), command.getProfile()).getRouterConfig();
                break;
            case BUILD:
                result = gen_build(command).getRouterConfig();
                break;
            case DISMANTLE:
                result = gen_dismantle(command).getRouterConfig();
                break;
        }
        return result;
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

    public RancidArguments opStatus(Command command)
            throws ConfigException, UrnMappingException  {
        String routerConfig;
        DeviceModel model = command.getModel();
        String deviceUrn = command.getDevice();
        String profile = command.getProfile();

        switch (model) {
            case ALCATEL_SR7750:
                routerConfig = acg.show(command.getAlu());
                break;
            case JUNIPER_EX:
                routerConfig = "UNSUPPORTED";
                break;
            case JUNIPER_MX:
                routerConfig = mcg.show(command.getMx());
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

    public RancidArguments gen_build(Command command)
            throws ConfigException, UrnMappingException  {
        String routerConfig = "";
        DeviceModel model = command.getModel();
        switch (model) {
            case ALCATEL_SR7750:
                routerConfig = acg.build(command.getAlu());
                break;
            case JUNIPER_EX:
                routerConfig = ecg.build(command.getEx());
                break;
            case JUNIPER_MX:
                routerConfig = mcg.build(command.getMx());
                break;
            default:
                throw new ConfigException("unknown model");
        }

        return buildRouterConfig(routerConfig, command.getDevice(), command.getModel(), command.getProfile());
    }

    public RancidArguments gen_dismantle(Command command) throws ConfigException, UrnMappingException  {
        String routerConfig = "";
        DeviceModel model = command.getModel();
        switch (model) {
            case ALCATEL_SR7750:
                routerConfig = acg.dismantle(command.getAlu());
                break;
            case JUNIPER_EX:
                routerConfig = ecg.dismantle(command.getEx());
                break;
            case JUNIPER_MX:
                routerConfig = mcg.dismantle(command.getMx());
                break;
            default:
                throw new ConfigException("unknown model");
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
