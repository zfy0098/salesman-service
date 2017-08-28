package com.rhjf.salesman.service.service;

import com.rhjf.account.modle.domain.salesman.LoginUser;
import com.rhjf.account.modle.domain.salesman.ParamterData;
import com.rhjf.account.modle.domain.salesman.Salesman;
import com.rhjf.account.modle.domain.salesman.TermKey;
import com.rhjf.salesman.core.constants.Constants;
import com.rhjf.salesman.core.constants.RespCode;
import com.rhjf.salesman.core.service.LoginService;
import com.rhjf.salesman.core.util.DESUtil;
import com.rhjf.salesman.core.util.DateUtil;
import com.rhjf.salesman.core.util.MD5;
import com.rhjf.salesman.core.util.UtilsConstant;
import com.rhjf.salesman.service.mapper.*;
import com.rhjf.salesman.service.util.MakeCipherText;
import com.rhjf.salesman.service.util.SignKey;
import com.rhjf.salesman.service.util.auth.AuthService;
import com.rhjf.salesman.service.util.auth.Author;
import com.rhjf.salesman.service.util.email.SendMail;
import com.sun.org.apache.bcel.internal.generic.RETURN;
import net.sf.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Transactional
@Service("loginService")
public class LoginServiceImpl implements LoginService{


	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private LoginMapper loginMapper;
	
	@Autowired
	private UserBankCardMapper userbank;
	
	@Autowired
	private TermkeyMapper termkeyMapper;

	@Autowired
	private SalesmanMapper salesmanMapper;

	@Autowired
	private SalesManProfitMapper salesManProfitMapper;

	@Autowired
	private SMSCodeMapper smsCodeMapper;


	/**  获取用户信息  **/
	public LoginUser userInfo(String LoginID){

		Map<String,String> map = new HashMap<String,String>();
		map.put("loginid",LoginID);
		return loginMapper.login(map);
	}
	
	
	/** 获取用户秘钥 **/
	public TermKey userTermkey(String userID){
		return termkeyMapper.queryUserKey(userID);
	}


	/**
	 *   用户登录
	 * @param user
	 * @param paramter
	 * @return
	 */
	@Override
	public ParamterData login(LoginUser user, ParamterData paramter) {


		log.info("用户：" + user.getLoginID() + "执行登录操作");

		Map<String,String> map = new HashMap<String,String>();
		map.put("loginid", paramter.getLoginID());
		LoginUser userInfo = loginMapper.login(map);
		if(userInfo == null){
			paramter.setRespCode(RespCode.userDoesNotExist[0]);
			paramter.setRespDesc(RespCode.userDoesNotExist[1]);
		}else{

			if(user.getUserType()==null||!"SALESMAN".equals(user.getUserType())){
				log.info("用户角色不允许登录改APP ，该用户登录账号：" + user.getLoginID() + " 用户类型：" + user.getUserType());
				paramter.setRespCode(RespCode.PermissionDeniedError[0]);
				paramter.setRespCode(RespCode.PermissionDeniedError[1]);
				return paramter;
			}


			MakeCipherText makeCipherText = new MakeCipherText();
			String pwd = makeCipherText.calLoginPwd(paramter.getLoginID(),user.getLoginPwd(), paramter.getSendTime());

			/**  登录密码错误 **/
			if(!paramter.getLoginPWD().equals(pwd)){

				log.info("用户:" + user.getLoginID() + "登录密码错误. 用户上传：" + paramter.getLoginPWD() + "平台计算:" + pwd);

				paramter.setRespCode(RespCode.PasswordError[0]);
				paramter.setRespDesc(RespCode.PasswordError[1]);
			}else{

				user.setLastLoginTime(DateUtil.getNowTime(DateUtil.yyyyMMddHHmmss));
				user.setLoginPSN(paramter.getTerminalInfo());

				loginMapper.updateUserLoginInfo(user);
				TermKey termKey = termkeyMapper.queryUserKey(user.getID());
				if(termKey == null){
					log.info("用户：" + user.getLoginID() + "秘钥配置为空，将重新分配秘钥信息");

					/** 为用户分配秘钥  **/
					String termTmkKey = MD5.md5(UtilsConstant.getUUID(), "UTF-8").toUpperCase();
					String tmkKey = MD5.md5(UtilsConstant.getUUID(), "UTF-8").toUpperCase();

					termKey = new TermKey();
					termKey.setID(UtilsConstant.getUUID());
					termKey.setUserID(user.getID());
					termKey.setTermTmkKey(termTmkKey);
					termKey.setTmkKey(tmkKey);
					termkeyMapper.addTermKey(termKey);
				}

				try {
					String tmk = DESUtil.bcd2Str(DESUtil.decrypt3(termKey.getTermTmkKey(), Constants.initKey));
					paramter.setSecretKey(tmk);
					paramter.setRespCode(RespCode.SUCCESS[0]);
					paramter.setRespDesc(RespCode.SUCCESS[1]);
				} catch (Exception e) {
					log.error("登录转换秘钥错误:" + e.getMessage()  ,  e);
				}
			}
		}
		return paramter;
	}
	

