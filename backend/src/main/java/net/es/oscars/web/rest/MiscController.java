package net.es.oscars.web.rest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.util.stream.Stream;



@RestController
@Slf4j
public class MiscController {

    @Value("${logging.file}:backend.log")
    private String logfile;

    @RequestMapping(value = "/api/version", method = RequestMethod.GET)
    public String getVersion() {
        return "1.0.9";
    }


    @RequestMapping(value = "/api/log", method = RequestMethod.GET, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public StreamingResponseBody getLog() {
        return new StreamingResponseBody() {

            @Override
            public void writeTo(OutputStream out) throws IOException {
                Stream<String> stream = Files.lines(Paths.get(logfile));

                stream.forEach(s -> {
                    String o = s + "\n";
                    try {
                        out.write(o.getBytes());
                    } catch (IOException ex) {
                        //
                    }
                });

                out.flush();
            }
        };
    }

}