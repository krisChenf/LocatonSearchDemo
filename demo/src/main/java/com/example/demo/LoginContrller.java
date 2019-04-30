package com.example.demo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import javax.servlet.http.HttpServletRequest;

/**
 * 登录控制层
 * @return 登录页面
 * @Author chenfei
 */
@Controller
public class LoginContrller {
    @Value("${customUrl.gitHubOAuth}")
    private String oauthUrl;

    /**
     * 重定向到github OAuth身份认证
     */
    @RequestMapping("/login")
    public String login(HttpServletRequest request) {
        String access_token = (String) request.getSession().getAttribute("access_token");
        // 登录判断 未登录 跳转登录页面 并拉去用户信息
        if (StringUtils.isEmpty(access_token)) {
            return "redirect:" + oauthUrl;
        }
        // 已登录直接转发至搜索界面
        return "forward:/search/toSearchPage";
    }

}
