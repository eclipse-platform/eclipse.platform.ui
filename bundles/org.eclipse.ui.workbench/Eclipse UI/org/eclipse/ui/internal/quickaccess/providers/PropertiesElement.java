/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.quickaccess.providers;

import java.util.Objects;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.dialogs.PropertyDialog;
import org.eclipse.ui.quickaccess.QuickAccessElement;

/**
 * @since 3.3
 *
 */
public class PropertiesElement extends QuickAccessElement {

	private Object selectedElement;
	private IPreferenceNode preferenceNode;

	/* package */ PropertiesElement(Object selectedElement, IPreferenceNode preferenceNode) {
		this.selectedElement = selectedElement;
		this.preferenceNode = preferenceNode;
	}

	@Override
	public void execute() {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window != null) {
			PropertyDialog dialog = PropertyDialog.createDialogOn(window.getShell(), preferenceNode.getId(),
					selectedElement);
			dialog.open();
		}
	}

	@Override
	public String getId() {
		return preferenceNode.getId();
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		Image image = preferenceNode.getLabelImage();
		if (image != null) {
			ImageDescriptor descriptor = ImageDescriptor.createFromImage(image);
			return descriptor;
		}
		return null;
	}

	@Override
	public String getLabel() {
		return preferenceNode.getLabelText();
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(preferenceNode);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final PropertiesElement other = (PropertiesElement) obj;
		return Objects.equals(preferenceNode, other.preferenceNode);
	}
}
