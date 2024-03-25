/********************************************************************************
 * Copyright (c) 2019 Lakshminarayana Nekkanti(narayana.nekkanti@gmail.com)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 3
 *
 * Contributor
 * Lakshminarayana Nekkanti - initial API and implementation
 ********************************************************************************/
package org.eclipse.ui.internal.genericeditor;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorMatchingStrategy;

/**
 * Overrides the default generic editor icon with the content-type specific
 * icons contributed using the extension point
 * "org.eclipse.ui.genericeditor.icons".
 */
public class GenericEditorWithContentTypeIcon implements IEditorDescriptor {
	private IEditorDescriptor editorDescriptor;
	private String fileName;

	public GenericEditorWithContentTypeIcon(String fileName, IEditorDescriptor editorDescriptor) {
		Assert.isNotNull(editorDescriptor);
		this.fileName = fileName;
		this.editorDescriptor = editorDescriptor;
	}

	@Override
	public String getId() {
		return this.editorDescriptor.getId();
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		try {
			ImageDescriptor image = GenericEditorPlugin.getDefault().getContentTypeImagesRegistry()
					.getImageDescriptor(Platform.getContentTypeManager().findContentTypesFor(fileName));
			if (image != null) {
				return image;
			}
		} catch (Exception e) {
			GenericEditorPlugin.getDefault().getLog()
					.log(new Status(IStatus.ERROR, GenericEditorPlugin.BUNDLE_ID, e.getMessage(), e));
		}
		return this.editorDescriptor.getImageDescriptor();
	}

	@Override
	public String getLabel() {
		return this.editorDescriptor.getLabel();
	}

	@Override
	public boolean isInternal() {
		return this.editorDescriptor.isInternal();
	}

	@Override
	public boolean isOpenInPlace() {
		return this.editorDescriptor.isOpenInPlace();
	}

	@Override
	public boolean isOpenExternal() {
		return this.editorDescriptor.isOpenExternal();
	}

	@Override
	public IEditorMatchingStrategy getEditorMatchingStrategy() {
		return this.editorDescriptor.getEditorMatchingStrategy();
	}
}
