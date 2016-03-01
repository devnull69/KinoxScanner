package org.theiner.kinoxscanner.data;

/**
 * Created by TTheiner on 26.02.2016.
 */
public class Film {
    private String name;
    private String addr;
    private String lastDate;

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

    public String toQueryString() {
        return addr + ".html";
    }
}
