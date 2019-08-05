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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ResourceLocator;

public class IconsRegistry {

	private static final String EXTENSION_POINT_ID = GenericEditorPlugin.BUNDLE_ID + ".icons"; //$NON-NLS-1$
	private Map<IContentType, ImageDescriptor> extensions = new LinkedHashMap<>();
	private boolean outOfSync = true;

	public IconsRegistry() {
		Platform.getExtensionRegistry().addRegistryChangeListener(event -> outOfSync = true, EXTENSION_POINT_ID);
	}

	public ImageDescriptor getImageDescriptor(IContentType[] contentTypes) {
		if (this.outOfSync) {
			sync();
		}
		return Arrays.stream(contentTypes).sorted(Collections.reverseOrder(Comparator.comparingInt(ContentTypeSpecializationComparator::depth))).map(extensions::get).filter(Objects::nonNull).findFirst().orElse(null);
	}

	private void sync() {
		Set<IContentType> toRemoveContentTypes = new HashSet<>(this.extensions.keySet());
		for (IConfigurationElement extension : Platform.getExtensionRegistry().getConfigurationElementsFor(EXTENSION_POINT_ID)) {
			try {
				final String contentTypeId = extension.getAttribute(GenericContentTypeRelatedExtension.CONTENT_TYPE_ATTRIBUTE);
				if (contentTypeId == null || contentTypeId.isEmpty()) {
					continue;
				}

				final IContentType contentType = Platform.getContentTypeManager().getContentType(contentTypeId);
				if (contentType == null) {
					continue;
				}
				toRemoveContentTypes.remove(contentType);
				if (!this.extensions.containsKey(contentType)) {
					final String icon = extension.getAttribute("icon"); //$NON-NLS-1$
					if (icon == null || icon.isEmpty()) {
						continue;
					}
					ResourceLocator.imageDescriptorFromBundle(extension.getNamespaceIdentifier(), icon).ifPresent(imageDescriptor -> this.extensions.put(contentType, imageDescriptor));
				}
			} catch (Exception ex) {
				GenericEditorPlugin.getDefault().getLog().log(new Status(IStatus.ERROR, GenericEditorPlugin.BUNDLE_ID, ex.getMessage(), ex));
			}
		}

		for (IContentType toRemove : toRemoveContentTypes) {
			this.extensions.remove(toRemove);
		}
		this.outOfSync = false;
	}
}
