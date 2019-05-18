/**
 *  Copyright (c) 2018 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v2.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v20.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - Bug 538111 - [generic editor] Extension point for ICharacterPairMatcher
 */
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
import org.eclipse.jface.text.source.ICharacterPairMatcher;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * A registry of character pair matchers provided by extension
 * <code>org.eclipse.ui.genericeditor.characterPairMatchers</code>. Those
 * extensions are specific to a given {@link IContentType}.
 * 
 * @since 1.2
 */
public class CharacterPairMatcherRegistry {

	private static final String EXTENSION_POINT_ID = GenericEditorPlugin.BUNDLE_ID + ".characterPairMatchers"; //$NON-NLS-1$

	private Map<IConfigurationElement, GenericContentTypeRelatedExtension<ICharacterPairMatcher>> extensions = new HashMap<>();
	private boolean outOfSync = true;

	/**
	 * Creates the registry and binds it to the extension point.
	 */
	public CharacterPairMatcherRegistry() {
		Platform.getExtensionRegistry().addRegistryChangeListener(new IRegistryChangeListener() {
			@Override
			public void registryChanged(IRegistryChangeEvent event) {
				outOfSync = true;
			}
		}, EXTENSION_POINT_ID);
	}

	/**
	 * Get the contributed {@link IPresentationReconciliers}s that are relevant to
	 * hook on source viewer according to document content types.
	 * 
	 * @param sourceViewer the source viewer we're hooking completion to.
	 * @param editor       the text editor
	 * @param contentTypes the content types of the document we're editing.
	 * @return the list of {@link ICharacterPairMatcher} contributed for at least
	 *         one of the content types.
	 */
	public List<ICharacterPairMatcher> getCharacterPairMatchers(ISourceViewer sourceViewer, ITextEditor editor,
			Set<IContentType> contentTypes) {
		if (this.outOfSync) {
			sync();
		}
		return this.extensions.values().stream().filter(ext -> contentTypes.contains(ext.targetContentType))
				.filter(ext -> ext.matches(sourceViewer, editor))
				.sorted(new ContentTypeSpecializationComparator<ICharacterPairMatcher>())
				.map(GenericContentTypeRelatedExtension<ICharacterPairMatcher>::createDelegate)
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
							new GenericContentTypeRelatedExtension<ICharacterPairMatcher>(extension));
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
