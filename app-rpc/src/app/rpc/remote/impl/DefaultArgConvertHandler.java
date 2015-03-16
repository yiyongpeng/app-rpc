package app.rpc.remote.impl;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import app.core.AccessException;
import app.core.Remote;
import app.rpc.remote.DriverManager;
import app.rpc.remote.InetObject;
import app.rpc.remote.InetReferenceArgument;
import app.rpc.remote.InvokeInterceptor;
import app.rpc.remote.ObjectSession;
import app.rpc.remote.RemoteMethod;
import app.rpc.remote.RemoteObject;
import app.rpc.remote.RemoteType;
import app.rpc.remote.ServiceObject;
import app.rpc.remote.service.ServiceContext;
import app.rpc.utils.ConfigUtils;

/**
 * 调用参数转换处理器
 * 
 * @author yiyongpeng
 * 
 */
public class DefaultArgConvertHandler implements InvokeInterceptor {
	private static final int CAPACITY_RECYCLE = 1024;

	public void invokeBefore(InetObject roi, RemoteMethod method, Object[] args) {
		if (args != null) {
			RemoteType[] params = method.getParameterTypes();
			for (int i = 0; i < params.length; i++) {
				args[i] = doConvert(roi, method, i, args[i]);
			}
		}
	}

	public Object invokeAfter(Object value, InetObject roi,
			RemoteMethod method, Object[] args) {
		return doConvert(roi, method, -1, value);
	}

	/**
	 * 参数转换处理
	 * 
	 * @param o
	 *            被调用对象
	 * @param m
	 *            被调用方法
	 * @param i
	 *            参数索引位置，-1表示返回参数
	 * @param v
	 *            参数值
	 * @return
	 */
	protected Object doConvert(InetObject o, RemoteMethod m, int i, Object v) {
		if (v == null)
			return null;
		if (o instanceof RemoteObject) {
			if (i == -1) {
				if (v instanceof InetProxyArgument) {
					// 远程引用参数转换成代理对象(拆信)
					InetProxyArgument ipa = (InetProxyArgument) v;
					v = ipa.getRemote(getSession(o));
					// 销毁网络引用对象
					ipa.destroy();
				}
			} else if (isRemote(m, i, v)) {
				v = newInetProxyArgument(getSession(o), o, m, i, v);
			} else if (!(v instanceof Serializable)) {
				throw new AccessException(
						"The class ("
								+ v.getClass().getCanonicalName()
								+ ") not implement interface: 'app.core.Remote' or 'java.io.Serializable'.");
			}
		} else {
			if (i != -1) {
				if (v instanceof InetProxyArgument) {
					// 远程引用参数转换成代理对象(拆信)
					InetProxyArgument ipa = (InetProxyArgument) v;
					v = ipa.getRemote(getSession(o));
				}
			} else if (isRemote(m, i, v)) {
				v = newInetProxyArgument(getSession(o), o, m, i, v);
			} else if (!(v instanceof Serializable)) {
				throw new AccessException(
						new StringBuilder("The class (")
								.append(v.getClass().getCanonicalName())
								.append(") not implement interface: 'app.core.Remote' or 'java.io.Serializable'.")
								.toString());
			}
		}
		// else if (Proxy.isProxyClass(v.getClass())) {
		// InvocationHandler ih = Proxy.getInvocationHandler(v);
		// if (ih != null && ih instanceof RemoteObject) {
		// // 代理对象参数转换成远程引用(寄信)
		// v = new InetProxyArgument(getSession(o), o, m, i, v);
		// } else if (v instanceof Remote) {
		// // 其他代理类参数实例必须有Remote标记接口
		// v = new InetProxyArgument(getSession(o), o, m, i, v);
		// } else {
		// throw new AccessException("The proxy-object(" + i
		// + ") have no realize interface 'java.rmi.Remote'.");
		// }
		// }else
		// if ((/* 服务器输出参数 */(!isClient && i == -1) || /* 客户端输出参数 */(isClient &&
		// i != -1))
		// && (v instanceof Remote || (DriverManager.AUTO_REMOTE && isInterface(
		// m, i)))) {
		// // 远程对象转换成远程引用(寄信)
		// v = new InetProxyArgument(getSession(o), o, m, i, v);
		// } else if (((isClient && i != -1) || (!isClient && i == -1))
		// && !(v instanceof Serializable)) {
		// throw new AccessException(
		// "The object("
		// + i
		// +
		// ") have no realize interface 'java.rmi.Remote' or 'java.io.Serializable'.");
		// }
		return v;
	}

	protected Object newInetProxyArgument(ObjectSession session, InetObject o,
			RemoteMethod m, int i, Object v) {
		InetProxyArgument ipa = recycle.poll();
		if (ipa == null) {
			ipa = new InetProxyArgument();
		}
		ipa.init(session, o, m, i, v);
		return ipa;
	}

