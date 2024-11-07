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
 *     Jan-Hendrik Diederich, Bredex GmbH - bug 201052
 *******************************************************************************/
package org.eclipse.ui;

import org.eclipse.jface.resource.ImageDescriptor;

/**
 * An association between a file name/extension and a list of known editors for
 * files of that type.
 * <p>
 * The name and extension can never empty or null. The name may contain the
 * single wild card character (*) to indicate the editor applies to all files
 * with the same extension (e.g. *.doc). The name can never embed the wild card
 * character within itself (i.e. rep*)
 * </p>
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 *
 * @see IEditorRegistry#getFileEditorMappings
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IFileEditorMapping {
	/**
	 * Returns the default editor registered for this type mapping.
	 *
	 * @return the descriptor of the default editor, or <code>null</code> if there
	 *         is no default editor registered. Will also return <code>null</code>
	 *         if the default editor exists but fails the Expressions check.
	 */
	IEditorDescriptor getDefaultEditor();

	/**
	 * Returns the list of editors registered for this type mapping.
	 *
	 * @return a possibly empty list of editors.
	 */
	IEditorDescriptor[] getEditors();

	/**
	 * Returns the list of editors formerly registered for this type mapping which
	 * have since been deleted.
	 *
	 * @return a possibly empty list of editors
	 */
	IEditorDescriptor[] getDeletedEditors();

	/**
	 * Returns the file's extension for this type mapping.
	 *
	 * @return the extension for this mapping
	 */
	String getExtension();

	/**
	 * Returns the descriptor of the image to use for a file of this type.
	 * <p>
	 * The image is obtained from the default editor. A default file image is
	 * returned if no default editor is available.
	 * </p>
	 *
	 * @return the descriptor of the image to use for a resource of this type
	 */
	ImageDescriptor getImageDescriptor();

	/**
	 * Returns the label to use for this mapping. Labels have the form
	 * "<i>name.extension</i>".
	 *
	 * @return the label to use for this mapping
	 */
	String getLabel();

	/**
	 * Returns the file's name for this type mapping.
	 *
	 * @return the name for this mapping
	 */
	String getName();
}
