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


import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Describes a reference provider for the quick diff facility. Any extension to the
 * <code>quickdiff.referenceprovider</code> extension point has to provide a class implementing
 * this interface.
 * <p>Extenders must provider a zero-arg constructor in order for the plug-in class loading mechanism
 * to work.</p>
 * 
 * @since 3.0
 */
public interface IQuickDiffProviderImplementation extends IQuickDiffReferenceProvider {
	
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
