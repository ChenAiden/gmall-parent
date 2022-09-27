package com.atguigu.gmall.weball.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Aiden
 * @create 2022-09-26 11:27
 */
@Controller
public class LoginController {

    /**
     * 跳转到登陆页面
     *
     * @param request
     * @return
     */
    @GetMapping("/login.html")
    public String login(HttpServletRequest request) {

        request.setAttribute("originUrl", request.getParameter("originUrl"));

        return "login";
    }

}
