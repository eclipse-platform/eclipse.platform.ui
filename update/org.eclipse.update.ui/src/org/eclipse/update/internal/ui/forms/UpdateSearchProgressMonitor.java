package org.eclipse.update.internal.ui.forms;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.*;

public class UpdateSearchProgressMonitor implements IProgressMonitor {
	private UpdateSearchContribution contribution;
	private int total;
	private int workedSoFar;
	private boolean canceled;
	private String taskName;
	private String subTaskName;
	private IStatusLineManager manager;
	
	public UpdateSearchProgressMonitor(IStatusLineManager manager) {
		this.manager = manager;
	}
	
	public IContributionItem getContribution() {
		return contribution;
	}
	
	/**
	 * @see IProgressMonitor#beginTask(String, int)
	 */
	public void beginTask(String taskName, int total) {
		this.total = total;
		this.taskName = taskName;
		subTaskName="";
		workedSoFar = 0;
		contribution = new UpdateSearchContribution("updateProgress");
		manager.add(contribution);
		manager.update(false);
		update();
		contribution.startAnimation();
	}
	
	/**
	 * @see IProgressMonitor#done()
	 */
	public void done() {
		contribution.stopAnimation();
		manager.remove(contribution.getId());
		contribution.dispose();
		manager.update(false);
		contribution=null;
	}

	/**
	 * @see IProgressMonitor#internalWorked(double)
	 */
	public void internalWorked(double arg0) {
	}


	/**
	 * @see IProgressMonitor#isCanceled()
	 */
	public boolean isCanceled() {
		return canceled;
	}


	/**
	 * @see IProgressMonitor#setCanceled(boolean)
	 */
	public void setCanceled(boolean arg0) {
		this.canceled = canceled;
	}


	/**
	 * @see IProgressMonitor#setTaskName(String)
	 */
	public void setTaskName(String taskName) {
		this.taskName = taskName;
		update();
	}


	/**
	 * @see IProgressMonitor#subTask(String)
	 */
	public void subTask(String subTaskName) {
		/*
		this.subTaskName = subTaskName;
		update();
		*/
	}


	/**
	 * @see IProgressMonitor#worked(int)
	 */
	public void worked(int amount) {
		workedSoFar += amount;
		update();
	}
	
	private void update() {
		if (contribution==null) return;
		int percent = 0;
		if (total>0 && workedSoFar>0)
			percent = (workedSoFar * 100)/total;
		//String tooltip = taskName + " ("+percent+"%) "+subTaskName;
		String tooltip = taskName + " ("+percent+"%)";
		contribution.setToolTipText(tooltip);
	}
}