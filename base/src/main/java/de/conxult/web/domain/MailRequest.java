/*
 * Copyright by https://conxult.de
 */
package de.conxult.web.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 *
 * @author joerg
 */

@Getter @Setter @Accessors(chain = true)
public class MailRequest {

    List<String>        to  = new ArrayList<>();
    List<String>        cc  = new ArrayList<>();
    List<String>        bcc = new ArrayList<>();

    Map<String, Object> data = new TreeMap<>();

    public MailRequest(String to) {
        addTo(to);
    }

    public MailRequest addTo(String to) {
        this.to.add(to);
        return this;
    }

    public MailRequest addCC(String cc) {
        this.cc.add(cc);
        return this;
    }

    public MailRequest addBcc(String bcc) {
        this.bcc.add(bcc);
        return this;
    }

    public MailRequest addData(String name, Object value) {
        this.data.put(name,  value);
        return this;
    }

}
