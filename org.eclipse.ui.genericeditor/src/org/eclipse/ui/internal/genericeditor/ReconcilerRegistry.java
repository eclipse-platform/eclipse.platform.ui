/*******************************************************************************
 * Copyright (c) 2017 Red Hat Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Lucas Bullen (Red Hat Inc.) - initial implementation
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
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * A registry of reconciliers provided by extensions
 * <code>org.eclipse.ui.genericeditor.reconcilers</code> and
 * <code>org.eclipse.ui.genericeditor.foldingReconcilers</code>. Those
 * extensions are specific to a given {@link IContentType}.
 *
 * @since 1.1
 */
public class ReconcilerRegistry {

	private static final String EXTENSION_POINT_ID = GenericEditorPlugin.BUNDLE_ID + ".reconcilers"; //$NON-NLS-1$
	private static final String HIGHLIGHT_EXTENSION_POINT_ID = GenericEditorPlugin.BUNDLE_ID + ".highlightReconcilers"; //$NON-NLS-1$
	private static final String FOLDING_EXTENSION_POINT_ID = GenericEditorPlugin.BUNDLE_ID + ".foldingReconcilers"; //$NON-NLS-1$

	private Map<IConfigurationElement, GenericContentTypeRelatedExtension<IReconciler>> extensions = new HashMap<>();
	private Map<IConfigurationElement, GenericContentTypeRelatedExtension<IReconciler>> highlightExtensions = new HashMap<>();
	private Map<IConfigurationElement, GenericContentTypeRelatedExtension<IReconciler>> foldingExtensions = new HashMap<>();
	private boolean outOfSync = true;
	private boolean highlightOutOfSync = true;
	private boolean foldingOutOfSync = true;

	/**
	 * Creates the registry and binds it to the extension point.
	 */
	public ReconcilerRegistry() {
		Platform.getExtensionRegistry().addRegistryChangeListener(event -> {
			outOfSync = true;
		}, EXTENSION_POINT_ID);

		Platform.getExtensionRegistry().addRegistryChangeListener(event -> {
			highlightOutOfSync = true;
		}, HIGHLIGHT_EXTENSION_POINT_ID);

		Platform.getExtensionRegistry().addRegistryChangeListener(event -> {
			foldingOutOfSync = true;
		}, FOLDING_EXTENSION_POINT_ID);
	}

	/**
	 * Get the contributed {@link IReconciliers}s that are relevant to hook on
	 * source viewer according to document content types.
	 * 
	 * @param sourceViewer the source viewer we're hooking completion to.
	 * @param editor       the text editor
	 * @param contentTypes the content types of the document we're editing.
	 * @return the list of {@link IReconciler} contributed for at least one of the
	 *         content types, sorted by most generic content type to most specific.
	 */
	public List<IReconciler> getReconcilers(ISourceViewer sourceViewer, ITextEditor editor,
			Set<IContentType> contentTypes) {
		if (this.outOfSync) {
			sync();
		}
		List<IReconciler> reconcilers = this.extensions.values().stream()
				.filter(ext -> contentTypes.contains(ext.targetContentType))
				.filter(ext -> ext.matches(sourceViewer, editor))
				.sorted(new ContentTypeSpecializationComparator<IReconciler>().reversed())
				.map(GenericContentTypeRelatedExtension<IReconciler>::createDelegate).collect(Collectors.toList());
		return reconcilers;
	}

	/**
	 * Get the contributed highlight {@link IReconciliers}s that are relevant to
	 * hook on source viewer according to document content types.
	 * 
	 * @param sourceViewer the source viewer we're hooking completion to.
	 * @param editor       the text editor
	 * @param contentTypes the content types of the document we're editing.
	 * @return the list of highlight {@link IReconciler}s contributed for at least
	 *         one of the content types, sorted by most generic content type to most
	 *         specific.
	 */
	public List<IReconciler> getHighlightReconcilers(ISourceViewer sourceViewer, ITextEditor editor,
			Set<IContentType> contentTypes) {
		if (this.highlightOutOfSync) {
			syncHighlight();
		}
		List<IReconciler> highlightReconcilers = this.highlightExtensions.values().stream()
				.filter(ext -> contentTypes.contains(ext.targetContentType))
				.filter(ext -> ext.matches(sourceViewer, editor))
				.sorted(new ContentTypeSpecializationComparator<IReconciler>().reversed())
				.map(GenericContentTypeRelatedExtension<IReconciler>::createDelegate).collect(Collectors.toList());
		return highlightReconcilers;
	}

