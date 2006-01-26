/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * An <code>IDocument</code> represents an editable subset of the domain
 * model. Different workbench parts (editors and views) may present the same
 * document in different ways. This interface allows the workbench to provide
 * more appropriate handling of operations such as saving and closing workbench
 * parts. For example, if two editors sharing the same document with unsaved
 * changes are closed simultaneously, the user is only prompted to save the
 * changes once for the shared document, rather than once for each editor.
 * <p>
 * Workbench parts that work in terms of documents should implement
 * {@link IDocumentSource}.
 * </p>
 * 
 * @see IDocumentSource
 * @since 3.2
 */
public interface IDocument {

	/**
	 * Returns the name of this document for display purposes.
	 * 
	 * @return the document's name; never <code>null</code>.
	 */
	String getName();

	/**
	 * Returns the tool tip text for this document. This text is used to
	 * differentiate between two input with the same name. For instance,
	 * MyClass.java in folder X and MyClass.java in folder Y. The format of the
	 * text varies between input types.
	 * 
	 * @return the tool tip text; never <code>null</code>
	 */
	String getToolTipText();

	/**
	 * Returns the image descriptor for this document.
	 * 
	 * @return the image descriptor for this document; may be <code>null</code>
	 *         if there is no image
	 */
	public ImageDescriptor getImageDescriptor();

	/**
	 * Saves the contents of this document.
	 * <p>
	 * If the save is cancelled through user action, or for any other reason,
	 * the part should invoke <code>setCancelled</code> on the
	 * <code>IProgressMonitor</code> to inform the caller.
	 * </p>
	 * <p>
	 * This method is long-running; progress and cancellation are provided by
	 * the given progress monitor.
	 * </p>
	 * 
	 * @param monitor
	 *            the progress monitor
	 */
	void doSave(IProgressMonitor monitor);

	/**
	 * Returns whether the contents of this document have changed since the last
	 * save operation.
	 * <p>
	 * <b>Note:</b> this method is called frequently, for example by actions to
	 * determine their enabled status.
	 * </p>
	 * 
	 * @return <code>true</code> if the contents have been modified and need
	 *         saving, and <code>false</code> if they have not changed since
	 *         the last save
	 */
	boolean isDirty();

}
