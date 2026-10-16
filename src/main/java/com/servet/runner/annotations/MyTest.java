
package com.servet.runner.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Bir metodu test olarak işaretler (JUnit'teki @Test'in karşılığı). */
@Retention(RetentionPolicy.RUNTIME) // Runtime'da reflection ile okunabilmesi için şart
@Target(ElementType.METHOD)
public @interface MyTest {

    /** Testin fırlatması beklenen exception. Varsayılan: hiçbiri. */
    Class<? extends Throwable> expected() default None.class;

    /** "Exception beklenmiyor" anlamına gelen işaretçi sınıf. */
    final class None extends Throwable {
        private static final long serialVersionUID = 1L;

        private None() {
        }
    }
}