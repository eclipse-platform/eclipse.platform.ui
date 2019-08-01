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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.ide.IEditorAssociationOverride;

public class GenericEditorAssociationOverride implements IEditorAssociationOverride {

	private Map<String, IEditorDescriptor> descriptorMap = new HashMap<String, IEditorDescriptor>();

	@Override
	public IEditorDescriptor[] overrideEditors(IEditorInput editorInput, IContentType contentType,
			IEditorDescriptor[] editorDescriptors) {
		return overrideEditors(editorInput.getName(), contentType, editorDescriptors);
	}

	@Override
	public IEditorDescriptor[] overrideEditors(String fileName, IContentType contentType,
			IEditorDescriptor[] editorDescriptors) {
		return Arrays.stream(editorDescriptors).map(descripter -> {
			return getEditorDescriptorForFile(descripter, fileName);
		}).toArray(size -> new IEditorDescriptor[size]);
	}

	@Override
	public IEditorDescriptor overrideDefaultEditor(IEditorInput editorInput, IContentType contentType,
			IEditorDescriptor editorDescriptor) {
		return overrideDefaultEditor(editorInput.getName(), contentType, editorDescriptor);
	}

	@Override
	public IEditorDescriptor overrideDefaultEditor(String fileName, IContentType contentType,
			IEditorDescriptor editorDescriptor) {
		return getEditorDescriptorForFile(editorDescriptor, fileName);
	}

	private IEditorDescriptor getEditorDescriptorForFile(IEditorDescriptor defaultDescripter, String fileName) {
		if (defaultDescripter != null
				&& "org.eclipse.ui.genericeditor.GenericEditor".equals(defaultDescripter.getId())) { //$NON-NLS-1$
			if (!descriptorMap.containsKey(fileName)) {
				descriptorMap.put(fileName, new GenericEditorDescriptor(fileName, defaultDescripter));
			}
			return descriptorMap.get(fileName);
		}
		return defaultDescripter;
	}
}
