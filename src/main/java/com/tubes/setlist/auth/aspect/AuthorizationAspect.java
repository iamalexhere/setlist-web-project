package com.tubes.setlist.auth.aspect;

import java.util.Arrays;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.tubes.setlist.auth.RequiredRole;
import jakarta.servlet.http.HttpSession;

@Aspect
@Component
public class AuthorizationAspect {

    @Autowired
    private HttpSession session;

    @Before("@annotation(requiredRole)")
    public void checkAuthorization(JoinPoint joinPoint, RequiredRole requiredRole) throws Throwable {
        String[] roles = requiredRole.value();
        
        Integer userId = (Integer) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");

        if(userId == null) {
            throw new RuntimeException("User not logged in");
        }

        if (Arrays.asList(roles).contains("*")) {
            return;
        }

        if(!Arrays.asList(roles).contains(role)) {
            throw new SecurityException("User does not have required role: " + Arrays.toString(roles));
        }
    }
}
