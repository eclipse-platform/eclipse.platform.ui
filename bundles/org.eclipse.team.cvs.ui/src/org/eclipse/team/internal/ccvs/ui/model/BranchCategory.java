package org.eclipse.team.internal.ccvs.ui.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.ICVSUIConstants;
import org.eclipse.ui.model.IWorkbenchAdapter;
/**
 * BranchCategory is the model element for the branches category
 * for a particular repsitory in the repositories view. Its children
 * are the array of all known branch tags, other than HEAD, for the
 * given repository.
 */
public class BranchCategory extends CVSModelElement implements IAdaptable {
	private ICVSRepositoryLocation repository;
	
	/**
	 * TeamStreamsCategory constructor.
	 */
	public BranchCategory(ICVSRepositoryLocation repository) {
		super();
		this.repository = repository;
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
		CVSTag[] tags = CVSUIPlugin.getPlugin().getRepositoryManager().getKnownBranchTags(repository);
		BranchTag[] branchElements = new BranchTag[tags.length];
		for (int i = 0; i < tags.length; i++) {
			branchElements[i] = new BranchTag(tags[i], repository);
		}
		return branchElements;
	}
	
	/**
	 * Returns an image descriptor to be used for displaying an object in the workbench.
	 * Returns null if there is no appropriate image.
	 *
	 * @param object The object to get an image descriptor for.
	 */
	public ImageDescriptor getImageDescriptor(Object object) {
		return CVSUIPlugin.getPlugin().getImageDescriptor(ICVSUIConstants.IMG_BRANCHES_CATEGORY);
	}
	
	/**
	 * Returns the name of this element.  This will typically
	 * be used to assign a label to this object when displayed
	 * in the UI.  Returns an empty string if there is no appropriate
	 * name for this object.
	 *
	 * @param object The object to get a label for.
	 */
	public String getLabel(Object o) {
		return "Branches";
	}
	
	/**
	 * Returns the logical parent of the given object in its tree.
	 * Returns null if there is no parent, or if this object doesn't
	 * belong to a tree.
	 *
	 * @param object The object to get the parent for.
	 */
	public Object getParent(Object o) {
		return repository;
	}
	
	/**
	 * Return the repository the given element belongs to.
	 */
	public ICVSRepositoryLocation getRepository(Object o) {
		return repository;
	}
}
