package org.eclipse.team.core.sync;
  
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;

public abstract class LocalSyncElement implements ILocalSyncElement {

	/*
	 * @see ILocalSyncElement#getSyncKind(int, IProgressMonitor)
	 */
	public int getSyncKind(int granularity, IProgressMonitor progress) {
		IRemoteResource base = getBase();
		IResource local = getLocal();
		
		int sync = IN_SYNC;
		
		if(isDirty()) {
				if(hasRemote()) {
					sync = OUTGOING | CHANGE;	
				} else {
					sync = OUTGOING | ADDITION;
				}
		}
		return sync;
	}	
}