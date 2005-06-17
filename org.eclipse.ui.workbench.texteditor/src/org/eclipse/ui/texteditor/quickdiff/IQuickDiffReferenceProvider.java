/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.texteditor.quickdiff;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.jface.text.IDocument;

import org.eclipse.ui.texteditor.ITextEditor;


/**
 * The protocol a reference provider for Quick Diff has to implement. Quick Diff references provide
 * a reference document (an <code>IDocument</code>) that is used as the original against which
 * diff information is generated.
 * <p>Extensions to the extension point <code>quickdiff.referenceprovider</code> have to implement
 * this interface (plus another interface for plug-in and UI management.</p>
 *
 * @since 3.0
 */
public interface IQuickDiffReferenceProvider {
	/**
	 * Returns the reference document for the quick diff display.
	 *
	 * @param monitor a preference monitor to monitor / cancel the process, or <code>null</code>
	 * @return the reference document for the quick diff display or <code>null</code> if getting the
	 * document was canceled or there is no reference available.
	 * @throws CoreException if getting the document fails.
	 */
	IDocument getReference(IProgressMonitor monitor) throws CoreException;

	/**
	 * Called when the reference is no longer used and the provider can free resources.
	 */
	void dispose();

	/**
	 * Returns the id of this reference provider.
	 *
	 * @return the id of this provider as stated in the extending plugin's manifest.
	 */
	String getId();

	/**
	 * Sets the active editor for the provider implementation. Will usually just be called right after
	 * creation of the implementation.
	 *
	 * @param editor the active editor.
	 */
	void setActiveEditor(ITextEditor editor);

	/**
	 * Gives the implementation a hook to publish its enablement. The action corresponding to this
	 * implementation might be grayed out or not shown at all based on the value presented here.
	 *
	 * @return <code>false</code> if the implementation cannot be executed, <code>true</code> if it can,
	 * or if it cannot be decided yet.
	 */
	boolean isEnabled();

	/**
	 * Sets the id of this implementation. This method will be called right after creation, and
	 * <code>id</code> will be set to the <code>Id</code> attribute specified in the extension's
	 * declaration.
	 *
	 * @param id the provider's new id.
	 */
	void setId(String id);
}
