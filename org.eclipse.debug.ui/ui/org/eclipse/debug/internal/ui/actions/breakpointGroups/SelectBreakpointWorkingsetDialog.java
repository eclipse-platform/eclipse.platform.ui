/*******************************************************************************
 * Copyright (c) 2006, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.actions.breakpointGroups;

import java.util.ArrayList;

import org.eclipse.debug.internal.ui.AbstractDebugCheckboxSelectionDialog;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.PlatformUI;

/**
 * Dialog to allow the selection of working sets without all of the overhead of the
 * platform UI working set dialog
 * 
 * @since 3.3
 */
public class SelectBreakpointWorkingsetDialog extends AbstractDebugCheckboxSelectionDialog {
	
	private static final String SETTINGS_ID = DebugUIPlugin.getUniqueIdentifier() + ".DELETE_ASSOCIATED_CONFIGS_DIALOG"; //$NON-NLS-1$
	private IWorkingSet[] fWorkingSetList = null;
	
	/**
	 * Constructor
	 * @param parentShell the parent to open this dialog on
	 */
	protected SelectBreakpointWorkingsetDialog(Shell parentShell) {
		super(parentShell);
		fWorkingSetList = getBreakpointWorkingSets();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.launchConfigurations.AbstractDebugCheckboxSelectionDialog#addViewerListeners(org.eclipse.jface.viewers.StructuredViewer)
	 */
	protected void addViewerListeners(StructuredViewer viewer) {
		CheckboxTableViewer checkViewer = getCheckBoxTableViewer();
		if (checkViewer != null){
			checkViewer.addCheckStateListener(new ICheckStateListener(){
				public void checkStateChanged(CheckStateChangedEvent event) {
					getCheckBoxTableViewer().setCheckedElements(new Object[] {event.getElement()});
					getButton(IDialogConstants.OK_ID).setEnabled(true);
				}
			});
		}
	}
	
	/**
	 * Returns the current listing of breakpoint <code>IWorkingSet</code>s
	 * @return an array of the current breakpoint <code>IWorkingSet</code>s
	 */
	private IWorkingSet[] getBreakpointWorkingSets() {
		IWorkingSet[] ws = PlatformUI.getWorkbench().getWorkingSetManager().getAllWorkingSets();
		ArrayList list = new ArrayList();
		for(int i = 0; i < ws.length; i++) {
			if(IDebugUIConstants.BREAKPOINT_WORKINGSET_ID.equals(ws[i].getId())) {
				list.add(ws[i]);
			}
		}
		return (IWorkingSet[]) list.toArray(new IWorkingSet[list.size()]);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.launchConfigurations.AbstractDebugSelectionDialog#getViewerInput()
	 */
	protected Object getViewerInput() {
		return fWorkingSetList;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.launchConfigurations.AbstractDebugSelectionDialog#getDialogSettingsId()
	 */
	protected String getDialogSettingsId() {
		return SETTINGS_ID;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.launchConfigurations.AbstractDebugSelectionDialog#getHelpContextId()
	 */
	protected String getHelpContextId() {
		return IDebugHelpContextIds.SELECT_DEFAULT_WORKINGSET_DIALOG;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.launchConfigurations.AbstractDebugSelectionDialog#getViewerLabel()
	 */
	protected String getViewerLabel() {
		return BreakpointGroupMessages.SelectBreakpointWorkingsetDialog_0;
	}
	
}
