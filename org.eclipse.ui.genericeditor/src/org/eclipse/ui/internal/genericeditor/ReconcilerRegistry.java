/*******************************************************************************
 * Copyright (c) 2017 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Lucas Bullen (Red Hat Inc.) - initial implementation
 *******************************************************************************/
package org.eclipse.ui.internal.genericeditor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IRegistryChangeEvent;
import org.eclipse.core.runtime.IRegistryChangeListener;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.source.ISourceViewer;

/**
 * A registry of reconciliers provided by extension <code>org.eclipse.ui.genericeditor.reconcilers</code>.
 * Those extensions are specific to a given {@link IContentType}.
 * 
 * @since 1.1
 */
public class ReconcilerRegistry {

	private static final String EXTENSION_POINT_ID = GenericEditorPlugin.BUNDLE_ID + ".reconcilers"; //$NON-NLS-1$

	/**
	 * This class wraps and proxies an {@link IReconciler} provided through extensions
	 * and loads it lazily when it can contribute to the editor, then delegates all operations to
	 * actual reconcilier.
	 */
	private static class ReconcilerExtension {
		private static final String CLASS_ATTRIBUTE = "class"; //$NON-NLS-1$
		private static final String CONTENT_TYPE_ATTRIBUTE = "contentType"; //$NON-NLS-1$

		private IConfigurationElement extension;
		private IContentType targetContentType;

		private ReconcilerExtension(IConfigurationElement element) throws Exception {
			this.extension = element;
			this.targetContentType = Platform.getContentTypeManager().getContentType(element.getAttribute(CONTENT_TYPE_ATTRIBUTE));
		}

		public IReconciler createDelegate() {
			try {
				return (IReconciler) extension.createExecutableExtension(CLASS_ATTRIBUTE);
			} catch (CoreException e) {
				GenericEditorPlugin.getDefault().getLog().log(new Status(IStatus.ERROR, GenericEditorPlugin.BUNDLE_ID, e.getMessage(), e));
			}
			return null;
		}
		
	}
	private Map<IConfigurationElement, ReconcilerExtension> extensions = new HashMap<>();
	private boolean outOfSync = true;

	/**
	 * Creates the registry and binds it to the extension point.
	 */
	public ReconcilerRegistry() {
		Platform.getExtensionRegistry().addRegistryChangeListener(new IRegistryChangeListener() {
			@Override
			public void registryChanged(IRegistryChangeEvent event) {
				outOfSync = true;
			}
		}, EXTENSION_POINT_ID);
	}

	/**
	 * Get the contributed {@link IReconciliers}s that are relevant to hook on source viewer according
	 * to document content types. 
	 * @param sourceViewer the source viewer we're hooking completion to.
	 * @param contentTypes the content types of the document we're editing.
	 * @return the list of {@link IReconciler} contributed for at least one of the content types.
	 */
	public List<IReconciler> getReconcilers(ISourceViewer sourceViewer, Set<IContentType> contentTypes) {
		if (this.outOfSync) {
			sync();
		}
		List<IReconciler> res = new ArrayList<>();
		for (ReconcilerExtension ext : this.extensions.values()) {
			if (contentTypes.contains(ext.targetContentType)) {
				res.add(ext.createDelegate());
			}
		}
		return res;
	}

	private void sync() {
		Set<IConfigurationElement> toRemoveExtensions = new HashSet<>(this.extensions.keySet());
		for (IConfigurationElement extension : Platform.getExtensionRegistry().getConfigurationElementsFor(EXTENSION_POINT_ID)) {
			toRemoveExtensions.remove(extension);
			if (!this.extensions.containsKey(extension)) {
				try {
					this.extensions.put(extension, new ReconcilerExtension(extension));
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
