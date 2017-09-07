package com.rhjf.salesman.service.service;

import com.rhjf.account.modle.domain.salesman.*;
import com.rhjf.salesman.core.constants.Constants;
import com.rhjf.salesman.core.constants.RespCode;
import com.rhjf.salesman.core.service.MerchantService;
import com.rhjf.salesman.core.util.*;
import com.rhjf.salesman.service.mapper.*;
import com.rhjf.salesman.service.util.auth.AuthService;
import com.rhjf.salesman.service.util.auth.Author;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by hadoop on 2017/8/11.
 */
@Transactional
public class MerchantServiceImpl implements MerchantService {


    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private LoginMapper loginMapper;

    @Autowired
    private UserBankCardMapper userBankCardMapper;

    @Autowired
    private TermkeyMapper termkeyMapper;

    @Autowired
    private UserConfigMapper userConfigMapper;

    @Autowired
    private PayChannelMapper payChannelMapper;

    @Autowired
    private BankCodeMapper bankCodeMapper;

    @Autowired
    private PayMerchantMapper payMerchantMapper;


    @Autowired
    private AuthenticationMapper authenticationMapper;

    @Autowired
    private UserPropertiesMapper userPropertiesMapper;

    /**
     * 业务员录入商户
     */
    @Override
    public ParamterData inputMerchant(LoginUser user, ParamterData paramter) {


        log.info("业务员:" + user.getLoginID() + "添加商户, 添加的手机号：" + paramter.getMerchantLoginID());

        try {


            /**
             *    用户银行卡号鉴权
             */
            if(authencation(paramter.getName() , paramter.getIDCard() , paramter.getBankCardNo())){

                log.info("商户：" + paramter.getMerchantLoginID() + "信用鉴权没有通过 , 卡号:" + paramter.getBankCardNo());
                paramter.setRespCode(RespCode.BankCardInfoErroe[0]);
                paramter.setRespDesc(RespCode.BankCardInfoErroe[1]);
                return paramter;
            }


            Map<String, String> userParamMap = new HashMap<>();
            userParamMap.put("loginid", paramter.getMerchantLoginID());
            userParamMap.put("BankInfoStatus", "1");
            LoginUser user2 = loginMapper.login(userParamMap);
            if (user2 != null) {
                log.info("用户：" + paramter.getMerchantLoginID() + "已经入网成功，请更换信息");
                paramter.setRespCode(RespCode.AlreadyInTheNetError[0]);
                paramter.setRespDesc(RespCode.AlreadyInTheNetError[1]);
                return paramter;
            }

            boolean flag = UtilsConstant.checkMerchantName(paramter.getMerchantName());
            if(flag){

                log.info("用户："+ user.getLoginID() + "商户名不合法," + paramter.getMerchantName());

                paramter.setRespDesc(RespCode.MerchantNameError[0]);
                paramter.setRespDesc(RespCode.MerchantNameError[1]);
                return paramter;
            }

            Map<String,String> BKmap = bankCodeMapper.bankBinMap(paramter.getBankCardNo());
            if(BKmap!=null&&"CREDIT_CARD".equals(UtilsConstant.ObjToStr(BKmap.get("cardName")))){
                log.info("用户：" +  paramter.getMerchantLoginID() + "填写的结算账号为信用卡, 卡号为：" +  paramter.getBankCardNo());
                paramter.setRespCode(RespCode.AccountNoError[0]);
                paramter.setRespDesc(RespCode.AccountNoError[1]);
                return paramter;
            }

            //逻辑代码，可以写上你的逻辑处理代码
            String uuid = UtilsConstant.getUUID();

            /**  添加商户基本信息  **/

            /** 随机生成的营业执照号 **/
            String businessLicense = UtilsConstant.RandCode();

            LoginUser merchantInfo = new LoginUser();
            merchantInfo.setID(uuid);
            merchantInfo.setLoginID(paramter.getMerchantLoginID());
            merchantInfo.setLoginPwd(Constants.defaultLoginPWD);
            merchantInfo.setName(paramter.getName());
            merchantInfo.setMerchantBillName(paramter.getMerchantName());
            merchantInfo.setMerchantPersonName(paramter.getName());
            merchantInfo.setMerchantName(paramter.getMerchantName());
            merchantInfo.setMerchantTypeValue("PERSON");
            merchantInfo.setSalesManID(user.getID());
            merchantInfo.setAgentID(user.getAgentID());
            merchantInfo.setIDCardNo(paramter.getIDCard());
            merchantInfo.setAgentID(user.getAgentID());
            merchantInfo.setThreeLevel(user.getID());
            merchantInfo.setMerchantLeve(Integer.parseInt(paramter.getMerchantLevel()));
            merchantInfo.setState(paramter.getState());
            merchantInfo.setCity(paramter.getCity());
            merchantInfo.setRegion(paramter.getCounty());
            merchantInfo.setBusinessLicense(businessLicense);
            merchantInfo.setBankInfoStatus(1);
            merchantInfo.setPhotoStatus(0);
            merchantInfo.setAddress(paramter.getAddress() + "" + paramter.getHouseNumber());
            merchantInfo.setRegisterTime(DateUtil.getNowTime(DateUtil.yyyyMMddHHmmss));
            merchantInfo.setSalesManID(user.getID());
            merchantInfo.setUserType("MERCHANT");
            merchantInfo.setNeedLogin(1);
            log.info("业务员：" + user.getLoginID() + "添加商户基本信息. 商户注册手机号: " + paramter.getMerchantLoginID());

            /** 为商户分配秘钥  **/
            String termTmkKey = MD5.md5(UtilsConstant.getUUID(), "UTF-8").toUpperCase();
            String tmkKey = MD5.md5(UtilsConstant.getUUID(), "UTF-8").toUpperCase();

            TermKey termkey = new TermKey();
            termkey.setID(UtilsConstant.getUUID());
            termkey.setUserID(merchantInfo.getID());
            termkey.setTermTmkKey(termTmkKey);
            termkey.setTmkKey(tmkKey);

            log.info("业务员：" + user.getLoginID() + ", 添加新商户， 为新商户分配秘钥 , " + paramter.getMerchantLoginID() + "" +
                    "秘钥: termtmkkey = " + termTmkKey + " ;  tmkKey = " + tmkKey);


            // 根据客户端上传的支行名称 获取银行名称， 银联号等信息
            Map<String, String> bankCodeParams = new HashMap<>();
            bankCodeParams.put("bankBranch", paramter.getBankSubbranch());
            BankCode bankCode = bankCodeMapper.getBankCode(bankCodeParams);

            log.info("根据支行名称获取银行信息: " + JSONObject.fromObject(bankCode).toString());

            String bankType = "TOPRIVATE";

            /** 添加商户结算信息 **/
            UserBankCard bankCard = new UserBankCard();
            bankCard.setID(UtilsConstant.getUUID());
            bankCard.setUserID(merchantInfo.getID());
            bankCard.setAccountName(paramter.getName());
            bankCard.setBankName(bankCode.getBankName());
            bankCard.setBankBranch(bankCode.getBankBranch());
            bankCard.setBankCode(bankCode.getBankCode());
            bankCard.setSettleCreditCard(paramter.getCreditCardNo());
            bankCard.setAccountNo(paramter.getBankCardNo());
            bankCard.setBankProv(paramter.getBankProv());
            bankCard.setBankCity(paramter.getBankCity());
            bankCard.setSettleBankType(bankType);

            log.info("添加商户" + paramter.getMerchantLoginID() + "结算卡信息:");

            /**   添加商户费率信息 **/
            double feeRate = 4.6;
            if ("1".equals(paramter.getMerchantLevel())) {
                feeRate = 4.2;
            } else if ("2".equals(paramter.getMerchantLevel())) {
                feeRate = 3.8;
            }

            log.info("新增商户：" + paramter.getMerchantLoginID() + "的商户等级为" + paramter.getMerchantLevel() + " 优惠费率为：" + feeRate);

            List<PayChannel> payChannels = payChannelMapper.payChannelList();

            List<UserConfig> userConfigs = new ArrayList<>();
            for (int i = 0; i < payChannels.size(); i++) {
                UserConfig userconfig = new UserConfig();
                userconfig.setID(UtilsConstant.getUUID());
                userconfig.setUserID(merchantInfo.getID());
                userconfig.setPayChannel(payChannels.get(i).getID());
                userconfig.setSaleAmountMax("0");
                userconfig.setSaleAmountMaxDay("0");
                userconfig.setT1SaleRate(5);
                userconfig.setT0SaleRate(5);

                userconfig.setT1SettlementRate(feeRate);
                userconfig.setT0SettlementRate(feeRate);

                userConfigs.add(userconfig);
            }


            //  入网返回具体描述信息
            String respMsg = "";

            /**  上平台发送入网请求 **/

            log.info("新商户:" + merchantInfo.getLoginID() + "鉴权成功，向上游报商户");


            Random random = new Random(Constants.alipayMCCType.length - 1);
            int index = random.nextInt(Constants.alipayMCCType.length - 1);
            String alipaymcccNumber = Constants.alipayMCCType[index];

            random = new Random(Constants.wxMCCType.length - 1);
            index = random.nextInt(Constants.wxMCCType.length - 1);
            Integer wxmcccNumber = Constants.wxMCCType[index];

            UserConfig wxUserConfig = null;
            UserConfig aliUserConfig = null;

            for (int i = 0; i < userConfigs.size(); i++) {
                if (userConfigs.get(i).getPayChannel() == 1) {
                    wxUserConfig = userConfigs.get(i);
                }
                if (userConfigs.get(i).getPayChannel() == 2) {
                    aliUserConfig = userConfigs.get(i);
                }
            }


            Map<String, Object> merchantInMap = new TreeMap<String, Object>();
            merchantInMap.put("channelName", Constants.REPORT_CHANNELNAME);
            merchantInMap.put("channelNo", Constants.REPORT_CHANNELNO);
            merchantInMap.put("merchantName", merchantInfo.getMerchantName());
            merchantInMap.put("merchantBillName", merchantInfo.getMerchantBillName());
            merchantInMap.put("installProvince", merchantInfo.getState());
            merchantInMap.put("installCity", merchantInfo.getCity());
            merchantInMap.put("installCounty", merchantInfo.getRegion());
            merchantInMap.put("operateAddress", merchantInfo.getAddress());
            merchantInMap.put("merchantType", merchantInfo.getMerchantTypeValue());
            merchantInMap.put("businessLicense", businessLicense);
            merchantInMap.put("legalPersonName", merchantInfo.getName());
            merchantInMap.put("legalPersonID", merchantInfo.getIDCardNo());
            merchantInMap.put("merchantPersonName", merchantInfo.getName());
            merchantInMap.put("merchantPersonPhone", merchantInfo.getLoginID());

            merchantInMap.put("wxType", wxmcccNumber);
            merchantInMap.put("wxT1Fee", wxUserConfig.getT1SaleRate() / 10.0);
            merchantInMap.put("wxT0Fee", wxUserConfig.getT0SaleRate() / 10.0);

            merchantInMap.put("alipayType", alipaymcccNumber);
            merchantInMap.put("alipayT1Fee", aliUserConfig.getT1SaleRate() / 10.0);
            merchantInMap.put("alipayT0Fee", aliUserConfig.getT0SaleRate() / 10.0);

            merchantInMap.put("bankType", bankType);
            merchantInMap.put("accountName", bankCard.getAccountName());
            merchantInMap.put("accountNo", DESUtil.encode(Constants.REPORT_DES3_KEY, bankCard.getAccountNo()));
            merchantInMap.put("bankName", bankCode.getBankName());
            merchantInMap.put("bankProv", bankCard.getBankProv());
            merchantInMap.put("bankCity", bankCard.getBankCity());
            merchantInMap.put("bankBranch", bankCard.getBankBranch());
            merchantInMap.put("bankCode", bankCard.getBankCode());


            log.info("需要签名的的数据：" + JSONObject.fromObject(merchantInMap).toString() + Constants.REPORT_SIGN_KEY);

            String sign = MD5.sign(JSONObject.fromObject(merchantInMap).toString() + Constants.REPORT_SIGN_KEY, "utf-8");
            merchantInMap.put("sign", sign.toUpperCase());


            log.info("用户" + merchantInfo.getLoginID() + "入网请求报文:" + merchantInMap.toString());


            JSONObject respJS = null;
            String respCode = null;
            try {
                String content = HttpClient.post(Constants.REPORT_URL, merchantInMap, null);
                log.info("用户" + merchantInfo.getLoginID() + "入网响应报文:" + content);

                respJS = JSONObject.fromObject(content);
                respCode = respJS.getString("respCode");
            } catch (Exception e) {

                log.info(user.getLoginID() + "入网异常：" + e.getMessage());
                log.error("新增商户" + merchantInfo.getLoginID() + "入网异常:", e);

                paramter.setRespCode("01");
                paramter.setRespDesc("失败,请稍后再试");
            }

            if (Constants.payRetCode.equals(respCode)) {

                log.info("商户：" + paramter.getMerchantLoginID() + "在平台入网成功,保存商户秘钥等信息");

                String merchantNo = respJS.getString("merchantNo");// 商户号
                String signKey = respJS.getString("signKey");        //  微信签名秘钥
                String desKey = respJS.getString("desKey");            //  微信des秘钥
                String queryKey = respJS.getString("queryKey");        //  查询秘钥

                String AlipaySignKey = respJS.getString("AlipaySignKey");    // 支付宝签名秘钥
                String AlipaydesKey = respJS.getString("AlipaydesKey");        // 支付des秘钥

                /**
                 *  保存商户通道商编和密码信息
                 */
                List<PayMerchant> list = new ArrayList<PayMerchant>();

                PayMerchant payMerchant = new PayMerchant();
                payMerchant.setMerchantID(merchantNo);
                payMerchant.setMerchantName(merchantInfo.getMerchantName());
                payMerchant.setSignKey(signKey);
                payMerchant.setDESKey(desKey);
                payMerchant.setQueryKey(queryKey);
                payMerchant.setUserID(merchantInfo.getID());
                payMerchant.setPayType(Constants.PayChannelWXScancode);

                list.add(payMerchant);

                payMerchant = new PayMerchant();
                payMerchant.setMerchantID(merchantNo);
                payMerchant.setMerchantName(merchantInfo.getMerchantName());
                payMerchant.setSignKey(AlipaySignKey);
                payMerchant.setDESKey(AlipaydesKey);
                payMerchant.setQueryKey(queryKey);
                payMerchant.setUserID(merchantInfo.getID());
                payMerchant.setPayType(Constants.payChannelAliScancode);

                list.add(payMerchant);

                payMerchantMapper.saveMerchantInfo(list);

                log.info("保存新增商户：" + paramter.getMerchantLoginID() + "基本信息");
                loginMapper.addMerchant(merchantInfo);
                log.info("保存新增商户：" + paramter.getMerchantLoginID() + "秘钥信息");
                termkeyMapper.addTermKey(termkey);
                log.info("保存新增商户：" + paramter.getMerchantLoginID() + "结算信息");
                userBankCardMapper.addMerchantBankCardInfo(bankCard);
                log.info("保存新增商户：" + paramter.getMerchantLoginID() + " 交易费率信息");
                userConfigMapper.addMerchantConfig(userConfigs);

                paramter.setRespCode(RespCode.SUCCESS[0]);
                paramter.setRespDesc(RespCode.SUCCESS[1]);

            } else {
                if (respJS.has("respMsg")) {
                    respMsg = respJS.getString("respMsg");
                }
                log.info(user.getLoginID() + "入网异常：上游报备失败 , " + respMsg);

                paramter.setRespCode("01");
                paramter.setRespDesc(respMsg);
            }


        } catch (Exception e) {

            log.info("业务员" + user.getLoginID() + "新增商户:" + paramter.getMerchantLoginID() + "信息保存失败:" + e.getMessage());
            log.error("业务员" + user.getLoginID() + "新增商户:" + paramter.getMerchantLoginID() + "信息保存失败:", e);

            paramter.setRespCode(RespCode.ServerDBError[0]);
            paramter.setRespDesc(RespCode.ServerDBError[1]);
        }

        return paramter;
    }


