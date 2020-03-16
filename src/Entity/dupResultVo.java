package Entity;


import Method.Method_dup;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class dupResultVo {
    String fullpath;
    String filesizekb;
    String text;

    public dupResultVo(String fullpath, String filesizekb, String text) {
        this.fullpath = fullpath;
        this.filesizekb = filesizekb;
        this.text = text;
    }

    public String getFullpath() {
        return fullpath;
    }

    public String getFilesizekb() {
        return filesizekb;
    }

    public String getText() {
        return text;
    }

}
