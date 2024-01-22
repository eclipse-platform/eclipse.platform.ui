/*******************************************************************************
 * Copyright (c) 2003, 2018 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * IBM - Initial API and implementation
 * Remy Chi Jian Suen (Versant Corporation) - bug 255005
 * Lars Vogel <Lars.Vogel@vogella.com> - Bug 472654
 *******************************************************************************/
package org.eclipse.ui.internal.progress;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.e4.ui.internal.workbench.swt.CSSConstants;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.PartSite;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.part.WorkbenchPart;
import org.eclipse.ui.progress.IProgressService;
import org.eclipse.ui.progress.IWorkbenchSiteProgressService;
import org.eclipse.ui.progress.WorkbenchJob;

/**
 * The WorkbenchSiteProgressService is the concrete implementation of the
 * WorkbenchSiteProgressService used by the workbench components.
 */
public class WorkbenchSiteProgressService implements IWorkbenchSiteProgressService, IJobBusyListener {
	PartSite site;

	/**
	 * Map from Job to Boolean (non-null). Interpretation of the value:
	 * <ul>
	 * <li>{@link Boolean#TRUE}: The half busy cursor has been requested.</li>
	 * <li>{@link Boolean#FALSE}: The half busy cursor has not (yet) been
	 * requested.</li>
	 * </ul>
	 */
	private Map<Job, Boolean> busyJobs = new HashMap<>();

	private Object busyLock = new Object();

	IPropertyChangeListener[] changeListeners = new IPropertyChangeListener[0];

	private int waitCursorJobCount;

	private Object waitCursorLock = new Object();

	private SiteUpdateJob updateJob;

	/**
	 * Flag that keeps state from calls to {@link #incrementBusy()} and
	 * {@link #decrementBusy()}.
	 */
	private int busyCount = 0;

	/**
	 * Job to run UI updates.
	 */
	public class SiteUpdateJob extends WorkbenchJob {
		private boolean busy;

		Object lock = new Object();

		/**
		 * Set whether we are updating with the wait or busy cursor.
		 */
		void setBusy(boolean cursorState) {
			synchronized (lock) {
				busy = cursorState;
			}
		}

		private SiteUpdateJob() {
			super(ProgressMessages.WorkbenchSiteProgressService_CursorJob);
		}

		@Override
		public IStatus runInUIThread(IProgressMonitor monitor) {
			Control control = (Control) site.getModel().getWidget();
			if (control == null || control.isDisposed()) {
				return Status.CANCEL_STATUS;
			}
			synchronized (lock) {
				// Update cursors if we are doing that
				Cursor cursor = null;
				if (waitCursorJobCount != 0) {
					// at least one job which is running has requested for wait cursor
					cursor = control.getDisplay().getSystemCursor(SWT.CURSOR_APPSTARTING);
				}
				control.setCursor(cursor);
				showBusy(busy);
				IWorkbenchPart part = site.getPart();
				if (part instanceof WorkbenchPart) {
					((WorkbenchPart) part).showBusy(busy);
				}
			}
			return Status.OK_STATUS;
		}

	}

	/**
	 * Create a new instance of the receiver with a site of partSite
	 *
	 * @param partSite PartSite.
	 */
	public WorkbenchSiteProgressService(final PartSite partSite) {
		site = partSite;
		updateJob = new SiteUpdateJob();
		updateJob.setSystem(true);
	}

	/**
	 * Dispose the resources allocated by the receiver.
	 */
	public void dispose() {
		if (updateJob != null) {
			updateJob.cancel();
		}

		ProgressManager.getInstance().removeListener(this);
		showBusy(false);
	}

	@Override
	public void busyCursorWhile(IRunnableWithProgress runnable) throws InvocationTargetException, InterruptedException {
		getWorkbenchProgressService().busyCursorWhile(runnable);
	}

	@Override
	public void schedule(Job job, long delay, boolean useHalfBusyCursor) {
		job.addJobChangeListener(getJobChangeListener(useHalfBusyCursor));
		job.schedule(delay);
	}

	@Override
	public void schedule(Job job, long delay) {
		schedule(job, delay, false);
	}

	@Override
	public void schedule(Job job) {
		schedule(job, 0L, false);
	}

	@Override
	public void showBusyForFamily(Object family) {
		ProgressManager.getInstance().addListenerToFamily(family, this);
	}

	/**
	 * Get the job change listener for this site.
	 *
	 * @param useHalfBusyCursor <code>true</code> to use half busy cursor
	 * @return IJobChangeListener
	 */
	public IJobChangeListener getJobChangeListener(final boolean useHalfBusyCursor) {
		return new JobChangeAdapter() {

			@Override
			public void aboutToRun(IJobChangeEvent event) {
				incrementBusy(event.getJob(), useHalfBusyCursor);
			}

			@Override
			public void done(IJobChangeEvent event) {
				Job job = event.getJob();
				decrementBusy(job);
				job.removeJobChangeListener(this);
			}
		};
	}

