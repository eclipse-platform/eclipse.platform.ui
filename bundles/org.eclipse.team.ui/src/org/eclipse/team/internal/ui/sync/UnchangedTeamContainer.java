package org.eclipse.team.internal.ui.sync;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.compare.CompareUI;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.compare.structuremergeviewer.Differencer;
import org.eclipse.compare.structuremergeviewer.IDiffContainer;
import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.core.resources.IResource;
import org.eclipse.swt.graphics.Image;

/**
 * A node in a diff tree that represents a folder with no changes
 * to itself, it is only a placeholder for changes in its children.
 */
public class UnchangedTeamContainer extends DiffNode implements ITeamNode {
	private IResource resource;
	protected SyncCompareInput diffModel;
	
	public UnchangedTeamContainer(SyncCompareInput input, IDiffContainer parent, IResource resource) {
		this(input, parent, resource, Differencer.NO_CHANGE);
	}
	
	public UnchangedTeamContainer(SyncCompareInput input, IDiffContainer parent, IResource resource, int description) {
		super(parent, description);
		this.resource = resource;
		this.diffModel =input;
	}
	
	/*
	 * Method declared on ITeamNode
	 */
	public int getChangeDirection() {
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

	/**
	 * For debugging purposes only.
	 */
	public String toString() {
		return "UnchangedTeamContainer(" + resource.getName() + ")";
	}
}
