package app.rpc.remote.deploy;
import java.net.URL;
import java.net.URLClassLoader;

public class DefaultDeployClassLoader extends URLClassLoader implements DeployClassLoader {
	private boolean rewrite;
	
	public DefaultDeployClassLoader(URL[] urls) {
		super(urls);
		
	}

	@Override
	public void init(DeployContext context) {
		String revertStr = context.getProperties().getProperty("class-loader.rewrite", "false");
		this.rewrite = Boolean.parseBoolean(revertStr);
//		System.err.println("class-loader.rewrite "+revertStr+"  "+context.getName());
	}

	@Override
	public void destroy() {
		try{
			close();
		}catch (Throwable e) {
		}
		this.rewrite = false;
	}
	
	@Override
	protected Class<?> loadClass(String name, boolean resolve)
			throws ClassNotFoundException {
		if (rewrite){
			try {
				// First, check if the class has already been loaded
				Class<?> c = findLoadedClass(name);
				if (c == null) {
					// If still not found, then invoke findClass in order
					// to find the class.
					c = findClass(name);
				}
				if (resolve) {
					resolveClass(c);
				}
				return c;
			} catch (Throwable e) {
			}
		}
		return super.loadClass(name, resolve);
	}
	
	@Override
	public URL getResource(String name) {
		if(rewrite){
	        URL url = findResource(name);
	        if (url != null) {
	        	return url;
	        }
		}
		return super.getResource(name);
    }
	
}
