/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.compare.internal;

import org.eclipse.jface.viewers.ISelection;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.compare.*;
import org.eclipse.compare.structuremergeviewer.*;


/**
 * A two-way or three-way compare for arbitrary IResources.
 */
class ResourceCompareInput extends CompareEditorInput {
	
	private static final boolean NORMALIZE_CASE= true;
	
	private boolean fThreeWay= false;
	private IStructureComparator fAncestor;
	private IStructureComparator fLeft;
	private IStructureComparator fRight;
	private IResource fAncestorResource;
	private IResource fLeftResource;
	private IResource fRightResource;
	
	
	private class MyDiffNode extends DiffNode {
		public MyDiffNode(IDiffContainer parent, int description, ITypedElement ancestor, ITypedElement left, ITypedElement right) {
			super(parent, description, ancestor, left, right);
		}
		protected void fireChange() {
			super.fireChange();
			setDirty(true);
		}
	}
	
	/**
	 * Creates an compare editor input for the given selection.
	 */
	ResourceCompareInput(CompareConfiguration config) {
		super(config);
	}
		
	/**
	 * Returns true if compare can be executed for the given selection.
	 */
	boolean setSelection(ISelection s) {

		IResource[] selection= Utilities.getResources(s);
		if (selection.length < 2 || selection.length > 3)
			return false;

		fThreeWay= selection.length == 3;
		
		fLeftResource= selection[0];
		fRightResource= selection[1];
		if (fThreeWay) {
			fLeftResource= selection[1];		
			fRightResource= selection[2];
		}
		
		fAncestor= null;
		fLeft= getStructure(fLeftResource);
		fRight= getStructure(fRightResource);
					
		if (incomparable(fLeft, fRight))
			return false;

		if (fThreeWay) {
			fAncestorResource= selection[0];
			fAncestor= getStructure(fAncestorResource);
			
			if (incomparable(fAncestor, fRight))
				return false;
		}

		return true;
	}
	
	/**
	 * Returns true if the given arguments cannot be compared.
	 */
	private boolean incomparable(IStructureComparator c1, IStructureComparator c2) {
		if (c1 == null || c2 == null)
			return true;
		return isLeaf(c1) != isLeaf(c2);
	}
	
	/**
	 * Returns true if the given arguments is a leaf.
	 */
	private boolean isLeaf(IStructureComparator c) {
		if (c instanceof ITypedElement) {
			ITypedElement te= (ITypedElement) c;
			return !ITypedElement.FOLDER_TYPE.equals(te.getType());
		}
		return false;
	}
	
	/**
	 * Creates a <code>IStructureComparator</code> for the given input.
	 * Returns <code>null</code> if no <code>IStructureComparator</code>
	 * can be found for the <code>IResource</code>.
	 */
	private IStructureComparator getStructure(IResource input) {
		
		if (input instanceof IContainer)
			return new ResourceNode(input);
			
		if (input instanceof IFile) {
			ResourceNode rn= new ResourceNode(input);
			IFile file= (IFile) input;
			String type= normalizeCase(file.getFileExtension());
			if ("JAR".equals(type) || "ZIP".equals(type))
				return new ZipStructureCreator().getStructure(rn);
			return rn;
		}
		return null;
	}
	
	/**
	 * Performs a two-way or three-way diff on the current selection.
	 */
	public Object prepareInput(IProgressMonitor pm) {
				
		CompareConfiguration cc= (CompareConfiguration) getCompareConfiguration();
	
		String leftLabel= fLeftResource.getName();
		cc.setLeftLabel(leftLabel);
		cc.setLeftImage(CompareUIPlugin.getImage(fLeftResource));
		
		String rightLabel= fRightResource.getName();
		cc.setRightLabel(rightLabel);
		cc.setRightImage(CompareUIPlugin.getImage(fRightResource));
		
		StringBuffer title= new StringBuffer();
		title.append("Compare (");
		if (fThreeWay) {			
			String ancestorLabel= fAncestorResource.getName();
			cc.setAncestorLabel(ancestorLabel);
			cc.setAncestorImage(CompareUIPlugin.getImage(fAncestorResource));
			title.append(ancestorLabel);
			title.append("-");
		}
		title.append(leftLabel);
		title.append("-");
		title.append(rightLabel);
		title.append(")");
		setTitle(title.toString());
			
		try {													
			pm.beginTask("Operation in Progress...", IProgressMonitor.UNKNOWN);

			Differencer d= new Differencer() {
				protected Object visit(Object parent, int description, Object ancestor, Object left, Object right) {
					return new MyDiffNode((IDiffContainer) parent, description, (ITypedElement)ancestor, (ITypedElement)left, (ITypedElement)right);
				}
			};
			
			return d.findDifferences(fThreeWay, pm, null, fAncestor, fLeft, fRight);
		
		} finally {
			pm.done();
		}
	}
	
	private static String normalizeCase(String s) {
		if (NORMALIZE_CASE && s != null)
			return s.toUpperCase();
		return s;
	}
}

