/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.compare.internal;

import java.text.MessageFormat;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.swt.widgets.Composite;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.Viewer;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

import org.eclipse.compare.*;
import org.eclipse.compare.structuremergeviewer.*;


/**
 * A two-way or three-way compare for arbitrary IResources.
 */
class ResourceCompareInput extends CompareEditorInput {
	
	private static final boolean NORMALIZE_CASE= true;
	
	private boolean fThreeWay= false;
	private Object fRoot;
	private IStructureComparator fAncestor;
	private IStructureComparator fLeft;
	private IStructureComparator fRight;
	private IResource fAncestorResource;
	private IResource fLeftResource;
	private IResource fRightResource;
	private DiffTreeViewer fDiffViewer;
	
	
	class MyDiffNode extends DiffNode {
		
		private boolean fDirty= false;
		private ITypedElement fLastId;
		private String fLastName;
		
		
		public MyDiffNode(IDiffContainer parent, int description, ITypedElement ancestor, ITypedElement left, ITypedElement right) {
			super(parent, description, ancestor, left, right);
		}
		public void fireChange() {
			super.fireChange();
			setDirty(true);
			fDirty= true;
			if (fDiffViewer != null)
				fDiffViewer.refresh(this);
		}
		void clearDirty() {
			fDirty= false;
		}
		public String getName() {
			if (fLastName == null)
				fLastName= super.getName();
			if (fDirty)
				return '<' + fLastName + '>';
			return fLastName;
		}
		
		public ITypedElement getId() {
			ITypedElement id= super.getId();
			if (id == null)
				return fLastId;
			fLastId= id;
			return id;
		}
	}
	
	/**
	 * Creates an compare editor input for the given selection.
	 */
	ResourceCompareInput(CompareConfiguration config) {
		super(config);
	}
		
	public Viewer createDiffViewer(Composite parent) {
		Viewer v= super.createDiffViewer(parent);
		if (v instanceof DiffTreeViewer)
			fDiffViewer= (DiffTreeViewer) v;
		return v;
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
			return new BufferedResourceNode(input);
			
		if (input instanceof IFile) {
			IStructureComparator rn= new BufferedResourceNode(input);
			IFile file= (IFile) input;
			String type= normalizeCase(file.getFileExtension());
			if ("JAR".equals(type) || "ZIP".equals(type)) //$NON-NLS-2$ //$NON-NLS-1$
				return new ZipFileStructureCreator().getStructure(rn);
			return rn;
		}
		return null;
	}
	
	/**
	 * Performs a two-way or three-way diff on the current selection.
	 */
	public Object prepareInput(IProgressMonitor pm) throws InvocationTargetException {
				
		CompareConfiguration cc= (CompareConfiguration) getCompareConfiguration();
	
		try {
			// fix for PR 1GFMLFB: ITPUI:WIN2000 - files that are out of sync with the file system appear as empty							
			fLeftResource.refreshLocal(IResource.DEPTH_INFINITE, pm);
			fRightResource.refreshLocal(IResource.DEPTH_INFINITE, pm);
			if (fThreeWay && fAncestorResource != null)
				fAncestorResource.refreshLocal(IResource.DEPTH_INFINITE, pm);
			// end fix						
				
			pm.beginTask(Utilities.getString("ResourceCompare.taskName"), IProgressMonitor.UNKNOWN); //$NON-NLS-1$

			cc.setLeftLabel(buildLabel(fLeftResource));
			cc.setLeftImage(CompareUIPlugin.getImage(fLeftResource));
			
			cc.setRightLabel(buildLabel(fRightResource));
			cc.setRightImage(CompareUIPlugin.getImage(fRightResource));

			String leftLabel= fLeftResource.getName();
			String rightLabel= fRightResource.getName();
			
			String title;
			if (fThreeWay) {			
				cc.setAncestorLabel(buildLabel(fAncestorResource));
				cc.setAncestorImage(CompareUIPlugin.getImage(fAncestorResource));
				String format= Utilities.getString("ResourceCompare.threeWay.title"); //$NON-NLS-1$
				String ancestorLabel= fAncestorResource.getName();
				title= MessageFormat.format(format, new String[] {ancestorLabel, leftLabel, rightLabel} );
			} else {
				String format= Utilities.getString("ResourceCompare.twoWay.title"); //$NON-NLS-1$
				title= MessageFormat.format(format, new String[] {leftLabel, rightLabel} );
			}
			setTitle(title);
			
			Differencer d= new Differencer() {
				protected Object visit(Object parent, int description, Object ancestor, Object left, Object right) {
					return new MyDiffNode((IDiffContainer) parent, description, (ITypedElement)ancestor, (ITypedElement)left, (ITypedElement)right);
				}
			};
			
			fRoot= d.findDifferences(fThreeWay, pm, null, fAncestor, fLeft, fRight);
			return fRoot;
			
		} catch (CoreException ex) {
			throw new InvocationTargetException(ex);
		} finally {
			pm.done();
		}
	}
	
	private String buildLabel(IResource r) {
		//return r.getName();
		String n= r.getFullPath().toString();
		if (n.charAt(0) == IPath.SEPARATOR)
			return n.substring(1);
		return n;
	}
	
	public void saveChanges(IProgressMonitor pm) throws CoreException {
		super.saveChanges(pm);
		if (fRoot instanceof DiffNode) {
			try {
				commit(pm, (DiffNode) fRoot);
			} finally {
				if (fDiffViewer != null)
					fDiffViewer.refresh();				
				setDirty(false);
			}
		}
	}
	
	/*
	 * Recursively walks the diff tree and commits all changes.
	 */
	private static void commit(IProgressMonitor pm, DiffNode node) throws CoreException {
		
		if (node instanceof MyDiffNode)		
			((MyDiffNode)node).clearDirty();
		
		ITypedElement left= node.getLeft();
		if (left instanceof BufferedResourceNode)
			((BufferedResourceNode) left).commit(pm);
			
		ITypedElement right= node.getRight();
		if (right instanceof BufferedResourceNode)
			((BufferedResourceNode) right).commit(pm);

		IDiffElement[] children= node.getChildren();
		if (children != null) {
			for (int i= 0; i < children.length; i++) {
				IDiffElement element= children[i];
				if (element instanceof DiffNode)
					commit(pm, (DiffNode) element);
			}
		}
	}
	
	private static String normalizeCase(String s) {
		if (NORMALIZE_CASE && s != null)
			return s.toUpperCase();
		return s;
	}
}

