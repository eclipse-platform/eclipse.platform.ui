package org.eclipse.update.core;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.util.Stack;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.internal.boot.Policy;

/**
 * Delegating wrapper for IProgressMonitor used for 
 * installation handling.
 * 
 * @since 2.0
 */
public class InstallMonitor implements IProgressMonitor {

	private IProgressMonitor monitor;
	private Stack tasks;

	private String taskString;
	private String subTaskString;
	private boolean showDetails;
	private long totalCopyCount;

	private class MonitorState {

		private String taskString;
		private String subTaskString;
		private boolean showDetails;
		private long totalCopyCount;

		public MonitorState(
			String taskString,
			String subTaskString,
			boolean showDetails,
			long totalCopyCount) {
			this.taskString = taskString;
			this.subTaskString = subTaskString;
			this.showDetails = showDetails;
			this.totalCopyCount = totalCopyCount;
		}

		public String getTaskString() {
			return this.taskString;
		}

		public String getSubTaskString() {
			return this.subTaskString;
		}

		public boolean getShowDetails() {
			return this.showDetails;
		}

		public long getTotalCopyCount() {
			return this.totalCopyCount;
		}
	}

	public InstallMonitor(IProgressMonitor monitor) {
		this.monitor = monitor;
		this.tasks = new Stack();
		this.taskString = ""; //$NON-NLS-1$
		this.subTaskString = ""; //$NON-NLS-1$
		this.showDetails = false;
		this.totalCopyCount = 0;
	}

	/*
	 * @see IProgressMonitor#beginTask(String, int)
	 */
	public void beginTask(String name, int totalWork) {
		taskString = name;
		monitor.beginTask(name, totalWork);
	}

	/*
	 * @see IProgressMonitor#done()
	 */
	public void done() {
		monitor.done();
	}

	/*
	 * @see IProgressMonitor#internalWorked(double)
	 */
	public void internalWorked(double work) {
		monitor.internalWorked(work);
	}

	/*
	 * @see IProgressMonitor#isCanceled()
	 */
	public boolean isCanceled() {
		return monitor.isCanceled();
	}

	/*
	 * @see IProgressMonitor#setCanceled(boolean)
	 */
	public void setCanceled(boolean value) {
		monitor.setCanceled(value);
	}

	/*
	 * @see IProgressMonitor#setTaskName(String)
	 */
	public void setTaskName(String name) {
		this.taskString = name;
		this.subTaskString = ""; //$NON-NLS-1$
		this.showDetails = false;
		this.totalCopyCount = 0;
		monitor.subTask(""); //$NON-NLS-1$
		monitor.setTaskName(name);
	}

	/*
	 * @see IProgressMonitor#subTask(String)
	 */
	public void subTask(String name) {
		this.subTaskString = name;
		this.showDetails = false;
		this.totalCopyCount = 0;
		monitor.subTask(name);
	}

	/*
	 * @see IProgressMonitor#worked(int)
	 */
	public void worked(int work) {
		monitor.worked(work);
	}

	public void saveState() {
		tasks.push(
			new MonitorState(taskString, subTaskString, showDetails, totalCopyCount));
	}

	public void restoreState() {
		if (tasks.size() > 0) {
			MonitorState state = (MonitorState) tasks.pop();
			setTaskName(state.getTaskString());
			subTask(state.getSubTaskString());
			this.showDetails = state.getShowDetails();
			this.totalCopyCount = state.getTotalCopyCount();
		}
	}

	public void showCopyDetails(boolean setting) {
		this.showDetails = setting;
	}

	public void setTotalCount(long count) {
		this.totalCopyCount = count;
	}

	public void setCopyCount(long count) {
		if (showDetails && count > 0) {
			long countK = count / 1024;
			long totalK = totalCopyCount / 1024;
			String msg =
				(totalK <= 0)
					? Policy.bind("InstallMonitor.DownloadSize", Long.toString(countK))
					: Policy.bind(
						"InstallMonitor.DownloadSizeLong",
						Long.toString(countK),
						Long.toString(totalK));
			//$NON-NLS-1$ //$NON-NLS-2$
			monitor.subTask(subTaskString + msg);
		}
	}
}