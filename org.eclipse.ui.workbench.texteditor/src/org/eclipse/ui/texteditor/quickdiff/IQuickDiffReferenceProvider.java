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

import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.jface.text.IDocument;


/**
 * The protocol a reference provider for quickdiff has to implement. Quickdiff references provide
 * a reference document (an <code>IDocument</code>) that is used as the original against which 
 * diff information is generated.
 * <p>Extensions to the extension point <code>quickdiff.referenceprovider</code> have to implement
 * this interface (plus another interface for plugin and UI management.</p>
 * 
 * @since 3.0
 * @see IQuickDiffProviderImplementation
 */
public interface IQuickDiffReferenceProvider {
	/**
	 * Returns the reference document for the quick diff display.
	 * 
	 * @param monitor a preference monitor to monitor / cancel the process, or <code>null</code>
	 * @return the reference document for the quick diff display
	 */
	IDocument getReference(IProgressMonitor monitor);

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
}
