package net.es.oscars.pss.svc;


import lombok.extern.slf4j.Slf4j;
import net.es.oscars.pss.beans.ControlPlaneException;
import net.es.oscars.pss.prop.RancidProps;
import net.es.oscars.pss.rancid.RancidArguments;
import net.es.oscars.pss.rancid.RancidResult;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.zeroturnaround.exec.InvalidExitValueException;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.ProcessResult;
import org.zeroturnaround.exec.stream.slf4j.Slf4jStream;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

@Slf4j
@Component
public class RancidRunner {
    private RancidProps props;

    @Autowired
    public RancidRunner(RancidProps props) {
        this.props = props;
    }

    public RancidResult runRancid(RancidArguments arguments)
            throws ControlPlaneException, IOException, InterruptedException, TimeoutException {

        if (!props.getExecute()) {
            log.info("configured to not actually run rancid");
            return RancidResult.builder().commandline("").details("").exitCode(0).build();
        }
        File temp = File.createTempFile("oscars-routerConfig-", ".tmp");

        log.info("routerConfig: " + arguments.getRouterConfig());

        FileUtils.writeStringToFile(temp, arguments.getRouterConfig());
        String tmpPath = temp.getAbsolutePath();
        log.info("created temp file" + tmpPath);
        String host = props.getHost();
        String cloginrc = props.getCloginrc();

        String command_line = "";
        String details;


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
            details = res.getOutput().getUTF8();
            log.info("output is: " + details);


        } else {

            String remotePath = "/tmp/" + temp.getName();
            String scpTo = host + ":" + remotePath;

            // scp the file to remote host: /tmp/
            try {
                log.info("SCPing: " + tmpPath + " -> " + scpTo);
                new ProcessExecutor()
                        .command("scp", tmpPath, scpTo)
                        .exitValues(0)
                        .execute();

                // run remote rancid..
                String[] rancidCliArgs = {
                        "ssh",
                        host,
                        arguments.getExecutable(),
                        "-x", remotePath,
                        "-f", cloginrc,
                        arguments.getRouter()
                };
                command_line = StringUtils.join(rancidCliArgs, " ");
                log.info("executing rancid command line "+command_line);

                ProcessResult res = new ProcessExecutor()
                        .command(rancidCliArgs)
                        .exitValue(0)
                        .readOutput(true)
                        .execute();

                details = res.getOutput().getUTF8();
                log.info("output is: " + details);

                log.debug("deleting: " + scpTo);

                String remoteDelete = "rm " + remotePath;
                new ProcessExecutor().command("ssh", host, remoteDelete)
                        .exitValue(0).execute();


            } catch (InvalidExitValueException ex) {
                throw new ControlPlaneException("error running Rancid!");

            } finally {

                FileUtils.deleteQuietly(temp);
            }

        }
        // delete local file
        FileUtils.deleteQuietly(temp);
        return RancidResult.builder().commandline(command_line).details(details).exitCode(0).build();

    }


}
