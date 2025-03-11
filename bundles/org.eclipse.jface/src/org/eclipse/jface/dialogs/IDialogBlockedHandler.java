/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.dialogs;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;

/**
 * The IDialogBlockedHandler is the handler used by JFace to provide extra
 * information when a blocking has occurred. There is one static instance of this
 * class used by WizardDialog and ModalContext.
 *
 * @see org.eclipse.core.runtime.IProgressMonitor#clearBlocked()
 * @see org.eclipse.core.runtime.IProgressMonitor#setBlocked(IStatus)
 * @see WizardDialog
 * @since 3.0
 */
public interface IDialogBlockedHandler {
	/**
	 * The blockage has been cleared. Clear the
	 * extra information and resume.
	 */
	public void clearBlocked();

	/**
	 * A blockage has occurred. Show the blockage and
	 * forward any actions to blockingMonitor.
	 * <b>NOTE:</b> This will open any blocked notification immediately
	 * even if there is a modal shell open.
	 *
	 * @param parentShell The shell this is being sent from. If the parent
	 * shell is <code>null</code> the behavior will be the same as
	 * IDialogBlockedHandler#showBlocked(IProgressMonitor, IStatus, String)
	 *
	 * @param blocking The monitor to forward to. This is most
	 * important for calls to <code>cancel()</code>.
	 * @param blockingStatus The status that describes the blockage
	 * @param blockedName The name of the locked operation.
	 * @see IDialogBlockedHandler#showBlocked(IProgressMonitor, IStatus, String)
	 */
	public void showBlocked(Shell parentShell, IProgressMonitor blocking,
			IStatus blockingStatus, String blockedName);

	/**
	 * A blockage has occurred. Show the blockage when there is
	 * no longer any modal shells in the UI and forward any actions
	 * to blockingMonitor.
	 *
	 * <b>NOTE:</b> As no shell has been specified this method will
	 * not open any blocked notification until all other modal shells
	 * have been closed.
	 *
	 * @param blocking The monitor to forward to. This is most
	 * important for calls to <code>cancel()</code>.
	 * @param blockingStatus The status that describes the blockage
	 * @param blockedName The name of the locked operation.
	 */
	public void showBlocked(IProgressMonitor blocking, IStatus blockingStatus,
			String blockedName);
}
