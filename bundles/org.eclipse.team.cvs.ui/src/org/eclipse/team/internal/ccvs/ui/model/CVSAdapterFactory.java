package org.eclipse.team.internal.ccvs.ui.model;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
 
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.team.ccvs.core.ICVSFile;
import org.eclipse.team.ccvs.core.ICVSFolder;
import org.eclipse.team.ccvs.core.ICVSRemoteFile;
import org.eclipse.team.ccvs.core.ICVSRemoteFolder;
import org.eclipse.team.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.views.properties.IPropertySource;

public class CVSAdapterFactory implements IAdapterFactory {
	private Object fileAdapter = new RemoteFileElement();
	private Object folderAdapter = new RemoteFolderElement();
	private Object rootAdapter = new CVSRepositoryRootElement();

	// Property cache
	private Object cachedPropertyObject = null;
	private Object cachedPropertyValue = null;

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
		if (IPropertySource.class == adapterType) {
			return getPropertySource(adaptableObject);
		}
		return null;
	}
	/** (Non-javadoc)
	 * Method declared on IAdapterFactory.
	 */
	public Class[] getAdapterList() {
		return new Class[] {IWorkbenchAdapter.class, IPropertySource.class};
	}
	/**
	 * Returns the property source for the given object.  Caches
	 * the result because the property sheet is extremely inefficient,
	 * it asks for the source seven times in a row.
	 */
	public Object getPropertySource(Object adaptableObject) {
		if (adaptableObject == cachedPropertyObject) {
			return cachedPropertyValue;
		}
		cachedPropertyObject = adaptableObject;
		if (adaptableObject instanceof ICVSRemoteFile) {
			cachedPropertyValue = new CVSRemoteFilePropertySource((ICVSRemoteFile)adaptableObject);
		} else if (adaptableObject instanceof ICVSRemoteFolder) {
			cachedPropertyValue = new CVSRemoteFolderPropertySource((ICVSRemoteFolder)adaptableObject);
		} else if (adaptableObject instanceof ICVSRepositoryLocation) {
			cachedPropertyValue = new CVSRepositoryLocationPropertySource((ICVSRepositoryLocation)adaptableObject);
		} else {
			cachedPropertyValue = null;
		}
		return cachedPropertyValue;
	}
}
