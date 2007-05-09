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
package org.eclipse.debug.internal.ui.launchConfigurations;

import java.util.Arrays;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;

/**
 * This class provides selection dialog using a checkbox table viewer. 
 * 
 * @since 3.3
 */
public abstract class AbstractDebugCheckboxSelectionDialog extends AbstractDebugSelectionDialog {

	protected CheckboxTableViewer fTableViewer;
	protected Table fTable;
	
	/**
	 * Constructor
	 * @param parentShell
	 */
	public AbstractDebugCheckboxSelectionDialog(Shell parentShell) {
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
	}
	
	/**
	 * Create and return a viewer to use in this dialog.
	 * 
	 * @param parent the composite the viewer should be created in
	 * @return the viewer to use in the dialog
	 */
	protected StructuredViewer createViewer(Composite parent){
		//by default return a checkbox table viewer
		fTable = new Table(parent, SWT.BORDER | SWT.SINGLE | SWT.CHECK);
		fTable.setLayoutData(new GridData(GridData.FILL_BOTH));
		fTableViewer = new CheckboxTableViewer(fTable);
		return fTableViewer;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.launchConfigurations.AbstractDebugSelectionDialog#addViewerListeners(org.eclipse.jface.viewers.StructuredViewer)
	 */
	protected void addViewerListeners(StructuredViewer viewer) {
		fTableViewer.addCheckStateListener(new DefaultCheckboxListener());
	}
	
	/**
	 * A checkbox state listener that ensures that exactly one element is checked
	 * and enables the OK button when this is the case.
	 *
	 */
	private class DefaultCheckboxListener implements ICheckStateListener{
		public void checkStateChanged(CheckStateChangedEvent event) {
			fTableViewer.setCheckedElements(new Object[] {event.getElement()});
			getButton(IDialogConstants.OK_ID).setEnabled(true);		
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.dialogs.SelectionDialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
	 */
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);
		getButton(IDialogConstants.OK_ID).setEnabled(false);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void okPressed() {
		Object[] elements =  fTableViewer.getCheckedElements();
		setResult(Arrays.asList(elements));
		super.okPressed();
	}
	    
}
