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
				incrementJobCount(job);
			}

			/* (non-Javadoc)
			 * @see org.eclipse.core.runtime.jobs.IJobListener#finished(org.eclipse.core.runtime.jobs.Job, int)
			 */
			public void finished(Job job, IStatus result) {
				decrementJobCount(job);
			}
			/* (non-Javadoc)
			 * @see org.eclipse.core.runtime.jobs.IJobListener#paused(org.eclipse.core.runtime.jobs.Job)
			 */
			public void paused(Job job) {
				decrementJobCount(job);

			}

			private void incrementJobCount(Job job) {
				//Don't count the animate job itself
				if (job instanceof AnimateJob)
					return;

				if (jobCount == 0)
					setEnabledImage();
				jobCount++;
			}

			private void decrementJobCount(Job job) {
				//Don't count the animate job itself
				if (job instanceof AnimateJob)
					return;
				if (jobCount == 1)
					setDisabledImage();
				jobCount--;
			}

			/* (non-Javadoc)
			 * @see org.eclipse.core.runtime.jobs.IJobListener#awake(org.eclipse.core.runtime.jobs.Job)
			 */
			public void awake(Job job) {
				// XXX Auto-generated method stub

			}
			/* (non-Javadoc)
			 * @see org.eclipse.core.runtime.jobs.IJobListener#running(org.eclipse.core.runtime.jobs.Job)
			 */
			public void running(Job job) {
				// XXX Auto-generated method stub

			}
			/* (non-Javadoc)
			 * @see org.eclipse.core.runtime.jobs.IJobListener#scheduled(org.eclipse.core.runtime.jobs.Job)
			 */
			public void scheduled(Job job) {
				// XXX Auto-generated method stub

			}

			/* (non-Javadoc)
			 * @see org.eclipse.core.runtime.jobs.IJobListener#sleeping(org.eclipse.core.runtime.jobs.Job)
			 */
			public void sleeping(Job job) {
				// XXX Auto-generated method stub

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