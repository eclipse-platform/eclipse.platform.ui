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

public class EditorReference extends WorkbenchPartReference implements IEditorReference {

	private IEditorInput input;

	EditorReference(IWorkbenchPage page, MPart part, IEditorInput input) {
		super(page, part);
		this.input = input;
	}

	public String getFactoryId() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	public IEditorPart getEditor(boolean restore) {
		return (IEditorPart) getPart(restore);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IEditorReference#isPinned()
	 */
	public boolean isPinned() {
		// TODO Auto-generated method stub
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
