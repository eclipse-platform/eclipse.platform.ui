package org.eclipse.team.internal.ui.sync;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
 
import org.eclipse.compare.CompareUI;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.ResourceNode;
import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.compare.structuremergeviewer.Differencer;
import org.eclipse.compare.structuremergeviewer.IDiffContainer;
import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.core.resources.IResource;
import org.eclipse.swt.graphics.Image;

/**
 * <b>Note:</b> This class/interface is part of an interim API that is still under 
 * development and expected to change significantly before reaching stability. 
 * It is being made available at this early stage to solicit feedback from pioneering 
 * adopters on the understanding that any code that uses this API will almost 
 * certainly be broken (repeatedly) as the API evolves.
 * 
 * A node in a diff tree that represents a folder with no changes
 * to itself, it is only a placeholder for changes in its children.
 */
public class UnchangedTeamContainer extends DiffNode implements ITeamNode {
	private IResource resource;
	
	public UnchangedTeamContainer(IDiffContainer parent, IResource resource) {
		this(parent, resource, Differencer.NO_CHANGE);
	}
	
	public UnchangedTeamContainer(IDiffContainer parent, IResource resource, int description) {
		super(parent, description);
		setLeft(new ResourceNode(resource));
		this.resource = resource;
	}
	
	/*
	 * Method declared on ITeamNode
	 */
	public int getChangeDirection() {
		return ITeamNode.NO_CHANGE;
	}
	
	/*
	 * @see ITeamNode#getChangeType()
	 */
	public int getChangeType() {
		return ITeamNode.NO_CHANGE;
	}
	
	public Image getImage() {
		return CompareUI.getImage(getType());
	}
	
	public String getName() {
		return resource.getName();
	}
	
	/**
	 * Returns the resource underlying this diff node.
	 */
	public IResource getResource() {
		return resource;
	}

	public String getType() {
		return ITypedElement.FOLDER_TYPE;
	}	
}
