package app.rpc.remote.spring.tag;

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import app.rpc.remote.spring.DataSourceRemoteFactoryBean;
import app.rpc.remote.spring.SimpleRemoteFactoryBean;

public class RemoteBeanDefinitionParser implements BeanDefinitionParser {

	public BeanDefinition parse(Element tag, ParserContext context) {
		RootBeanDefinition def = new RootBeanDefinition();

		// 注册ID属性
		if (tag.hasAttribute("id")) {
			String id = tag.getAttribute("id");
			BeanDefinitionHolder idHolder = new BeanDefinitionHolder(def, id);
			BeanDefinitionReaderUtils.registerBeanDefinition(idHolder,
					context.getRegistry());
		}

		if (!tag.hasAttribute("interfaces"))
			throw new BeanCreationException("Not set interfaces attribute!");

		String interfaces = tag.getAttribute("interfaces");
		def.getPropertyValues().addPropertyValue("interfaces", interfaces);

		if (tag.hasAttribute("handle")) {
			String handle = tag.getAttribute("handle");
			def.getPropertyValues().addPropertyValue("handle", handle);
		}

		if (tag.hasAttribute("connection")) {
			// 设置Bean Class 自定义连接
			def.setBeanClass(SimpleRemoteFactoryBean.class);
			String connection = tag.getAttribute("connection");
			RuntimeBeanReference beanRef = new RuntimeBeanReference(connection);
			beanRef.setSource(context.extractSource(tag));
			def.getPropertyValues().addPropertyValue("connection", beanRef);
		} else if (tag.hasAttribute("dataSource")) {
			// 设置Bean Class 自定义数据源
			def.setBeanClass(DataSourceRemoteFactoryBean.class);
			String dataSource = tag.getAttribute("dataSource");
			RuntimeBeanReference beanRef = new RuntimeBeanReference(dataSource);
			beanRef.setSource(context.extractSource(tag));
			def.getPropertyValues().addPropertyValue("dataSource", beanRef);
		} else {
			// 设置Bean Class 自定义内部连接
			if (!tag.hasAttribute("url"))
				throw new BeanCreationException("Not set url attribute!");

			def.setBeanClass(SimpleRemoteFactoryBean.class);
			String url = tag.getAttribute("url");
			def.getPropertyValues().addPropertyValue("url", url);

			if (tag.hasAttribute("username")) {
				String username = tag.getAttribute("username");
				def.getPropertyValues().addPropertyValue("username", username);
			}
			if (tag.hasAttribute("password")) {
				String password = tag.getAttribute("password");
				def.getPropertyValues().addPropertyValue("password", password);
			}
		}
		return def;
	}

}
