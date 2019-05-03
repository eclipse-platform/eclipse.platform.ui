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
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.quickaccess.QuickAccessElement;

/**
 * @since 3.3
 *
 */
public class EditorElement extends QuickAccessElement {

	private static final String DIRTY_MARK = "*"; //$NON-NLS-1$

	private static final String separator = " - "; //$NON-NLS-1$

	private IEditorReference editorReference;

	/* package */ EditorElement(IEditorReference editorReference) {
		this.editorReference = editorReference;
	}

	@Override
	public void execute() {
		IWorkbenchPart part = editorReference.getPart(true);
		if (part != null) {
			IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			if (activePage != null) {
				activePage.activate(part);
			}
		}
	}

	@Override
	public String getId() {
		return editorReference.getId() + editorReference.getTitleToolTip();
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		Image titleImage = editorReference.getTitleImage();
		if (titleImage == null) {
			return null;
		}
		return ImageDescriptor.createFromImage(titleImage);
	}

	@Override
	public String getLabel() {
		boolean dirty = editorReference.isDirty();
		return (dirty ? DIRTY_MARK : "") + editorReference.getTitle() + separator + editorReference.getTitleToolTip(); //$NON-NLS-1$
	}

	@Override
	public String getSortLabel() {
		return editorReference.getTitle();
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(editorReference);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final EditorElement other = (EditorElement) obj;
		return Objects.equals(editorReference, other.editorReference);
	}
}
