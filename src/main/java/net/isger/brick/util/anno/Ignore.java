package net.isger.brick.util.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 忽略注解
 * 
 * @author issing
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.METHOD, ElementType.TYPE })
public @interface Ignore {

    public enum Mode {

        /** 排除 */
        EXCLUDE,

        /** 引入 */
        INCLUDE;

    }

    /**
     * 忽略模式
     * 
     * @return
     */
    Mode mode() default Mode.EXCLUDE;

}
