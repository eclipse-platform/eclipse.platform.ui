/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.texteditor.spelling;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;

import org.eclipse.ui.internal.texteditor.TextEditorPlugin;

import org.eclipse.ui.texteditor.spelling.SpellingEngineDescriptor;

/**
 * A spelling engine registry used to access the
 * {@link SpellingEngineDescriptor}s that describe the spelling engine
 * extensions.
 *
 * @see SpellingEngineDescriptor
 * @since 3.1
 */
public class SpellingEngineRegistry {

	/**
	 * Extension id of spelling engine extension point.
	 * (value <code>"spellingEngine"</code>).
	 */
	public static final String SPELLING_ENGINE_EXTENSION_POINT= "spellingEngine"; //$NON-NLS-1$

	/** Ids mapped to descriptors */
	private Map fDescriptorsMap;

	/** Default descriptor or <code>null</code> */
	private SpellingEngineDescriptor fDefaultDescriptor;

	/** All descriptors */
	private SpellingEngineDescriptor[] fDescriptors;

	/** <code>true</code> iff the extensions have been loaded at least once */
	private boolean fLoaded= false;

	/**
	 * Returns the descriptor with the given id or <code>null</code> if
	 * none could be found.
	 *
	 * @param id the id
	 * @return the descriptor with the given id or <code>null</code>
	 */
	public SpellingEngineDescriptor getDescriptor(String id) {
		ensureExtensionsLoaded();
		return (SpellingEngineDescriptor) fDescriptorsMap.get(id);
	}

	/**
	 * Returns the default descriptor.
	 *
	 * @return the default descriptor
	 */
	public SpellingEngineDescriptor getDefaultDescriptor() {
		ensureExtensionsLoaded();
		return fDefaultDescriptor;
	}

	/**
	 * Returns all descriptors.
	 *
	 * @return all descriptors
	 */
	public SpellingEngineDescriptor[] getDescriptors() {
		ensureExtensionsLoaded();
		return fDescriptors;
	}

	/**
	 * Reads all extensions.
	 * <p>
	 * This method can be called more than once in order to reload
	 * from a changed extension registry.
	 * </p>
	 */
	public synchronized void reloadExtensions() {
		List descriptors= new ArrayList();
		fDescriptorsMap= new HashMap();
		fDefaultDescriptor= null;
		IConfigurationElement[] elements= Platform.getExtensionRegistry().getConfigurationElementsFor(TextEditorPlugin.PLUGIN_ID, SPELLING_ENGINE_EXTENSION_POINT);
		for (int i= 0; i < elements.length; i++) {
			SpellingEngineDescriptor descriptor= new SpellingEngineDescriptor(elements[i]);
			descriptors.add(descriptor);
			fDescriptorsMap.put(descriptor.getId(), descriptor);
			if (fDefaultDescriptor == null && descriptor.isDefault())
				fDefaultDescriptor= descriptor;
		}
		fDescriptors= (SpellingEngineDescriptor[]) descriptors.toArray(new SpellingEngineDescriptor[descriptors.size()]);
		fLoaded= true;
		if (fDefaultDescriptor == null && fDescriptors.length > 0)
			fDefaultDescriptor= fDescriptors[0];
	}

	/**
	 * Ensures the extensions have been loaded at least once.
	 */
	private void ensureExtensionsLoaded() {
		if (!fLoaded)
			reloadExtensions();
	}
}
