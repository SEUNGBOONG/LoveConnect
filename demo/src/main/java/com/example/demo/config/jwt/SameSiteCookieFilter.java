package com.example.demo.config.jwt;

import jakarta.servlet.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class SameSiteCookieFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (response instanceof HttpServletResponse httpServletResponse) {
            HttpServletResponse wrappedResponse = new HttpServletResponseWrapper(httpServletResponse) {
                @Override
                public void addCookie(Cookie cookie) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(cookie.getName()).append("=").append(cookie.getValue())
                            .append("; Path=").append(cookie.getPath() == null ? "/" : cookie.getPath());

                    if (cookie.getMaxAge() >= 0) {
                        sb.append("; Max-Age=").append(cookie.getMaxAge());
                    }

                    String serverName = request.getServerName();
                    boolean isLocal = serverName.equals("localhost") || serverName.equals("127.0.0.1");

                    // 운영 환경(https)에서만 Secure; SameSite=None 적용
                    if (!isLocal) {
                        sb.append("; Secure; SameSite=None");
                    }

                    if (cookie.isHttpOnly()) sb.append("; HttpOnly");

                    if (cookie.getDomain() != null) {
                        sb.append("; Domain=").append(cookie.getDomain());
                    }

                    super.addHeader("Set-Cookie", sb.toString());
                }
            };
            chain.doFilter(request, wrappedResponse);
        } else {
            chain.doFilter(request, response);
        }
    }
}
