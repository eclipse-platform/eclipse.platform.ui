/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs;

import org.eclipse.jface.viewers.GenericListItem;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

/**
 * The PreferencesCategoryItem is the item for displaying 
 * preferences in the WorkbenchPreferencesDialog.
 */
public class PreferencesCategoryItem extends GenericListItem {

	private Composite control;
	private Label imageLabel;
	private Label textLabel;
	private ILabelProvider labelProvider;

	/**
	 * Create a new instance of the receiver for displaying
	 * wrapped element.
	 * @param wrappedElement
	 * @param provider
	 */
	public PreferencesCategoryItem(Object wrappedElement, ILabelProvider provider) {
		super(wrappedElement);
		labelProvider = provider;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.GenericListItem#dispose()
	 */
	public void dispose() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.GenericListItem#createControl(org.eclipse.swt.widgets.Composite, org.eclipse.swt.graphics.Color)
	 */
	public void createControl(Composite parent, Color color) {

		control = new Composite(parent, SWT.CENTER);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 1;
		layout.verticalSpacing = 0;
		control.setLayout(layout);
		control.setBackground(color);

		
		imageLabel = new Label(control, SWT.CENTER);
		imageLabel.setBackground(color);
		imageLabel.setImage(labelProvider.getImage(getElement()));
		imageLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		textLabel = new Label(control, SWT.CENTER);
		textLabel.setText(labelProvider.getText(getElement()));
		textLabel.setBackground(color);
		textLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

	

	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.GenericListItem#getControl()
	 */
	public Control getControl() {
		return control;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.GenericListItem#addMouseListener(org.eclipse.swt.events.MouseListener)
	 */
	public void addMouseListener(MouseListener listener) {
		imageLabel.addMouseListener(listener);
		textLabel.addMouseListener(listener);

	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.GenericListItem#clearHighlight()
	 */
	public void clearHighlight() {
		// TODO Auto-generated method stub

	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.GenericListItem#highlightForSelection()
	 */
	public void highlightForSelection() {
		// TODO Auto-generated method stub

	}

}