	/**
	 *    签到
	 */
	@Override
	public ParamterData sign(LoginUser user,ParamterData paramter) {

		log.info("用户" + user.getLoginID() + "执行签到操作");

		SignKey signKey = new SignKey();
		
		TermKey termKey = termkeyMapper.queryUserKey(user.getID());

		Map<String,String> map = signKey.GetKey(termKey.getTermTmkKey());

		termKey.setMacKey(map.get("keyDB").toString());
		
		int x = termkeyMapper.sign(termKey);
		
		paramter.setSecretKey(map.get("keyTerm").toString());
		
		if(x > 0){

			log.info("用户：" + user.getLoginID() + "签到成功 , 获得秘钥:" + map.get("keyTerm").toString());
			paramter.setRespCode(RespCode.SUCCESS[0]);
			paramter.setRespDesc(RespCode.SUCCESS[1]);
		}else{
			log.info("用户：" + user.getLoginID() + "签到保存数据失败");
			paramter.setRespCode(RespCode.ServerDBError[0]);
			paramter.setRespDesc(RespCode.ServerDBError[1]);
		}
		return paramter;
	}


	/**
	 *   校验密码 是否正确
	 * @param user
	 * @param paramter
	 * @return
	 */
	public ParamterData verifyPassword(LoginUser user  , ParamterData paramter){

		MakeCipherText makeCipherText = new MakeCipherText();

		String passwd = makeCipherText.calLoginPwd(paramter.getLoginID(),user.getLoginPwd(), paramter.getSendTime());

		if(!passwd.equals(paramter.getLoginPWD())){
			log.info("用户" + user.getLoginID() + "密码错误, 上送密码：" + paramter.getLoginPWD() + ", 平台计算密码:" + passwd);
			paramter.setRespCode(RespCode.PasswordError[0]);
			paramter.setRespDesc(RespCode.PasswordError[1]);
		} else {

			log.info("校验密码通过, " + passwd);

			paramter.setRespCode(RespCode.SUCCESS[0]);
			paramter.setRespDesc(RespCode.SUCCESS[1]);
		}
		return paramter;
	}



	public ParamterData verificationEmail(LoginUser  user , ParamterData paramterData){


		//生成验证码
		String smsCode = UtilsConstant.GetSmsCode();
		Map<String,String> map = new HashMap<>();
		map.put("id" , UtilsConstant.getUUID());
		map.put("phone" , user.getLoginID());
		map.put("smsCode" , smsCode);

		int nRet = smsCodeMapper.insertSmsCode(map);
		if(nRet < 0){
			log.info("记录校验码失败，业务员登录账号=【"+user.getLoginID()+"】");
			paramterData.setRespCode("96");
			paramterData.setRespDesc("验证码发送失败");
			return paramterData;
		}


//		String content = "<div style='width:600px; height:350px; border-bottom:1px dashed #666; margin:0 auto; '> " +
//				"<h1>亲爱的用户:</h1> <br> <br> <p>您好！感谢您使用爱码付，您正在进行邮箱验证，本次请求的验证码为：<span style='font-size:25px; color: #FF6600;'>" + smsCode
//				+ "</span><br>(为了保障您账号的安全性，请在1小时内完成验证)</p><br><br><br> <h3>爱码付团队</h3> <br>" +
//				"<h3>" + DateUtil.getNowTime("yyyy")+ "年" +DateUtil.getNowTime("MM")+ "月"+DateUtil.getNowTime("dd")+"日</h3></div>";


		String content = "<div style='width:500px; height:300px; border-bottom:1px dashed #999; margin:0 auto; padding-right:20px; font-family: '宋体';'>" +
				"<h1 style='font-size:20px;'>亲爱的用户:</h1><p style='line-height: 36px; margin:42px 0; font-size:15px;'>您好！感谢您使用爱码付，您正在进行邮箱验证，本次请求的验证码为:<br>" +
				"<span style='font-size:28px; color: #D86640;'>"+smsCode+"</span>(为了保障您账号的安全性，请在1小时内完成验证)</p><h4 style='font-size:15px;'>爱码付团队</h4>" +
				"<h4 style='margin:28px 0;font-size:15px; '>"+ DateUtil.getNowTime("yyyy")+ "年" +DateUtil.getNowTime("MM")+ "月"+DateUtil.getNowTime("dd")+"日</h4></div>";


		try {
			SendMail.sendMail("【爱码付团队】 邮箱验证码", content, new String[]{user.getLoginID()} , null , null);

			paramterData.setRespCode(RespCode.SUCCESS[0]);
			paramterData.setRespDesc(RespCode.SUCCESS[1]);

		} catch (Exception e){
			log.error("业务员修改密码验证码发送邮件失败，" + user.getLoginID() , e);
			paramterData.setRespCode("96");
			paramterData.setRespDesc("验证码发送失败");
		}
		return paramterData;
	}


