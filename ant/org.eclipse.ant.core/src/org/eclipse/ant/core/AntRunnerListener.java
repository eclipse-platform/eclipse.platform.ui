package org.eclipse.ant.core;import org.apache.tools.ant.BuildEvent;import org.apache.tools.ant.BuildListener;
public interface AntRunnerListener extends BuildListener {	public void messageLogged(String message,int priority);
}
