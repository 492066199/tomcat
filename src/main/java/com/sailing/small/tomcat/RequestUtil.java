package com.sailing.small.tomcat;

import javax.servlet.http.Cookie;
import java.util.ArrayList;

public class RequestUtil {
    public static Cookie[] parseCookieHeader(String header) {
        if ((header == null) || (header.length() < 1) )
        return (new Cookie[0]);
        ArrayList cookies = new ArrayList();
        while (header.length() > 0) {
            int semicolon = header.indexOf(';');
            if (semicolon < 0)
                semicolon = header.length();
            if (semicolon == 0)
                break;
            String token = header.substring(0, semicolon);
            if (semicolon < header.length())
                header = header.substring(semicolon + 1);
            else
                header = "";
            try {
                int equals = token.indexOf('=');
                if (equals > 0) {
                    String name = token.substring(0, equals).trim();
                    String value = token.substring(equals+1).trim();
                    cookies.add(new Cookie(name, value));
                }
            }
            catch (Throwable e) {
                ;
            }
        }
        return ((Cookie[]) cookies.toArray (new Cookie [cookies.size ()]));
    }

}
