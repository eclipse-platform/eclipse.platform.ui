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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.GenericListItem;

/**
 * The PreferencesCategoryItem is the item for displaying 
 * preferences in the WorkbenchPreferencesDialog.
 */
public class PreferencesCategoryItem extends GenericListItem {

	private Composite control;
	private Label imageLabel;
	private Label textLabel;

	/**
	 * Create a new instance of the receiver for displaying
	 * wrapped element.
	 * @param wrappedElement
	 */
	public PreferencesCategoryItem(Object wrappedElement) {
		super(wrappedElement);
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

		IPreferenceNode node = (IPreferenceNode) getElement();

		control = new Composite(parent, SWT.CENTER);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 1;
		control.setLayout(layout);
		control.setBackground(color);

		Image image = node.getLabelImage();
		if (image == null)
			image = JFaceResources.getImage(Dialog.DLG_IMG_MESSAGE_INFO);

		imageLabel = new Label(control, SWT.CENTER);
		imageLabel.setImage(image);
		imageLabel.setBackground(color);
		imageLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		textLabel = new Label(control, SWT.CENTER);
		textLabel.setText(node.getLabelText());
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

}
