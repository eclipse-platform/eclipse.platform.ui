package org.eclipse.team.internal.ccvs.core.resources;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.internal.ccvs.core.CVSException;
 
 /**
  * Same as a RemoteFile except that the tag is fixed to a particular revision
  */
public class RemoteFileRevision extends RemoteFile {

	/**
	 * Constructor for RemoteFileRevision.
	 * @param parent
	 * @param name
	 * @param tag
	 */
	protected RemoteFileRevision(RemoteFolder parent, String name, String tag) {
		super(parent, name, tag);
	}
	
	/**
	 * @see IRemoteFile#getRevision()
	 */
	public String getRevision(IProgressMonitor monitor) throws CVSException {
		return tag;
	}
	
	public String getRevision() {
		return tag;
	}

}

