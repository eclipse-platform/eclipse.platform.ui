/*******************************************************************************
 * Copyright (c) 2016 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * - Mickael Istria (Red Hat Inc.)
 *******************************************************************************/
package org.eclipse.ui.internal.genericeditor;

import java.util.HashMap;
import java.util.HashSet;
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
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.source.ISourceViewer;

/**
 * Text hover registry that manages the detectors
 * contributed by the <code>org.eclipse.ui.workbench.texteditor.hoverProvider</code> extension point for
 * targets contributed by the <code>org.eclipse.ui.workbench.texteditor.hyperlinkDetectorTargets</code> extension point.
 *
 * @since 1.0
 */
public final class TextHoverRegistry {

	private static final String EXTENSION_POINT_ID = GenericEditorPlugin.BUNDLE_ID + ".hoverProviders"; //$NON-NLS-1$

	private Map<IConfigurationElement, TextHoverExtension> extensions = new HashMap<>();
	private boolean outOfSync = true;

	private static class TextHoverExtension {
		private static final String CONTENT_TYPE_ATTRIBUTE = "contentType"; //$NON-NLS-1$
		private static final String CLASS_ATTRIBUTE = "class"; //$NON-NLS-1$

		private IConfigurationElement extension;
		private IContentType targetContentType;
		private ITextHover delegate;

		public TextHoverExtension(IConfigurationElement extension) throws Exception {
			this.extension = extension;
			this.targetContentType = Platform.getContentTypeManager().getContentType(extension.getAttribute(CONTENT_TYPE_ATTRIBUTE));
		}

		private ITextHover getDelegate() {
			if (this.delegate == null) {
				try {
					this.delegate = (ITextHover) extension.createExecutableExtension(CLASS_ATTRIBUTE);
				} catch (CoreException e) {
					e.printStackTrace();
				}
			}
			return delegate;
		}

	}

	public TextHoverRegistry(IPreferenceStore preferenceStore) {
		Platform.getExtensionRegistry().addRegistryChangeListener(new IRegistryChangeListener() {
			@Override
			public void registryChanged(IRegistryChangeEvent event) {
				outOfSync = true;
			}
		}, EXTENSION_POINT_ID);
	}

	public ITextHover getAvailableHover(ISourceViewer sourceViewer, Set<IContentType> contentTypes) {
		if (this.outOfSync) {
			sync();
		}
		// TODO rather that returning the 1st active hover, consider
		// supporting compound/aggregated hovers.
		for (TextHoverExtension ext : this.extensions.values()) {
			if (contentTypes.contains(ext.targetContentType)) {
				return ext.getDelegate();
			}
		}
		return null;
	}

	private void sync() {
		Set<IConfigurationElement> toRemoveExtensions = new HashSet<>(this.extensions.keySet());
		for (IConfigurationElement extension : Platform.getExtensionRegistry().getConfigurationElementsFor(EXTENSION_POINT_ID)) {
			toRemoveExtensions.remove(extension);
			if (!this.extensions.containsKey(extension)) {
				try {
					this.extensions.put(extension, new TextHoverExtension(extension));
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
