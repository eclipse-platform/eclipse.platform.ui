/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.progress;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.progress.*;

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

	public boolean completed() {
		return complete;
	}

}
