/*******************************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial API and implementation
 ******************************************************************************/
package org.eclipse.ant.internal.ui;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.*;

/**
 * A widget group for the types tab of the ant classpath preference page.
 */
public class TypesPage extends CustomizeAntPage {
	//button constants
	protected static final int ADD_TYPE_BUTTON= IDialogConstants.CLIENT_ID + 1;
	protected static final int EDIT_TYPE_BUTTON = IDialogConstants.CLIENT_ID + 2;
	protected static final int REMOVE_BUTTON = IDialogConstants.CLIENT_ID + 3;

public TypesPage() {
}
protected void addButtonsToButtonGroup(Composite parent) {
	createButton(parent, "preferences.customize.addTypeButtonTitle", ADD_TYPE_BUTTON);
	createButton(parent, "preferences.customize.editTypeButtonTitle", EDIT_TYPE_BUTTON);
	createSeparator(parent);
	createButton(parent, "preferences.customize.removeButtonTitle", REMOVE_BUTTON);
}
protected void buttonPressed(int buttonID) {
	super.buttonPressed(buttonID);
}
public TabItem createTabItem(TabFolder folder) {
	TabItem item = new TabItem(folder, SWT.NONE);
	item.setText(Policy.bind("preferences.customize.typesPageTitle"));
	final Image image = AntUIPlugin.getPlugin().getImageDescriptor(AntUIPlugin.IMG_TYPE).createImage();
	item.setImage(image);
	item.setData(this);
	item.setControl(createControl(folder));
	item.addDisposeListener(new DisposeListener() {
		public void widgetDisposed(DisposeEvent e) {
			if (image != null)
				image.dispose();
		}
	});
	return item;
}
}