/*
 * Created on May 21, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.eclipse.ui.internal.progress;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.widgets.Display;

/**
 * @author tod
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public abstract class UIJob extends NotifyingJob {

	Display display;	

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public final IStatus run(IProgressMonitor monitor) {
		final IStatus[] result = new IStatus[1];
		final IProgressMonitor finalMonitor = monitor;
		getDisplay().asyncExec(new Runnable() {
			/* (non-Javadoc)
			 * @see java.lang.Runnable#run()
			 */
			public void run() {
				result[0] = runInUIThread(finalMonitor);

			}

		});
		return result[0];
	}

	public void setDisplay(Display runDisplay) {
		display = runDisplay;
	}

	public Display getDisplay() {
		return display;
	}

	/**
	 * Run the job in the UI Thread.
	 * @param montior
	 * @return
	 */
	public abstract IStatus runInUIThread(IProgressMonitor montior);

}
