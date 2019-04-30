package com.example.demo;

import com.alibaba.fastjson.JSONObject;
import com.example.demo.pojo.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import utils.HttpClient;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * gitHub OAUth 开放授权认证服务
 */
@Component
public class OAuthService {
    private final static Logger logger = LoggerFactory.getLogger(OAuthService.class);
    private final static Map<String, Object> outhenData = new HashMap<String, Object>();
    @Value("${customUrl.userIfoUrl}")
    private String userInfUrl;
    @Value("${customUrl.userTockenUrl}")
    private String userTockenUrl;
    @Value("${customUrl.clientSecret}")
    private String clientSecret;
    @Value("${customUrl.clientId}")
    private String clientId;

    /**
     * gitHub登录后跳回至本地，
     * 并根据返回参数，再次从gitHub获取tocken和用户信息，存入session
     *
     * @param request
     * @return 用户信息
     */
    public String getUserInfo(HttpServletRequest request) {
        String code = request.getParameter("code");
        if (StringUtils.isEmpty(code)) {// code为空
            return null;
        }
        logger.info("user code is" + code);
        // 获取用户access_token
        outhenData.put("code", code);
        outhenData.put("client_secret", clientSecret);
        outhenData.put("client_id", clientId);
        String gitHubResp;
        try {
            gitHubResp = HttpClient.doPost(userTockenUrl, outhenData);
        } catch (Exception e) {
            logger.error("登录异常...", e);
            return null;
        }
        if (StringUtils.isEmpty(gitHubResp)) {
            logger.info("获取获取用户access_token返回为空....");
            return null;
        }
        String access_token = getAccessTocken(gitHubResp);
        if (StringUtils.isEmpty(access_token)) {
            logger.info("用户access_token返回为空....");
            return null;
        }
        request.getSession().setAttribute("access_token", access_token);
        logger.info("access_token is " + access_token);
        // 获取用户信息
        String userInfo;
        try {
            userInfo = HttpClient.doGet(userInfUrl + access_token);
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("拉取用户信息异常....", e);
            return null;
        }
        if (StringUtils.isEmpty(userInfo)) {
            logger.info("拉取gitHub返回用户信息为空...");
        }
        logger.info("获取用户信息成功 ：" + userInfo);
        User user = JSONObject.parseObject(userInfo, User.class);
        request.getSession().setAttribute("user", user);
        return userInfo;
    }

    /**
     * 解析 返回用户access_tocken
     *
     * @param gitHubResp
     * @return
     */
    private String getAccessTocken(String gitHubResp) {
        String[] strs = gitHubResp.split("&");
        Map map = new HashMap();
        for (String s : strs) {
            String[] ar1 = s.split("=");
            String key = (String) ar1[0];
            String value = null;
            if (ar1.length > 1) {
                value = (String) ar1[1];
            }
            map.put(key, value);
        }
        return (String) map.get("access_token");
    }
}
