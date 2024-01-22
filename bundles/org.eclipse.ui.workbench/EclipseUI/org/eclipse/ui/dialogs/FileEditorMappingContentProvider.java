/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
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
package org.eclipse.ui.dialogs;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.ui.IFileEditorMapping;

/**
 * A content provider for displaying of <code>IFileEditorMapping</code> objects
 * in viewers.
 * <p>
 * This class has a singleton instance,
 * <code>FileEditorMappingContentProvider.INSTANCE</code>, which can be used any
 * place this kind of content provider is needed.
 * </p>
 *
 * @see org.eclipse.jface.viewers.IContentProvider
 */
public class FileEditorMappingContentProvider implements IStructuredContentProvider {

	/**
	 * Singleton instance accessor.
	 */
	public static final FileEditorMappingContentProvider INSTANCE = new FileEditorMappingContentProvider();

	/**
	 * Creates an instance of this class. The private visibility of this constructor
	 * ensures that this class is only usable as a singleton.
	 */
	private FileEditorMappingContentProvider() {
		super();
	}

	@Override
	public Object[] getElements(Object element) {
		IFileEditorMapping[] array = (IFileEditorMapping[]) element;
		return array == null ? new Object[0] : array;
	}

}
