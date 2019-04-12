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
package org.eclipse.ui.dialogs;

/**
 * Implementors of this interface answer one of the prescribed return codes when
 * asked whether to overwrite a certain path string (which could represent a
 * resource path, a file system path, etc).
 */
public interface IOverwriteQuery {
	/**
	 * Return code indicating the operation should be canceled.
	 */
	String CANCEL = "CANCEL"; //$NON-NLS-1$

	/**
	 * Return code indicating the entity should not be overwritten, but operation
	 * should not be canceled.
	 */
	String NO = "NO"; //$NON-NLS-1$

	/**
	 * Return code indicating the entity should be overwritten.
	 */
	String YES = "YES"; //$NON-NLS-1$

	/**
	 * Return code indicating the entity should be overwritten, and all subsequent
	 * entities should be overwritten without prompting.
	 */
	String ALL = "ALL"; //$NON-NLS-1$

	/**
	 * Return code indicating the entity should not be overwritten, and all
	 * subsequent entities should not be overwritten without prompting.
	 */
	String NO_ALL = "NOALL"; //$NON-NLS-1$

	/**
	 * Returns one of the return code constants declared on this interface,
	 * indicating whether the entity represented by the passed String should be
	 * overwritten.
	 * <p>
	 * This method may be called from a non-UI thread, in which case this method
	 * must run the query in a sync exec in the UI thread, if it needs to query the
	 * user.
	 * </p>
	 * 
	 * @param pathString the path representing the entity to be overwritten
	 * @return one of the return code constants declared on this interface
	 */
	String queryOverwrite(String pathString);
}
