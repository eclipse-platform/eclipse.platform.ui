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

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

/**
 * @since 3.3
 * 
 */
public class EditorElement extends QuickAccessElement {

	private static final String DIRTY_MARK = "*"; //$NON-NLS-1$

	private static final String separator = " - "; //$NON-NLS-1$

	private IEditorReference editorReference;

	/* package */EditorElement(IEditorReference editorReference, EditorProvider editorProvider) {
		super(editorProvider);
		this.editorReference = editorReference;
	}

	public void execute() {
		IWorkbenchPart part = editorReference.getPart(true);
		if (part != null) {
			IWorkbenchPage activePage = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow().getActivePage();
			if (activePage != null) {
				activePage.activate(part);
			}
		}
	}

	public String getId() {
		return editorReference.getId() + editorReference.getTitleToolTip();
	}

	public ImageDescriptor getImageDescriptor() {
		return ImageDescriptor.createFromImage(editorReference.getTitleImage());
	}

	public String getLabel() {
		boolean dirty = editorReference.isDirty();
		return (dirty ? DIRTY_MARK : "") + editorReference.getTitle() + separator + editorReference.getTitleToolTip(); //$NON-NLS-1$
	}

	public String getSortLabel() {
		return editorReference.getTitle();
	}

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((editorReference == null) ? 0 : editorReference.hashCode());
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final EditorElement other = (EditorElement) obj;
		if (editorReference == null) {
			if (other.editorReference != null)
				return false;
		} else if (!editorReference.equals(other.editorReference))
			return false;
		return true;
	}
}
