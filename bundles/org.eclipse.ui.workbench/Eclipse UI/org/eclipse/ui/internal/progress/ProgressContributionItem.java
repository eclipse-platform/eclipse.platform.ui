/*
 * Created on Apr 11, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.eclipse.ui.internal.progress;

import org.eclipse.core.internal.jobs.JobManager;
import org.eclipse.core.runtime.jobs.IJobListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.StatusLineLayoutData;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;

public class ProgressContributionItem extends ContributionItem {

	private AnimatedCanvas canvas;

	public ProgressContributionItem(String id) {
		super(id);
		JobManager.getInstance().addJobListener(new IJobListener() {

			int jobCount = 0;
			/* (non-Javadoc)
			 * @see org.eclipse.core.runtime.jobs.IJobListener#aboutToRun(org.eclipse.core.runtime.jobs.Job)
			 */
			public void aboutToRun(Job job) {
				incrementJobCount();
			}
			/* (non-Javadoc)
			 * @see org.eclipse.core.runtime.jobs.IJobListener#aboutToSchedule(org.eclipse.core.runtime.jobs.Job)
			 */
			public void aboutToSchedule(Job job) {

			}
			/* (non-Javadoc)
			 * @see org.eclipse.core.runtime.jobs.IJobListener#finished(org.eclipse.core.runtime.jobs.Job, int)
			 */
			public void finished(Job job, IStatus result) {
				decrementJobCount();
			}
			/* (non-Javadoc)
			 * @see org.eclipse.core.runtime.jobs.IJobListener#paused(org.eclipse.core.runtime.jobs.Job)
			 */
			public void paused(Job job) {
				decrementJobCount();

			}		
			
			/* (non-Javadoc)
			 * @see org.eclipse.core.runtime.jobs.IJobListener#resumed(org.eclipse.core.runtime.jobs.Job)
			 */
			public void resumed(Job job) {
				// XXX Auto-generated method stub

			}

			/* (non-Javadoc)
			 * @see org.eclipse.core.runtime.jobs.IJobListener#running(org.eclipse.core.runtime.jobs.Job)
			 */
			public void running(Job job) {
				// XXX Auto-generated method stub

			}

			private void incrementJobCount() {
				if (jobCount == 0)
					setEnabledImage();
				jobCount++;
			}

			private void decrementJobCount() {
				if (jobCount == 1)
					setDisabledImage();
				jobCount--;
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

}