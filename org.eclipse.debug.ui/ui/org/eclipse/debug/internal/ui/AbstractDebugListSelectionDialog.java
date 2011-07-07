/*******************************************************************************
 * Copyright (c) 2007, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

/**
 * This class provides a simple selection dialog displaying items in a table.
 * 
 * @since 3.3
 */
public abstract class AbstractDebugListSelectionDialog extends AbstractDebugSelectionDialog {

	protected TableViewer fListViewer;
	
	/**
	 * Constructor
	 * @param parentShell the parent shell
	 */
	public AbstractDebugListSelectionDialog(Shell parentShell) {
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.AbstractDebugSelectionDialog#createViewer(org.eclipse.swt.widgets.Composite)
	 */
	protected StructuredViewer createViewer(Composite parent){
		//by default return a table viewer
		fListViewer = new TableViewer(parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = 150;
		fListViewer.getTable().setLayoutData(gd);
		return fListViewer;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.launchConfigurations.AbstractDebugSelectionDialog#addViewerListeners(org.eclipse.jface.viewers.StructuredViewer)
	 */
	protected void addViewerListeners(StructuredViewer viewer) {
		viewer.addSelectionChangedListener(new ISelectionChangedListener(){
			public void selectionChanged(SelectionChangedEvent event) {
				getButton(IDialogConstants.OK_ID).setEnabled(isValid());
			}
		});
		viewer.addDoubleClickListener(new IDoubleClickListener(){
			public void doubleClick(DoubleClickEvent event) {
				if (isValid()){
					okPressed();
				}
			}
		});
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.AbstractDebugSelectionDialog#isValid()
	 */
	protected boolean isValid() {
		if(fListViewer != null) {
			ISelection sel = fListViewer.getSelection();
			if(sel instanceof IStructuredSelection) {
				return ((IStructuredSelection)sel).size() == 1;
			}
		}
		return false;
	}
	
    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    protected void okPressed() {
		ISelection selection = fViewer.getSelection();
		if (selection instanceof IStructuredSelection) {
			setResult(((IStructuredSelection) selection).toList());
		}
		super.okPressed();
    }
	
}
