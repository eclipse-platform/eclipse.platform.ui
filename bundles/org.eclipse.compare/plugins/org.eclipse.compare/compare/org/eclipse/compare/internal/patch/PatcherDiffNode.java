/**
 * 
 */
package org.eclipse.compare.internal.patch;

import java.util.ArrayList;

import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.compare.structuremergeviewer.IDiffContainer;
import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.core.resources.IResource;

public class PatcherDiffNode extends DiffNode {
	
	public static final int HUNK = 0;
	public static final int DIFF = 1;
	public static final int PROJECT = 2;
	
	// DiffProject associated with this DiffNode
	private DiffProject diffProject;
	
	// Diff associated with this DiffNode
	private Diff diff = null;
	// Hunk associated with this DiffNode
	private Hunk hunk = null;

	// The name of this node
	private String name = null;

	//Used for retargetting
	private String originalName = null;
	private IResource newTarget = null;
	
	//whether to include this element - always initially true for a diff that has at least one hunk that can be applied
	boolean includeElement = true;
	
	private int type;
	
	public PatcherDiffNode(IDiffContainer parent, int kind,
			ITypedElement ancestor, ITypedElement left, ITypedElement right,
			Diff diff) {
		super(parent, kind, ancestor, left, right);
		this.diff = diff;
		includeElement = true;
		name = diff.getLabel(diff);
		originalName = name;
		type = DIFF;
	}

	public PatcherDiffNode(IDiffContainer parent, int kind,
			ITypedElement ancestor, ITypedElement left, ITypedElement right,
			Hunk hunk) {
		super(parent, kind, ancestor, left, right);
		this.hunk = hunk;
		includeElement = false;
		name = hunk.getLabel(hunk);
		originalName = name;
		type = HUNK;
	}

	public PatcherDiffNode(IDiffContainer parent, int kind, ITypedElement ancestor, ITypedElement left, ITypedElement right, DiffProject diffProject) {
		super(parent, kind, ancestor, left, right);
		this.diffProject = diffProject;
		includeElement = true;
		name = diffProject.getLabel(diffProject);
		originalName = name;
		type = PROJECT;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	Diff getDiff() {
		return diff;
	}

	Hunk getHunk() {
		return hunk;
	}
	
	DiffProject getDiffProject(){
		return diffProject;
	}
	
	public boolean isRetargeted(){
		return !name.equals(originalName);
	}
	
	public String getOriginalName(){
		return originalName;
	}
	
	public void setNewTarget(IResource resource){
		this.newTarget = resource;	
	}

	public IResource getNewTarget(){
		return newTarget;
	}

	public boolean getIncludeElement() {
		return includeElement;
	}

	public void setIncludeElement(boolean include) {
		this.includeElement = include;
		if (type == PROJECT){
			diffProject.setEnabled(include);
		} else if (type == DIFF){
			diff.setEnabled(include);
		} else if (type == HUNK){
			hunk.setEnabled(include);
		}
	}
	
	public int getPatchNodeType(){
		return type;
	}
	
	public IDiffElement[] getChildren() {
		if (type == PROJECT && !includeElement)
			return new IDiffElement[0];
		else if (type == PROJECT){
			//prune out diffs that don't have any hunks associated with them (because of retargeting)
			IDiffElement[] diffs = super.getChildren();
			ArrayList diffsToDisplay = new ArrayList();
			for (int i = 0; i < diffs.length; i++) {
				PatcherDiffNode diffNode = ((PatcherDiffNode )diffs[i]);
				if (diffNode.getPatchNodeType() == PatcherDiffNode.DIFF &&
					diffNode.getDiff().getHunks().length != 0){
					diffsToDisplay.add(diffs[i]);
				}
			}
			return (IDiffElement[]) diffsToDisplay.toArray(new IDiffElement[diffsToDisplay.size()]);
		}
		else if (type == DIFF && !includeElement)
			return new IDiffElement[0];
		
		return super.getChildren();
	}
}
