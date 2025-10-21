package com.sk.movie.security;

import com.sk.movie.entities.UserRole;
import com.sk.movie.exceptions.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RBACInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            RequiresRole requiresRole = handlerMethod.getMethodAnnotation(RequiresRole.class);

            if (requiresRole != null) {
                // Get user role from JWT token or session
                UserRole userRole = (UserRole) request.getAttribute("userRole");

                if (userRole == null) {
                    throw new UnauthorizedException("Authentication required");
                }

                if (userRole != requiresRole.value() && userRole != UserRole.ADMIN) {
                    throw new UnauthorizedException(
                            "Access denied. Required role: " + requiresRole.value()
                    );
                }
            }
        }
        return true;
    }
}
