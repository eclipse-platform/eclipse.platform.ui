package org.eclipse.ui;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html
 
Contributors:
**********************************************************************/
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizard;

/**
 * Interface for project capability configuration wizards.
 * <p>
 * Clients should implement this interface and include the name of their class
 * in an extension contributed to the workbench's capabilities wizard extension point 
 * (named <code>"org.eclipse.ui.capabilities"</code>).
 * </p>
 *
 * @see org.eclipse.jface.wizard.IWizard
 * @since 2.0
 */
public interface ICapabilityWizard extends IWizard {
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
	void init(IWorkbench workbench, IStructuredSelection selection, IProject project);
}
