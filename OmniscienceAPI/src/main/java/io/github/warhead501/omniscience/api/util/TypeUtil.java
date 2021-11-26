package io.github.warhead501.omniscience.api.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TypeUtil {

    /**
     * Java implementation of preg_match_all by https://github.com/raimonbosch.
     *
     * @param p       Pattern
     * @param subject String value to match against.
     * @return Array of matches.
     */
    public static String[] pregMatchAll(Pattern p, String subject) {
        Matcher m = p.matcher(subject);
        StringBuilder out = new StringBuilder();
        boolean split = false;
        while (m.find()) {
            out.append(m.group());
            out.append("~");
            split = true;
        }
        return (split) ? out.toString().split("~") : new String[0];
    }
}
