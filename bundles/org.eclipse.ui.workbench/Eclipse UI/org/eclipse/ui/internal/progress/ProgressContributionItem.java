/*
 * Created on Apr 11, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.eclipse.ui.internal.progress;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.StatusLineLayoutData;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;

public class ProgressContributionItem
	extends ContributionItem
	implements IProgressMonitor {

	interface ProgressContributionListener {

		/**
		 * Refresh the listener for the TaskInfo. If info is
		 * null refresh the entire tree.
		 * @param info
		 */
		public void refresh(TaskInfo info);
	}

	private final ProgressService service;

	private AnimatedCanvas canvas;
	private ArrayList statuses = new ArrayList();
	private TaskInfoWithProgress currentStatus;
	Collection listeners = new ArrayList();

	ProgressContributionItem(ProgressService service, String id) {
		super(id);
		this.service = service;
	}

	void addListener(ProgressContributionListener listener) {
		listeners.add(listener);
	}

	void removeListener(ProgressContributionListener listener) {
		listeners.remove(listener);
	}

	private void refreshListeners(final TaskInfo info) {

		canvas.getControl().getDisplay().asyncExec(new Runnable() {
			/* (non-Javadoc)
			 * @see java.lang.Runnable#run()
			 */
			public void run() {
				Iterator iterator = listeners.iterator();
				while (iterator.hasNext()) {
					ProgressContributionListener listener =
						(ProgressContributionListener) iterator.next();
					listener.refresh(info);
				}
			}
		});
	}
	public void fill(Composite parent) {
		if (canvas == null) {
			canvas = new AnimatedCanvas("running.gif", "back.gif");
			canvas.createCanvas(parent);

			StatusLineLayoutData data = new StatusLineLayoutData();
			Rectangle bounds = canvas.getImage().getBounds();
			data.widthHint = bounds.width;
			data.heightHint = bounds.height;
			canvas.getControl().setLayoutData(data);
		}
	}

	public void setDisabledImage() {
		canvas.setAnimated(false);

	}

	public void setEnabledImage() {
		canvas.setAnimated(true);

	}

	public void dispose() {
		canvas.dispose();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IProgressMonitor#beginTask(java.lang.String, int)
	 */
	public void beginTask(String name, int totalWork) {
		currentStatus = new TaskInfoWithProgress(name, totalWork);
		statuses.add(currentStatus);
		setEnabledImage();
		refreshListeners(currentStatus);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IProgressMonitor#done()
	 */
	public void done() {
		// XXX Auto-generated method stub

		setDisabledImage();
		statuses.clear();
		refreshListeners(currentStatus);

	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IProgressMonitor#internalWorked(double)
	 */
	public void internalWorked(double work) {
		// XXX Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IProgressMonitor#isCanceled()
	 */
	public boolean isCanceled() {
		// XXX Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IProgressMonitor#setCanceled(boolean)
	 */
	public void setCanceled(boolean value) {
		// XXX Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IProgressMonitor#setTaskName(java.lang.String)
	 */
	public void setTaskName(String name) {
		// XXX Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IProgressMonitor#subTask(java.lang.String)
	 */
	public void subTask(String name) {
		TaskInfo info = currentStatus.addSubtask(name);
		refreshListeners(info);

	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IProgressMonitor#worked(int)
	 */
	public void worked(int work) {
		// XXX Auto-generated method stub
		currentStatus.addWork(work);
	}

	TaskInfoWithProgress[] getStatuses() {
		TaskInfoWithProgress[] infos =
			new TaskInfoWithProgress[statuses.size()];
		statuses.toArray(infos);
		return infos;
	}
}