package org.eclipse.team.internal.ccvs.ui.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.team.ccvs.core.ICVSRemoteFile;
import org.eclipse.team.ccvs.core.ICVSRemoteFolder;
import org.eclipse.team.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.ui.model.IWorkbenchAdapter;

public class CVSAdapterFactory implements IAdapterFactory {
	private Object fileAdapter = new RemoteFileElement();
	private Object folderAdapter = new RemoteFolderElement();
	private Object rootAdapter = new CVSRepositoryRootElement();

	/** (Non-javadoc)
	 * Method declared on IAdapterFactory.
	 */
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (IWorkbenchAdapter.class == adapterType) {
			if (adaptableObject instanceof ICVSRemoteFile) {
				return fileAdapter;
			} else if (adaptableObject instanceof ICVSRepositoryLocation) {
				return rootAdapter;
			} else if (adaptableObject instanceof ICVSRemoteFolder) {
				return folderAdapter;
			}
			return null;
		}
		return null;
	}
	/** (Non-javadoc)
	 * Method declared on IAdapterFactory.
	 */
	public Class[] getAdapterList() {
		return new Class[] {IWorkbenchAdapter.class};
	}
}
