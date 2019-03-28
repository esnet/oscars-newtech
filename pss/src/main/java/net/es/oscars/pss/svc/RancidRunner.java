package net.es.oscars.pss.svc;


import lombok.extern.slf4j.Slf4j;
import net.es.oscars.pss.beans.ControlPlaneException;
import net.es.oscars.pss.beans.PssProfile;
import net.es.oscars.pss.prop.PssProps;
import net.es.oscars.pss.prop.RancidProps;
import net.es.oscars.pss.rancid.RancidArguments;
import net.es.oscars.pss.rancid.RancidResult;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.zeroturnaround.exec.InvalidExitValueException;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.ProcessResult;
import org.zeroturnaround.exec.stream.slf4j.Slf4jStream;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

@Slf4j
@Component
public class RancidRunner {
    private PssProps pssProps;

    @Autowired
    public RancidRunner(PssProps props) {
        this.pssProps = props;
    }

    public RancidResult runRancid(RancidArguments arguments, String profile)
            throws ControlPlaneException, IOException, InterruptedException, TimeoutException {

        PssProfile pssProfile = PssProfile.find(pssProps, profile);
        RancidProps props = pssProfile.getRancid();

        /*
        ObjectMapper mapper = new ObjectMapper();
        String pretty = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(props);
        log.info(pretty);
    */
        if (!props.getPerform()) {
            log.info("configured to not actually run rancid");
            String output = "Not performing rancid, output is router config.\n"+arguments.getRouterConfig();
            return RancidResult.builder().commandline("").output(output).exitCode(0).build();
        }
        File temp = File.createTempFile("oscars-routerConfig-", ".tmp");

        log.info("routerConfig: " + arguments.getRouterConfig());

        FileUtils.writeStringToFile(temp, arguments.getRouterConfig());
        String tmpPath = temp.getAbsolutePath();
        log.info("created temp file " + tmpPath);
        String host = props.getHost();
        String cloginrc = props.getCloginrc();

        String command_line = "";
        String output;


        if (host.equals("localhost")) {
            String[] rancidCliArgs = {
                    arguments.getExecutable(),
                    "-x", tmpPath,
                    "-f", cloginrc,
                    arguments.getRouter()
            };

            command_line = StringUtils.join(rancidCliArgs, " ");

            // run local rancid
            ProcessResult res = new ProcessExecutor()
                    .command(rancidCliArgs)
                    .exitValue(0)
                    .readOutput(true)
                    .execute();
            output = res.getOutput().getUTF8();
            //log.info("output is: " + output);


        } else {
            String username = props.getUsername();
            String idFile = props.getIdentityFile();
            List<String> sshOpts = props.getSshOptions();

            String remotePath = "/tmp/" + temp.getName();

            if (username != null && username.length() > 0) {
                host = username + "@" + host;
            }


            String scpTo = host + ":" + remotePath;
            List<String> scpArgs = new ArrayList<>();
            scpArgs.add("scp");
            scpArgs.add("-q");
            if (idFile != null && idFile.length() > 0) {
                scpArgs.add("-i");
                scpArgs.add(idFile);
            }
            if (sshOpts != null && sshOpts.size() > 0) {
                for (String opt: sshOpts) {
                    scpArgs.add("-o");
                    scpArgs.add(opt);
                }
            }
            scpArgs.add(tmpPath);
            scpArgs.add(scpTo);



            List<String> rancidArgs = new ArrayList<>();
            rancidArgs.add("ssh");
            rancidArgs.add("-q");
            if (idFile != null && idFile.length() > 0) {
                rancidArgs.add("-i");
                rancidArgs.add(idFile);
            }
            if (sshOpts != null && sshOpts.size() > 0) {
                for (String opt: sshOpts) {
                    rancidArgs.add("-o");
                    rancidArgs.add(opt);
                }
            }
            rancidArgs.add(host);

            rancidArgs.add(arguments.getExecutable());
            rancidArgs.add("-x");
            rancidArgs.add(remotePath);
            rancidArgs.add("-f");
            rancidArgs.add(cloginrc);
            rancidArgs.add(arguments.getRouter());


            List<String> rmArgs = new ArrayList<>();
            rmArgs.add("ssh");
            rmArgs.add("-q");
            if (idFile != null && idFile.length() > 0) {
                rmArgs.add("-i");
                rmArgs.add(idFile);
            }
            if (sshOpts != null && sshOpts.size() > 0) {
                for (String opt : sshOpts) {
                    rmArgs.add("-o");
                    rmArgs.add(opt);
                }
            }
            rmArgs.add(host);
            rmArgs.add("rm");
            rmArgs.add(remotePath);




            // scp the file to remote host: /tmp/
            try {
                command_line = StringUtils.join(scpArgs, " ");
                log.info("executing scp, command line: [" + command_line+"]");

                new ProcessExecutor()
                        .command(scpArgs)
                        .exitValues(0)
                        .redirectError(Slf4jStream.ofCaller().asError())
                        .execute();

                command_line = StringUtils.join(rancidArgs, " ");
                log.info("executing rancid, command line: [" + command_line+"]");
                ProcessResult res = new ProcessExecutor()
                        .command(rancidArgs)
                        .exitValue(0)
                        .readOutput(true)
                        .redirectError(Slf4jStream.ofCaller().asError())
                        .execute();

                output = res.getOutput().getUTF8();
                log.info("rancid output was:\n"+  output);

            } catch (InvalidExitValueException ex) {
                log.error(ex.getMessage(), ex);

                throw new ControlPlaneException("error running Rancid!");

            } finally {
                // try to delete the SCP'd file even if an error occurred
                try {
                    command_line = StringUtils.join(rmArgs, " ");
                    log.info("deleting scp'd file, command line: [" + command_line + "]");

                    new ProcessExecutor()
                            .command(rmArgs)
                            .exitValue(0)
                            .readOutput(true)
                            .redirectError(Slf4jStream.ofCaller().asError())
                            .execute();
                } catch (InvalidExitValueException rmEx) {
                    log.error(rmEx.getMessage(), rmEx);
                    log.error("could not rm scp'd file; likely scp never happened");
                }


                FileUtils.deleteQuietly(temp);
            }

        }
        // delete local file
        FileUtils.deleteQuietly(temp);
        return RancidResult.builder().commandline(command_line).output(output).exitCode(0).build();

    }


}
