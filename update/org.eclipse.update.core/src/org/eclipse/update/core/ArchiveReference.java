package org.eclipse.update.core;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import org.eclipse.update.core.model.ArchiveReferenceModel;

/**
 * Convenience implementation of a site archive.
 * <p>
 * This class may be instantiated or subclassed by clients.
 * </p> 
 * @see org.eclipse.update.core.IArchiveReference
 * @see org.eclipse.update.core.model.ArchiveReferenceModel
 * @since 2.0
 */
public class ArchiveReference
	extends ArchiveReferenceModel
	implements IArchiveReference {

	/**
	 * Constructor for ArchiveReference
	 * @since 2.0
	 */
	public ArchiveReference() {
		super();
	}

	/**
	 * @see Object#toString()
	 * @since 2.0
	 */
	public String toString() {
		String result = "IArchiveReference: "; //$NON-NLS-1$
		result =
			result
				+ ((getPath() == null)
					? getURL().toExternalForm()
					: getPath() + " : " + getURL().toExternalForm());
		//$NON-NLS-1$
		return result;
	}
}