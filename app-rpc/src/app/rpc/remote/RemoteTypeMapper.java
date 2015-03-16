package app.rpc.remote;

import gnu.trove.map.TByteObjectMap;
import gnu.trove.map.TMap;
import gnu.trove.map.hash.TByteObjectHashMap;
import gnu.trove.map.hash.THashMap;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.apache.log4j.Logger;

public final class RemoteTypeMapper {
	private static final Logger log = Logger.getLogger(RemoteTypeMapper.class);
	private static final TMap<String, RemoteType> clazz2rt = new THashMap<String, RemoteType>();
	private static final TByteObjectMap<RemoteType> type2rt = new TByteObjectHashMap<RemoteType>();

	private static RemoteTypeFactory factory = new DefaultRemoteTypeFactory();
	
	public static void setRemoteTypeFactory(RemoteTypeFactory rtf) {
		RemoteTypeMapper.factory = rtf;
	}
	public static RemoteTypeFactory getRemoteTypeFactory() {
		return factory;
	}
	
	/**
	 * 注册基本类型
	 * @param clazz
	 * @param rt
	 */
	public static void regist(Class<?> clazz, RemoteType rt) {
		// if (clazz2rt.containsKey(clazz.getCanonicalName()))
		// throw new IllegalArgumentException("已被注册的本地类型：" + clazz);
		// if (type2rt.containsKey(rt.getType()))
		// throw new IllegalArgumentException("已被注册的远程类型：" + rt.getType());
		clazz2rt.put(getClass2RtKey(clazz), rt);
		type2rt.put(rt.getType(), rt);

		log.debug("regist type,  code: " + rt.getType() + "  class: "
				+ clazz.getCanonicalName() + "  convertor-class: "
				+ rt.getClass().getCanonicalName());
	}

	
	public static RemoteType mapping(Class<?> clazz, boolean value) {
		String other = getClass2RtKey(clazz);
		RemoteType rt = clazz2rt.get(other);
		if (rt == null) {
			rt = factory.create(clazz, value);
			// 当不同类加载器加载同名类时，不能用类名缓存，只能缓存基本类型
		}
		return rt;
	}

	private static String getClass2RtKey(Class<?> clazz) {
		return clazz.getName();
	}
	
	public static RemoteType read(ObjectInput in) throws IOException {
		byte type = in.readByte();
		RemoteType rt = type2rt.get(type);
		if (rt == null){
			rt = factory.create(in);
		}
		return rt;
	}

	public static void write(RemoteType type, ObjectOutput out)
			throws IOException {
		out.writeByte(type.getType());
		type.writeExternal(out);
	}

	private RemoteTypeMapper() {
	}

}
