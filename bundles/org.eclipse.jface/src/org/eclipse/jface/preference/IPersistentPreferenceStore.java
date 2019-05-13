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
package org.eclipse.jface.preference;

import java.io.IOException;

/**
 * IPersistentPreferenceStore is a preference store that can
 * be saved.
 */
public interface IPersistentPreferenceStore extends IPreferenceStore {

	/**
	 * Saves the non-default-valued preferences known to this preference
	 * store to the file from which they were originally loaded.
	 *
	 * @exception java.io.IOException if there is a problem saving this store
	 */
	public void save() throws IOException;

}
