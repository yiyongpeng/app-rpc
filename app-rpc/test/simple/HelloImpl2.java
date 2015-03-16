package simple;

import java.io.Serializable;

import app.util.POJO;

public class HelloImpl2 extends POJO implements Hello, Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6428811793131253010L;
	
	private String name;
	private String msg = "";
	private Hello parent;

	public HelloImpl2() {
	}

	public HelloImpl2(String msg) {
		super();
		this.msg = msg;
	}

	@Override
	public Hello getParent() {
		return parent;
	}

	@Override
	public void setParent(Hello hello) {
		this.parent = hello;
	}

	@Override
	public String say(char ch, boolean bool, byte bt, short sh, int in, long l,
			float fl, double doub) {
		return "[" + name + "]: " + msg + "  (" + ch + bool + bt + sh + in + l
				+ fl + doub + ")";
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public boolean isTrue() {
		return true;
	}

	public Object isTrue2() {
		return null;
	}

	public int[] getArr() {
		return new int[] { 0, 0, 1 };
	}

	@Override
	public SeriModel getModel(SeriModel model) {
		return model;
	}

}