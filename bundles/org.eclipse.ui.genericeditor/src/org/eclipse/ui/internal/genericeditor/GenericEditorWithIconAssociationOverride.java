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

/**
 * Overrides all the default generic editor icon with the content-type specific
 * icons contributed using the extension point
 * "org.eclipse.ui.genericeditor.icons".Content provides uses the overridden
 * icons to show in Project Explorer
 */
public class GenericEditorWithIconAssociationOverride implements IEditorAssociationOverride {

	private Map<String, IEditorDescriptor> descriptorMap = new HashMap<>();

	@Override
	public IEditorDescriptor[] overrideEditors(IEditorInput editorInput, IContentType contentType,
			IEditorDescriptor[] editorDescriptors) {
		return editorInput != null ? overrideEditors(editorInput.getName(), contentType, editorDescriptors)
				: editorDescriptors;
	}

	@Override
	public IEditorDescriptor[] overrideEditors(String fileName, IContentType contentType,
			IEditorDescriptor[] editorDescriptors) {
		return Arrays.stream(editorDescriptors).map(descriptor -> getEditorDescriptorForFile(descriptor, fileName)).toArray(size -> new IEditorDescriptor[size]);
	}

	@Override
	public IEditorDescriptor overrideDefaultEditor(IEditorInput editorInput, IContentType contentType,
			IEditorDescriptor editorDescriptor) {
		return editorInput != null ? overrideDefaultEditor(editorInput.getName(), contentType, editorDescriptor)
				: editorDescriptor;
	}

	@Override
	public IEditorDescriptor overrideDefaultEditor(String fileName, IContentType contentType,
			IEditorDescriptor editorDescriptor) {
		return getEditorDescriptorForFile(editorDescriptor, fileName);
	}

	private IEditorDescriptor getEditorDescriptorForFile(IEditorDescriptor defaultDescriptor, String fileName) {
		if (defaultDescriptor != null && ExtensionBasedTextEditor.GENERIC_EDITOR_ID.equals(defaultDescriptor.getId())
				&& fileName != null && !fileName.isEmpty()) {
			if (!descriptorMap.containsKey(fileName)) {
				descriptorMap.put(fileName, new GenericEditorWithContentTypeIcon(fileName, defaultDescriptor));
			}
			return descriptorMap.get(fileName);
		}
		return defaultDescriptor;
	}
}
