package app.rpc.remote.impl;

import java.util.ArrayList;
import java.util.List;

import app.rpc.remote.InetObject;
import app.rpc.remote.InvokeAfterHandler;
import app.rpc.remote.InvokeBeforeHandler;
import app.rpc.remote.InvokeInterceptor;
import app.rpc.remote.RemoteMethod;

/**
 * 方法调用拦截器
 * 
 * @author yiyongpeng
 * 
 */
public class MethodInvokeInterceptor implements InvokeInterceptor {

	/**
	 * 调用前处理
	 * 
	 * @param roi
	 * @param method
	 * @param args
	 */

	public void invokeBefore(InetObject roi, RemoteMethod method, Object[] args) {
		if (invokeBeforeHandles != null)
			for (int i = 0, size = invokeBeforeHandles.size(); i < size; i++)
				invokeBeforeHandles.get(i).invokeBefore(roi, method, args);
	}

	/**
	 * 调用后处理
	 * 
	 * @param ro
	 * @param method
	 * @param args
	 * @param value
	 */

	public Object invokeAfter(Object value, InetObject ro, RemoteMethod method,
			Object[] args) {
		if (this.invokeAfterHandles != null)
			for (int i = 0, size = invokeAfterHandles.size(); i < size; i++)
				value = invokeAfterHandles.get(i).invokeAfter(value, ro,
						method, args);
		return value;
	}

	/**
	 * 移除调用前处理器
	 * 
	 * @param before
	 * @return
	 */
	public boolean removeInvokeBeforeHandle(InvokeBeforeHandler before) {
		return this.getInvokeBeforeHandles().remove(before);
	}

	/**
	 * 移除调用后处理器
	 * 
	 * @param after
	 * @return
	 */
	public boolean removeInvokeAfterHandle(InvokeAfterHandler after) {
		return this.getInvokeAfterHandles().remove(after);
	}

	/**
	 * 添加调用前处理器
	 * 
	 * @param before
	 * @return
	 */
	public boolean addInvokeBeforeHandle(InvokeBeforeHandler before) {
		return this.getInvokeBeforeHandles().add(before);
	}

	/**
	 * 添加调用后处理器
	 * 
	 * @param after
	 * @return
	 */
	public boolean addInvokeAfterHandle(InvokeAfterHandler after) {
		return this.getInvokeAfterHandles().add(after);
	}

	private List<InvokeAfterHandler> getInvokeAfterHandles() {
		if (invokeAfterHandles == null)
			invokeAfterHandles = new ArrayList<InvokeAfterHandler>();
		return invokeAfterHandles;
	}

	private List<InvokeBeforeHandler> getInvokeBeforeHandles() {
		if (invokeBeforeHandles == null)
			invokeBeforeHandles = new ArrayList<InvokeBeforeHandler>();
		return invokeBeforeHandles;
	}

	public static MethodInvokeInterceptor getInstance() {
		return instance;
	}

	private List<InvokeBeforeHandler> invokeBeforeHandles;
	private List<InvokeAfterHandler> invokeAfterHandles;

	private static MethodInvokeInterceptor instance = new MethodInvokeInterceptor();
}
