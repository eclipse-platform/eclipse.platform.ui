package org.eclipse.ant.core;/* * (c) Copyright IBM Corp. 2000, 2001. * All Rights Reserved. */ import org.apache.tools.ant.BuildEvent;import org.apache.tools.ant.BuildListener;
public interface AntRunnerListener extends BuildListener {	public void messageLogged(String message,int priority);
}
