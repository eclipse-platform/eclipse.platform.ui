/**
 *  Copyright (c) 2017 Angelo ZERR.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - [CodeMining] Provide extension point for CodeMining - Bug 528419
 */
package org.eclipse.ui.internal.texteditor.codemining;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

import org.eclipse.jface.text.codemining.ICodeMiningProvider;
import org.eclipse.jface.text.source.ISourceViewer;

import org.eclipse.ui.internal.texteditor.TextEditorPlugin;

import org.eclipse.ui.texteditor.ITextEditor;

/**
 * A codemining providers registry used to access the
 * {@link CodeMiningProviderDescriptor}s that describe the codemining provider
 * extensions.
 *
 * @see CodeMiningProviderDescriptor
 * @since 3.10
 */
public class CodeMiningProviderRegistry {

	/**
	 * Extension id of spelling engine extension point. (value
	 * <code>"codeMiningProviders"</code>).
	 */
	public static final String CODEMINING_PROVIDERS_EXTENSION_POINT = "codeMiningProviders"; //$NON-NLS-1$

	/** All descriptors */
	private CodeMiningProviderDescriptor[] fDescriptors;

	/** <code>true</code> iff the extensions have been loaded at least once */
	private boolean fLoaded = false;

	/**
	 * Returns all descriptors.
	 *
	 * @return all descriptors
	 */
	private CodeMiningProviderDescriptor[] getDescriptors() {
		ensureExtensionsLoaded();
		return fDescriptors;
	}

	/**
	 * Reads all extensions.
	 * <p>
	 * This method can be called more than once in order to reload from a changed
	 * extension registry.
	 * </p>
	 */
	public synchronized void reloadExtensions() {
		List<CodeMiningProviderDescriptor> descriptors = new ArrayList<>();
		IConfigurationElement[] elements = Platform.getExtensionRegistry()
				.getConfigurationElementsFor(TextEditorPlugin.PLUGIN_ID, CODEMINING_PROVIDERS_EXTENSION_POINT);
		for (int i = 0; i < elements.length; i++) {
			IConfigurationElement element = elements[i];
			try {
				CodeMiningProviderDescriptor descriptor = new CodeMiningProviderDescriptor(element);
				descriptors.add(descriptor);
			} catch (CoreException e) {
				TextEditorPlugin.getDefault().getLog()
						.log(new Status(IStatus.ERROR, element.getNamespaceIdentifier(), e.getMessage()));
			}
		}
		fDescriptors = descriptors.toArray(new CodeMiningProviderDescriptor[descriptors.size()]);
		fLoaded = true;
	}

	/**
	 * Ensures the extensions have been loaded at least once.
	 */
	private void ensureExtensionsLoaded() {
		if (!fLoaded)
			reloadExtensions();
	}

	/**
	 * Returns the codemining providers for the given viewer and editor and null
	 * otherwise.
	 *
	 * @param viewer
	 *            the viewer
	 * @param editor
	 *            the editor
	 * @return the codemining providers for the given viewer and editor and null
	 *         otherwise.
	 */
	public ICodeMiningProvider[] getProviders(ISourceViewer viewer, ITextEditor editor) {
		List<ICodeMiningProvider> providers = new ArrayList<>();
		for (CodeMiningProviderDescriptor descriptor : getDescriptors()) {
			if (descriptor.matches(viewer, editor)) {
				ICodeMiningProvider provider = descriptor.createCodeMiningProvider(editor);
				if (provider != null) {
					providers.add(provider);
				}
			}
		}
		return providers.size() > 0 ? providers.toArray(new ICodeMiningProvider[providers.size()]) : null;
	}
}