    /**
     * 上传商户照片
     **/
    public ParamterData updatePhoto(LoginUser user, ParamterData paramterData) {


        String imgPath = Constants.imgPath;
        String imgUrl = Constants.imgUrl;

        /** 手持身份证照片  **/
        String handheldIDPhoto = paramterData.getHandheldIDPhoto();

        /** 身份证正面照片 **/
        String IDCardFrontPhoto = paramterData.getIDCardFrontPhoto();

        /** 身份证反面照片 **/
        String IDCardReversePhoto = paramterData.getIDCardReversePhoto();

        /** 银行卡照片 **/
        String bankCardPhoto = paramterData.getBankCardPhoto();

        /** 营业执照照片**/
        String businessPhoto = paramterData.getBusinessPhoto();


        String handheldIDurl = "", iDCardFront = "", iDCardReverse = "", bankCard = "", business = "";

        try {

            log.info(user.getLoginID() + "保存照片信息,保存顺序：手持身份证照片，身份证正面照照片，身份证反面照片，银行卡照片，营业执照照片");

            String imgName = UtilsConstant.getUUID();
            String postfix = ".jpg";

            if (!new File(imgPath + paramterData.getMerchantLoginID() + File.separator).exists()) {
                log.info(paramterData.getMerchantLoginID() + "保存图片的文件夹不存在，将创建文件 ，文件夹名称为该用户的手机号");
                new File(imgPath + paramterData.getMerchantLoginID() + File.separator).mkdirs();
            }


            if (!UtilsConstant.strIsEmpty(handheldIDPhoto)) {
                Image64Bit.GenerateImage(handheldIDPhoto.replace("\n", "").replace("\t", ""), imgPath + paramterData.getMerchantLoginID() + File.separator + imgName + postfix);
                handheldIDurl = imgUrl + paramterData.getMerchantLoginID() + File.separator + imgName + postfix;
                log.info(user.getLoginID() + "保存手持身份证照片成功");
            } else {
                log.info(user.getLoginID() + "手持身份证照片为空");
            }


            if (!UtilsConstant.strIsEmpty(IDCardFrontPhoto)) {
                imgName = UtilsConstant.getUUID();
                Image64Bit.GenerateImage(IDCardFrontPhoto.replace("\n", "").replace("\t", ""), imgPath + paramterData.getMerchantLoginID() + File.separator + imgName + postfix);
                iDCardFront = imgUrl + paramterData.getMerchantLoginID() + File.separator + imgName + postfix;

                log.info(user.getLoginID() + "保存身份证正面照片成功");
            } else {
                log.info(user.getLoginID() + "身份证正面照片为空");
            }


            if (!UtilsConstant.strIsEmpty(IDCardReversePhoto)) {
                imgName = UtilsConstant.getUUID();
                Image64Bit.GenerateImage(IDCardReversePhoto.replace("\n", "").replace("\t", ""), imgPath + paramterData.getMerchantLoginID() + File.separator + imgName + postfix);
                iDCardReverse = imgUrl + paramterData.getMerchantLoginID() + File.separator + imgName + postfix;

                log.info(user.getLoginID() + "保存身份证反面照片成功");
            } else {
                log.info(user.getLoginID() + "身份证反面照片为空");
            }


            if (!UtilsConstant.strIsEmpty(bankCardPhoto)) {
                imgName = UtilsConstant.getUUID();
                Image64Bit.GenerateImage(bankCardPhoto.replace("\n", "").replace("\t", ""), imgPath + paramterData.getMerchantLoginID() + File.separator + imgName + postfix);
                bankCard = imgUrl + paramterData.getMerchantLoginID() + File.separator + imgName + postfix;

                log.info(user.getLoginID() + "保存银行卡照片成功");
            } else {
                log.info(user.getLoginID() + "银行卡照片为空");
            }


            if (!UtilsConstant.strIsEmpty(businessPhoto)) {
                imgName = UtilsConstant.getUUID();
                Image64Bit.GenerateImage(businessPhoto.replace("\n", "").replace("\t", ""), imgPath + paramterData.getMerchantLoginID() + File.separator + imgName + ".jpg");
                business = imgUrl + paramterData.getMerchantLoginID() + File.separator + imgName + postfix;

                log.info(user.getLoginID() + "保存营业执照照片成功");
            } else {
                log.info(user.getLoginID() + "营业执照照片为空");
            }


        } catch (IOException e) {
            log.error(user.getLoginID() + "照片信息保存失败", e);
            log.info(user.getLoginID() + "照片信息保存失败");
            paramterData.setRespCode(RespCode.IMGSAVEError[0]);
            paramterData.setRespDesc(RespCode.IMGSAVEError[1]);
            return paramterData;
        }

        Map<String, String> photoMap = new HashMap<>();
        photoMap.put("HandheldIDPhoto", handheldIDurl);
        photoMap.put("IDCardFrontPhoto", iDCardFront);
        photoMap.put("IDCardReversePhoto", iDCardReverse);
        photoMap.put("BankCardPhoto", bankCard);
        photoMap.put("BusinessPhoto", business);
        photoMap.put("loginID", paramterData.getMerchantLoginID());

        int ret = loginMapper.updatePhotoInfo(photoMap);
        if (ret > 0) {
            log.info(paramterData.getMerchantLoginID() + "上传照片成功");
            paramterData.setRespCode(RespCode.SUCCESS[0]);
            paramterData.setRespDesc(RespCode.SUCCESS[1]);
        } else {
            log.info(paramterData.getMerchantLoginID() + "上传照片更新数据库失败");
            paramterData.setRespCode(RespCode.ServerDBError[0]);
            paramterData.setRespDesc(RespCode.ServerDBError[1]);
        }


        /** 手持身份证照片  **/
        paramterData.setHandheldIDPhoto("");

        /** 身份证正面照片 **/
        paramterData.setIDCardFrontPhoto("");

        /** 身份证反面照片 **/
        paramterData.setIDCardReversePhoto("");

        /** 银行卡照片 **/
        paramterData.setBankCardPhoto("");

        /** 营业执照照片**/
        paramterData.setBusinessPhoto("");

        System.gc();
        System.runFinalization();

        return paramterData;
    }


