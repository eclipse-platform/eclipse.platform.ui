package org.eclipse.team.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.ui.IWorkbench;

/**
 * IConfigurationWizard defines the interface that users of the extension
 * point org.eclipse.team.ui.configurationWizards must implement.
 */
public interface IConfigurationWizard extends IWizard {
	/**
	 * Initializes this creation wizard using the passed workbench and
	 * object selection.
	 * <p>
	 * This method is called after the no argument constructor and
	 * before other methods are called.
	 * </p>
	 *
	 * @param workbench the current workbench
	 * @param project the selected project
	 */
	void init(IWorkbench workbench, IProject project);
}

