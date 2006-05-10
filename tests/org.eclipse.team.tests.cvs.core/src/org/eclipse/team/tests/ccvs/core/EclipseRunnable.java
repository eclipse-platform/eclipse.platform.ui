package org.eclipse.team.tests.ccvs.core;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.ui.TeamOperation;

public class EclipseRunnable implements Runnable {
	private TeamOperation op;
	private Exception ex;
	private IProgressMonitor monitor;
	
	public EclipseRunnable(TeamOperation op, IProgressMonitor monitor) {
		this.monitor = monitor;
		this.op = op;
	}
	
	public void run() {
		try {
			op.run(monitor);
		} catch (InvocationTargetException e) {
			ex = e;
		} catch (InterruptedException e) {
			ex = e;
		}
	}
	
	public Exception getException(){
		return ex;
	}
}