package net.es.oscars.pss.svc;

import net.es.oscars.pss.params.mx.TaggedIfce;
import net.es.oscars.pss.beans.KeywordWithContext;
import net.es.oscars.pss.beans.KeywordFormat;
import net.es.oscars.pss.beans.KeywordValidationCriteria;
import net.es.oscars.pss.beans.KeywordValidationResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class KeywordValidator {

    public Map<KeywordWithContext, KeywordValidationResult> validate(Map<KeywordWithContext, KeywordValidationCriteria> criteriaMap) {
        Map<KeywordWithContext, KeywordValidationResult> result = new HashMap<>();
        criteriaMap.forEach((k, v) -> result.put(k,validate(k, v.getFormat(), v.getLength())));
        return result;
    }

    public KeywordValidationResult validate(KeywordWithContext context, KeywordFormat type, Integer allowedLength) {
        KeywordValidationResult res = KeywordValidationResult.builder().error("").valid(true).build();
        String keyword = context.getKeyword();
        if (keyword == null || keyword.length() == 0) {
            res.setValid(false);
            res.setError("null or empty keyword; context: "+context.getContext()+"\n");
            return res;
        }

        String err = "keyword: " + keyword;
        switch (type) {
            case ALPHANUMERIC:
                if (!StringUtils.isAlphanumeric(keyword)) {
                    err += " not alphanumeric\n";
                    res.setValid(false);
                }
                break;
            case IPV4_ADDRESS:

                String IPADDRESS_PATTERN =
                        "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                         "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                         "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                         "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";

                Pattern pattern = Pattern.compile(IPADDRESS_PATTERN);
                Matcher matcher = pattern.matcher(keyword);
                if (!matcher.matches()) {
                    err += " not an IP address\n";
                    res.setValid(false);
                }
                break;
            case ALPHANUMERIC_DASH_UNDERSCORE_SPACE:
                pattern = Pattern.compile("[\\sa-zA-Z0-9_-]+");
                matcher = pattern.matcher(keyword);
                if (!matcher.matches()) {
                    err += " not alphanumeric / dash / underscore\n";
                    res.setValid(false);
                }
                break;
            case ALPHANUMERIC_DASH_UNDERSCORE:
                pattern = Pattern.compile("[a-zA-Z0-9_-]+");
                matcher = pattern.matcher(keyword);
                if (!matcher.matches()) {
                    err += " not alphanumeric / dash / underscore\n";
                    res.setValid(false);
                }
                break;

        }
        if (keyword.length() > allowedLength) {
            err += " length over " + allowedLength;
            res.setValid(false);
        }
        res.setError(err);


        return res;

    }
    public KeywordValidationResult gatherErrors(Map<KeywordWithContext, KeywordValidationResult> results) {
        KeywordValidationResult overall = KeywordValidationResult.builder().error("").valid(true).build();
        StringBuilder errorStr = new StringBuilder("");

        for (KeywordWithContext keyword : results.keySet()) {
            KeywordValidationResult res = results.get(keyword);
            if (!res.getValid()) {
                errorStr.append(res.getError());
                overall.setValid(false);
            }
        }
        overall.setError(errorStr.toString());
        return overall;
    }

    public KeywordValidationResult verifyIfces(List<TaggedIfce> ifces) {
        KeywordValidationResult result = KeywordValidationResult.builder().error("").valid(true).build();
        if (ifces == null) {
            return result;
        }

        StringBuilder errorStr = new StringBuilder("");
        for (TaggedIfce ifce : ifces) {
            if (ifce.getVlan() < 2 || ifce.getVlan() > 4094) {
                String err = ifce.getPort() + " : vlan " + ifce.getVlan() + " out of range (2-4094)\n";
                errorStr.append(err);
                result.setValid(false);
            }
        }
        result.setError(errorStr.toString());
        return result;

    }



}
