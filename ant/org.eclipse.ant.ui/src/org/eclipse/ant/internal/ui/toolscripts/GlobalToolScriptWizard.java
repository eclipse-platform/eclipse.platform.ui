package org.eclipse.ant.internal.ui.toolscripts;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.jface.wizard.Wizard;

/**
 * The global tool script wizard is run from the workbench toolbar or menus.
 * It has the following steps:
 *  - ask for the name of the script to run
 *  - optional Ant configuration if it is an ant script
 *  - refresh options for refreshing the workspace after running the script
 *  - finally run the script
 */
public class GlobalToolScriptWizard extends AbstractToolScriptWizard {
/*
 * @see IWizard#addPages()
 */
public void addPages() {
	super.addPages();
}
/*
 * @see IWizard#performFinish()
 */
public boolean performFinish() {
	return false;
}
}