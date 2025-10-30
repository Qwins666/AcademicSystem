package com.academic.filter;

import com.academic.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;


@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                  FilterChain filterChain) throws ServletException, IOException {

        // 首先尝试从请求头获取token
        String token = request.getHeader("Authorization");

        // 添加日志
        logger.info("JWT Filter - Request URI的值: " + request.getRequestURI());
        logger.info("JWT Filter - Authorization header的值: " + token);

        // 如果请求头中没有token，则尝试从URL参数中获取
        if (token == null || !token.startsWith("Bearer ")) {
            token = request.getParameter("token");
            logger.info("JWT Filter - URL parameter token的值: " + token);
            // 从URL参数获取的token不需要去掉Bearer前缀
        } else {
            // 从请求头获取的token需要去掉Bearer前缀
            token = token.substring(7);
        }

        if (token != null) {

            try {
                if (jwtUtil.validateToken(token, jwtUtil.getUsernameFromToken(token))) {
                    String role = jwtUtil.getRoleFromToken(token);
                    String username = jwtUtil.getUsernameFromToken(token);

                    logger.info("JWT Filter - Valid token for user: " + username + " with role: " + role);

                    UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                            username,
                            null,
                            Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role))
                        );

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }else{
                    logger.warn("JWT Filter - Token validation failed");
                }
            } catch (Exception e) {
                logger.error("JWT Filter - Error processing token: " + e.getMessage());
                SecurityContextHolder.clearContext();
            }
        }else{
            logger.warn("JWT Filter - No Bearer token found in Authorization header");
        }

        filterChain.doFilter(request, response);
    }
}
