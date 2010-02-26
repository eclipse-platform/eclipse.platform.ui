/*******************************************************************************
 * Copyright (c) 2003, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 * Remy Chi Jian Suen (Versant Corporation) - bug 255005
 *******************************************************************************/
package org.eclipse.ui.internal.progress;
import java.lang.reflect.InvocationTargetException;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.internal.e4.compatibility.E4Util;
import org.eclipse.ui.progress.IProgressService;
import org.eclipse.ui.progress.IWorkbenchSiteProgressService;


public class WorkbenchSiteProgressService implements IWorkbenchSiteProgressService {

	private IEclipseContext context;

	public WorkbenchSiteProgressService(IEclipseContext context) {
		this.context = context;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.progress.IProgressService#busyCursorWhile(org.eclipse.
	 * jface.operation.IRunnableWithProgress)
	 */
	public void busyCursorWhile(IRunnableWithProgress runnable) throws InvocationTargetException,
			InterruptedException {
		getWorkbenchProgressService().busyCursorWhile(runnable);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.progress.IWorkbenchSiteProgressService#schedule(org.eclipse
	 * .core.runtime.jobs.Job, long, boolean)
	 */
	public void schedule(Job job, long delay, boolean useHalfBusyCursor) {
		// TODO: e4 compat schedule
		E4Util.unsupported("schedule"); //$NON-NLS-1$
		job.schedule(delay);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.progress.IWorkbenchSiteProgressService#schedule(org.eclipse
	 * .core.runtime.jobs.Job, int)
	 */
	public void schedule(Job job, long delay) {
		schedule(job, delay, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.progress.IWorkbenchSiteProgressService#schedule(org.eclipse
	 * .core.runtime.jobs.Job)
	 */
	public void schedule(Job job) {
		schedule(job, 0L, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.progress.IWorkbenchSiteProgressService#showBusyForFamily
	 * (java.lang.Object)
	 */
	public void showBusyForFamily(Object family) {
		// TODO: e4 compat showBusyForFamily
		E4Util.unsupported("showBsyForFamily"); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.internal.progress.IJobBusyListener#decrementBusy(org.eclipse
	 * .core.runtime.jobs.Job)
	 */
	public void decrementBusy(Job job) {
		// TODO: e4 compat decrementBusy
		E4Util.unsupported("decrementBusy"); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.internal.progress.IJobBusyListener#incrementBusy(org.eclipse
	 * .core.runtime.jobs.Job)
	 */
	public void incrementBusy(Job job) {
		// TODO: e4 compat incrementBusy
		E4Util.unsupported("incrementBusy"); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.progress.IWorkbenchSiteProgressService#warnOfContentChange
	 * ()
	 */
	public void warnOfContentChange() {
		// TODO: e4 compat warnOfContentChange
		E4Util.unsupported("warnOfContentChange"); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.progress.IProgressService#showInDialog(org.eclipse.swt
	 * .widgets.Shell, org.eclipse.core.runtime.jobs.Job)
	 */
	public void showInDialog(Shell shell, Job job) {
		getWorkbenchProgressService().showInDialog(shell, job);
	}

	/**
	 * Get the progress service for the workbnech,
	 * 
	 * @return IProgressService
	 */
	private IProgressService getWorkbenchProgressService() {
		return (IProgressService) context.get(IProgressService.class.getName());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.operation.IRunnableContext#run(boolean, boolean,
	 * org.eclipse.jface.operation.IRunnableWithProgress)
	 */
	public void run(boolean fork, boolean cancelable, IRunnableWithProgress runnable)
			throws InvocationTargetException, InterruptedException {
		getWorkbenchProgressService().run(fork, cancelable, runnable);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.progress.IProgressService#runInUI(org.eclipse.jface.operation
	 * .IRunnableContext, org.eclipse.jface.operation.IRunnableWithProgress,
	 * org.eclipse.core.runtime.jobs.ISchedulingRule)
	 */
	public void runInUI(IRunnableContext context, IRunnableWithProgress runnable,
			ISchedulingRule rule) throws InvocationTargetException, InterruptedException {
		getWorkbenchProgressService().runInUI(context, runnable, rule);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.progress.IProgressService#getLongOperationTime()
	 */
	public int getLongOperationTime() {
		return getWorkbenchProgressService().getLongOperationTime();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.progress.IProgressService#registerIconForFamily(org.eclipse
	 * .jface.resource.ImageDescriptor, java.lang.Object)
	 */
	public void registerIconForFamily(ImageDescriptor icon, Object family) {
		getWorkbenchProgressService().registerIconForFamily(icon, family);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.progress.IProgressService#getIconFor(org.eclipse.core.
	 * runtime.jobs.Job)
	 */
	public Image getIconFor(Job job) {
		return getWorkbenchProgressService().getIconFor(job);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.progress.IWorkbenchSiteProgressService#showBusy(boolean)
	 */
	public void incrementBusy() {
		// TODO: e4 compat incrementBusy
		E4Util.unsupported("incrementBusy"); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.progress.IWorkbenchSiteProgressService#showBusy(boolean)
	 */
	public void decrementBusy() {
		// TODO: e4 compat decrementBusy
		E4Util.unsupported("decrementBusy"); //$NON-NLS-1$
	}
}
