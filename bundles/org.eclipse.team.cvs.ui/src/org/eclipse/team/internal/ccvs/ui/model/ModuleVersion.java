package org.eclipse.team.internal.ccvs.ui.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.team.ccvs.core.CVSTag;
import org.eclipse.team.ccvs.core.ICVSRemoteFolder;
import org.eclipse.team.ccvs.core.ICVSRemoteResource;
import org.eclipse.team.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * This class represents a version of a module in the repository.
 * The children are remote resources.
 */
public class ModuleVersion extends CVSModelElement implements IAdaptable {
	ICVSRemoteFolder resource;
	CVSTag tag;
	RemoteModule parent;
		
	/**
	 * RemoteProject constructor.
	 */
	public ModuleVersion(ICVSRemoteFolder resource, CVSTag tag, RemoteModule parent) {
		this.resource = resource;
		this.tag = tag;
		this.parent = parent;
	}
	
	public CVSTag getTag() {
		return tag;
	}
	public ICVSRemoteResource getCVSResource() {
		return resource;
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
		try {
			return resource.members(new NullProgressMonitor());
		} catch (TeamException e) {
			CVSUIPlugin.log(e.getStatus());
			return null;
		}
	}
	
	/**
	 * Returns an image to be used for displaying an object in the desktop.
	 * @param object The object to get an image for.
	 * @param owner The viewer that the image will be used in.  The image will
	 * be disposed when this viewer is closed.  If the owner is null, a new 
	 * image is returned, and the caller is responsible for disposing it.
	 */
	public ImageDescriptor getImageDescriptor(Object object) {
		return PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJ_FOLDER);
	}
	
	/**
	 * Returns the name of this element.  This will typically
	 * be used to assign a label to this object when displayed
	 * in the UI.
	 */
	public String getLabel(Object o) {
		return resource.getName() + " " + tag.getName();
	}
	
	/**
	 * Returns the logical parent of the given object in its tree.
	 */
	public Object getParent(Object o) {
		return parent;
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
		return "ProjectVersion(" + resource.getName() + ")";
	}
}
