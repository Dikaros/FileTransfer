package com.dikaros.filetransfer.bean;

import java.io.File;

import dikaros.json.annotation.JsonParam;
import dikaros.json.annotation.Jsonable;

/**
 * Created by Dikaros on 2016/5/11.
 */
public class FileMsg implements Jsonable {


    @JsonParam(name = "length")
    long length;
    @JsonParam(name = "name")
    String name;
    @JsonParam(name = "type")
    String type;

    public FileMsg(File file){
        this.length = file.length();
        this.name = file.getName();
        this.type = getTailName(file.getName());
    }

    private String getTailName(String name) {
        String type = null;
        if (name.contains(".")) {
            String[] splits = name.split("\\.");
            // System.out.println(splits.length);
            type = splits[splits.length - 1].toLowerCase();
        }
        return type;
    }

    public long getLength() {
        return length;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }
}
