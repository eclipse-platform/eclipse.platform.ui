/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizard;

/**
 * Interface for project capability install wizard. The wizard is
 * responsible for collecting any necessary information from the
 * user.
 * <p>
 * The install wizard must only add natures that represent the
 * capability and any capabilities it handles the UI for. The
 * install wizard must handle the situation where some or all
 * of the required capabilites are already installed.
 * </p><p>
 * The <code>IProject</code> passed in the init method will exist
 * and can be queried for the existance of other natures. The
 * install wizard must <b>not</b> close nor delete the project
 * passed in the init method. The install wizard must <b>not</b>
 * rename the project passed in the init method.
 * </p><p>
 * The install wizard can change the location of the project if
 * required, but must inform the user of this action.
 * </p><p>
 * Clients should implement this interface and include the name of their class
 * in an extension contributed to the workbench's capabilities wizard extension point 
 * (named <code>"org.eclipse.ui.capabilities"</code>).
 * </p>
 * <p>
 * <b>NOTE:</b> This is experimental API, which may be changed or removed at any point
 * in time. This API should not be called, overridden or otherwise used in production code.
 * </p>
 *
 * @see org.eclipse.jface.wizard.IWizard
 * @see org.eclipse.ui.ICapabilityUninstallWizard
 * @since 2.0
 */
public interface ICapabilityInstallWizard extends IWizard {
	
	/**
	 * Initializes this capability wizard using the passed workbench,
	 * object selection, and project.
	 * <p>
	 * This method is called after the no argument constructor and
	 * before other methods are called.
	 * </p>
	 *
	 * @param workbench the current workbench
	 * @param selection the current object selection
	 * @param project the project to configure with a capability
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection, IProject project);
}
