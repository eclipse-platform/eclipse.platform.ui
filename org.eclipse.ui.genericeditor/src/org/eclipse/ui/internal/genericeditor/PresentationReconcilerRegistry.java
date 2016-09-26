/*******************************************************************************
 * Copyright (c) 2016 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Sopot Cela, Mickael Istria (Red Hat Inc.) - initial implementation
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
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.presentation.IPresentationDamager;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.IPresentationRepairer;
import org.eclipse.jface.text.source.ISourceViewer;

/**
 * A registry of presentation reconciliers provided by extension <code>org.eclipse.ui.genericeditor.presentationReconcilers</code>.
 * Those extensions are specific to a given {@link IContentType}.
 * 
 * @since 1.0
 */
public class PresentationReconcilerRegistry {

	private static final String EXTENSION_POINT_ID = GenericEditorPlugin.BUNDLE_ID + ".presentationReconcilers"; //$NON-NLS-1$

	/**
	 * This class wraps and proxies an {@link IPresentationReconcilier} provided through extensions
	 * and loads it lazily when it can contribute to the editor, then delegates all operations to
	 * actual reconcilier.
	 */
	private static class PresentationReconcilerExtension implements IPresentationReconciler {
		private static final String CLASS_ATTRIBUTE = "class"; //$NON-NLS-1$
		private static final String CONTENT_TYPE_ATTRIBUTE = "contentType"; //$NON-NLS-1$

		private IConfigurationElement extension;
		private IContentType targetContentType;

		private IPresentationReconciler delegate;

		private PresentationReconcilerExtension(IConfigurationElement element) throws Exception {
			this.extension = element;
			this.targetContentType = Platform.getContentTypeManager().getContentType(element.getAttribute(CONTENT_TYPE_ATTRIBUTE));
		}

		private IPresentationReconciler getDelegate() {
			if (this.delegate == null) {
				try {
					this.delegate = (IPresentationReconciler) extension.createExecutableExtension(CLASS_ATTRIBUTE);
				} catch (CoreException e) {
					e.printStackTrace();
				}
			}
			return delegate;
		}

		@Override
		public void install(ITextViewer viewer) {
			getDelegate().install(viewer);
		}

		@Override
		public void uninstall() {
			getDelegate().uninstall();
		}

		@Override
		public IPresentationDamager getDamager(String contentType) {
			return getDelegate().getDamager(contentType);

		}

		@Override
		public IPresentationRepairer getRepairer(String contentType) {
			return getDelegate().getRepairer(contentType);
		}

	}
	private Map<IConfigurationElement, PresentationReconcilerExtension> extensions = new HashMap<>();
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
		List<IPresentationReconciler> res = new ArrayList<>();
		for (PresentationReconcilerExtension ext : this.extensions.values()) {
			if (contentTypes.contains(ext.targetContentType)) {
				res.add(ext);
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
					this.extensions.put(extension, new PresentationReconcilerExtension(extension));
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