    /**
     * 商户列表
     **/
    @Override
    public ParamterData merchantlist(LoginUser user, ParamterData paramter) {


        log.info("业务员查询商户列表:" + user.getLoginID() + ",  查询关键词:" + paramter.getMerchantName());


        String merchantName = paramter.getMerchantName();

        if (UtilsConstant.strIsEmpty(merchantName)) {
            merchantName = "";
        }

        Map<String, String> map = new HashMap<>();
        map.put("userID", user.getID());
        map.put("merchantName", merchantName);

        List<Map<String, String>> list = loginMapper.merchantlist(map);

        paramter.setList(JSONArray.fromObject(list).toString());

        paramter.setRespCode(RespCode.SUCCESS[0]);
        paramter.setRespDesc(RespCode.SUCCESS[1]);
        return paramter;
    }


    /**
     * 修改商户等级
     *
     * @param user
     * @param paramter
     * @return
     */
    public ParamterData updateMerchantLevel(LoginUser user, ParamterData paramter) {


        String merchantLevel = paramter.getMerchantLevel();
        String loginID = paramter.getMerchantLoginID();

        Map<String,String> map = new HashMap<>();
        map.put("loginid" , loginID);

        //  获取要修改商户等级的商户信息
        LoginUser user2 = loginMapper.login(map);


        Integer tokerCount = loginMapper.tokerCount(user2.getID());

        log.info("商户：" + loginID + "拓客人数为：" + tokerCount + " ， 想要修改等级为：" + merchantLevel);

        if(tokerCount>=2 && tokerCount<5){
            // 当前用户已经发展超过2人
            if("0".equals(merchantLevel)){
                paramter.setRespCode(RespCode.EditMerchantLevelError[0]);
                paramter.setRespDesc(RespCode.EditMerchantLevelError[1]);
                return paramter;
            }
        }else if(tokerCount >= 5){

            if("0".equals(merchantLevel)||"1".equals(merchantLevel)){
                paramter.setRespCode(RespCode.EditMerchantLevelError2[0]);
                paramter.setRespDesc(RespCode.EditMerchantLevelError2[1]);
                return paramter;
            }
        }


        SimpleDateFormat sdf = new SimpleDateFormat(DateUtil.yyyy_MM_dd);//小写的mm表示的是分钟

        UserProperties userProperties = userPropertiesMapper.getUserProperties(user2.getID());

        try {
            Date nowDate = sdf.parse(DateUtil.getNowTime(DateUtil.yyyy_MM_dd));
            if(userProperties != null){
                //  证明存在修改记录
                Date merchantLevelDate =  userProperties.getMerchantLevelDate();


                long time1 = nowDate.getTime();
                long time2 = merchantLevelDate.getTime();
                long diff = time1 - time2;

                Long days = diff / (1000 * 60 * 60 * 24);

                if(days < 30){

                    log.info("距离上次修改商户等级没有查过30天： 当天时间:" + DateUtil.getNowTime(DateUtil.yyyy_MM_dd) + ". 上次修改时间:" + sdf.format(merchantLevelDate));
                    paramter.setRespCode(RespCode.EditMerchantLevelError3[0]);
                    paramter.setRespDesc(RespCode.EditMerchantLevelError3[1]);
                    return paramter;
                }else{
                    //  超过30天允许修改 将 修改时间更新为当前时间

                    log.info("距离上次修改商户登录已经超过30天，允许修改，并将系统时间更新为当前时间, 商户手机号：" + user2.getLoginID());
                    userProperties.setMerchantLevelDate(nowDate);
                    userPropertiesMapper.updateUserProperties(userProperties);
                }

            }else{
                // 没有修改记录 增加一条时间记录值

                log.info("商户：" + user2.getLoginID() + "没有修改记录，将在系统中增加一条数据，时间为当前时间");
                userProperties = new UserProperties();
                userProperties.setID(UtilsConstant.getUUID());
                userProperties.setUserID(user2.getID());
                userProperties.setMerchantLevelDate(nowDate);

                userPropertiesMapper.addUserProperties(userProperties);

            }
        } catch (ParseException e) {
            log.error("格式换日期出现异常：" + e.getMessage() , e);

            paramter.setRespCode(RespCode.NETWORKError[0]);
            paramter.setRespCode(RespCode.NETWORKError[1]);
            return paramter;
        }


        map = new HashMap<>();
        map.put("merchantLevel", merchantLevel);
        map.put("loginID", loginID);

        loginMapper.updateMerchantLevel(map);

        UserConfig userConfig = new UserConfig();
        userConfig.setUserID(user2.getID());

        Double feeRate = 4.6;
        if ("0".equals(merchantLevel)) {
            feeRate = 4.6;
        } else if ("1".equals(merchantLevel)) {
            feeRate = 4.2;
        } else if ("2".equals(merchantLevel)) {
            feeRate = 3.8;
        }
        userConfig.setT0SettlementRate(feeRate);
        userConfig.setT1SettlementRate(feeRate);
        userConfigMapper.updateUserConfig(userConfig);
        paramter.setRespCode(RespCode.SUCCESS[0]);
        paramter.setRespDesc(RespCode.SUCCESS[1]);
        return paramter;
    }


