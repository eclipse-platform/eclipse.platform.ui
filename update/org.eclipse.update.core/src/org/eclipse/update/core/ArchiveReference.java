package org.eclipse.update.core;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import java.net.URL;
import org.eclipse.update.core.IURLEntry;
import org.eclipse.update.core.model.ArchiveReferenceModel;
import org.eclipse.update.core.model.URLEntryModel;
import org.eclipse.update.internal.core.*;

/**
 * Default implementation of IURLEntry
 */

public class ArchiveReference extends ArchiveReferenceModel implements IArchiveEntry{

	/**
	 * Constructor for URLEntry
	 */
	public ArchiveReference() {
		super();
	}
	
	/*
	 * @see Object#toString()
	 */
	public String toString() {
		String result = "IURLEntry: ";
		 result = result +( (getPath()==null)?getURL().toExternalForm():getPath() + " : "+getURL().toExternalForm());
		return result;
	}

	
	}

