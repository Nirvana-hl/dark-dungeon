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
        String requestPath = request.getRequestURI();
        
        // 只处理需要认证的路径
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                // 验证 token 是否有效
                if (!jwtUtil.validateToken(token)) {
                    System.out.println("[JwtFilter] Token 已过期或无效: " + requestPath);
                    filterChain.doFilter(request, response);
                    return;
                }
                
                Claims claims = jwtUtil.getClaimsFromToken(token);
                if (claims != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    String userIdStr = claims.get("userId", String.class);
                    if (StringUtils.hasText(userIdStr)) {
                        Long userId = null;
                        try {
                            userId = Long.parseLong(userIdStr);
                        } catch (NumberFormatException e) {
                            System.out.println("[JwtFilter] 无法解析 userId: " + userIdStr);
                        }

                        if (userId != null) {
                            User user = userService.getUserById(userId);
                            if (user == null) {
                                System.out.println("[JwtFilter] 用户不存在: userId=" + userId);
                            } else if (!"active".equalsIgnoreCase(user.getStatus())) {
                                System.out.println("[JwtFilter] 用户状态不是 active: userId=" + userId + ", status=" + user.getStatus());
                            } else {
                                // 设置认证信息
                                UsernamePasswordAuthenticationToken authenticationToken =
                                        new UsernamePasswordAuthenticationToken(
                                                user,
                                                null,
                                                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
                                authenticationToken.setDetails(
                                        new WebAuthenticationDetailsSource().buildDetails(request));
                                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                                System.out.println("[JwtFilter] 认证成功: userId=" + userId + ", path=" + requestPath);
                            }
                        }
                    } else {
                        System.out.println("[JwtFilter] Token 中未找到 userId: " + requestPath);
                    }
                }
            } catch (io.jsonwebtoken.ExpiredJwtException e) {
                System.out.println("[JwtFilter] Token 已过期: " + requestPath);
            } catch (io.jsonwebtoken.MalformedJwtException e) {
                System.out.println("[JwtFilter] Token 格式错误: " + requestPath);
            } catch (io.jsonwebtoken.security.SignatureException e) {
                System.out.println("[JwtFilter] Token 签名验证失败: " + requestPath);
            } catch (Exception e) {
                System.out.println("[JwtFilter] Token 验证异常: " + e.getClass().getSimpleName() + " - " + e.getMessage() + ", path=" + requestPath);
                e.printStackTrace();
            }
        } else {
            // 没有 Authorization 头，记录日志（仅对需要认证的路径）
            if (!requestPath.startsWith("/auth/") && 
                !requestPath.startsWith("/player-characters") &&
                !requestPath.startsWith("/stages") &&
                !requestPath.startsWith("/card-characters") &&
                !requestPath.startsWith("/cards") &&
                !requestPath.equals("/") &&
                !requestPath.equals("/health")) {
                System.out.println("[JwtFilter] 请求缺少 Authorization 头: " + requestPath);
            }
        }

        filterChain.doFilter(request, response);
    }
}


