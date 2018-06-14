/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.zorgrdjan.web.filteri;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 *
 * @author Zoran
 */
@WebFilter(filterName = "FilterAplikacije", urlPatterns = {"/*"})
public class FilterAplikacije implements Filter {

    @Override
    public void init(FilterConfig fc) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        
        try {
            HttpServletRequest reqt = (HttpServletRequest) request;
            HttpServletResponse resp = (HttpServletResponse) response;
            HttpSession ses = reqt.getSession(false);

            String reqURI = reqt.getRequestURI();
            if (reqURI.contains("/login.xhtml") || reqURI.contains("/pogled1.xhtml") ||
                    reqURI.contains("/pogled2.xhtml")) {
                if (reqURI.contains("/login.xhtml")
                        || (ses != null && ses.getAttribute("kor_ime") != null)) {
                    chain.doFilter(request, response);
                } else {
                    resp.sendRedirect(reqt.getContextPath() + "/faces/login.xhtml");
                }
            } else {
                chain.doFilter(request, response);
            }
        } catch (IOException | ServletException e) {
            System.out.println(e.getMessage());
        }

    }

    @Override
    public void destroy() {

    }

}
