/*******************************************************************************
 * Copyright (c) 2002, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.examples.cheatsheets.pattern.actions;

import java.net.URL;

import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.cheatsheets.AbstractItemExtensionElement;
import org.eclipse.ui.forms.events.*;
import org.eclipse.ui.forms.widgets.*;
import org.eclipse.ui.tests.cheatsheets.CheatSheetTestPlugin;

public class ExtensionActionI extends AbstractItemExtensionElement {

	private Image helpImage;
	private FormToolkit toolkit;
	
	/**
	 * @param attributeName
	 */
	public ExtensionActionI(String attributeName) {
		super(attributeName);
	}

	protected ImageHyperlink createButton(Composite parent, Image image, Color color, String toolTipText) {
		ImageHyperlink button = new ImageHyperlink(parent, SWT.NULL);
		toolkit.adapt(button, true, true);
		button.setImage(image);
		button.setBackground(color);
		button.setToolTipText(toolTipText);

		return button;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.cheatsheets.AbstractItemExtensionElement#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite c) {
		System.out.println("ADDING STUFF TO COMPOSITE!!!"); //$NON-NLS-1$

		toolkit = new FormToolkit(c.getDisplay());

		String imageFileName = "icons/sample.gif"; //$NON-NLS-1$
		URL imageURL = CheatSheetTestPlugin.getDefault().find(new Path(imageFileName));
		ImageDescriptor imageDescriptor = ImageDescriptor.createFromURL(imageURL);
		helpImage = imageDescriptor.createImage();

		ImageHyperlink helpButton = createButton(c, helpImage, c.getBackground(), "Button I"); //$NON-NLS-1$
		helpButton.addHyperlinkListener(new HyperlinkAdapter() {
			public void linkActivated(HyperlinkEvent e) {
				System.out.println("Button I Pressed!!!");//$NON-NLS-1$
			}
		});
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.cheatsheets.AbstractItemExtensionElement#handleAttribute(java.lang.String)
	 */
	public void handleAttribute(String attributeValue) {
		System.out.println("HANDLING ATTRIBUTE: "+attributeValue); //$NON-NLS-1$
		return;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.cheatsheets.AbstractItemExtensionElement#dispose()
	 */
	public void dispose() {
		if(helpImage != null)
			helpImage.dispose();

		if(toolkit != null)
			toolkit.dispose();
	}
}
