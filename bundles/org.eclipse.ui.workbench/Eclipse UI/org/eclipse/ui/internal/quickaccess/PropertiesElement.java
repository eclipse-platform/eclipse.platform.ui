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

package org.eclipse.ui.internal.quickaccess;

import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.dialogs.PropertyDialog;

/**
 * @since 3.3
 *
 */
public class PropertiesElement extends QuickAccessElement {

	private Object selectedElement;
	private IPreferenceNode preferenceNode;
	
	/* package */PropertiesElement(Object selectedElement, IPreferenceNode preferenceNode, PropertiesProvider propertiesProvider) {
		super(propertiesProvider);
		this.selectedElement = selectedElement;
		this.preferenceNode = preferenceNode;
	}
	
	public void execute() {
		IWorkbenchWindow window = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		if (window != null) {
			PropertyDialog dialog = PropertyDialog.createDialogOn(window
					.getShell(), preferenceNode.getId(), selectedElement);
			dialog.open();
		}
	}

	public String getId() {
		return preferenceNode.getId();
	}

	public ImageDescriptor getImageDescriptor() {
		Image image = preferenceNode.getLabelImage();
		if (image != null) {
			ImageDescriptor descriptor = ImageDescriptor.createFromImage(image);
			return descriptor;
		}
		return null;
	}

	public String getLabel() {
		return preferenceNode.getLabelText();
	}
	
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((preferenceNode == null) ? 0 : preferenceNode.hashCode());
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final PropertiesElement other = (PropertiesElement) obj;
		if (preferenceNode == null) {
			if (other.preferenceNode != null)
				return false;
		} else if (!preferenceNode.equals(other.preferenceNode))
			return false;
		return true;
	}	
}
