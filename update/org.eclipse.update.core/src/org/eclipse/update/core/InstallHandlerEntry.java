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
 * Convenience implementation of an install handler entry.
 * <p>
 * This class may be instantiated or subclassed by clients.
 * </p> 
 * @see org.eclipse.update.core.IInstallHandlerEntry
 * @see org.eclipse.update.core.model.InstallHandlerEntryModel
 * @see org.eclipse.update.core.IInstallHandler
 * @since 2.0
 */
public class InstallHandlerEntry
	extends InstallHandlerEntryModel
	implements IInstallHandlerEntry {

	/**
	 * Constructor for InstallHandlerEntry.
	 * @since 2.0
	 */
	public InstallHandlerEntry() {
		super();
	}
}
