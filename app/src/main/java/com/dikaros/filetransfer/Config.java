package com.dikaros.filetransfer;

import android.os.Environment;

import java.io.File;
import java.net.InetAddress;

/**
 * Created by Dikaros on 2016/5/11.
 */
public class Config {

    public  static  String PERSONAL_FILE = Environment.getDataDirectory().getAbsolutePath()+"/"+"FileTransfer";

    public static File  getPersonalFile(){
        File file = new File(PERSONAL_FILE);
        if (!file.exists()){
            file.mkdirs();
        }
        return file;
    }

    public static InetAddress CONNECTED_OWNER_IP = null;

    public static P2pRole CURRENT_ROLE = P2pRole.NONE;
    public enum P2pRole{
        GROUP_OWNRR,GROUP_MEMBER,NONE
    }

    public static boolean isSenerDevice = false;
}
