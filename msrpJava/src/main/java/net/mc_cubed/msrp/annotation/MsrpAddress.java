/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.mc_cubed.msrp.annotation;

import java.lang.annotation.Retention;
import static java.lang.annotation.ElementType.*;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * @author charles
 */
@Target({FIELD, PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface MsrpAddress {
    String host() default "";
    int port() default 0;
}