	@Override
	public void decrementBusy(Job job) {
		Object halfBusyCursorState;
		synchronized (busyLock) {
			halfBusyCursorState = busyJobs.remove(job);
			if (halfBusyCursorState == null) {
				return;
			}
		}
		if (halfBusyCursorState == Boolean.TRUE) {
			synchronized (waitCursorLock) {
				waitCursorJobCount--;
			}
		}
		try {
			decrementBusy();
		} catch (Exception ex) {
			// protecting against assertion failures
			WorkbenchPlugin.log(ex);
		}
	}

	@Override
	public void incrementBusy(Job job) {
		incrementBusy(job, false);
	}

	private void incrementBusy(Job job, boolean useHalfBusyCursor) {
		Object halfBusyCursorState;
		synchronized (busyLock) {
			halfBusyCursorState = busyJobs.get(job);
			if (useHalfBusyCursor || halfBusyCursorState != Boolean.TRUE)
				busyJobs.put(job, Boolean.valueOf(useHalfBusyCursor));
		}
		if (useHalfBusyCursor && halfBusyCursorState != Boolean.TRUE) {
			// want to set busy cursor and it has not been set before
			synchronized (waitCursorLock) {
				waitCursorJobCount++;
			}
		}
		if (halfBusyCursorState != null) {
			// incrementBusy(Job, boolean) has been called before
			if (useHalfBusyCursor && halfBusyCursorState == Boolean.FALSE) {
				// need to update cursor without changing busy count:
				synchronized (busyLock) {
					updateJob.setBusy(true);
				}
				if (PlatformUI.isWorkbenchRunning()) {
					updateJob.schedule(100);
				} else {
					updateJob.cancel();
				}
			}
			return;
		}
		incrementBusy();
	}

	@Override
	public void warnOfContentChange() {
		MPart part = site.getModel();
		if (!part.getTags().contains(CSSConstants.CSS_CONTENT_CHANGE_CLASS)) {
			part.getTags().add(CSSConstants.CSS_CONTENT_CHANGE_CLASS);
		}
	}

	@Override
	public void showInDialog(Shell shell, Job job) {
		getWorkbenchProgressService().showInDialog(shell, job);
	}

	/**
	 * Get the progress service for the workbnech,
	 *
	 * @return IProgressService
	 */
	private IProgressService getWorkbenchProgressService() {
		return site.getWorkbenchWindow().getWorkbench().getProgressService();
	}

	@Override
	public void run(boolean fork, boolean cancelable, IRunnableWithProgress runnable)
			throws InvocationTargetException, InterruptedException {
		getWorkbenchProgressService().run(fork, cancelable, runnable);
	}

	@Override
	public void runInUI(IRunnableContext context, IRunnableWithProgress runnable, ISchedulingRule rule)
			throws InvocationTargetException, InterruptedException {
		getWorkbenchProgressService().runInUI(context, runnable, rule);
	}

	@Override
	public int getLongOperationTime() {
		return getWorkbenchProgressService().getLongOperationTime();
	}

	@Override
	public void registerIconForFamily(ImageDescriptor icon, Object family) {
		getWorkbenchProgressService().registerIconForFamily(icon, family);
	}

	@Override
	public Image getIconFor(Job job) {
		return getWorkbenchProgressService().getIconFor(job);
	}

	@Override
	public void incrementBusy() {
		synchronized (busyLock) {
			this.busyCount++;
			if (busyCount != 1) {
				return;
			}
			updateJob.setBusy(true);
		}
		if (PlatformUI.isWorkbenchRunning()) {
			updateJob.schedule(100);
		} else {
			updateJob.cancel();
		}
	}

	@Override
	public void decrementBusy() {
		synchronized (busyLock) {
			Assert.isTrue(busyCount > 0,
					"Ignoring unexpected call to IWorkbenchSiteProgressService.decrementBusy().  This might be due to an earlier call to this method."); //$NON-NLS-1$
			this.busyCount--;
			if (busyCount != 0) {
				return;
			}
			updateJob.setBusy(false);
		}
		if (PlatformUI.isWorkbenchRunning()) {
			updateJob.schedule(100);
		} else {
			updateJob.cancel();
		}
	}

	/**
	 * This method is made public only for the tests. Clients should not be using
	 * this method
	 *
	 * @return the updateJob that updates the site
	 */
	public SiteUpdateJob getUpdateJob() {
		return updateJob;
	}

	/**
	 * Update UI to show busy state.
	 *
	 * @param busy <code>true</code> if we are busy
	 */
	protected void showBusy(boolean busy) {
		MPart part = site.getModel();
		if (busy) {
			part.getTags().add(CSSConstants.CSS_BUSY_CLASS);
		} else {
			part.getTags().remove(CSSConstants.CSS_BUSY_CLASS);
		}
	}
}
