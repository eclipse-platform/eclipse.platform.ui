/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * <code>IEditorInput</code> is a light weight descriptor of editor input, like
 * a file name but more abstract. It is not a model. It is a description of the
 * model source for an <code>IEditorPart</code>.
 * <p>
 * Clients implementing this editor input interface should override
 * <code>Object.equals(Object)</code> to answer true for two inputs that are the
 * same. The <code>IWorkbenchPage.openEditor</code> APIs are dependent on this
 * to find an editor with the same input.
 * </p>
 * <p>
 * Clients should extend this interface to declare new types of editor inputs.
 * </p>
 * <p>
 * An editor input is passed to an editor via the <code>IEditorPart.init</code>
 * method. Due to the wide range of valid editor inputs, it is not possible to
 * define generic methods for getting and setting bytes.
 * </p>
 * <p>
 * Editor input must implement the <code>IAdaptable</code> interface; extensions
 * are managed by the platform's adapter manager.
 * </p>
 * <p>
 * Please note that it is important that the editor input be light weight.
 * Within the workbench, the navigation history tends to hold on to editor
 * inputs as a means of reconstructing the editor at a later time. The
 * navigation history can hold on to quite a few inputs (i.e., the default is
 * fifty). The actual data model should probably not be held in the input.
 * </p>
 *
 *
 * @see org.eclipse.ui.IEditorPart
 * @see org.eclipse.ui.IWorkbenchPage#openEditor(IEditorInput, String)
 * @see org.eclipse.ui.IWorkbenchPage#openEditor(IEditorInput, String, boolean)
 */
public interface IEditorInput extends IAdaptable {
	/**
	 * Returns whether the editor input exists.
	 * <p>
	 * This method is primarily used to determine if an editor input should appear
	 * in the "File Most Recently Used" menu. An editor input will appear in the
	 * list until the return value of <code>exists</code> becomes <code>false</code>
	 * or it drops off the bottom of the list.
	 *
	 * @return <code>true</code> if the editor input exists; <code>false</code>
	 *         otherwise
	 */
	boolean exists();

	/**
	 * Returns the image descriptor for this input.
	 *
	 * <p>
	 * Note: although a null return value has never been permitted from this method,
	 * there are many known buggy implementations that return null. Clients that
	 * need the image for an editor are advised to use IWorkbenchPart.getImage()
	 * instead of IEditorInput.getImageDescriptor(), or to recover from a null
	 * return value in a manner that records the ID of the problematic editor input.
	 * Implementors that have been returning null from this method should pick some
	 * other default return value (such as
	 * ImageDescriptor.getMissingImageDescriptor()).
	 * </p>
	 *
	 * @return the image descriptor for this input; may be <code>null</code> if
	 *         there is no image.
	 */
	ImageDescriptor getImageDescriptor();

	/**
	 * Returns the name of this editor input for display purposes.
	 * <p>
	 * For instance, when the input is from a file, the return value would
	 * ordinarily be just the file name.
	 *
	 * @return the name string; never <code>null</code>;
	 */
	String getName();

	/**
	 * Returns an object that can be used to save the state of this editor input.
	 *
	 * @return the persistable element, or <code>null</code> if this editor input
	 *         cannot be persisted
	 */
	IPersistableElement getPersistable();

	/**
	 * Returns the tool tip text for this editor input. This text is used to
	 * differentiate between two input with the same name. For instance,
	 * MyClass.java in folder X and MyClass.java in folder Y. The format of the text
	 * varies between input types.
	 *
	 * @return the tool tip text; never <code>null</code>.
	 */
	String getToolTipText();
}
