package org.eclipse.ui.internal.misc;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
/**
 * ShellCommand is used to execute a string command line one
 * or more times.
 */
public class ShellCommand {
	protected String[] fstrCommand;
	protected boolean fbSync = false;
	protected int fnRetCode = 0;
	protected Exception fException = null;
	protected boolean fbDone = false;
/**
 * Create a new shell command.
 *
 * @param command the shell command to execute.
 * @param bSync if <code>true</code> the command is executed in the current thread.  Otherwise,
 *			the command is executed asyncronously in a separate thread.
 */
public ShellCommand(String[] command, boolean bSync) {
	fstrCommand = command;
	fbSync = bSync;
	init();
}
/**
 * Return the current exception for the command.
 */
final public synchronized Exception getException() {
	return fException;
}
/**
 * Return the current result from the thread.
 */
final public synchronized int getRetCode() {
	return fnRetCode;
}
/**
 * Return true if the shell command is done.
 */
protected synchronized void init() {
	fbDone = false;
	fnRetCode = 0;
	fException = null;
}
/**
 * Return true if the shell command is done.
 */
final public synchronized boolean isDone() {
	return fbDone;
}
/**
 * Run the shell command.
 * <p>
 * If this method was synchronized a deadlock would occur when any of the
 * synchronized data access methods are called.  Hence, it is not synchronized, but
 * data modification is done by calling synchronized methods.
 */
protected void primRun() {
	try {
		Process p = Runtime.getRuntime().exec(fstrCommand);
		int nRetCode = p.waitFor();
		setRetCode(nRetCode);
	} catch (Exception e) {
		setException(e);
	}
	setDone(true);
}
/**
 * Run the shell command.
 */
final public void run() {
	init();
	if (fbSync) {
		primRun();
	} else {
		Runnable oRunnable = new Runnable() {
			public void run() {
				primRun();
			}
		};
		Thread oThread = new Thread(oRunnable);
		oThread.start();
	}
}
/**
 * Set the done flag.
 */
final public synchronized void setDone(boolean b) {
	fbDone = b;
}
/**
 * Set the current exception for the command.
 */
final public synchronized void setException(Exception e) {
	fException = e;
}
/**
 * Set the current result from the thread.
 */
final public synchronized void setRetCode(int n) {
	fnRetCode = n;
}
}
