package app.rpc.remote;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import app.core.AccessException;
import app.util.POJO;

/**
 * 远程基础类型
 * 
 * @author yiyongpeng
 * 
 */
public class RemoteType4Basic extends POJO implements RemoteType {

	@Override
	public boolean isValue() {
		return true;
	}
	
	public boolean isInterface() {
		return false;
	}

//	public Class<?> getTypeClass() {
//		if (clazz == null) {
//			switch (type) {
//			case TYPE_VOID:
//				clazz = void.class;
//				break;
//			case TYPE_BYTE:
//				clazz = byte.class;
//				break;
//			case TYPE_SHORT:
//				clazz = short.class;
//				break;
//			case TYPE_INT:
//				clazz = int.class;
//				break;
//			case TYPE_LONG:
//				clazz = long.class;
//				break;
//			case TYPE_FLOAT:
//				clazz = float.class;
//				break;
//			case TYPE_DOUBLE:
//				clazz = double.class;
//				break;
//			case TYPE_BOOLEAN:
//				clazz = boolean.class;
//				break;
//			case TYPE_CHAR:
//				clazz = char.class;
//				break;
//			default:
//				throw new AccessException("没有此类型的定义。");
//			}
//		}
//		return clazz;
//	}

	public void readExternal(ObjectInput in) throws IOException {
	}

	public void writeExternal(ObjectOutput out) throws IOException {
	}

	public RemoteType4Basic(Class<?> clazz) {
//		this.clazz = clazz;
		if (clazz == void.class) {
			type = (TYPE_VOID);
		} else if (clazz == byte.class) {
			type = (TYPE_BYTE);
		} else if (clazz == short.class) {
			type = (TYPE_SHORT);
		} else if (clazz == int.class) {
			type = (TYPE_INT);
		} else if (clazz == long.class) {
			type = (TYPE_LONG);
		} else if (clazz == float.class) {
			type = (TYPE_FLOAT);
		} else if (clazz == double.class) {
			type = (TYPE_DOUBLE);
		} else if (clazz == boolean.class) {
			type = (TYPE_BOOLEAN);
		} else if (clazz == char.class) {
			type = (TYPE_CHAR);
		} else {
			throw new IllegalArgumentException("not support: " + clazz);
		}
	}

	public void writeValue(Object value, ObjectOutput out) throws IOException {
		switch (type) {
		case TYPE_VOID:
			break;
		case TYPE_BYTE:
			out.writeByte((Byte) value);
			break;
		case TYPE_SHORT:
			out.writeShort((Short) value);
			break;
		case TYPE_INT:
			out.writeInt((Integer) value);
			break;
		case TYPE_LONG:
			out.writeLong((Long) value);
			break;
		case TYPE_FLOAT:
			out.writeFloat((Float) value);
			break;
		case TYPE_DOUBLE:
			out.writeDouble((Double) value);
			break;
		case TYPE_BOOLEAN:
			out.writeBoolean((Boolean) value);
			break;
		case TYPE_CHAR:
			out.writeChar((Character) value);
			break;
		default:
			throw new AccessException("没有此类型的定义。");
		}
	}

	/**
	 * 读返回值
	 * 
	 * @throws IOException
	 */

	public Object readValue(ObjectSession session, ObjectInput in) throws IOException {
		switch (type) {
		case TYPE_VOID:
			return null;
		case TYPE_BYTE:
			return in.readByte();
		case TYPE_SHORT:
			return in.readShort();
		case TYPE_INT:
			return in.readInt();
		case TYPE_LONG:
			return in.readLong();
		case TYPE_FLOAT:
			return in.readFloat();
		case TYPE_DOUBLE:
			return in.readDouble();
		case TYPE_BOOLEAN:
			return in.readBoolean();
		case TYPE_CHAR:
			return in.readChar();
		default:
			throw new AccessException("没有此类型的定义。");
		}
	}

	public String getName() {
		switch (type) {
		case TYPE_VOID:
			return void.class.getName();
		case TYPE_BYTE:
			return byte.class.getName();
		case TYPE_SHORT:
			return short.class.getName();
		case TYPE_INT:
			return int.class.getName();
		case TYPE_LONG:
			return long.class.getName();
		case TYPE_FLOAT:
			return float.class.getName();
		case TYPE_DOUBLE:
			return double.class.getName();
		case TYPE_BOOLEAN:
			return boolean.class.getName();
		case TYPE_CHAR:
			return char.class.getName();
		default:
			throw new AccessException("没有此类型的定义。");
		}
	}

	public byte getType() {
		return type;
	}

	private byte type;

	// --------------------------------

	public static final int TYPE_VOID = 0;
	public static final int TYPE_BYTE = 1;
	public static final int TYPE_SHORT = 2;
	public static final int TYPE_INT = 3;
	public static final int TYPE_LONG = 4;
	public static final int TYPE_FLOAT = 5;
	public static final int TYPE_DOUBLE = 6;
	public static final int TYPE_BOOLEAN = 7;
	public static final int TYPE_CHAR = 8;
	public static final int TYPE_OTHER = 9;

}