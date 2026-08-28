/*******************************************************************************
 * Copyright (c) 2026 Vector Informatik GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.ui.internal.findandreplace.overlay;

import org.eclipse.jface.resource.ImageDescriptor;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

/**
 * Input for {@link TestTextEditor}, holding its content in memory.
 */
public class TestTextEditorInput implements IEditorInput {

	private final String content;

	public TestTextEditorInput(String content) {
		this.content = content;
	}

	public String getContent() {
		return content;
	}

	@Override
	public boolean exists() {
		return true;
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return ImageDescriptor.getMissingImageDescriptor();
	}

	@Override
	public String getName() {
		return "Test"; //$NON-NLS-1$
	}

	@Override
	public IPersistableElement getPersistable() {
		return null;
	}

	@Override
	public String getToolTipText() {
		return getName();
	}

	@Override
	public <T> T getAdapter(Class<T> adapter) {
		return null;
	}

}
