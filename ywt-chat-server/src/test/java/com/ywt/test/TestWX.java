package com.ywt.test;

import com.ywt.user.config.WxMpProperties;
import com.ywt.user.domain.enums.IdempotentEnum;
import com.ywt.user.service.UserBackpackService;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.result.WxMpQrCodeTicket;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * 功能描述
 *
 * @author: ywt
 * @date: 2024年04月17日 17:38
 */

@SpringBootTest
@RunWith(SpringRunner.class)
public class TestWX {

    @Autowired
    private WxMpService wxMpService;

    @Autowired
    private UserBackpackService userBackpackService;

    @Test
    public void test() throws WxErrorException {
        WxMpQrCodeTicket wxMpQrCodeTicket = wxMpService.getQrcodeService().qrCodeCreateTmpTicket(1, 1000);
        String url = wxMpQrCodeTicket.getUrl();
        System.out.println("url = " + url);
    }

    @Test
    public void testUserBanked()  {
       userBackpackService.acquireItem(11001L,1L, IdempotentEnum.UID,"11001");
    }

}
