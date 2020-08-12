package com.knoban.atlas.commands;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Alden Bansemer (kNoAPP)
 */
@Deprecated
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CommandInfo {

	String name();
    String[] aliases() default {};
    String description() default "";
    String usage() default "";
    String permission() default "";
    int[] length() default {-1}; // Including a -1 anywhere in the array will signal that the command takes unlimited args.
    int min() default 0; // Minimum number of args. Useful for commands that take unlimited args.
    int argMatch() default -1;
}
