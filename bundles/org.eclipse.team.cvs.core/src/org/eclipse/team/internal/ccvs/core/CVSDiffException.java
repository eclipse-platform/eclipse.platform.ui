package org.eclipse.team.internal.ccvs.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;

/**
 * This is a special Exception for the diff command and is
 * thrown when there is a difference between the two
 * compared files (this is the default behavior of the
 * diff-command)
 */
public class CVSDiffException extends CVSException {

	public CVSDiffException() {
		super(Policy.bind(("CVSDiffException.message")));
	}


}

