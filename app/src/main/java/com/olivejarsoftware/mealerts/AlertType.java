package com.olivejarsoftware.mealerts;

import com.google.api.services.gmail.model.MessagePartHeader;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.RegEx;

/**
 * Created by tap on 7/29/16.
 */
public class AlertType {
    static Pattern alertPtrn = Pattern.compile("(.+)", Pattern.CASE_INSENSITIVE);
    public String alert = "";
    public String date="";
    public String msgId="";

    AlertType(String id, List<MessagePartHeader> emailResult) {
        msgId = id;
        for (MessagePartHeader part : emailResult) {
            if (part.getName().equals("Subject")) {
                Matcher m = alertPtrn.matcher(part.getValue());
                if (m.matches()) {
                    alert = m.group(1);
                }
            }
            else {
                    if (part.getName().equals("Date")) {
                        date = part.getValue();
                    }
                }
            }
        }

}
