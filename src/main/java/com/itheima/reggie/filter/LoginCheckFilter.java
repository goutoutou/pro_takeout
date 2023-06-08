package com.itheima.reggie.filter;

import com.alibaba.fastjson.JSON;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

//检查用户是否已经完成登录
@WebFilter(filterName = "loginCheckFilter",urlPatterns = "/*")
@Slf4j
public class LoginCheckFilter implements Filter {
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();//路径匹配
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        //1获得本次请求的URI
        String requestURI = request.getRequestURI();
        log.info("拦截到请求：{}",requestURI);
        //把不需要过滤的请求路径在数组里记录
        String[] uris = new String[]{
                "/employee/login",
                "/employee/logout",
                "/backend/**","/front/**","/common/**",
                "/user/sendMsg","/user/login"
        };
        //2判断本次请求是否需要处理
        boolean check = check(requestURI,uris);
        //3不需要处理直接放行
        if (check) {
            log.info("不需要处理:{}",requestURI);
            filterChain.doFilter(request,response);
            return;
        }
        //4需要处理
        //判断用户端是否完成登录
        if(request.getSession().getAttribute("employee")!=null){
            log.info("用户已经登录：{}",request.getSession().getAttribute("employee"));
            Long empId = (Long) request.getSession().getAttribute("employee");
            BaseContext.setCurrentId(empId);
            filterChain.doFilter(request,response);
            return;
        }
        if(request.getSession().getAttribute("user")!=null){
            log.info("用户已经登录：{}",request.getSession().getAttribute("user"));
            Long userId = (Long) request.getSession().getAttribute("user");
            BaseContext.setCurrentId(userId);//将用户id存入ThreadLocal
            filterChain.doFilter(request,response);
            return;
        }
        //5没有登录则返回未登录结果，通过输出流方式向客户端页面响应数据
        log.info("用户未登录");
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
        return;
    }
    //路径匹配检查本次请求是否需要放行
    public boolean check(String requestURI, String[] urls){
        for(String url : urls){
            boolean match = PATH_MATCHER.match(url,requestURI);//spring提供的路径匹配类，完全匹配，另外还有*匹配
            if(match) return true;
        }
        return false;
    }
}
