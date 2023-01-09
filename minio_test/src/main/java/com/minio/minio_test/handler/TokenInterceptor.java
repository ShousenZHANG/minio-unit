package com.minio.minio_test.handler;

import com.minio.minio_test.config.JwtConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.SignatureException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 拦截器
 *
 * @author zhang
 * @date 2023/01/04
 */
@Component
public class TokenInterceptor extends HandlerInterceptorAdapter {

    @Resource
    private JwtConfig jwtConfig;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws SignatureException {
        //获取请求过来的地址信息
        String uri = request.getRequestURI();
        if (uri.contains("/login")) {
            return true;
        }
        //获取请求头中携带的token
        String token = request.getHeader(jwtConfig.getHeader());
        if (StringUtils.isEmpty(token)) {
            token = request.getParameter(jwtConfig.getHeader());
        }
        if (StringUtils.isEmpty(token)) {
            throw new SignatureException(jwtConfig.getHeader() + "不能为空");
        }

        Claims claims = null;
        try {
            claims = jwtConfig.getTokenClaim(token);
            if (claims == null || jwtConfig.isTokenExpired(claims.getExpiration())) {
                throw new SignatureException(jwtConfig.getHeader() + "失效，请重新登录。");
            }
        } catch (Exception e) {
            throw new SignatureException(jwtConfig.getHeader() + "失效，请重新登录。");
        }
        //设置 identityId 用户身份ID
        request.setAttribute("identityId", claims.getSubject());
        return true;
    }
}