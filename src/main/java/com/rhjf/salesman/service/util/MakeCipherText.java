package com.rhjf.salesman.service.util;

import com.rhjf.salesman.core.constants.Constants;
import com.rhjf.salesman.core.util.DESUtil;
import com.rhjf.salesman.core.util.MD5;
import org.springframework.beans.factory.annotation.Value;

/**
 * Created by hadoop on 2017/8/7.
 */
public class MakeCipherText {



//    private String protectINDEX = "3F27D9CB903EA24EF74E2F7E6D97D63E";




    /**
     *   制作登录密码
     * @param pwd
     * @return
     */
    public  String MakeLoginPwd(String pwd,String initKeyoutStr){
//        //加密
        try {
            // 解析密钥明文
            pwd = DESUtil.rightPad(pwd, 16, " ");
            pwd = DESUtil.bytes2HexStr(pwd.getBytes(), false);
            return DESUtil.bcd2Str(DESUtil.encrypt3(pwd, initKeyoutStr));
        } catch (Exception e) {

            e.printStackTrace();
            return "";
        }
    }


    public  String calLoginPwd(String usrID,String pwd ,String sendTime){
        // 加密
        try {

            System.out.println(pwd);

            // 解析密码明文
            String keyde = new String(DESUtil.decrypt3(pwd, Constants.protectINDEX));


            return MD5.sign(usrID + sendTime + keyde.replace(" ", "") ,"utf-8" );

        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }


    /**
     *   制作签名mac
     * @param macStr
     * @param key
     * @return
     */
    public  String makeMac(String macStr, String key) {
//        System.out.println("macStr" + macStr + ", key :" + key);
//        String initKey = LoadPro.loadProperties("config", "DBINDEX");
//        return DESUtil.mac(macStr, key, initKey);
        return null;
    }
}
