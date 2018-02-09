package com.ymlion.smsidentify.util;

import org.junit.Assert;

/**
 * sms test
 * Created by YMlion on 2018/2/9.
 */
public class SmsUtilTest {
    @org.junit.Test public void findCode1() throws Exception {
        String code = SmsUtil.findCode("您的账户1234正在办理业务，验证码123456，请妥善保管，切勿泄露。[世界银行]");
        System.out.println(code);
        Assert.assertEquals("123456", code);
    }

    @org.junit.Test public void findCode2() throws Exception {
        String code =
                SmsUtil.findCode("您正在登录世界银行业务办理页面，如非本人操作请致电4001234567，验证 码123456，请勿向他人泄露验证码。-世界银行");
        System.out.println(code);
        Assert.assertEquals("123456", code);
    }
}