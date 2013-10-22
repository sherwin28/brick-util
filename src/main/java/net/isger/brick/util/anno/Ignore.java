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

    /** 默认方式 */
    public static final int DEFAULT = 0;

    /** 完整方式 */
    public static final int COMPLETE = 1;

    /** 声明方式 */
    public static final int DECLARE = 2;

    /**
     * 忽略标识
     * 
     * @return
     */
    int value() default DEFAULT;

}
