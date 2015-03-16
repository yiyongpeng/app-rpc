package app.rpc.remote.impl;

import java.util.Map;

import org.apache.log4j.Logger;

import app.core.AccessException;
import app.net.AppFilter;
import app.net.AppFilterChain;
import app.net.AppHandler;
import app.net.AppRequest;
import app.net.AppResponse;
import app.rpc.remote.ObjectServerHandler;
import app.rpc.remote.Service;
import app.rpc.remote.ServiceManager;
import app.rpc.remote.service.InvokeService;
import app.rpc.remote.service.LoginService;
import app.rpc.remote.service.RegistorService;
import app.rpc.remote.service.ValidateService;
import app.util.ThreadContext;

public class DefaultObjectServiceManager implements AppFilter, ServiceManager {

	@Override
	public void destroy() {
		for (int i = 0; i < services.length; i++) {
			if (services[i] != null) {
				services[i].destory();
				services[i] = null;
			}
		}
	}
	
	@Override
	public void init(AppHandler handler) {
		this.handler = (ObjectServerHandler) handler;

		registor(DefaultRemoteVisitor.MODE_LOGIN, new LoginService());
		registor(DefaultRemoteVisitor.MODE_REGISTOR, new RegistorService());
		registor(DefaultRemoteVisitor.MODE_VALIDATE, new ValidateService());
		registor(DefaultRemoteVisitor.MODE_INVOKATION, new InvokeService());
		registor(DefaultRemoteVisitor.MODE_GATEWAY, new InvokeService());
	}

	public void doFilter(AppRequest request, AppResponse response,
			AppFilterChain filterChain) throws Exception {
		DefaultObjectMessage msg = (DefaultObjectMessage) request;
		int mode = msg.getMode();
		Service service = services[mode];
		if (service != null)
			try {
				Map<Object, Object> context = ThreadContext.init();
				context.put(ThreadContext.SCOPE_SESSION, msg.getSession());
				context.put(ThreadContext.SCOPE_REQUEST, msg);
				context.put(ThreadContext.SCOPE_RESPONSE, msg);

				msg.init();// 初始化
				service.doService(msg, msg);

			} finally {
				ThreadContext.destory();// 销毁线程上下文
			}
		else {
			log.warn("[Miss] service : " + mode);
			filterChain.doFilter(request, response);
		}
	}

	public void destory() {
		this.handler = null;
		for (int i = 0; i < services.length; i++) {
			if (services[i] != null)
				services[i].destory();
			services[i] = null;
		}
	}

	public Service unregistor(int mode) {
		Service service = services[mode];
		if (service != null) {
			service.destory();
			services[mode] = null;
		}
		return service;
	}

	public void registor(int mode, Service service) {
		if (service == null)
			throw new NullPointerException("service");
		if (services[mode] != null) {
			throw new AccessException("mode [" + mode + "] is registed.");
		}
		service.init(handler);
		services[mode] = service;
	}

	/* 服务列表 */
	private Service[] services = new Service[SERVICE_MAX];

	private ObjectServerHandler handler;

	private static int SERVICE_MAX = 256;

	private static final Logger log = Logger
			.getLogger(DefaultObjectServiceManager.class);
}
