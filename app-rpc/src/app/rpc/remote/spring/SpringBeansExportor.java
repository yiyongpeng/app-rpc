package app.rpc.remote.spring;

import java.util.Properties;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import app.core.Context;
import app.rpc.remote.Exportor;
import app.rpc.remote.impl.DefaultRemoteMethodCollection;
import app.rpc.remote.impl.DefaultServiceObject;
import app.rpc.utils.ConfigUtils;

public class SpringBeansExportor implements Exportor {

	private String beansLocation;
	private Context context;

	public AbstractApplicationContext getContext() {
		return beans;
	}
	
	public void destory() {
		String export = props.getProperty("export");
		if (export != null) {
			String[] beanNames = export.split(",");
			for (String beanName : beanNames) {
				beanName = beanName.trim();
				String handle = props.getProperty(beanName + ".handle", beanName);
				DefaultServiceObject so = (DefaultServiceObject) context
						.removeAttribute(DefaultRemoteMethodCollection
								.mappingInvokeHandle(handle));
				if (so != null) {
					so.destroy();
				}
			}
		}
		beans.close();
	}

	public static void export(Context app, ApplicationContext beans,
			Properties p) {
		String export = p.getProperty("export");
		if (export == null)
			return;
		String[] beanNames = export.split(",");
		for (String beanName : beanNames) {
			Object instance = beans.getBean(beanName);
			Class<?>[] interfaces = instance.getClass().getInterfaces();
			String handle = p.getProperty(beanName + ".handle", beanName);
			String interfacesArg = p.getProperty(beanName + ".interfaces", "")
					.trim();
			if (!interfacesArg.equals("")) {
				interfaces = ConfigUtils.parseInterfaces(interfacesArg);
			}
			DefaultServiceObject so = new DefaultServiceObject(handle,
					instance, interfaces);
			app.setAttribute(so.getInvokeHandle(), so);
			log.debug("export spring bean:" + beanName + "  =  " + instance);
		}
	}

	public void init(Context context) {
		this.context = context;
		this.beans = new FileSystemXmlApplicationContext(
				beansLocation);
		export(context, beans, props);
	}

	public SpringBeansExportor(String beansLocation, String exportLocation) {
		this.beansLocation = beansLocation;
		this.props = ConfigUtils.load(exportLocation, "utf-8");
	}

	private Properties props;
	private AbstractApplicationContext beans;

	private static final Logger log = Logger
			.getLogger(SpringBeansExportor.class);
}
