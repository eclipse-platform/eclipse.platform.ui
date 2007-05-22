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

package org.eclipse.ui.internal.navigator.filters;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.navigator.INavigatorContentService;

/**
 * @since 3.2
 * 
 */
public class CustomizationTab extends Composite { 
 
 
	private final INavigatorContentService contentService;

	private CheckboxTableViewer tableViewer;
	private final Set checkedItems = new HashSet();

	private ICheckStateListener checkListener = new ICheckStateListener() {

		public void checkStateChanged(CheckStateChangedEvent event) {
			if(event.getChecked())
				checkedItems.add(event.getElement());
			else
				checkedItems.remove(event.getElement());
		}
		
	};
 
	protected CustomizationTab(Composite parent,
			INavigatorContentService aContentService) {
		super(parent, SWT.RESIZE);
 
		contentService = aContentService;
		setFont(getParent().getFont()); 
		setLayout(new GridLayout()); 
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true); 
		setData(data);
 
	}

	protected Table getTable() {
		return tableViewer.getTable();
	}

	protected void addSelectionChangedListener(
			ISelectionChangedListener selectionListener) {
		if (tableViewer != null) {
			tableViewer
					.addSelectionChangedListener(selectionListener);
		}
	}

	protected void createTable() {
		  
		tableViewer = CheckboxTableViewer.newCheckList(this,SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);		
		tableViewer.addCheckStateListener(checkListener);		
		
		tableViewer.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));		
		tableViewer.getControl().setFont(getFont());

	} 
	
	/* (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Widget#dispose()
	 */
	public void dispose() { 
		tableViewer.removeCheckStateListener(checkListener);
		super.dispose();
		
	}

	protected void createInstructionsLabel(String labelText) {
		 
		Label extensionsInstructionLabel = new Label(this, SWT.BOLD | SWT.WRAP);

		GridData gridData = new GridData(GridData.FILL_HORIZONTAL
				| GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL
				| GridData.VERTICAL_ALIGN_FILL); 

		extensionsInstructionLabel.setLayoutData(gridData);
		extensionsInstructionLabel.setFont(getFont());
		extensionsInstructionLabel.setText(labelText);
	}
	 

	protected final INavigatorContentService getContentService() {
		return contentService;
	}

	protected final CheckboxTableViewer getTableViewer() {
		return tableViewer;
	}

	protected Set getCheckedItems() {
		return checkedItems;
	}

}
