package org.eclipse.update.core;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import org.eclipse.update.core.model.ArchiveReferenceModel;

/**
 * Default implementation of IArchiveReference
 */

public class ArchiveReference extends ArchiveReferenceModel implements IArchiveReference{

	/**
	 * Constructor for ArchiveReference
	 */
	public ArchiveReference() {
		super();
	}
	
	/*
	 * @see Object#toString()
	 */
	public String toString() {
		String result = "IArchiveReference: "; //$NON-NLS-1$
		 result = result +( (getPath()==null)?getURL().toExternalForm():getPath() + " : "+getURL().toExternalForm()); //$NON-NLS-1$
		return result;
	}

	
	}

