package org.eclipse.team.core.sync;
  
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.IOException;
import java.io.InputStream;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.TeamException;

public abstract class LocalSyncElement implements ILocalSyncElement {

	/*
	 * Helper method to create a remote sync element.
	 */
	public abstract ILocalSyncElement create(IResource local, IRemoteResource base, Object data);
	protected abstract Object getData();

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
	
	
	
	/*
	 * @see ILocalSyncElement#getName()
	 */
	public String getName() {
		return getLocal().getName();
	}

	/*
	 * @see ILocalSyncElement#isContainer()
	 */
	public boolean isContainer() {
		return getLocal().getType() != IResource.FILE;
	}

	/*
	 * @see ILocalSyncElement#members(IProgressMonitor)
	 */
	public ILocalSyncElement[] members(IProgressMonitor monitor) throws TeamException {
		try {
			if(getLocal().getType() != IResource.FILE) {
				IResource[] members = ((IContainer)getLocal()).members();
				List syncElements = new ArrayList(5);
				for (int i = 0; i < members.length; i++) {
					IResource iResource = members[i];
					// XXX: not sure what to do about the base?
					syncElements.add(create(iResource, null, getData()));
				}
				return (ILocalSyncElement[]) syncElements.toArray(new ILocalSyncElement[syncElements.size()]);		
			} else {
				return new ILocalSyncElement[0];
			}
		} catch(CoreException e) {
			throw new TeamException(e.getStatus());
		}
	}
}