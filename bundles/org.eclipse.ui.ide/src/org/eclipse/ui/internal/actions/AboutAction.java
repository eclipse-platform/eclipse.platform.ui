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
package org.eclipse.ui.internal.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.ui.AboutInfo;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.dialogs.AboutDialog;
import org.eclipse.ui.internal.ide.IDEApplication;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IHelpContextIds;

/**
 * Creates an About dialog and opens it.
 */
public class AboutAction
		extends Action 
		implements ActionFactory.IWorkbenchAction {
			
/**
 * The workbench window; or <code>null</code> if this
 * action has been <code>dispose</code>d.
 */
private IWorkbenchWindow workbenchWindow;

/**
 * Creates a new <code>AboutAction</code> with the given label
 */
public AboutAction(IWorkbenchWindow window) {
	if (window == null) {
		throw new IllegalArgumentException();
	}
	this.workbenchWindow = window;
	
	// use message with no fill-in
	AboutInfo primaryInfo = null;
	try {
		primaryInfo = IDEApplication.getPrimaryInfo();
	} catch (WorkbenchException e) {
		// do nothing
	}
	String productName = null;
	if (primaryInfo != null) {
		productName = primaryInfo.getProductName();
	}
	if (productName == null) {
		productName = ""; //$NON-NLS-1$
	}
	setText(IDEWorkbenchMessages.format("AboutAction.text", new Object[] { productName })); //$NON-NLS-1$
	setToolTipText(IDEWorkbenchMessages.format("AboutAction.toolTip", new Object[] { productName})); //$NON-NLS-1$
	setId("about"); //$NON-NLS-1$
	setActionDefinitionId("org.eclipse.ui.help.aboutAction"); //$NON-NLS-1$
	WorkbenchHelp.setHelp(this, IHelpContextIds.ABOUT_ACTION);
}

/* (non-Javadoc)
 * Method declared on IAction.
 */
public void run() {
	if (workbenchWindow == null) {
		// action has been disposed
		return;
	}
	try {
		AboutInfo primaryInfo = IDEApplication.getPrimaryInfo();
		AboutInfo[] featureInfos = IDEApplication.getFeatureInfos();
		new AboutDialog(workbenchWindow, primaryInfo, featureInfos).open();
	} catch (WorkbenchException e) {
		ErrorDialog.openError(
			workbenchWindow.getShell(), 
			IDEWorkbenchMessages.format("AboutAction.errorDialogTitle", new Object[] {getText()}),  //$NON-NLS-1$
			IDEWorkbenchMessages.getString("AboutInfo.infoReadError"),  //$NON-NLS-1$
			e.getStatus());
	}
}

/* (non-Javadoc)
 * Method declared on ActionFactory.IWorkbenchAction.
 */
public void dispose() {
	if (workbenchWindow == null) {
		// action has already been disposed
		return;
	}
	workbenchWindow = null;
}

}