    /**
     * 为商户添加信用卡
     **/
    public ParamterData addCreditCardNo(LoginUser user, ParamterData paramter) {

        //  查询商户信息
        Map<String, String> map = new HashMap<String, String>();
        map.put("loginid", paramter.getMerchantLoginID());
        LoginUser user2 = loginMapper.login(map);

        //  查询商户结算卡信息
        UserBankCard userBankCard = userBankCardMapper.getUserBankCardInfo(user2.getID());

        String settleCreditCard = paramter.getCreditCardNo();


        /**
         *  用户信用卡 卡号鉴权
         */
        boolean flag = authencation(userBankCard.getAccountName() , user2.getIDCardNo() ,settleCreditCard);
        if(flag){
            log.info("商户：" + paramter.getMerchantLoginID() + "信用鉴权没有通过 , 卡号:" + settleCreditCard);
            paramter.setRespCode(RespCode.BankCardInfoErroe[0]);
            paramter.setRespDesc(RespCode.BankCardInfoErroe[1]);
            return paramter;
        }

        userBankCard.setSettleCreditCard(settleCreditCard);
        userBankCard.setUserID(user2.getID());

        userBankCardMapper.addCreditCardNo(userBankCard);

        paramter.setRespCode(RespCode.SUCCESS[0]);
        paramter.setRespDesc(RespCode.SUCCESS[1]);

        return paramter;
    }



