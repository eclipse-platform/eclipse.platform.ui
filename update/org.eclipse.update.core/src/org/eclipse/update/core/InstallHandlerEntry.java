package org.eclipse.update.core;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import org.eclipse.update.core.model.InstallHandlerEntryModel;

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