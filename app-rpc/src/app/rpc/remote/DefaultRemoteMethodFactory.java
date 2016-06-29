package app.rpc.remote;

import java.lang.reflect.Method;

import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.LoaderClassPath;
import javassist.NotFoundException;

import org.apache.log4j.Logger;

import app.rpc.remote.impl.DefaultRemoteMethod;
import app.util.ServerMode;
import app.util.ThreadContext;

public class DefaultRemoteMethodFactory {
	private static final Logger log = Logger.getLogger(DefaultRemoteMethodFactory.class);
	public static boolean enableClassPool = true;

	/**
	 * RemoteMethod工厂方法
	 * 
	 * @param handle
	 * @param method
	 * @return
	 * @throws Exception
	 */
	public static RemoteMethod createRemoteMethod(int handle, ClassLoader loader, Method method) throws Exception {
		Class<?> clazz = proxyRemoteMethod(loader, method, DefaultRemoteMethod.class);
		DefaultRemoteMethod instance = (DefaultRemoteMethod) clazz.newInstance();
		instance.setHandle(handle);
		instance.parseMethod(method);
		return instance;
	}

	public static synchronized Class<?> proxyRemoteMethod(ClassLoader loader, Method method, Class<?> clazz) throws NotFoundException {
		if (!enableClassPool) {
			return clazz;
		}
		// GenProxy
//		List<ClassClassPath> cplist = new ArrayList<ClassClassPath>();
		try {
			ClassPool pool = getClassPool();
			CtClass oldClass = null;
			try {
				oldClass = pool.get(clazz.getName());
			} catch (NotFoundException e) {
				ClassClassPath cp = new ClassClassPath(clazz);
				pool.insertClassPath(cp);
				oldClass = pool.get(clazz.getName());
				pool.removeClassPath(cp);
//				cplist.add(cp);
			}
			String id = (Integer.toHexString(loader.hashCode()).toUpperCase() + "c" + Integer.toHexString(clazz.hashCode()).toUpperCase() + "m" + Integer.toHexString(method.toString().hashCode()).toUpperCase()).replace("-", "_");
			String proxyClassName = clazz.getName() + "$Proxy" + id;
			CtClass newClass = null;
			try {
				clazz = loader.loadClass(proxyClassName);
				return clazz;
			} catch (ClassNotFoundException e) {
			}
			newClass = pool.makeClass(proxyClassName, oldClass);
//		cplist.add(new ClassClassPath(method.getDeclaringClass()));
//		cplist.add(new ClassClassPath(method.getReturnType()));
			// Invoke method
			StringBuilder sb = new StringBuilder("protected Object invoke(Object instance, Object[] args) throws Throwable {\n");
			sb.append("  ");
			String returnCode = getBoxTypeCode(method.getReturnType());
			if (returnCode != null)
				sb.append("return ").append(returnCode);
			sb.append("(((").append(method.getDeclaringClass().getCanonicalName()).append(")instance).").append(method.getName()).append("(");
			Class<?>[] paramTypes = method.getParameterTypes();
			for (int i = 0; i < paramTypes.length; i++) {
				if (i > 0)
					sb.append(", ");
				appendArgCode(i, paramTypes[i], sb);
//			cplist.add(new ClassClassPath(paramTypes[i]));
			}
			sb.append("));\n");
			if (returnCode == null)
				sb.append("  return null;\n");
			sb.append("}\n");

			if (ServerMode.isDebug())
				log.debug(new StringBuilder("[Proxy] ").append(proxyClassName).append("(pool-loader:").append(pool.getClass().getClassLoader()).append(",class-loader:").append(loader).append(")\n").append(sb));

//		for (ClassClassPath cp : cplist) {
//			pool.insertClassPath(cp);
//		}
			newClass.addMethod(CtMethod.make(sb.toString(), newClass));
			clazz = newClass.toClass(loader, null);
		} catch (Exception e) {
			enableClassPool = false;
			System.err.println("proxy RemoteMethod error: " + e);
		} finally {
//			for (ClassClassPath cp : cplist) {
//				pool.removeClassPath(cp);
//			}
		}
		return clazz;
	}

	static {
		ClassPool.getDefault().appendClassPath(new LoaderClassPath(DefaultRemoteMethodFactory.class.getClassLoader()));
	}

	private static ClassPool getClassPool() {
		Object appPool = ThreadContext.contains() && ThreadContext.contains("ClassPool") ? ThreadContext.getAttribute("ClassPool") : null;
		ClassPool pool = appPool != null && (appPool instanceof ClassPool) ? (ClassPool) appPool : ClassPool.getDefault();
		return pool;
	}

	private static void appendArgCode(int i, Class<?> argType, StringBuilder sb) {
		String typeName = argType.getCanonicalName();
		String beginCode = "(";
		String endCode = "";
		if (argType == byte.class) {
			typeName = "Byte";
		} else if (argType == short.class) {
			typeName = "Short";
		} else if (argType == int.class) {
			typeName = "Integer";
		} else if (argType == long.class) {
			typeName = "Long";
		} else if (argType == float.class) {
			typeName = "Float";
		} else if (argType == double.class) {
			typeName = "Double";
		} else if (argType == char.class) {
			typeName = "Character";
		} else if (argType == boolean.class) {
			typeName = "Boolean";
		} else {
			beginCode = "";
		}
		if (beginCode.equals("") == false) {
			endCode = ")." + argType.getName() + "Value()";
		}
		sb.append(beginCode).append("(").append(typeName).append(")args[").append(i).append("]").append(endCode);
	}

	private static String getBoxTypeCode(Class<?> returnType) {
		if (returnType == void.class) {
			return null;
		} else if (returnType == byte.class) {
			return "new Byte";
		} else if (returnType == short.class) {
			return "new Short";
		} else if (returnType == int.class) {
			return "new Integer";
		} else if (returnType == long.class) {
			return "new Long";
		} else if (returnType == float.class) {
			return "new Float";
		} else if (returnType == double.class) {
			return "new Double";
		} else if (returnType == char.class) {
			return "new Character";
		} else if (returnType == boolean.class) {
			return "new Boolean";
		} else {
			return "";
		}
	}
}
