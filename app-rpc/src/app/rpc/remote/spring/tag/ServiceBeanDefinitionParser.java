package app.rpc.remote.spring.tag;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import app.rpc.remote.spring.RemoteServiceBean;

public class ServiceBeanDefinitionParser implements BeanDefinitionParser {

	public BeanDefinition parse(Element tag, ParserContext context) {
		RootBeanDefinition def = new RootBeanDefinition();

		// 设置Bean Class
		def.setBeanClass(RemoteServiceBean.class);

		// 注册ID属性
		String id = tag.getAttribute("id");
		BeanDefinitionHolder idHolder = new BeanDefinitionHolder(def, id);
		BeanDefinitionReaderUtils.registerBeanDefinition(idHolder,
				context.getRegistry());

		String bean = tag.getAttribute("bean");
		RuntimeBeanReference beanRef = new RuntimeBeanReference(bean);
		beanRef.setSource(context.extractSource(tag));
		def.getPropertyValues().addPropertyValue("bean", beanRef);

		String interfaces = tag.getAttribute("interfaces");
		def.getPropertyValues().addPropertyValue("interfaces", interfaces);

		String url = tag.getAttribute("url");
		def.getPropertyValues().addPropertyValue("url", url);

		if (tag.hasAttribute("handle")) {
			String handle = tag.getAttribute("handle");
			def.getPropertyValues().addPropertyValue("handle", handle);
		}
		return def;
	}
}
