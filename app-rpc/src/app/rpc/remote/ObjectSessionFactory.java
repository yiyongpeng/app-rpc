package app.rpc.remote;

import app.core.Connection;
import app.core.SessionFactory;

/**
 * Session工厂
 * 
 * @author yiyongpeng
 * 
 */
public interface ObjectSessionFactory extends SessionFactory {

	ObjectSession create(Connection conn, Object sid);

}
