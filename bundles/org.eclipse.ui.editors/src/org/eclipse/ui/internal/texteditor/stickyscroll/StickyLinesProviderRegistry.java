/*******************************************************************************
 * Copyright (c) 2024 SAP SE.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     SAP SE - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.texteditor.stickyscroll;

import static org.eclipse.ui.editors.text.EditorsUI.PLUGIN_ID;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

import org.eclipse.jface.text.source.ISourceViewer;

import org.eclipse.ui.internal.editors.text.EditorsPlugin;

import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.stickyscroll.IStickyLinesProvider;

/**
 * Registry to read sticky lines provider from corresponding extension point.
 */
public class StickyLinesProviderRegistry {
	/**
	 * Extension point id (value <code>"stickyLinesProviders"</code>).
	 */
	public static final String STICKY_LINES_PROVIDERS_EXTENSION_POINT= "stickyLinesProviders"; //$NON-NLS-1$

	/** All descriptors */
	private StickyLinesProviderDescriptor[] fDescriptors;

	/** <code>true</code> iff the extensions have been loaded at least once */
	private boolean fLoaded= false;

	/**
	 * Returns the sticky lines providers for the given viewer and editor. If no specific provider
	 * is registered, a {@link DefaultStickyLinesProvider} is returned.
	 *
	 * @param viewer the viewer
	 * @param editor the editor
	 * @return the sticky lines providers for the given viewer and editor and a default provider
	 *         otherwise.
	 */
	public IStickyLinesProvider getProviders(ISourceViewer viewer, ITextEditor editor) {
		for (StickyLinesProviderDescriptor descriptor : getDescriptors()) {
			if (descriptor.matches(viewer, editor)) {
				IStickyLinesProvider provider= descriptor.createStickyLinesProvider();
				if (provider != null) {
					return provider;
				}
			}
		}
		return new DefaultStickyLinesProvider();
	}

	/**
	 * Returns all descriptors.
	 *
	 * @return all descriptors
	 */
	private StickyLinesProviderDescriptor[] getDescriptors() {
		ensureExtensionsLoaded();
		return fDescriptors;
	}

	/**
	 * Reads all extensions.
	 * <p>
	 * This method can be called more than once in order to reload from a changed extension
	 * registry.
	 * </p>
	 */
	public synchronized void reloadExtensions() {
		List<StickyLinesProviderDescriptor> descriptors= new ArrayList<>();
		IConfigurationElement[] elements= Platform.getExtensionRegistry()
				.getConfigurationElementsFor(PLUGIN_ID, STICKY_LINES_PROVIDERS_EXTENSION_POINT);
		for (IConfigurationElement element : elements) {
			try {
				StickyLinesProviderDescriptor descriptor= new StickyLinesProviderDescriptor(element);
				descriptors.add(descriptor);
			} catch (CoreException e) {
				EditorsPlugin.getDefault().getLog()
						.log(new Status(IStatus.ERROR, element.getNamespaceIdentifier(), e.getMessage()));
			}
		}
		fDescriptors= descriptors.toArray(StickyLinesProviderDescriptor[]::new);
		fLoaded= true;
	}

	/**
	 * Ensures the extensions have been loaded at least once.
	 */
	private void ensureExtensionsLoaded() {
		if (!fLoaded)
			reloadExtensions();
	}
}