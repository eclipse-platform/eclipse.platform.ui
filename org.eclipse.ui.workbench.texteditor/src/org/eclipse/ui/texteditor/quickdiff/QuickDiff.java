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
package org.eclipse.ui.texteditor.quickdiff;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.eclipse.core.runtime.Assert;

import org.eclipse.jface.text.source.IAnnotationModel;

import org.eclipse.ui.internal.texteditor.TextEditorPlugin;
import org.eclipse.ui.internal.texteditor.quickdiff.DocumentLineDiffer;
import org.eclipse.ui.internal.texteditor.quickdiff.QuickDiffExtensionsRegistry;

import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Access class for the quick diff reference provider extension point.
 * <p>
 * This class may be instantiated, it is not intended to be subclassed.
 * </p>
 * @since 3.0
 * @noextend This class is not intended to be subclassed by clients.
 */
public class QuickDiff {

	/**
	 * Creates a new instance.
	 */
	public QuickDiff() {
	}

	/**
	 * Returns the descriptor of the "last saved version" reference provider.
	 * <p>
	 * Clients should not cache this value because it can change when plug-ins get dynamically added or removed.
	 * </p>
	 *
	 * @return the descriptor of "last saved version" reference provider or <code>null</code> if none
	 */
	public ReferenceProviderDescriptor getDefaultProvider() {
		QuickDiffExtensionsRegistry registry= TextEditorPlugin.getDefault().getQuickDiffExtensionRegistry();
		if (registry != null)
			return registry.getDefaultProvider();

		return null;
	}

	/**
	 * Returns a non-modifiable list of <code>ReferenceProviderDescriptor</code> describing all extension
	 * to the <code>quickDiffReferenceProvider</code> extension point.
	 * <p>
	 * Clients should not cache this list because it can change when plug-ins get dynamically added or removed.
	 * </p>
	 *
	 * @return the non-modifiable list of extensions to the <code>quickDiffReferenceProvider</code> extension point.
	 */
	public List getReferenceProviderDescriptors() {
		QuickDiffExtensionsRegistry registry= TextEditorPlugin.getDefault().getQuickDiffExtensionRegistry();
		if (registry != null)
			return registry.getReferenceProviderDescriptors();

		return Collections.EMPTY_LIST;
	}

	/**
	 * Returns the quick diff reference provider registered under <code>id</code>, or the default
	 * reference provider. The returned provider gets its editor set to <code>editor</code>. If neither
	 * the requested provider nor the default provider return <code>true</code> from <code>isEnabled</code> after
	 * having the editor set, <code>null</code> is returned.
	 * <p>
	 * Clients should not cache this value because it can change when plug-ins get dynamically added or removed.
	 * </p>
	 *
	 * @param editor the editor to be installed with the returned provider
	 * @param id the id as specified in the <code>plugin.xml</code> that installs the reference provider
	 * @return the reference provider registered under <code>id</code>, or the default reference provider, or <code>null</code>
	 */
	public IQuickDiffReferenceProvider getReferenceProviderOrDefault(ITextEditor editor, String id) {
		Assert.isNotNull(editor);
		Assert.isNotNull(id);

		List descs= getReferenceProviderDescriptors();
		// try to fetch preferred provider; load if needed
		for (Iterator iter= descs.iterator(); iter.hasNext();) {
			ReferenceProviderDescriptor desc= (ReferenceProviderDescriptor) iter.next();
			if (desc.getId().equals(id)) {
				IQuickDiffReferenceProvider provider= desc.createProvider();
				if (provider != null) {
					provider.setActiveEditor(editor);
					if (provider.isEnabled())
						return provider;
					provider.dispose();
					provider= null;
				}
			}
		}

		for (ListIterator iter= descs.listIterator(descs.size()); iter.hasPrevious();) {
			ReferenceProviderDescriptor desc= (ReferenceProviderDescriptor) iter.previous();
			IQuickDiffReferenceProvider provider= desc.createProvider();
			if (provider != null) {
				provider.setActiveEditor(editor);
				if (provider.isEnabled())
					return provider;
				provider.dispose();
				provider= null;
			}
		}

		return null;
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
		}
		return null;
	}

	/**
	 * Returns the identifier of the quick diff provider installed with the given diff annotation
	 * model, or the empty string if it is not a diff annotation model or has no configured diff
	 * provider.
	 *
	 * @param differ a diff annotation model
	 * @return the reference provider id, or the empty string for none
	 * @since 3.2
	 */
	public Object getConfiguredQuickDiffProvider(IAnnotationModel differ) {
		if (differ instanceof DocumentLineDiffer) {
			DocumentLineDiffer lineDiffer= (DocumentLineDiffer) differ;
			IQuickDiffReferenceProvider provider= lineDiffer.getReferenceProvider();
			if (provider != null)
				return provider.getId();
		}
		return ""; //$NON-NLS-1$
	}

}
