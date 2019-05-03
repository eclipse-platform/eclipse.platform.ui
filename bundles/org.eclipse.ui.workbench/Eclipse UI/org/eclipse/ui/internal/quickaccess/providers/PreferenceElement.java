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
import org.eclipse.ui.internal.dialogs.WorkbenchPreferenceDialog;
import org.eclipse.ui.internal.preferences.WorkbenchPreferenceExtensionNode;
import org.eclipse.ui.quickaccess.QuickAccessElement;

/**
 * @since 3.3
 *
 */
public class PreferenceElement extends QuickAccessElement {

	private static final String separator = " - "; //$NON-NLS-1$

	private IPreferenceNode preferenceNode;

	private String prefix;

	private String matchLabelCache;

	/* package */ PreferenceElement(IPreferenceNode preferenceNode, String prefix) {
		this.preferenceNode = preferenceNode;
		this.prefix = prefix;
	}

	@Override
	public void execute() {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window != null) {
			WorkbenchPreferenceDialog dialog = WorkbenchPreferenceDialog.createDialogOn(window.getShell(),
					preferenceNode.getId());
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
		if (prefix != null && prefix.length() > 0) {
			return preferenceNode.getLabelText() + separator + prefix;
		}
		return preferenceNode.getLabelText();
	}

	@Override
	public String getMatchLabel() {
		if (this.matchLabelCache == null) {
			StringBuilder builder = new StringBuilder();
			builder.append(super.getSortLabel());
			if (preferenceNode instanceof WorkbenchPreferenceExtensionNode) {
				((WorkbenchPreferenceExtensionNode) preferenceNode).getKeywordLabels().forEach(label -> {
					builder.append(separator);
					builder.append(label);
				});
			}
			this.matchLabelCache = builder.toString();
		}
		return this.matchLabelCache;
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
		final PreferenceElement other = (PreferenceElement) obj;
		return Objects.equals(preferenceNode, other.preferenceNode);
	}
}
