package com.rhjf.salesman.service.util.auth;

import java.util.HashMap;
import java.util.Map;


import net.sf.json.JSONObject;

public class AuthService {
	public Map<String, String> authKuai(Map<String,String> authMap)  {
		String accName=authMap.get("accName");
		String cardNo=authMap.get("cardNo");
		String certificateNo=authMap.get("certificateNo");

		Map<String,String> reqMap= new HashMap<String, String>();
		reqMap.put("accName",accName );//姓名
		reqMap.put("certificateNo",certificateNo);//身份证号
		reqMap.put("cardNo",cardNo);//卡号
		reqMap.put("funcCode",Author.FUNC_CODE); // 接入方功能编码
		reqMap.put("sysId", "sxf"); // 来源系统 (自定义)
		reqMap.put("bnkCd", "100");//银行3位编码
		reqMap.put("cardType", "1");//卡类型  1-借记 2-贷记 
		
		//获取签名
		String sign = Signature.getSign(reqMap, Author.KEY);
		System.out.println("===sign:" + sign);
		reqMap.put("sign", sign); // 签名 (必须)
		HttpSendResult res = HttpsPost.post(Author.NEW_API, reqMap, Author.TIMEOUT, Author.CHARACTERSET);
		System.out.println(res.getStatus());
		System.out.println(res.getResponseBody());
		JSONObject json = JSONObject.fromObject(res.getResponseBody());
		System.out.println(json.getString("respCode"));
		
		Map<String,String> map=new HashMap<String,String>();
		map.put("respCode", json.getString("respCode"));
		map.put("respMsg", json.getString("respMsg"));
		
		return map;
	}
}
