/*******************************************************************************
 * Copyright (c) 2023 Avaloq Group AG (http://www.avaloq.com).
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Andrew Lamb (Avaloq Group AG) - initial implementation
 *******************************************************************************/
package org.eclipse.ui.internal.genericeditor;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.content.IContentType;

/**
 * A registry of contributions to the content assistant look and feel provided
 * by extension
 * <code>org.eclipse.ui.genericeditor.contentAssistantLookAndFeel</code>. Those
 * extensions may be specific to a given {@link IContentType}.
 *
 * @since 1.3
 */
public class ContentAssistantLookAndFeelRegistry {

	private static final String EXTENSION_POINT_ID = GenericEditorPlugin.BUNDLE_ID + ".contentAssistantLookAndFeel"; //$NON-NLS-1$

	private Map<IConfigurationElement, GenericContentTypeRelatedExtension<IContentAssistantLookAndFeel>> extensions = new LinkedHashMap<>();
	private boolean outOfSync = true;

	/**
	 * Creates the registry and binds it to the extension point.
	 */
	public ContentAssistantLookAndFeelRegistry() {
		Platform.getExtensionRegistry().addRegistryChangeListener(event -> outOfSync = true, EXTENSION_POINT_ID);
	}

	/**
	 * Get the contributed {@link IContentAssistantLookAndFeel}s that are relevant
	 * to hook on content assistant according to document content types.
	 *
	 * @param contentTypes the content types of the document we're editing.
	 * @return the list of {@link IContentAssistantLookAndFeel} contributed for at
	 *         least one of the content types.
	 */
	public List<IContentAssistantLookAndFeel> getContentAssistantLookAndFeel(Set<IContentType> contentTypes) {
		if (this.outOfSync) {
			sync();
		}
		// we want to return extensions for least specialized content types first, with
		// null being less specialized than any specific content type, and the default
		// being the least specialized of all.
		ContentTypeSpecializationComparator<IContentAssistantLookAndFeel> mostSpecialFirst = new ContentTypeSpecializationComparator<>();
		List<IContentAssistantLookAndFeel> contributions = this.extensions.values().stream()
				.filter(ext -> ext.targetContentType == null || contentTypes.contains(ext.targetContentType))
				.sorted((left, right) -> {
					if (left == null && right == null) {
						return 0;
					}
					if (left == null) {
						return -1;
					}
					if (right == null) {
						return 1;
					}
					return -mostSpecialFirst.compare(left, right);
				}).map(GenericContentTypeRelatedExtension<IContentAssistantLookAndFeel>::createDelegate)
				.collect(Collectors.toList());
		contributions.add(0, IContentAssistantLookAndFeel.DEFAULT);
		return contributions;
	}

	private void sync() {
		Set<IConfigurationElement> toRemoveExtensions = new HashSet<>(this.extensions.keySet());
		for (IConfigurationElement extension : Platform.getExtensionRegistry()
				.getConfigurationElementsFor(EXTENSION_POINT_ID)) {
			toRemoveExtensions.remove(extension);
			if (!this.extensions.containsKey(extension)) {
				try {
					this.extensions.put(extension,
							new GenericContentTypeRelatedExtension<IContentAssistantLookAndFeel>(extension));
				} catch (Exception ex) {
					GenericEditorPlugin.getDefault().getLog()
							.log(new Status(IStatus.ERROR, GenericEditorPlugin.BUNDLE_ID, ex.getMessage(), ex));
				}
			}
		}
		for (IConfigurationElement toRemove : toRemoveExtensions) {
			this.extensions.remove(toRemove);
		}
		this.outOfSync = false;
	}
}
