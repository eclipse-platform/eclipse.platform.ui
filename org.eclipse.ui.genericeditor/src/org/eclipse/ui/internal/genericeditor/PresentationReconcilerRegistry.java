/*******************************************************************************
 * Copyright (c) 2016-2017 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Sopot Cela, Mickael Istria (Red Hat Inc.) - initial implementation
 *******************************************************************************/
package org.eclipse.ui.internal.genericeditor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IRegistryChangeEvent;
import org.eclipse.core.runtime.IRegistryChangeListener;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.source.ISourceViewer;

/**
 * A registry of presentation reconciliers provided by extension <code>org.eclipse.ui.genericeditor.presentationReconcilers</code>.
 * Those extensions are specific to a given {@link IContentType}.
 * 
 * @since 1.0
 */
public class PresentationReconcilerRegistry {

	private static final String EXTENSION_POINT_ID = GenericEditorPlugin.BUNDLE_ID + ".presentationReconcilers"; //$NON-NLS-1$

	private Map<IConfigurationElement, GenericContentTypeRelatedExtension<IPresentationReconciler>> extensions = new HashMap<>();
	private boolean outOfSync = true;

	/**
	 * Creates the registry and binds it to the extension point.
	 */
	public PresentationReconcilerRegistry() {
		Platform.getExtensionRegistry().addRegistryChangeListener(new IRegistryChangeListener() {
			@Override
			public void registryChanged(IRegistryChangeEvent event) {
				outOfSync = true;
			}
		}, EXTENSION_POINT_ID);
	}

	/**
	 * Get the contributed {@link IPresentationReconciliers}s that are relevant to hook on source viewer according
	 * to document content types. 
	 * @param sourceViewer the source viewer we're hooking completion to.
	 * @param contentTypes the content types of the document we're editing.
	 * @return the list of {@link IPresentationReconciler} contributed for at least one of the content types.
	 */
	public List<IPresentationReconciler> getPresentationReconcilers(ISourceViewer sourceViewer, Set<IContentType> contentTypes) {
		if (this.outOfSync) {
			sync();
		}
		return this.extensions.values().stream()
			.filter(ext -> contentTypes.contains(ext.targetContentType))
			.sorted(new ContentTypeSpecializationComparator<IPresentationReconciler>())
			.map(GenericContentTypeRelatedExtension<IPresentationReconciler>::createDelegate)
			.collect(Collectors.toList());
	}

	private void sync() {
		Set<IConfigurationElement> toRemoveExtensions = new HashSet<>(this.extensions.keySet());
		for (IConfigurationElement extension : Platform.getExtensionRegistry().getConfigurationElementsFor(EXTENSION_POINT_ID)) {
			toRemoveExtensions.remove(extension);
			if (!this.extensions.containsKey(extension)) {
				try {
					this.extensions.put(extension, new GenericContentTypeRelatedExtension<IPresentationReconciler>(extension));
				} catch (Exception ex) {
					GenericEditorPlugin.getDefault().getLog().log(new Status(IStatus.ERROR, GenericEditorPlugin.BUNDLE_ID, ex.getMessage(), ex));
				}
			}
		}
		for (IConfigurationElement toRemove : toRemoveExtensions) {
			this.extensions.remove(toRemove);
		}
		this.outOfSync = false;
	}

}