	private boolean isRemote(RemoteMethod m, int i, Object v) {
		return (v instanceof Remote)
				|| (DriverManager.AUTO_REMOTE && isInterface(m, i)&&!isValue(m, i));
	}

	private boolean isValue(RemoteMethod m, int i) {
		return (i==-1?m.getReturnType():m.getParameterTypes()[i]).isValue();
	}

	private boolean isInterface(RemoteMethod m, int i) {
		return (i != -1 ? m.getParameterTypes()[i] : m.getReturnType())
				.isInterface();
	}

	private ObjectSession getSession(InetObject o) {
		return (o instanceof RemoteObject) ? ((RemoteObject) o).getSession()
				: o instanceof ServiceObject ? ServiceContext.getSession()
						: null;
	}

	public static class InetProxyArgument implements InetReferenceArgument {
		private static final long serialVersionUID = 7743848250604340060L;

		private StringBuffer sb = new StringBuffer();

		private String handle;
		private String interfaces;
		private boolean local;

		// /**
		// * 将远程代理对象转换成Session级代理服务对象
		// *
		// * @param ro
		// * @param session
		// */
		// public InetProxyArgument(InetObject roi, RemoteMethod method, int i,
		// RemoteObject ro, ObjectSession session) {
		// handle = ro.getClass().getCanonicalName() + "@" + ro.hashCode();
		// interfaces = ro.getInterfaces();
		//
		// // 将外部远程对象（代理对象）注册到本地，给对方远程引用
		// if (!session.contains(handle))
		// session.setAttribute(handle, new DefaultServiceObject(handle,
		// ro.getProxy(), interfaces));
		// }

		/**
		 * 不可序列化的参数对象转换为Session级代理服务对象
		 * 
		 * @param session
		 * @param io
		 * @param m
		 * @param i
		 * @param v
		 * @throws ClassNotFoundException
		 */
		public void init(ObjectSession session, InetObject io, RemoteMethod m,
				int i, Object v) {
			RemoteObject ro = null;
			Class<?> clazz = v.getClass();
			boolean isProxy = Proxy.isProxyClass(clazz);
			if (isProxy) {
				InvocationHandler ih = Proxy.getInvocationHandler(v);
				if (ih instanceof RemoteObject) {
					local = (ro = (RemoteObject) ih).getSession() == session;
				}
			}

			// 告诉对方是本地引用
			if (local) {
				handle = ro.getHandle();
				interfaces = null;
				return;
			}

			// =========注册远程引用到当前Session，提供给对方远程引用===========

			RemoteType t = i == -1 ? m.getReturnType()
					: m.getParameterTypes()[i];
			Class<?>[] ic = t.isInterface() == false ? clazz.getInterfaces()
					: new Class[] { i == -1 ? m.getMethod().getReturnType()
							: m.getMethod().getParameterTypes()[i] };
			sb.setLength(0);
			handle = sb.append(io.getHandle()).append("!")
					.append(m.getString()).append(i).append(":")
					.append(clazz.getName()).append("@")
					.append(v.hashCode()).toString();
			interfaces = ConfigUtils.toInterfacesString(ic, sb);
			Integer ih = DefaultRemoteMethodCollection
					.mappingInvokeHandle(handle);
			Object value = session.getAttribute(ih);
			if (value != null) {
				if (!(value instanceof ServiceObject)) {
					throw new IllegalArgumentException(
							"session attribute is exists:  "
									+ session.getSessionId() + "  handle: "
									+ handle + " invoke-handle: " + ih);
				}
				ServiceObject so = (ServiceObject) value;
				if (so.getInstance() == v) {
					return;
				}
			}
			ServiceObject so = newDefaultServiceObject(session, handle, v, ic);
			session.setAttribute(so.getInvokeHandle(), so);
		}

		public Object getRemote(ObjectSession session) {
			if (local) {
				int ih = DefaultRemoteMethodCollection
						.mappingInvokeHandle(handle);
				ServiceObject so = (ServiceObject) session
						.getCoverAttributeOfApp(ih, null);
				return so.getInstance();
			} else {
				return session.getRemote(handle,
						ConfigUtils.parseInterfaces(interfaces));
			}
		}

		public void destroy() {
			handle = null;
			interfaces = null;
			local = false;

			recycle.offer(this);
		}
	}

	public DefaultArgConvertHandler() {
	}

	public static ServiceObject newDefaultServiceObject(ObjectSession session,
			String handle, Object v, Class<?>[] interfaces) {
		return new DefaultServiceObject(handle, v, interfaces);
	}

	protected static BlockingQueue<InetProxyArgument> recycle = new ArrayBlockingQueue<InetProxyArgument>(
			CAPACITY_RECYCLE);
}
