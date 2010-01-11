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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.internal.registry.EditorDescriptor;

public class CompatibilityEditor extends CompatibilityPart {

	private IEditorInput input;
	private EditorDescriptor descriptor;

	void set(IEditorInput input, EditorDescriptor descriptor) {
		this.input = input;
		this.descriptor = descriptor;

		initialized = true;
		create();
	}

	@Override
	protected IWorkbenchPart createPart() {
		try {
			return descriptor.createEditor();
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			throw new RuntimeException(e);
		}
	}

	@Override
	public void create() {
		if (initialized) {
			super.create();
		}
	}

	private boolean initialized = false;

	@Override
	protected void initialize(IWorkbenchPart part) throws PartInitException {
		((IEditorPart) part).init(new EditorSite(this.part, part, descriptor
				.getConfigurationElement()), input);
	}

	public IEditorPart getEditor() {
		return (IEditorPart) getPart();
	}

}
