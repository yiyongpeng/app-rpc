package simple;

import java.io.Serializable;

@SuppressWarnings("serial")
public class SeriModel implements Serializable {

	private String name;

	private int sex;

	public SeriModel(String name, int sex) {
		super();
		this.name = name;
		this.sex = sex;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getSex() {
		return sex;
	}

	public void setSex(int sex) {
		this.sex = sex;
	}

}
