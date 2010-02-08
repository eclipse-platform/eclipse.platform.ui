/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.e4.compatibility;

import org.eclipse.e4.ui.model.application.MPart;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.registry.EditorDescriptor;

public class EditorReference extends WorkbenchPartReference implements IEditorReference {

	private IEditorInput input;
	private EditorDescriptor descriptor;

	EditorReference(IWorkbenchPage page, MPart part, IEditorInput input, EditorDescriptor descriptor) {
		super(page, part);
		this.input = input;
		this.descriptor = descriptor;
	}

	public String getFactoryId() {
		// FIXME compat getFactoryId
		E4Util.unsupported("getFactoryId"); //$NON-NLS-1$
		return null;
	}

	public String getName() {
		return input.getName();
	}

	public IEditorPart getEditor(boolean restore) {
		IEditorPart part = (IEditorPart) super.getPart(restore);
		if (part == null && restore) {
			CompatibilityEditor editor = (CompatibilityEditor) getModel().getObject();
			try {
				editor.set(input, descriptor);
			} catch (PartInitException e) {
				WorkbenchPlugin.log(e);
			}
			return editor.getEditor();
		}
		return part;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IEditorReference#isPinned()
	 */
	public boolean isPinned() {
		// FIXME compat implement pinning
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IEditorReference#getEditorInput()
	 */
	public IEditorInput getEditorInput() throws PartInitException {
		return input;
	}

}
