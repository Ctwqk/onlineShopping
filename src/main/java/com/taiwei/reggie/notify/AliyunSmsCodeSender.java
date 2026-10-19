package com.taiwei.reggie.notify;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.profile.DefaultProfile;
import com.taiwei.reggie.common.CustomException;
import com.taiwei.reggie.config.ReggieProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * SMS delivery through Alibaba Cloud Dysmsapi. Enabled with {@code reggie.verification.channel=sms}; credentials,
 * signature and template come from {@code reggie.sms.*} (environment variables, never from source control).
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "reggie.verification", name = "channel", havingValue = "sms")
public class AliyunSmsCodeSender implements CodeSender {

    private final ReggieProperties.Sms sms;
    private final IAcsClient client;

    public AliyunSmsCodeSender(ReggieProperties properties) {
        this.sms = properties.getSms();
        if (!StringUtils.hasText(sms.getAccessKeyId()) || !StringUtils.hasText(sms.getAccessKeySecret())
                || !StringUtils.hasText(sms.getSignName()) || !StringUtils.hasText(sms.getTemplateCode())) {
            throw new IllegalStateException("reggie.verification.channel=sms requires reggie.sms.access-key-id, "
                    + "access-key-secret, sign-name and template-code (REGGIE_SMS_* environment variables)");
        }
        DefaultProfile profile = DefaultProfile.getProfile(sms.getRegionId(), sms.getAccessKeyId(),
                sms.getAccessKeySecret());
        this.client = new DefaultAcsClient(profile);
    }

    @Override
    public void send(String target, String code) {
        SendSmsRequest request = new SendSmsRequest();
        request.setSysRegionId(sms.getRegionId());
        request.setPhoneNumbers(target);
        request.setSignName(sms.getSignName());
        request.setTemplateCode(sms.getTemplateCode());
        request.setTemplateParam("{\"code\":\"" + code + "\"}");
        try {
            SendSmsResponse response = client.getAcsResponse(request);
            if (!"OK".equalsIgnoreCase(response.getCode())) {
                log.warn("SMS provider rejected the message: {} {}", response.getCode(), response.getMessage());
                throw new CustomException("failed to send the verification code");
            }
        } catch (ClientException e) {
            log.warn("SMS delivery failed: {}", e.getErrMsg());
            throw new CustomException("failed to send the verification code");
        }
    }
}
