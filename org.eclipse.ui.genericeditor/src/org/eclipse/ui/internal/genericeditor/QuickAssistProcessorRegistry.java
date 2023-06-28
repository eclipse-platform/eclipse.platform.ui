/*******************************************************************************
 * Copyright (c) 2016-2019 Red Hat Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * - Andrew Obuchowicz (Red Hat Inc.)
 *******************************************************************************/
package org.eclipse.ui.internal.genericeditor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jface.text.quickassist.IQuickAssistProcessor;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.texteditor.ITextEditor;

public class QuickAssistProcessorRegistry {

	private static final String EXTENSION_POINT_ID = GenericEditorPlugin.BUNDLE_ID + ".quickAssistProcessors"; //$NON-NLS-1$
	private Map<IConfigurationElement, GenericContentTypeRelatedExtension<IQuickAssistProcessor>> extensions = new HashMap<>();
	private boolean outOfSync = true;

	/**
	 * Creates the registry and binds it to the extension point.
	 */
	public QuickAssistProcessorRegistry() {
		Platform.getExtensionRegistry().addRegistryChangeListener(event -> outOfSync = true, EXTENSION_POINT_ID);
	}

	/**
	 * Get the contributed {@link IQuickAssistProcessor}s
	 *
	 * @return the list of {@link IQuickAssistProcessor} contributed
	 */

	public List<IQuickAssistProcessor> getQuickAssistProcessors(ISourceViewer sourceViewer, ITextEditor editor,
			Set<IContentType> contentTypes) {
		if (this.outOfSync) {
			sync();
		}
		return this.extensions.values().stream()
				.filter(ext -> contentTypes.contains(ext.targetContentType))
				.filter(ext -> ext.matches(sourceViewer, editor))
				.sorted(new ContentTypeSpecializationComparator<IQuickAssistProcessor>())
				.map(GenericContentTypeRelatedExtension<IQuickAssistProcessor>::createDelegate)
				.collect(Collectors.toList());
	}

	private void sync() {
		Set<IConfigurationElement> toRemoveExtensions = new HashSet<>(this.extensions.keySet());
		for (IConfigurationElement extension : Platform.getExtensionRegistry()
				.getConfigurationElementsFor(EXTENSION_POINT_ID)) {
			toRemoveExtensions.remove(extension);
			if (!this.extensions.containsKey(extension)) {
				try {
					this.extensions.put(extension,
							new GenericContentTypeRelatedExtension<IQuickAssistProcessor>(extension));
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
