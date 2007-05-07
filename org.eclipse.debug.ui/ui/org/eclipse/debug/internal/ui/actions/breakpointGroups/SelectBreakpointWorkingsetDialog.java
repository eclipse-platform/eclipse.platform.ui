/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
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

import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.SelectionDialog;
import org.eclipse.ui.model.AdaptableList;

/**
 * Dialog to allow the selection of working sets without all of the overhead of the
 * platform UI working set dialog
 * 
 * @since 3.3.0
 */
public class SelectBreakpointWorkingsetDialog extends SelectionDialog {
	
	private static final String SETTINGS_ID = DebugUIPlugin.getUniqueIdentifier() + ".DELETE_ASSOCIATED_CONFIGS_DIALOG"; //$NON-NLS-1$
	private Object fInitialSelection = null;
	private Object[] fResult = null;
	private CheckboxTableViewer fViewer = null;
	
	/**
	 * Constructor
	 * @param parentShell the parent to open this dialog on
	 * @param selection the initial selection
	 * @param multi if the dialog should allow multi-selection or not
	 */
	protected SelectBreakpointWorkingsetDialog(Shell parentShell, Object selection) {
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		fInitialSelection = selection;
	}

	/**
	 * Returns the current listing of breakpoint <code>IWorkingSet</code>s
	 * @return an array of the current breakpoint <code>IWorkingSet</code>s
	 */
	private IWorkingSet[] getBreakpointWorkingSets() {
		IWorkingSet[] ws = PlatformUI.getWorkbench().getWorkingSetManager().getAllWorkingSets();
		ArrayList list = new ArrayList();
		for(int i = 0; i < ws.length; i++) {
			if(IInternalDebugUIConstants.ID_BREAKPOINT_WORKINGSET.equals(ws[i].getId())) {
				list.add(ws[i]);
			}
		}
		return (IWorkingSet[]) list.toArray(new IWorkingSet[list.size()]);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		initializeDialogUnits(parent);
		Composite comp = (Composite) super.createDialogArea(parent);
		SWTFactory.createLabel(comp, BreakpointGroupMessages.SelectBreakpointWorkingsetDialog_0, 2);
		Table table = new Table(comp, SWT.BORDER | SWT.CHECK | SWT.V_SCROLL | SWT.H_SCROLL | SWT.SINGLE);
		fViewer = new CheckboxTableViewer(table);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 2;
		table.setLayoutData(gd);
		fViewer.setContentProvider(new ArrayContentProvider());
		fViewer.setInput(new AdaptableList(getBreakpointWorkingSets()).getChildren());
		fViewer.setLabelProvider(DebugUITools.newDebugModelPresentation());
		if(fInitialSelection != null) {
			fViewer.setChecked(fInitialSelection, true);
		}
		fViewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				fViewer.setCheckedElements(new Object[] {event.getElement()});
				getButton(IDialogConstants.OK_ID).setEnabled(true);
			}
		});
		Dialog.applyDialogFont(comp);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(comp, IDebugHelpContextIds.SELECT_DEFAULT_WORKINGSET_DIALOG);
		return comp;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.TrayDialog#createButtonBar(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createButtonBar(Composite parent) {
		Control control = super.createButtonBar(parent);
		getButton(IDialogConstants.OK_ID).setEnabled(fViewer.getCheckedElements().length > 0);
		return control;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.dialogs.SelectionDialog#getDialogBoundsSettings()
	 */
	protected IDialogSettings getDialogBoundsSettings() {
		IDialogSettings settings = DebugUIPlugin.getDefault().getDialogSettings();
		IDialogSettings section = settings.getSection(SETTINGS_ID);
		if (section == null) {
			section = settings.addNewSection(SETTINGS_ID);
		} 
		return section;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#getInitialSize()
	 */
	protected Point getInitialSize() {
		IDialogSettings settings = getDialogBoundsSettings();
		if(settings != null) {
			try {
				int width = settings.getInt("DIALOG_WIDTH"); //$NON-NLS-1$
				int height = settings.getInt("DIALOG_HEIGHT"); //$NON-NLS-1$
				if(width > 0 & height > 0) {
					return new Point(width, height);
				}
			}
			catch (NumberFormatException nfe) {
				return new Point(350, 400);
			}
		}
		return new Point(350, 400);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.dialogs.SelectionDialog#getResult()
	 */
	public Object[] getResult() {
		return fResult;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.dialogs.ListDialog#okPressed()
	 */
	protected void okPressed() {
		fResult = fViewer.getCheckedElements();
		//set pref if selected
		super.okPressed();
	}
	
}
