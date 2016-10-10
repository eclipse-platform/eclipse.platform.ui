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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

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

	private SortedSet<TextHoverExtension> extensions;
	private boolean outOfSync = true;

	static class TextHoverExtension {
		private static final String CONTENT_TYPE_ATTRIBUTE = "contentType"; //$NON-NLS-1$
		private static final String CLASS_ATTRIBUTE = "class"; //$NON-NLS-1$
		private static final String ID_ATTRIBUTE = "id"; //$NON-NLS-1$
		private static final String IS_BEFORE_ATTRIBUTE = "isBefore"; //$NON-NLS-1$
		private static final String IS_AFTER_ATTRIBUTE = "isAfter"; //$NON-NLS-1$

		private IConfigurationElement extension;
		private IContentType targetContentType;
		private ITextHover delegate;
		private String id;
		private String isBefore;
		private String isAfter;

		public TextHoverExtension(IConfigurationElement extension) throws Exception {
			this.extension = extension;
			this.targetContentType = Platform.getContentTypeManager().getContentType(extension.getAttribute(CONTENT_TYPE_ATTRIBUTE));
			this.id = extension.getAttribute(ID_ATTRIBUTE);
			this.isBefore = extension.getAttribute(IS_BEFORE_ATTRIBUTE);
			this.isAfter = extension.getAttribute(IS_AFTER_ATTRIBUTE);
		}

		public ITextHover getDelegate() {
			if (this.delegate == null) {
				try {
					this.delegate = (ITextHover) extension.createExecutableExtension(CLASS_ATTRIBUTE);
				} catch (CoreException e) {
					e.printStackTrace();
				}
			}
			return delegate;
		}

		public String getId() {
			if (this.id != null) {
				return this.id;
			}
			return this.extension.getContributor().getName() + '@' + toString();
		}

		public String getIsAfter() {
			return this.isAfter;
		}

		public String getIsBefore() {
			return this.isBefore;
		}

		IConfigurationElement getConfigurationElement() {
			return this.extension;
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
		List<TextHoverExtension> hoversToConsider = new ArrayList<>();
		for (TextHoverExtension ext : this.extensions) {
			if (contentTypes.contains(ext.targetContentType)) {
				hoversToConsider.add(ext);
			}
		}
		if (!hoversToConsider.isEmpty()) {
			return new CompositeTextHover(hoversToConsider);
		}
		return null;
	}

	private void sync() {
		Set<IConfigurationElement> toRemoveExtensions = new HashSet<>();
		Map<IConfigurationElement, TextHoverExtension> ext = new HashMap<>();
		if (this.extensions != null) {
			ext = this.extensions.stream().collect(Collectors.toMap(TextHoverExtension::getConfigurationElement, Function.identity()));
			toRemoveExtensions = ext.keySet();
		}
		for (IConfigurationElement extension : Platform.getExtensionRegistry().getConfigurationElementsFor(EXTENSION_POINT_ID)) {
			toRemoveExtensions.remove(extension);
			if (!ext.containsKey(extension)) {
				try {
					ext.put(extension, new TextHoverExtension(extension));
				} catch (Exception ex) {
					GenericEditorPlugin.getDefault().getLog().log(new Status(IStatus.ERROR, GenericEditorPlugin.BUNDLE_ID, ex.getMessage(), ex));
				}
			}
		}
		for (IConfigurationElement toRemove : toRemoveExtensions) {
			ext.remove(toRemove);
		}

		OrderedExtensionComparator comparator = new OrderedExtensionComparator(ext.values());
		this.extensions = new TreeSet<>(comparator);
		this.extensions.addAll(ext.values());
		this.outOfSync = false;
	}

}
