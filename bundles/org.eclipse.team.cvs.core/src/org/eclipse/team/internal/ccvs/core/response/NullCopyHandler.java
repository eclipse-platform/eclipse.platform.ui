package org.eclipse.team.internal.ccvs.core.response;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.team.internal.ccvs.core.resources.ICVSFile;

/**
 * This class is used for "cvs -n" silent operations which still send a Copy-file response
 */
public class NullCopyHandler extends CopyHandler {

	protected void copyFile(ICVSFile file, String target) {
		// Don't do the copy
	}
	
}
