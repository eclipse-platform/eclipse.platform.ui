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

/**
 * <b>Note:</b> This class/interface is part of an interim API that is still under 
 * development and expected to change significantly before reaching stability. 
 * It is being made available at this early stage to solicit feedback from pioneering 
 * adopters on the understanding that any code that uses this API will almost 
 * certainly be broken (repeatedly) as the API evolves.
 * 
 * A standard abstract class that provides implementations for <code>ILocalSyncElement</code>
 * methods.
 */
public abstract class LocalSyncElement implements ILocalSyncElement {

	/**
	 * Creates a client specific sync element from a <b>local</b> and <b>base</b>
	 * resources. The <b>base</b> resource may be <code>null</code> and should be
	 * intialized by the client if available.
	 * 
	 * @param local the local resource in the workbench. Will never be <code>null</code>.
	 * @param base the base resource, may me <code>null</code>.
	 * @param data client specific data.
	 * 
	 * @return a client specific sync element.
	 */
	public abstract ILocalSyncElement create(IResource local, IRemoteResource base, Object data);
	
	/**
	 * Client data that is passed to every <code>create()</code> call.
	 * 
	 * @return client specific data that will be passed to create.
	 */
	protected abstract Object getData();
	
	/**
	 * Client can decide is a specific element should be ignored from this sync element's
	 * children.
	 * 
	 * @param resource the resource to be queried.
	 * 
	 * @return <code>true</code> if this element should be ignored and not considered an 
	 * immediate child of this element, and <code>false</code> otherwise.
	 */
	protected abstract boolean isIgnored(IResource resource);

	/*
	 * @see ILocalSyncElement#getSyncKind(int, IProgressMonitor)
	 */
	public int getSyncKind(int granularity, IProgressMonitor progress) {
				
		// XXX not sure how local sync will be used?
		int sync = IN_SYNC;		
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
					// the base is initialy set to null, however the concrete subclass should
					// initialize the base if one is available.
					if(!isIgnored(iResource)) {
						syncElements.add(create(iResource, null, getData()));
					}
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