package org.eclipse.ui.internal.progress;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * The BooleanJobCompletionListener is a convenience class that
 * holds onto a result that is set to false if any status does
 * not equal OK.
 */
public class BooleanJobCompletionListener implements IJobCompletionListener {

	private boolean complete = false;
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.progress.IJobCompletionListener#finished(org.eclipse.core.runtime.IStatus)
	 */
	public void finished(IStatus status) {
		complete = Status.OK_STATUS.equals(status);

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.progress.IJobCompletionListener#aborted(org.eclipse.core.runtime.IStatus)
	 */
	public void aborted(IStatus status) {
		complete = false;

	}
	
	public boolean completed(){
		return complete;
	}

}
