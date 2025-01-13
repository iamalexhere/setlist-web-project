package com.tubes.setlist.auth;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.aspectj.lang.annotation.Aspect;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Aspect
public @interface RequiredRole {
    String[] value();
}
