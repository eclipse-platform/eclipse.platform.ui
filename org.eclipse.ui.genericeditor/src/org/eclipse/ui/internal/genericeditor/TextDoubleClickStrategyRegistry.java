/*******************************************************************************
 * Copyright (c) 2022 Avaloq Group AG (http://www.avaloq.com).
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
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.texteditor.ITextEditor;

public class TextDoubleClickStrategyRegistry {

	private static final String EXTENSION_POINT_ID = GenericEditorPlugin.BUNDLE_ID + ".textDoubleClickStrategies"; //$NON-NLS-1$

	private Map<IConfigurationElement, GenericContentTypeRelatedExtension<ITextDoubleClickStrategy>> extensions = new LinkedHashMap<>();
	private boolean outOfSync = true;

	/**
	 * Creates the registry and binds it to the extension point.
	 */
	public TextDoubleClickStrategyRegistry() {
		Platform.getExtensionRegistry().addRegistryChangeListener(event -> outOfSync = true, EXTENSION_POINT_ID);
	}

	/**
	 * Get the contributed {@link ITextDoubleClickStrategy}s that are relevant to
	 * hook on source viewer according to document content types.
	 *
	 * @param sourceViewer the source viewer we're hooking completion to.
	 * @param editor       the text editor
	 * @param contentTypes the content types of the document we're editing.
	 * @return the list of {@link ITextDoubleClickStrategy} contributed for at least
	 *         one of the content types.
	 */
	public Optional<ITextDoubleClickStrategy> getTextDoubleClickStrategy(ISourceViewer sourceViewer, ITextEditor editor,
			Set<IContentType> contentTypes) {
		if (this.outOfSync) {
			sync();
		}
		return this.extensions.values().stream().filter(ext -> contentTypes.contains(ext.targetContentType))
				.filter(ext -> ext.matches(sourceViewer, editor))
				.sorted(new ContentTypeSpecializationComparator<ITextDoubleClickStrategy>()).findFirst()
				.map(GenericContentTypeRelatedExtension<ITextDoubleClickStrategy>::createDelegate);
	}

	private void sync() {
		Set<IConfigurationElement> toRemoveExtensions = new HashSet<>(this.extensions.keySet());
		for (IConfigurationElement extension : Platform.getExtensionRegistry()
				.getConfigurationElementsFor(EXTENSION_POINT_ID)) {
			toRemoveExtensions.remove(extension);
			if (!this.extensions.containsKey(extension)) {
				try {
					this.extensions.put(extension,
							new GenericContentTypeRelatedExtension<ITextDoubleClickStrategy>(extension));
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
