package com.ywt.user.controller;

/**
 * 功能描述
 *
 * @author: ywt
 * @date: 2024年04月17日 16:35
 */

import com.ywt.user.service.WXService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.bean.WxOAuth2UserInfo;
import me.chanjar.weixin.common.bean.oauth2.WxOAuth2AccessToken;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpMessageRouter;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import me.chanjar.weixin.mp.bean.result.WxMpQrCodeTicket;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

/**
 * Description: 微信api交互接口
 * Date: 2023-03-19
 */
@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("wx/portal/public")
public class WxPortalController {

    private final WxMpService wxMpService;
    private final WxMpMessageRouter messageRouter;

    @Autowired
    private WXService wxService;

    @GetMapping("/test")
    public String test() throws WxErrorException {
        WxMpQrCodeTicket wxMpQrCodeTicket = wxMpService.getQrcodeService().qrCodeCreateTmpTicket(1, 1000);

        return wxMpQrCodeTicket.getUrl();
    }


    /**
     * 这段代码是一个基于Spring框架的Java代码，用于处理微信服务器发送过来的认证消息，通常用于验证你的服务器是否有效并接受来自微信服务器的消息。
     * 这是方法的签名，它接收来自微信服务器的四个参数：signature、timestamp、nonce、echostr。这些参数是微信服务器用于验证服务器有效性的参数。
     * @return
     */
    @GetMapping(produces = "text/plain;charset=utf-8")
    public String authGet(@RequestParam(name = "signature", required = false) String signature,
                          @RequestParam(name = "timestamp", required = false) String timestamp,
                          @RequestParam(name = "nonce", required = false) String nonce,
                          @RequestParam(name = "echostr", required = false) String echostr) {

        log.info("\n接收到来自微信服务器的认证消息：[{}, {}, {}, {}]", signature,
                timestamp, nonce, echostr);

        // 这段代码用于检查接收到的参数是否存在空值
        if (StringUtils.isAnyBlank(signature, timestamp, nonce, echostr)) {
            throw new IllegalArgumentException("请求参数非法，请核实!");
        }

        // 这段代码调用了wxService.checkSignature(timestamp, nonce, signature)方法，该方法用于验证签名是否正确。如果验证通过，则返回echostr，表示验证成功。
        if (wxMpService.checkSignature(timestamp, nonce, signature)) {
            return echostr;
        }

        return "非法请求";
    }

    @GetMapping("/callBack")
    public RedirectView callBack(@RequestParam String code) {
        WxOAuth2UserInfo userInfo;
        // 调用微信接口，使用code获取accessToken,再获取用户信息
        try {
            WxOAuth2AccessToken accessToken = wxMpService.getOAuth2Service().getAccessToken(code);
            userInfo = wxMpService.getOAuth2Service().getUserInfo(accessToken,"zh_CN");
        } catch (WxErrorException e) {
            throw new RuntimeException(e);
        }
        // 用户授权
        wxService.authorize(userInfo);

        RedirectView redirectView = new RedirectView();

        redirectView.setUrl("https://mp.weixin.qq.com/s?__biz=MzkwMjY4NTMyMQ==&mid=2247483654&idx=1&sn=81b295c3dbe89d390d492d42e4634ac9&chksm=c0a0f6e0f7d77ff679c7e66916bd116015e516511fb58c60da1e7102560e35323f33415f4486&token=2083311068&lang=zh_CN#rd");

        return redirectView;
    }

    @PostMapping(produces = "application/xml; charset=UTF-8")
    public String post(@RequestBody String requestBody,
                       @RequestParam("signature") String signature,
                       @RequestParam("timestamp") String timestamp,
                       @RequestParam("nonce") String nonce,
                       @RequestParam("openid") String openid,
                       @RequestParam(name = "encrypt_type", required = false) String encType,
                       @RequestParam(name = "msg_signature", required = false) String msgSignature) {
        // 记录接收到的微信请求信息
        log.info("\n接收微信请求：[openid=[{}], [signature=[{}], encType=[{}], msgSignature=[{}],"
                        + " timestamp=[{}], nonce=[{}], requestBody=[\n{}\n] ",
                openid, signature, encType, msgSignature, timestamp, nonce, requestBody);

        // 验证请求的合法性
        if (!wxMpService.checkSignature(timestamp, nonce, signature)) {
            throw new IllegalArgumentException("非法请求，可能属于伪造的请求！");
        }

        String out = null;
        if (encType == null) {
            // 明文传输的消息
            // 解析微信服务器发送过来的XML消息
            WxMpXmlMessage inMessage = WxMpXmlMessage.fromXml(requestBody);
            // 路由消息并获取回复消息
            WxMpXmlOutMessage outMessage = this.route(inMessage);
            if (outMessage == null) {
                return "";
            }
            // 将回复消息转换为XML格式
            out = outMessage.toXml();
        } else if ("aes".equalsIgnoreCase(encType)) {
            // aes加密的消息
            // 解密微信服务器发送过来的加密XML消息
            WxMpXmlMessage inMessage = WxMpXmlMessage.fromEncryptedXml(requestBody, wxMpService.getWxMpConfigStorage(),
                    timestamp, nonce, msgSignature);
            log.debug("\n消息解密后内容为：\n{} ", inMessage.toString());
            // 路由消息并获取回复消息
            WxMpXmlOutMessage outMessage = this.route(inMessage);
            if (outMessage == null) {
                return "";
            }
            // 将回复消息加密为XML格式
            out = outMessage.toEncryptedXml(wxMpService.getWxMpConfigStorage());
        }

        // 记录组装的回复信息
        log.debug("\n组装回复信息：{}", out);
        // 返回回复消息
        return out;
    }

    // 路由消息并获取回复消息
    private WxMpXmlOutMessage route(WxMpXmlMessage message) {
        try {
            return this.messageRouter.route(message);
        } catch (Exception e) {
            log.error("路由消息时出现异常！", e);
        }
        return null;
    }
}
