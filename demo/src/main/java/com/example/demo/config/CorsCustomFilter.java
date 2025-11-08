package com.example.demo.config;//package com.example.demo.config;
//
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import org.springframework.web.filter.OncePerRequestFilter;
//
//import java.io.IOException;
//
//public class CorsCustomFilter extends OncePerRequestFilter {
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest request,
//                                    HttpServletResponse response,
//                                    FilterChain filterChain) throws ServletException, IOException {
//        String origin = request.getHeader("Origin");
//
//        if ("https://daemyungdesk.com".equals(origin)
//                || "https://www.daemyungdesk.com".equals(origin)
//                || "http://localhost:3000".equals(origin)) {
//            response.setHeader("Access-Control-Allow-Origin", origin);
//        }
//
//        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
//        response.setHeader("Access-Control-Allow-Headers", "Origin, Content-Type, Accept, Authorization, Cookie");
//        response.setHeader("Access-Control-Allow-Credentials", "true");
//        response.setHeader("Access-Control-Max-Age", "3600");
//
//        // 🔑 OPTIONS 프리플라이트 요청은 여기서 끝내기
//        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
//            response.setStatus(HttpServletResponse.SC_OK);
//            return;
//        }
//
//        filterChain.doFilter(request, response);
//    }
//}
