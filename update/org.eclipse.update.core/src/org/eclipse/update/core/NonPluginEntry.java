package org.eclipse.update.core;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
 
import org.eclipse.update.core.INonPluginEntry;
import org.eclipse.update.core.VersionedIdentifier;
import org.eclipse.update.core.model.NonPluginEntryModel;

/**
 * Convenience implementation of non-plug-in entry.
 * <p>
 * This class may be instantiated or subclassed by clients.
 * </p> 
 * @see org.eclipse.update.core.INonPluginEntry
 * @see org.eclipse.update.core.model.NonPluginEntryModel
 * @since 2.0
 */
public class NonPluginEntry extends NonPluginEntryModel implements INonPluginEntry {
	
	/**
	 * Non-plug-in entry default constructor
	 */
	public NonPluginEntry() {
		super();
	}
}

