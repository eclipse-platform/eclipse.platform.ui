/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.texteditor.quickdiff;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IPluginRegistry;
import org.eclipse.core.runtime.Platform;

import org.eclipse.jface.text.Assert;
import org.eclipse.jface.text.source.IAnnotationModel;

import org.eclipse.ui.texteditor.ITextEditor;

import org.eclipse.ui.internal.texteditor.TextEditorPlugin;
import org.eclipse.ui.internal.texteditor.quickdiff.DocumentLineDiffer;

/**
 * Access class for the quick diff reference provider extension point.
 * 
 * @since 3.0
 */
public class QuickDiff {
	
	/** Extension point id for quick diff reference providers. */
	private static final String REFERENCE_PROVIDER_EXTENSION_POINT= "quickDiffReferenceProvider"; //$NON-NLS-1$
	/** The default reference provider's descriptor. */
	private ReferenceProviderDescriptor fDefaultDescriptor;
	/** The list returned to callers of <code>getExtensions</code>. */
	private List fDescriptors;

	/**
	 * Creates a new instance. 
	 */
	public QuickDiff() {
	}
	
	/**
	 * Returns the first descriptor with the <code>default</code> attribute set to <code>true</code>.
	 * 
	 * @return the descriptor of the default reference provider.
	 */
	public ReferenceProviderDescriptor getDefaultProvider() {
		ensureRegistered();
		return fDefaultDescriptor;
	}
	
	/**
	 * Returns a non-modifiable list of <code>ReferenceProviderDescriptor</code> describing all extension
	 * to the <code>quickDiffReferenceProvider</code> extension point.
	 * 
	 * @return the list of extensions to the <code>quickDiffReferenceProvider</code> extension point.
	 */
	public List getReferenceProviderDescriptors() {
		ensureRegistered();
		return fDescriptors;
	}
	
	/**
	 * Returns the quick diff reference provider registered under <code>id</code>, or the default
	 * reference provider. The returned provider gets its editor set to <code>editor</code>. If neither
	 * the requested provider nor the default provider return <code>true</code> from <code>isEnabled</code> after
	 * having the editor set, <code>null</code> is returned.
	 * 
	 * @param editor the editor to be installed with the returned provider
	 * @param id the id as specified in the <code>plugin.xml</code> that installs the reference provider
	 * @return the reference provider registered under <code>id</code>, or the default reference provider, or <code>null</code>
	 */
	public IQuickDiffReferenceProvider getReferenceProviderOrDefault(ITextEditor editor, String id) {
		Assert.isNotNull(editor);
		Assert.isNotNull(id);
		
		List descs= getReferenceProviderDescriptors();
		IQuickDiffProviderImplementation provider= null;
		// try to fetch preferred provider; load if needed
		for (Iterator iter= descs.iterator(); iter.hasNext();) {
			ReferenceProviderDescriptor desc= (ReferenceProviderDescriptor) iter.next();
			if (desc.getId().equals(id)) {
				provider= desc.createProvider();
				if (provider != null) {
					provider.setActiveEditor(editor);
					if (provider.isEnabled())
						break;
					provider.dispose();
					provider= null;
				}
			}
		}
		
		// if not found, get default provider as specified by the extension point
		if (provider == null) {
			ReferenceProviderDescriptor defaultDescriptor= getDefaultProvider();
			if (defaultDescriptor != null) {
				provider= defaultDescriptor.createProvider();
				if (provider != null) {
					provider.setActiveEditor(editor);
					if (!provider.isEnabled()) {
						provider.dispose();
						provider= null;
					}
				}
			}
		}
		
		return provider;
	}
	
	/**
	 * Creates a new line differ annotation model with its reference provider set to the reference provider
	 * obtained by calling <code>getReferenceProviderOrDefault(editor, id)</code>.
	 * 
	 * @param editor the editor to be installed with the returned provider
	 * @param id the id as specified in the <code>plugin.xml</code> that installs the reference provider
	 * @return a quick diff annotation model
	 */
	public IAnnotationModel createQuickDiffAnnotationModel(ITextEditor editor, String id) {
		IQuickDiffReferenceProvider provider= getReferenceProviderOrDefault(editor, id);
		if (provider != null) {
			DocumentLineDiffer differ= new DocumentLineDiffer();
			differ.setReferenceProvider(provider);
			return differ;
		} else
			return null;
	}

	/**
	 * Ensures that the extensions are read and stored in <code>fDescriptors</code>. 
	 */
	private void ensureRegistered() {
		if (fDescriptors == null)
			registerExtensions();
	}

	/** Reads all extensions. */
	private void registerExtensions() {
		IPluginRegistry registry= Platform.getPluginRegistry();
		List list= new ArrayList();

		IConfigurationElement[] elements= registry.getConfigurationElementsFor(TextEditorPlugin.getPluginId(), REFERENCE_PROVIDER_EXTENSION_POINT);
		for (int i= 0; i < elements.length; i++) {
			ReferenceProviderDescriptor desc= new ReferenceProviderDescriptor(elements[i]);
			if (fDefaultDescriptor == null && desc.getDefault())
				fDefaultDescriptor= desc;
			list.add(desc);
		}
		
		fDescriptors= Collections.unmodifiableList(list);
	}

}
