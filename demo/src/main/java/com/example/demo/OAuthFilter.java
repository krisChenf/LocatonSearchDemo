package com.example.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 登录认证拦截
 * @author chenfei 2019年4月29日
 */
@WebFilter(urlPatterns = {"/search/*"},filterName = "testFilter1")
public class OAuthFilter implements Filter {
    private final static Logger logger = LoggerFactory.getLogger(OAuthFilter.class);
    @Autowired
    private OAuthService oAuthService;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        // 登录判断 未登录 跳转登录页面 并拉去用户信息
        String access_token = (String) request.getSession().getAttribute("access_token");
        if (StringUtils.isEmpty(access_token)){
            String userInfo = oAuthService.getUserInfo(request);
            if(StringUtils.isEmpty(userInfo)) {
                logger.info("用户认证异常....");
                // 方案1 重定向到错误页面
//                RequestDispatcher dispatcher = request.getRequestDispatcher("/error.html");
//                dispatcher.forward(request, servletResponse);
//                 通过流回写错误信息
                PrintWriter out = servletResponse.getWriter();
                out.print("<script>alert('不是该系统用户！请重新登录');</script>");
                out.close();
                return;
            }
        }
        filterChain.doFilter(servletRequest,servletResponse);
    }

    @Override
    public void destroy() {

    }
}
