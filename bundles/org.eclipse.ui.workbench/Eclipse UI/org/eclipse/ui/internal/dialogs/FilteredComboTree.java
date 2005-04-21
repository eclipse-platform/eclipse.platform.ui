/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

/**
 * The FilteredComboTree is a filtered tree that uses an 
 * editable combo rather than just a text.
 */
public class FilteredComboTree extends FilteredTree {

	private Combo filterCombo;

	/**
	 * Create a new instance of the receiver.
	 * @param parent
	 * @param treeStyle
	 */
	public FilteredComboTree(Composite parent, int treeStyle) {
		super(parent, treeStyle);
	}

	/**
	 *  Create a new instance of the receiver with a supplied filter.
	 * @param parent
	 * @param treeStyle
	 * @param filter
	 */
	public FilteredComboTree(Composite parent, int treeStyle, PatternFilter filter) {
		super(parent, treeStyle, filter);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.dialogs.FilteredTree#createFilterControl(org.eclipse.swt.widgets.Composite)
	 */
	protected void createFilterControl(Composite parent) {
		filterCombo = new Combo(parent, SWT.DROP_DOWN | SWT.BORDER);
		
		// do not allow the dialog to be closed when enter is pressed
		filterCombo.addListener(SWT.Traverse, new Listener() {
			public void handleEvent(Event e) {
				if (e.detail == SWT.TRAVERSE_RETURN)
					e.doit = false;
			}
		});
		filterCombo.addFocusListener(new FocusAdapter(){
			/* (non-Javadoc)
			 * @see org.eclipse.swt.events.FocusAdapter#focusLost(org.eclipse.swt.events.FocusEvent)
			 */
			public void focusLost(FocusEvent e) {
				String [] textValues = filterCombo.getItems();
				String newText = filterCombo.getText();
				
				//Only add new entries
				for (int i = 0; i < textValues.length; i++) {
					if(textValues[i].equals(newText))
						return;					
				}
				filterCombo.add(newText,0);
			}
		});
		filterCombo.addSelectionListener(new SelectionAdapter(){
			/* (non-Javadoc)
			 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			public void widgetSelected(SelectionEvent e) {
				textChanged();
			}
		});
		filterCombo.getAccessible().addAccessibleListener(getAccessibleListener());
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.dialogs.FilteredTree#getFilterControl()
	 */
	public Control getFilterControl() {
		return filterCombo;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.dialogs.FilteredTree#getFilterControlText()
	 */
	protected String getFilterControlText() {
		return filterCombo.getText();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.dialogs.FilteredTree#setFilterText(java.lang.String)
	 */
	protected void setFilterText(String string) {
		filterCombo.setText(string);
	}
	/**
	 * Get the combo box used by the receiver.
	 * @return Combo
	 */
	public Combo getFilterCombo() {
		return filterCombo;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.dialogs.FilteredTree#getFilterText()
	 */
	protected String getFilterText() {
		return filterCombo.getText();
	}
}
