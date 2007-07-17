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
package org.eclipse.debug.internal.ui;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;

/**
 * This class provides selection dialog using a checkbox table viewer. 
 * 
 * @since 3.3
 */
public abstract class AbstractDebugCheckboxSelectionDialog extends AbstractDebugSelectionDialog {
	
	/**
	 * Whether to add Select All / Deselect All buttons to the custom footer controls.
	 */
	private boolean fShowSelectButtons = false;
	
	/**
	 * Constructor
	 * @param parentShell
	 */
	public AbstractDebugCheckboxSelectionDialog(Shell parentShell) {
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
	}
	
	/**
	 * Returns the viewer cast to the correct instance.  Possibly <code>null</code> if
	 * the viewer has not been created yet.
	 * @return the viewer cast to CheckboxTableViewer
	 */
	protected CheckboxTableViewer getCheckBoxTableViewer() {
		return (CheckboxTableViewer) fViewer;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.launchConfigurations.AbstractDebugSelectionDialog#initializeControls()
	 */
	protected void initializeControls() {
		List selectedElements = getInitialElementSelections();
		if (selectedElements != null && !selectedElements.isEmpty()){
			getCheckBoxTableViewer().setCheckedElements(selectedElements.toArray());
			getCheckBoxTableViewer().setSelection(StructuredSelection.EMPTY);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.launchConfigurations.AbstractDebugSelectionDialog#createViewer(org.eclipse.swt.widgets.Composite)
	 */
	protected StructuredViewer createViewer(Composite parent){
		//by default return a checkbox table viewer
		Table table = new Table(parent, SWT.BORDER | SWT.SINGLE | SWT.CHECK);
		table.setLayoutData(new GridData(GridData.FILL_BOTH));
		return new CheckboxTableViewer(table);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.launchConfigurations.AbstractDebugSelectionDialog#addViewerListeners(org.eclipse.jface.viewers.StructuredViewer)
	 */
	protected void addViewerListeners(StructuredViewer viewer) {
		getCheckBoxTableViewer().addCheckStateListener(new DefaultCheckboxListener());
	}
	
	/**
	 * A checkbox state listener that ensures that exactly one element is checked
	 * and enables the OK button when this is the case.
	 *
	 */
	private class DefaultCheckboxListener implements ICheckStateListener{
		public void checkStateChanged(CheckStateChangedEvent event) {
			refreshEnablement();
		}
	}
	
	/**
	 * Provides an opportunity to update the enablement of the OK button and other controls.
	 * By default, updates the OK button to be enabled only if at least one element in the
	 * checkbox viewer is checked.
	 * 
	 * This class calls this method when the dialog is first opened, when a checkStateChanged event occurs
	 * (if the default listener is used), and when the select all or deselect all buttons are pressed (if they
	 * are enabled).
	 */
	protected void refreshEnablement(){
		getButton(IDialogConstants.OK_ID).setEnabled(getCheckBoxTableViewer().getCheckedElements().length > 0);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.dialogs.SelectionDialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
	 */
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);
		refreshEnablement();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void okPressed() {
		Object[] elements =  getCheckBoxTableViewer().getCheckedElements();
		setResult(Arrays.asList(elements));
		super.okPressed();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.launchConfigurations.AbstractDebugSelectionDialog#addCustomFooterControls(org.eclipse.swt.widgets.Composite)
	 */
	protected void addCustomFooterControls(Composite parent) {
		if (fShowSelectButtons){
			Composite comp = SWTFactory.createComposite(parent, 2, 1, GridData.FILL_HORIZONTAL);
			GridData gd = (GridData) comp.getLayoutData();
			gd.horizontalAlignment = SWT.END;
			Button button = SWTFactory.createPushButton(comp, DebugUIMessages.AbstractDebugCheckboxSelectionDialog_0, null);
			button.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					getCheckBoxTableViewer().setAllChecked(true);
					refreshEnablement();
				}
			});
			button = SWTFactory.createPushButton(comp, DebugUIMessages.AbstractDebugCheckboxSelectionDialog_1, null);
			button.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					getCheckBoxTableViewer().setAllChecked(false);
					refreshEnablement();
				}
			});
		}
	}
	
	/**
	 * If this setting is set to true before the dialog is opened, a Select All and 
	 * a Deselect All button will be added to the custom footer controls.  The default
	 * setting is false.
	 * 
	 * @param setting whether to show the select all and deselect all buttons
	 */
	protected void setShowSelectAllButtons(boolean setting){
		fShowSelectButtons = setting;
	}
	    
}
