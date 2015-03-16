package app.rpc.remote.spring.tag;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

public class NamespaceHandler extends NamespaceHandlerSupport {

	public void init() {
		registerBeanDefinitionParser("remote", new RemoteBeanDefinitionParser());
		registerBeanDefinitionParser("service",
				new ServiceBeanDefinitionParser());

	}

}
