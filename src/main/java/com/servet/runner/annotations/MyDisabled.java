package com.servet.runner.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Testi atlar (JUnit'teki @Disabled). */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface MyDisabled {

    /** Neden atlandığı. */
    String value() default "";
}