	/** 忘记密码修改密码 **/
	public ParamterData forgetpwd(LoginUser user  , ParamterData paramter){

		MakeCipherText makeCipherText = new MakeCipherText();

		String loginID = user.getLoginID();
		String smsCode = paramter.getSmsCode();
		Map<String,String> codeMap = smsCodeMapper.getSmsCode(loginID);


		if(codeMap == null || codeMap.isEmpty()){

			log.info("用户：" + paramter.getMerchantLoginID() + "查询验证码数据为空，提示用户从新获取");

			paramter.setRespCode(RespCode.SMSCodeError[0]);
			paramter.setRespDesc("验证已经失效，请重新获取");
			return paramter;
		}


		log.info("验证新增用户的手机验证码是否正确:"+ user.getLoginID() + ",输入验证码:" + smsCode +
				" , 系统保存验证码：" + codeMap.get("smsCode"));


		if(codeMap != null && smsCode.equals(codeMap.get("smsCode"))){

			String password = makeCipherText.MakeLoginPwd(paramter.getLoginPWD(),Constants.protectINDEX);

			user.setLoginPwd(password);
			int x = loginMapper.updatepwd(user);

			if(x > 0){
				paramter.setRespCode(RespCode.SUCCESS[0]);
				paramter.setRespDesc(RespCode.SUCCESS[1]);
			}else{
				paramter.setRespCode(RespCode.ServerDBError[0]);
				paramter.setRespDesc(RespCode.ServerDBError[1]);
			}
			smsCodeMapper.delSmsCode(loginID);

		} else{
			paramter.setRespCode(RespCode.SMSCodeError[0]);
			paramter.setRespDesc(RespCode.SMSCodeError[1]);
		}

		return paramter;
	}


	/**
	 *   修改密码
	 * @param user
	 * @param paramter
	 * @return
	 */
	@Override
	public ParamterData updatePassword(LoginUser user , ParamterData paramter){

		MakeCipherText makeCipherText = new MakeCipherText();
		String password = makeCipherText.MakeLoginPwd(paramter.getLoginPWD(),Constants.protectINDEX);
		user.setLoginPwd(password);
		int x = loginMapper.updatepwd(user);

		if(x > 0){
			paramter.setRespCode(RespCode.SUCCESS[0]);
			paramter.setRespDesc(RespCode.SUCCESS[1]);
		}else{
			paramter.setRespCode(RespCode.ServerDBError[0]);
			paramter.setRespDesc(RespCode.ServerDBError[1]);
		}

		return paramter;
	}

	
	/**  根据银行卡号获取银行名称 **/
	public ParamterData getBankName(LoginUser user , ParamterData paramter){


		paramter.setRespCode(RespCode.SUCCESS[0]);
		paramter.setRespDesc(RespCode.SUCCESS[1]);


		return paramter;
	}


