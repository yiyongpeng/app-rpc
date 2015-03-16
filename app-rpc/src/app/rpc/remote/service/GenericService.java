package app.rpc.remote.service;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.apache.log4j.Logger;

import app.core.AccessException;
import app.net.AppResponse;
import app.rpc.remote.ObjectRequest;
import app.rpc.remote.ObjectResponse;
import app.rpc.remote.ObjectServerHandler;
import app.rpc.remote.ObjectSession;
import app.rpc.remote.Parameter;
import app.rpc.remote.Service;
import app.rpc.remote.deploy.DeployContext;
import app.rpc.remote.impl.DefaultClientObjectConnection;

public abstract class GenericService<P extends Parameter> implements Service {
	private static final int CACHE_PARAMETER_MAX = 64;

	protected final Logger log = Logger.getLogger(this.getClass());

	private Class<? extends Parameter> pType;
	protected ObjectServerHandler handler;

	public void destory() {
		this.handler = null;
		this.pType = null;
	}

	public final void doService(ObjectRequest request, ObjectResponse response)
			throws Exception {
		P param = null;
		Thread t = null;
		ClassLoader loader = null;
		boolean changed = false;
		try {
			// 服务器端未登录用户，拒绝任何请求
			if (!(request.getSession() instanceof DefaultClientObjectConnection)
					&& !(pType.isAssignableFrom(LoginParameter.class))) {
				LoginParameter login = (LoginParameter) request.getSession()
						.getAttribute(ObjectSession.LOGIN_USER);
				if (login == null) {
					response.setStatus(AppResponse.STATUS_PERMISSION_DENIED);
					return;
				} else {
					DeployContext deploy = login.getDeployContext();
					if (deploy != null) {
						t = Thread.currentThread();
						loader = t.getContextClassLoader();
						t.setContextClassLoader(login.getClassLoader());
						changed = true;
					}
				}
			}
			param = newParameter(request);
			doService(param, request, response);
		} finally {
			if (changed) {
				t.setContextClassLoader(loader);
			}
			recycle(param);
		}
	}

	private void recycle(P param) {
		if (param != null) {
			param.destory();
			recycle.offer(param);
		}
	}

	protected abstract void doService(P param, ObjectRequest request,
			ObjectResponse response) throws IOException;

	@SuppressWarnings("unchecked")
	public void init(ObjectServerHandler handler) {
		this.handler = handler;

		// 获取参数类型P变量值
		ParameterizedType pt = (ParameterizedType) this.getClass()
				.getGenericSuperclass();
		pType = (Class<? extends Parameter>) pt.getActualTypeArguments()[0];

		// 检测是否有默认构造方法
		try {
			pType.getConstructor();
		} catch (Exception e) {
			throw new AccessException("请求参数类没有默认构造方法。", e);
		}
	}

	@SuppressWarnings("unchecked")
	private P newParameter(ObjectRequest request) throws Exception {
		P p = recycle.poll();
		if (p == null)
			p = (P) pType.newInstance();
		p.init(request);
		return p;
	}

	private final BlockingQueue<P> recycle = new ArrayBlockingQueue<P>(
			CACHE_PARAMETER_MAX);
}
