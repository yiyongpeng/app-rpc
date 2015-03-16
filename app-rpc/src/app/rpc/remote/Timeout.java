package app.rpc.remote;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * 调用超时控制注解
 * 
 * @author yiyongpeng
 * 
 */
@Inherited
@Documented
@Retention(RUNTIME)
@Target({ TYPE, METHOD })
public @interface Timeout {
	/**
	 * 指定超时时间毫秒
	 * 
	 * @return 超时毫秒，0使用系统默认值，-1永久等待
	 */
	int time() default -1;

	/**
	 * 指定超时重试次数
	 * 
	 * @return -1无限； 0不重试； 1-255重试次数；
	 */
	int retry() default 0;
}
