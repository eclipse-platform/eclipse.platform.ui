package org.eclipse.team.internal.ccvs.ui.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.core.runtime.CoreException;import org.eclipse.core.runtime.IAdaptable;import org.eclipse.jface.resource.ImageDescriptor;import org.eclipse.swt.custom.BusyIndicator;import org.eclipse.swt.widgets.Display;import org.eclipse.team.ccvs.core.ICVSRemoteFolder;
import org.eclipse.team.ccvs.core.ICVSRemoteResource;
import org.eclipse.team.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.ICVSUIConstants;
import org.eclipse.ui.model.IWorkbenchAdapter;
/**
 * This class represents an IProject resource in a repository.  The
 * children of a RemoteProject are its versions.
 */
public class RemoteProject extends CVSModelElement implements IAdaptable {
	ICVSRemoteFolder resource;
	VersionCategory category;
	
	/**
	 * RemoteProject constructor.
	 */
	public RemoteProject(ICVSRemoteFolder resource, VersionCategory category) {
		this.resource = resource;
		this.category = category;
	}
	
	/**
	 * Returns an object which is an instance of the given class
	 * associated with this object. Returns <code>null</code> if
	 * no such object can be found.
	 */
	public Object getAdapter(Class adapter) {
		if (adapter == IWorkbenchAdapter.class) return this;
		return null;
	}
	
	/**
	 * Returns the children of this object.  When this object
	 * is displayed in a tree, the returned objects will be this
	 * element's children.  Returns an empty enumeration if this
	 * object has no children.
	 */
	public Object[] getChildren(Object o) {
		// This will be improved; for now, just get all version tags for the repository
		Tag[] tags = CVSUIPlugin.getPlugin().getRepositoryManager().getKnownVersionTags(resource.getRepository());
		ProjectVersion[] result = new ProjectVersion[tags.length];
		for (int i = 0; i < result.length; i++) {
			String tag = tags[i].getTag();
			resource.setTag(tag);
			result[i] = new ProjectVersion(resource, tag, this);
		}
		return result;
	}
	
	/**
	 * Returns an image to be used for displaying an object in the desktop.
	 * @param object The object to get an image for.
	 * @param owner The viewer that the image will be used in.  The image will
	 * be disposed when this viewer is closed.  If the owner is null, a new 
	 * image is returned, and the caller is responsible for disposing it.
	 */
	public ImageDescriptor getImageDescriptor(Object object) {
		return CVSUIPlugin.getPlugin().getImageDescriptor(ICVSUIConstants.IMG_PROJECT_VERSION);
	}
	
	/**
	 * Returns the name of this element.  This will typically
	 * be used to assign a label to this object when displayed
	 * in the UI.
	 */
	public String getLabel(Object o) {
		return resource.getName();
	}
	
	/**
	 * Returns the logical parent of the given object in its tree.
	 */
	public Object getParent(Object o) {
		return category;
	}

	/**
	 * Return the repository the given element belongs to.
	 */
	public ICVSRepositoryLocation getRepository(Object o) {
		return resource.getRepository();
	}
	
	/** (Non-javadoc)
	 * For debugging purposes only.
	 */
	public String toString() {
		return "RemoteProject(" + resource.getName() + ")";
	}
}
