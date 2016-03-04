package org.theiner.kinoxscanner.data;

import java.io.Serializable;

/**
 * Created by TTheiner on 26.02.2016.
 */
public class Film implements KinoxElement, Serializable{
    private String name;
    private String addr;
    private String lastDate;

    private static final long serialVersionUID = 0L;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public String getLastDate() {
        return lastDate;
    }

    public void setLastDate(String lastDate) {
        this.lastDate = lastDate;
    }

    @Override
    public String toQueryString() {
        return addr + ".html";
    }
}
