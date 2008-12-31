/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.texteditor.quickdiff;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;

import org.eclipse.ui.internal.texteditor.TextEditorPlugin;

import org.eclipse.ui.texteditor.quickdiff.ReferenceProviderDescriptor;

/**
 * Access class for the quick diff reference provider extension point.
 *
 * @since 3.0
 */
public class QuickDiffExtensionsRegistry {

	/** The default reference provider's descriptor. */
	private ReferenceProviderDescriptor fDefaultDescriptor;
	/** The list returned to callers of <code>getExtensions</code>. */
	private List fDescriptors;

	/**
	 * Creates a new instance.
	 */
	public QuickDiffExtensionsRegistry() {
	}

	/**
	 * Returns the default provider, which is the last saved version.
	 *
	 * @return the descriptor of the default reference provider.
	 */
	public synchronized ReferenceProviderDescriptor getDefaultProvider() {
		ensureRegistered();
		return fDefaultDescriptor;
	}

	/**
	 * Returns a non-modifiable list of <code>ReferenceProviderDescriptor</code> describing all extension
	 * to the <code>quickDiffReferenceProvider</code> extension point.
	 *
	 * @return the list of extensions to the <code>quickDiffReferenceProvider</code> extension point.
	 */
	public synchronized List getReferenceProviderDescriptors() {
		ensureRegistered();
		return fDescriptors;
	}

	/**
	 * Ensures that the extensions are read and stored in <code>fDescriptors</code>.
	 */
	private void ensureRegistered() {
		if (fDescriptors == null)
			reloadExtensions();
	}

	/**
	 * Reads all extensions.
	 * <p>
	 * This method can be called more than once in
	 * order to reload from a changed extension registry.
	 * </p>
	 */
	public synchronized void reloadExtensions() {
		fDefaultDescriptor= null;
		IExtensionRegistry registry= Platform.getExtensionRegistry();
		List list= new ArrayList();

		IConfigurationElement[] elements= registry.getConfigurationElementsFor(TextEditorPlugin.PLUGIN_ID, TextEditorPlugin.REFERENCE_PROVIDER_EXTENSION_POINT);
		for (int i= 0; i < elements.length; i++) {
			ReferenceProviderDescriptor desc= new ReferenceProviderDescriptor(elements[i]);
			if (desc.getId().equals("org.eclipse.ui.internal.editors.quickdiff.LastSaveReferenceProvider")) //$NON-NLS-1$
				fDefaultDescriptor= desc;
			list.add(desc);
		}

		// make sure the default is the first one in the list
		if (fDefaultDescriptor != null) {
			list.remove(fDefaultDescriptor);
			list.add(0, fDefaultDescriptor);
		}

		fDescriptors= Collections.unmodifiableList(list);
	}
}
