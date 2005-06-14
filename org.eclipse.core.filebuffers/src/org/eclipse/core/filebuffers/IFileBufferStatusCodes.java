/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.filebuffers;

/**
 * This interface provides the list of status codes that are used by the file
 * buffer plug-in when it throws {@link org.eclipse.core.runtime.CoreException}.
 * <p>
 * Clients are not supposed to implement that interface.
 * </p>
 *
 * @since 3.1
 */
public interface IFileBufferStatusCodes {

	/**
	 * Changing the content of a file buffer failed.
	 */
	int CONTENT_CHANGE_FAILED= 1;

	/**
	 * Creation of file buffer failed.
	 */
	int CREATION_FAILED= 2;
}