	/** 某一个商户为业务员制造的收益  **/
	public ParamterData queryMerchantTotalAmount(LoginUser user, ParamterData paramter){

		Map<String,String> map = new HashMap<>();
		map.put("merchantID" , paramter.getID());
		map.put("UserID" , user.getID());

		log.info("业务员：" + user.getLoginID() + "查询 商户id:" + paramter.getID() + "为自己创造的收益");
		Map<String, String> resultMap = loginMapper.queryMerchantTotalAmount(map);

		Integer tokerCount  = loginMapper.merchantTokerCount(map);

		log.info("业务员：" + user.getLoginID() + "查询 商户id:" + paramter.getID() + "为自己创造的收益 , 收益金额：" +  String.valueOf(resultMap.get("Amount")));

		paramter.setCount(String.valueOf(tokerCount));
		paramter.setAmount(String.valueOf(resultMap.get("Amount")));
		paramter.setRespCode(RespCode.SUCCESS[0]);
		paramter.setRespDesc(RespCode.SUCCESS[1]);

		return paramter;
	}


	/**  用户提现 **/
	public ParamterData txProfit(LoginUser user , ParamterData paramter){

		String amount = paramter.getAmount();
		Salesman salesman = salesmanMapper.salesmanInfo(user.getSalesManID());
		log.info("业务员：" + user.getLoginID() + "发起提现请求,提现金额:" + paramter.getAmount());


		Map<String,Object> map = new HashMap<>();
		map.put("in_loginID",user.getID());
		map.put("in_amount", amount);
		map.put("in_termSerno" , paramter.getSendTime() );
		map.put("in_txType","0");
		map.put("in_accountNo" , salesman.getAccountNo());
		map.put("ret" , 0);

		loginMapper.txProfit(map);

		Integer ret = Integer.parseInt(map.get("ret").toString());

		if(ret==2){
			log.info("业务员："+ user.getLoginID()+ "提款金额不足，提款金额：" + paramter.getAmount());
			paramter.setRespCode(RespCode.TXAMOUNTNOTENOUGH[0]);
			paramter.setRespDesc(RespCode.TXAMOUNTNOTENOUGH[1]);
		} else if( ret == 1){

			user = userInfo(user.getLoginID());
			paramter.setBalance(user.getFeeBalance());

			log.info("业务员："+ user.getLoginID()+ "提款成功");
			paramter.setRespCode(RespCode.SUCCESS[0]);
			paramter.setRespDesc(RespCode.SUCCESS[1]);
		}else{
			paramter.setRespCode(RespCode.ServerDBError[0]);
			paramter.setRespDesc(RespCode.ServerDBError[1]);
		}
		return paramter;
	}



	/**  提现记录 **/
	public ParamterData TxRecordList(LoginUser user , ParamterData paramter){


		log.info("业务员" + user.getLoginID() + "获取提现记录, 查询时间:" + paramter.getTradeDate());


		/**   交易年月   **/
		StringBuffer stringBuffer = new StringBuffer(paramter.getTradeDate());

		StringBuffer sbf = new StringBuffer(paramter.getTradeDate());

		if(paramter.getTradeDate().length() < 6){
			stringBuffer.insert(4, "-0");
			sbf.insert(4, "0");
		}else{
			stringBuffer.insert(4, "-");
		}


		log.info("查询时间：分润总和时间" + stringBuffer.toString());
		log.info("查询时间：提现记录时间:" + sbf.toString());


		Map<String,String> params = new HashMap<>();
		params.put("salesManID" , user.getID());
		params.put("tradeDate" , sbf.toString());


		Map<String,String> pro = salesManProfitMapper.profitMonth(params);
		paramter.setProfitTotal(pro.get("totalProfit"));
		log.info("业务员：" + user.getLoginID() + "时间：" + stringBuffer.toString() + "分润总和:" + pro.toString());

		params.clear();
		params.put("userID",user.getID());
		params.put("tradeDate",stringBuffer.toString());
		List<Map<String,String>> list = loginMapper.TxRecordList(params);
		log.info("业务员：" + user.getLoginID() + "时间：" + stringBuffer.toString() + "提现记录:" + JSONArray.fromObject(list).toString());

		paramter.setList(JSONArray.fromObject(list).toString());
		paramter.setRespCode(RespCode.SUCCESS[0]);
		paramter.setRespDesc(RespCode.SUCCESS[1]);

		return paramter;
	}

}
