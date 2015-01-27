package com.freedom.health4j.api.impl.common;

import com.freedom.health4j.Constants;
import com.freedom.health4j.api.ReportNotifier;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * email notifier.
 */
public class EmailNotifier implements ReportNotifier {

    private static final Log logger = LogFactory.getLog(EmailNotifier.class);

    private Properties commonConfig;

    public EmailNotifier() {
    }

    @Override
    public void doNotify() {
        try {
            HtmlEmail email = new HtmlEmail();

            email.setHostName(getCommonConfig().getProperty("health4j.email.host"));
            email.setSmtpPort(Integer.parseInt(getCommonConfig().getProperty("health4j.email.port")));

            String authName = getCommonConfig().getProperty("health4j.email.auth.name");
            String authPwd = getCommonConfig().getProperty("health4j.email.auth.password");
            email.setAuthentication(authName, authPwd);

            email.setCharset("UTF-8");
            String subject = getCommonConfig().getProperty("health4j.email.subject");
            email.setSubject(getCommonConfig().getProperty(Constants.COMMON_PROJECT_KEY) + "-" + subject);

            email.addTo(getCommonConfig().getProperty("health4j.email.to"));
            email.setFrom(getCommonConfig().getProperty("health4j.email.from"));

            String cc = getCommonConfig().getProperty("health4j.email.cc");
            if (cc != null && cc.length() > 0)
                email.addCc(cc);

            String reportBasePathStr = getCommonConfig().getProperty(Constants.COMMON_REPORT_BASE_PATH_KEY);
            String reportPathStr = getCommonConfig().getProperty(Constants.COMMON_REPORT_PATH_KEY);
            Path reportPath = Paths.get(reportBasePathStr, reportPathStr);

            String htmlContent = new String(Files.readAllBytes(reportPath), "utf-8");
            email.setHtmlMsg(htmlContent);
            email.setTextMsg("你的邮箱当前不支持接收HTML格式的消息，请通过附件查看!");

            email.setDebug(logger.isDebugEnabled());
            email.buildMimeMessage();

            email.sendMimeMessage();
        } catch (EmailException | IOException e) {
            throw new RuntimeException(e.toString());
        }
    }

    @Override
    public Properties getCommonConfig() {
        return commonConfig;
    }

    @Override
    public void setCommonConfig(Properties commonConfig) {
        this.commonConfig = commonConfig;
    }
}
