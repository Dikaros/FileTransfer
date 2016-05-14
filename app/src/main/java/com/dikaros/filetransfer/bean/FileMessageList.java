package com.dikaros.filetransfer.bean;

import com.dikaros.filetransfer.Config;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import dikaros.json.annotation.JsonListParam;
import dikaros.json.annotation.JsonParam;
import dikaros.json.annotation.Jsonable;

/**
 * Created by Dikaros on 2016/5/14.
 */
public class FileMessageList implements Jsonable {



    public FileMessageList(List<File> files){
        for (File f:
              files) {
            msgs.add(new FileMsg(f));
        }
        if (files.size()==1){
            info="对方发来了1个文件"+"文件名："+msgs.get(0).getName()+"大小："+ Config.generateFileSize(msgs.get(0).getLength())+"是否接收？";
        }else {
            info="对方发来了"+files.size()+"个文件，是否接收？";
        }

    }

    @JsonListParam(name = "msgs",contentClassName = FileMsg.class,classType = JsonListParam.TYPE.LIST)
    ArrayList<FileMsg> msgs = new ArrayList<>();

    @JsonParam(name = "info")
    String info;

    public void setInfo(String info) {
        this.info = info;
    }

    public String getInfo() {
        return info;
    }

    public ArrayList<FileMsg> getMsgs() {
        return msgs;
    }

    public int getCount(){
        return msgs.size();
    }
}
