package org.eclipse.team.core.target;

import java.net.URL;

import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.sync.IRemoteResource;

public interface IRemoteTargetResource extends IRemoteResource {
	/**
	 * Returns the URL of this remote resource.
	 */
	public URL getURL() throws TeamException;
	
	/**
	 * Returns the size of the resource. 
	 */
	public int getSize() throws TeamException;
}
