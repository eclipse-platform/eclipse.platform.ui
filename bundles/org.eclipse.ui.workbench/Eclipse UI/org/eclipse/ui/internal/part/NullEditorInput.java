/*******************************************************************************
 * Copyright (c) 2004, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.part;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.internal.EditorReference;

/**
 * @since 3.1
 */
public class NullEditorInput implements IEditorInput {

	private EditorReference editorReference;

	/**
	 * Creates a <code>NullEditorInput</code>.
	 */
	public NullEditorInput() {
	}

	/**
	 * Creates a <code>NullEditorInput</code> for the
	 * given editor reference.
	 *
	 * @param editorReference the editor reference
	 * @since 3.4
	 */
	public NullEditorInput(EditorReference editorReference) {
		Assert.isLegal(editorReference != null);
		this.editorReference= editorReference;

	}

    @Override
	public boolean exists() {
        return false;
    }

    @Override
	public ImageDescriptor getImageDescriptor() {
        return ImageDescriptor.getMissingImageDescriptor();
    }

    @Override
	public String getName() {
		String result = null;
		if (editorReference != null) {
			result = editorReference.getName();
		}
		if (result != null) {
			return result;
		}
        return ""; //$NON-NLS-1$
    }

    @Override
	public IPersistableElement getPersistable() {
        return null;
    }

    @Override
	public String getToolTipText() {
		if (editorReference != null)
			return editorReference.getTitleToolTip();
        return ""; //$NON-NLS-1$
    }

    @Override
	public Object getAdapter(Class adapter) {
        return null;
    }

}
