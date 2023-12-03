/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
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

package org.eclipse.ui.internal.ide.dialogs;

import org.eclipse.core.filesystem.IFileStore;

/**
 * IFileStoreFilter is an interface that defines a filter on file
 * stores.
 * @since 3.2
 */
public interface IFileStoreFilter {

	/**
	 * Return whether or not this store is accepted by the receiver.
	 * @param store IFileStore
	 * @return boolean <code>true</code> if this store is accepted.
	 */
	public abstract boolean accept(IFileStore store);

}
