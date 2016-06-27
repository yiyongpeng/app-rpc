package app.rpc.api;

import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import app.rpc.Startup;
import app.rpc.utils.InputStreamRemoteAdapter;
import app.rpc.utils.OutputStreamRemoteAdapter;
import app.rpc.utils.RemoteInputStream;
import app.rpc.utils.RemoteOutputStream;
import bsh.EvalError;
import bsh.Interpreter;

public class BshApiImpl implements BshApi {
    private Map<String, Object> vars = new HashMap<String, Object>();

    public void bsh(RemoteInputStream rin, RemoteOutputStream rout, RemoteOutputStream rerr) {
	ClassLoader cl = Thread.currentThread().getContextClassLoader();
	if (cl == null) {
	    cl = getClass().getClassLoader();
	}
	Reader in = new InputStreamReader(new InputStreamRemoteAdapter(rin));
	PrintStream out = new PrintStream(new OutputStreamRemoteAdapter(rout));
	PrintStream err = new PrintStream(new OutputStreamRemoteAdapter(rerr));
	Interpreter i = new Interpreter(in, out, err, true);
	i.setClassLoader(cl);
	i.setExitOnEOF(false);
	setVars(i);
	Thread thread = new Thread(i, "bsh-" + i.hashCode());
	thread.setDaemon(true);
	thread.start();

    }

    public String bsheval(String code) {
	Interpreter i = new Interpreter();
	i.setClassLoader(Thread.currentThread().getContextClassLoader());
	try {
	    setVars(i);
	    return String.valueOf(i.eval(code));
	} catch (EvalError e) {
	    throw new RuntimeException(e.toString());
	}
    }

    protected void setVars(Interpreter i) {
	try {
	    i.set("app", Startup.getConector().getServerHandler());
	    for (Entry<String, Object> entry : vars.entrySet()) {
		i.set(entry.getKey(), entry.getValue());
	    }
	} catch (EvalError e) {
	    e.printStackTrace();
	}
    }

    public Map<String, Object> getVars() {
	return vars;
    }

    public void setVars(Map<String, Object> vars) {
	this.vars = vars;
    }

}