    public  boolean authencation( String name , String IDCardNo ,String bankCardNo){
        Map<String, String> bankAuthencationMan = authenticationMapper.bankAuthenticationInfo(bankCardNo);
        if (bankAuthencationMan == null || bankAuthencationMan.isEmpty()) {

            log.info("未查到卡号：" + bankCardNo + "的鉴权信息");

            Map<String, String> authMap = new HashMap<String, String>();
            AuthService authService = new AuthService();
            authMap.put("accName", name);
            authMap.put("cardNo", bankCardNo);
            authMap.put("certificateNo", IDCardNo);
            Map<String, String> reqMap = authService.authKuai(authMap);

            log.info("新商户：鉴权，" + authMap.toString() + "鉴权结果:" + reqMap.toString());
            if (!reqMap.get("respCode").equals(Author.SUCESS_CODE)) {
                log.info("业务员新增用户： 银行信息鉴权没有通过");

                return true;
            } else {

                //  鉴权通过 将银行卡鉴权信息保存数据库
                Map<String, String> bankInfo = new HashMap<>();
                bankInfo.put("ID", UtilsConstant.getUUID());
                bankInfo.put("IdNumber", IDCardNo);
                bankInfo.put("RealName", name);
                bankInfo.put("BankCardNo", bankCardNo);
                bankInfo.put("RespCode", "00");
                bankInfo.put("RespDesc", reqMap.get("respMsg"));
                log.info("鉴权通过。将" + bankCardNo +"保存数据库");

                authenticationMapper.addAuthencationInfo(bankInfo);

            }
        } else {
            if (!name.equals(bankAuthencationMan.get("RealName")) || !IDCardNo.equals(bankAuthencationMan.get("IdNumber"))) {
                log.info("业务员新增用户：银行信息鉴权没有通过");
                return true;
            }else{
                log.info("卡号：" + bankCardNo + "查询到历史鉴权数据,并且信息一致");
            }
        }
        return false;
    }
}
