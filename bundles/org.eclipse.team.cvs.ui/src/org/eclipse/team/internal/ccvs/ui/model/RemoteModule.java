package org.eclipse.team.internal.ccvs.ui.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.core.runtime.CoreException;import org.eclipse.core.runtime.IAdaptable;import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;import org.eclipse.swt.custom.BusyIndicator;import org.eclipse.swt.widgets.Display;import org.eclipse.team.ccvs.core.ICVSRemoteFolder;
import org.eclipse.team.ccvs.core.ICVSRemoteResource;
import org.eclipse.team.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.ICVSUIConstants;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * This class represents an IProject resource in a repository.  The
 * children of a RemoteModule are its versions. A RemoteModule is
 * a child of the VersionsCategory.
 */
public class RemoteModule extends CVSModelElement implements IAdaptable {
	ICVSRemoteFolder folder;
	VersionCategory parent;
	
	/**
	 * RemoteProject constructor.
	 */
	public RemoteModule(ICVSRemoteFolder folder, VersionCategory parent) {
		this.folder = folder;
		this.parent = parent;
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
	 * object has no children. The children of the RemoteModule
	 * are the versions for that 
	 */
	public Object[] getChildren(Object o) {
		String[] tags = CVSUIPlugin.getPlugin().getRepositoryManager().getKnownVersionTags(folder);
		ModuleVersion[] result = new ModuleVersion[tags.length];
		for (int i = 0; i < result.length; i++) {
			String tag = tags[i];
			// There should be a better way of getting the resource with a given tag.
			try {
				ICVSRemoteResource[] members = folder.getRepository().members(tag, new NullProgressMonitor());
				String name = folder.getName();
				for (int j = 0; j < members.length; j++) {
					if (members[j].getName().equals(name)) {
						result[i] = new ModuleVersion((ICVSRemoteFolder)members[j], tag, this);
					}
				}
			} catch (TeamException e) {
				CVSUIPlugin.log(e.getStatus());
				return null;
			}
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
		return folder.getName();
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
		return folder.getRepository();
	}
	
	/** (Non-javadoc)
	 * For debugging purposes only.
	 */
	public String toString() {
		return "RemoteModule(" + folder.getName() + ")";
	}
	
	public ICVSRemoteResource getCVSResource() {
		return folder;
	}
}
