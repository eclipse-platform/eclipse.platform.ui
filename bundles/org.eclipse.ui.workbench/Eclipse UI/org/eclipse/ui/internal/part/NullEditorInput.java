/*******************************************************************************
 * Copyright (c) 2004, 2016 IBM Corporation and others.
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
	private String name;

	/**
	 * Creates a <code>NullEditorInput</code>.
	 */
	public NullEditorInput() {
	}

	/**
	 * Creates a <code>NullEditorInput</code> for the given editor reference.
	 *
	 * @param editorReference the editor reference
	 * @since 3.4
	 */
	public NullEditorInput(EditorReference editorReference) {
		Assert.isLegal(editorReference != null);
		this.name = editorReference.getName();
		this.editorReference = editorReference;

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
		if (name != null) {
			return name;
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
	public <T> T getAdapter(Class<T> adapter) {
		return null;
	}

}
