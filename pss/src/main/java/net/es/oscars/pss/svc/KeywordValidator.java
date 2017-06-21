package net.es.oscars.pss.svc;

import net.es.oscars.pss.beans.KeywordWithContext;
import net.es.oscars.pss.beans.KeywordFormat;
import net.es.oscars.pss.beans.KeywordValidationCriteria;
import net.es.oscars.pss.beans.KeywordValidationResult;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KeywordValidator {

    public static Map<KeywordWithContext, KeywordValidationResult> validate(Map<KeywordWithContext, KeywordValidationCriteria> criteriaMap) {
        Map<KeywordWithContext, KeywordValidationResult> result = new HashMap<>();
        criteriaMap.forEach((k, v) -> result.put(k,validate(k, v.getFormat(), v.getLength())));
        return result;
    }

    public static KeywordValidationResult validate(KeywordWithContext context, KeywordFormat type, Integer allowedLength) {
        KeywordValidationResult res = KeywordValidationResult.builder().error("").valid(true).build();
        String keyword = context.getKeyword();
        if (keyword == null) {
            res.setValid(false);
            res.setError("null keyword; context: "+context.getContext());
            return res;
        }

        String err = "keyword: " + keyword;
        switch (type) {
            case ALPHANUMERIC:
                if (!StringUtils.isAlphanumeric(keyword)) {
                    err += " not alphanumeric";
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
                    err += " not an IP address";
                    res.setValid(false);
                }
                break;
            case ALPHANUMERIC_DASH_UNDERSCORE_SPACE:
                pattern = Pattern.compile("[\\sa-zA-Z0-9_-]+");
                matcher = pattern.matcher(keyword);
                if (!matcher.matches()) {
                    err += " not alphanumeric / dash / underscore";
                    res.setValid(false);
                }
                break;
            case ALPHANUMERIC_DASH_UNDERSCORE:
                pattern = Pattern.compile("[a-zA-Z0-9_-]+");
                matcher = pattern.matcher(keyword);
                if (!matcher.matches()) {
                    err += " not alphanumeric / dash / underscore";
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

}
