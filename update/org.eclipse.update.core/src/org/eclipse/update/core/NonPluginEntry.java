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
package org.eclipse.update.core;

import org.eclipse.update.core.model.*;

/**
 * Convenience implementation of non-plug-in entry.
 * <p>
 * This class may be instantiated or subclassed by clients.
 * </p> 
 * @see org.eclipse.update.core.INonPluginEntry
 * @see org.eclipse.update.core.model.NonPluginEntryModel
 * @since 2.0
 */
public class NonPluginEntry
	extends NonPluginEntryModel
	implements INonPluginEntry {

	/**
	 * Non-plug-in entry default constructor
	 */
	public NonPluginEntry() {
		super();
	}
}
