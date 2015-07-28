import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import app.rpc.remote.Value;

public class TestAnno {

	public static void main(String[] args) throws Exception {
//		FileOutputStream fout = new FileOutputStream("1.obj");
//		ObjectOutputStream out = new ObjectOutputStream(fout);
//		out.writeObject(new HelloImpl());
//		out.close();
		
		FileInputStream fin = new FileInputStream("1.obj");
		ObjectInputStream in = new ObjectInputStream(fin);
		for (int i = 0; i < 10; i++) try{
			System.err.println(in.readObject());
			break;
		}catch (ClassNotFoundException e) {
		}
	}
	
	
	public static void main2(String[] args) throws NoSuchMethodException, SecurityException {
		Method me = TestAnno.class.getMethod("get",int.class, String.class);
		Value v = me.getAnnotation(Value.class);
		for(Annotation[] dd : me.getParameterAnnotations()){
			for(Annotation d : dd){
				System.out.println(d);
			}
		}
		System.err.println(v);
	}
	
	public @Value String get(int d ,@Value String dd){
		return null;
	}
	
}
