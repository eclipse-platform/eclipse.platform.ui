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
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.GenericListItem;

/**
 * The PreferencesCategoryItem is the item for displaying 
 * preferences in the WorkbenchPreferencesDialog.
 */
public class PreferencesTreeItem extends GenericListItem {

	private Composite control;

	private CLabel imageLabel;

	private CLabel textLabel;
	
	private Color gradientColor;
	
	//Keep a reference for the life of the instance.
	private Image cachedImage;

	/**
	 * Create a new instance of the receiver for displaying
	 * wrapped element.
	 * @param wrappedElement
	 */
	public PreferencesTreeItem(Object wrappedElement) {
		super(wrappedElement);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.GenericListItem#dispose()
	 */
	public void dispose() {
		cachedImage.dispose();

	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.GenericListItem#createControl(org.eclipse.swt.widgets.Composite, org.eclipse.swt.graphics.Color)
	 */
	public void createControl(Composite parent, Color color) {

		IPreferenceNode node = (IPreferenceNode) getElement();

		gradientColor = color;
		
		control = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.verticalSpacing = 0;
		control.setLayout(layout);
		control.setBackground(color);

		Image image;
		ImageDescriptor desc = ((WorkbenchPreferenceNode) node).getDescriptor();
		if (desc == null)
			image = JFaceResources.getImage(Dialog.DLG_IMG_MESSAGE_INFO);
		else{
			cachedImage = desc.createImage(true);
			image = cachedImage;
		}
		
		imageLabel = new CLabel(control, SWT.CENTER);
		imageLabel.setImage(image);
		imageLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		textLabel = new CLabel(control, SWT.CENTER);
		textLabel.setText(node.getLabelText());
		textLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		setColors(false);
		

	}

	/**
	 * Set the colors of the receiver based on whether or
	 * not it is selected.
	 * @param selected whether or not the receiver is selected
	 */
	private void setColors(boolean selected) {
		
		if(selected){
			Color selection = imageLabel.getDisplay().getSystemColor(SWT.COLOR_LIST_SELECTION);
			imageLabel.setBackground(selection);
			textLabel.setBackground(selection);
			return;
		}
		
		Color[] gradientColors = new Color[] { gradientColor,
				imageLabel.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND),
				imageLabel.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND),

		};
		Color background = imageLabel.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND);
		
		imageLabel.setBackground(background);
		textLabel.setBackground(background);
		
		imageLabel.setBackground(gradientColors,new int[] {50,50});
		textLabel.setBackground(gradientColors,new int[] {50,50});
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
		setColors(false);

	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.GenericListItem#highlightForSelection()
	 */
	public void highlightForSelection() {
		setColors(true);

	}

}
