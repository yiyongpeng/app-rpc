package app.rpc.remote;

import app.core.Context;

public interface Exportor {
	
	void init(Context app);

	void destory();
	
	Object getContext();
	
}