	/**
	 * Get the contributed folding {@link IReconciliers}s that are relevant to hook
	 * on source viewer according to document content types.
	 * 
	 * @param sourceViewer the source viewer we're hooking completion to.
	 * @param editor       the text editor
	 * @param contentTypes the content types of the document we're editing.
	 * @return the list of folding {@link IReconciler}s contributed for at least one
	 *         of the content types, sorted by most generic content type to most
	 *         specific.
	 */
	public List<IReconciler> getFoldingReconcilers(ISourceViewer sourceViewer, ITextEditor editor,
			Set<IContentType> contentTypes) {
		if (this.foldingOutOfSync) {
			syncFolding();
		}
		List<IReconciler> foldingReconcilers = this.foldingExtensions.values().stream()
				.filter(ext -> contentTypes.contains(ext.targetContentType))
				.filter(ext -> ext.matches(sourceViewer, editor))
				.sorted(new ContentTypeSpecializationComparator<IReconciler>().reversed())
				.map(GenericContentTypeRelatedExtension<IReconciler>::createDelegate).collect(Collectors.toList());
		return foldingReconcilers;
	}

	private void sync() {
		Set<IConfigurationElement> toRemoveExtensions = new HashSet<>(this.extensions.keySet());
		for (IConfigurationElement extension : Platform.getExtensionRegistry()
				.getConfigurationElementsFor(EXTENSION_POINT_ID)) {
			toRemoveExtensions.remove(extension);
			if (!this.extensions.containsKey(extension)) {
				try {
					this.extensions.put(extension, new GenericContentTypeRelatedExtension<IReconciler>(extension));
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

	private void syncHighlight() {
		Set<IConfigurationElement> toRemoveExtensions = new HashSet<>(this.extensions.keySet());
		for (IConfigurationElement extension : Platform.getExtensionRegistry()
				.getConfigurationElementsFor(HIGHLIGHT_EXTENSION_POINT_ID)) {
			toRemoveExtensions.remove(extension);
			if (!this.highlightExtensions.containsKey(extension)) {
				try {
					this.highlightExtensions.put(extension,
							new GenericContentTypeRelatedExtension<IReconciler>(extension));
				} catch (Exception ex) {
					GenericEditorPlugin.getDefault().getLog()
							.log(new Status(IStatus.ERROR, GenericEditorPlugin.BUNDLE_ID, ex.getMessage(), ex));
				}
			}
		}
		for (IConfigurationElement toRemove : toRemoveExtensions) {
			this.highlightExtensions.remove(toRemove);
		}
		this.highlightOutOfSync = false;
	}

	private void syncFolding() {
		Set<IConfigurationElement> toRemoveExtensions = new HashSet<>(this.extensions.keySet());
		for (IConfigurationElement extension : Platform.getExtensionRegistry()
				.getConfigurationElementsFor(FOLDING_EXTENSION_POINT_ID)) {
			toRemoveExtensions.remove(extension);
			if (!this.foldingExtensions.containsKey(extension)) {
				try {
					this.foldingExtensions.put(extension,
							new GenericContentTypeRelatedExtension<IReconciler>(extension));
				} catch (Exception ex) {
					GenericEditorPlugin.getDefault().getLog()
							.log(new Status(IStatus.ERROR, GenericEditorPlugin.BUNDLE_ID, ex.getMessage(), ex));
				}
			}
		}
		for (IConfigurationElement toRemove : toRemoveExtensions) {
			this.foldingExtensions.remove(toRemove);
		}
		this.foldingOutOfSync = false;
	}
}
