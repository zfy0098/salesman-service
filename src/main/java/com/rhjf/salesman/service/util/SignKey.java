package com.rhjf.salesman.service.util;

import com.rhjf.salesman.core.constants.Constants;
import com.rhjf.salesman.core.util.DESUtil;

import java.util.HashMap;
import java.util.Random;


public class SignKey {


	
	public  HashMap<String, String> GetKey(String tmkEncry) {
		
		Random random = new Random();
		HashMap<String, String> keyMap = new HashMap<String, String>();
		char[] codeSequence = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

		try {
			StringBuffer ret = new StringBuffer();
			for (int i = 0; i < 32; i++) {
				ret.append(String.valueOf(codeSequence[random.nextInt(16)]));
			}

			String data = ret.toString();
			// 解密TMK
			String tmk = DESUtil.bcd2Str(DESUtil.decrypt3(tmkEncry, Constants.initKey));
			
			//  获取校验码
			String checkCode = DESUtil.bcd2Str(DESUtil.encrypt3("0000000000000000", data));
			
			// 生成下发给终端的密钥
			String keyTerm = DESUtil.bcd2Str(DESUtil.encrypt3(data, tmk));
			// 生成存放到数据的密钥
			String keyDB = DESUtil.bcd2Str(DESUtil.encrypt3(data,Constants.DBINITKEY));
			keyMap.put("keyTerm", keyTerm);
			keyMap.put("keyDB", keyDB);
			keyMap.put("checkCode", checkCode.substring(0, 8));
			
		} catch (Exception e) {
			return null;
		}
		return keyMap;
	}
}
