package com.dungeon.security;

import com.dungeon.entity.User;
import com.dungeon.service.UserService;
import com.dungeon.util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

/**
 * JWT 认证过滤器：解析 Authorization 头中的 Bearer Token，并设置认证信息
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                Claims claims = jwtUtil.getClaimsFromToken(token);
                if (claims != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    String userIdStr = claims.get("userId", String.class);
                    if (StringUtils.hasText(userIdStr)) {
                        Long userId = null;
                        try {
                            userId = Long.parseLong(userIdStr);
                        } catch (NumberFormatException ignored) {}

                        if (userId != null) {
                            User user = userService.getUserById(userId);
                            if (user != null && "active".equalsIgnoreCase(user.getStatus())) {
                                UsernamePasswordAuthenticationToken authenticationToken =
                                        new UsernamePasswordAuthenticationToken(
                                                user,
                                                null,
                                                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
                                authenticationToken.setDetails(
                                        new WebAuthenticationDetailsSource().buildDetails(request));
                                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                // 无效 token，直接忽略，后续过滤器会返回 401/403
            }
        }

        filterChain.doFilter(request, response);
    }
}


