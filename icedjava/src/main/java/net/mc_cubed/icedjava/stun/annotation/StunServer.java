/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.mc_cubed.icedjava.stun.annotation;

import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.*;
import static java.lang.annotation.ElementType.*;
import java.lang.annotation.Target;
import javax.inject.Qualifier;

/**
 *
 * @author charles
 */
@Qualifier
@Retention(RUNTIME)
@Target({METHOD, FIELD, PARAMETER, TYPE})
public @interface StunServer {

}
