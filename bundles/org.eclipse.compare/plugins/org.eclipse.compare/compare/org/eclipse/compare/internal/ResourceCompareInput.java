/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2001
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
	
	private ISelection fSelection;
	
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
	ResourceCompareInput(CompareConfiguration config, ISelection selection) {
		super(config);
		fSelection= selection;
	}
		
	/**
	 * Performs a two-way or three-way diff on the current selection.
	 */
	public Object prepareInput(IProgressMonitor pm) {
		
		try {
			pm.beginTask("Comparing:", IProgressMonitor.UNKNOWN);
													
			IResource[] selection= Utilities.getResources(fSelection);
					
			if (selection.length < 2 || selection.length > 3) {
				setMessage("Selection must contain two or three resources");
				return null;
			}
			
			boolean threeWay= selection.length == 3;
			
			IResource lr= selection[0];
			IResource rr= selection[1];
			if (threeWay) {
				lr= selection[1];		
				rr= selection[2];
			}
			
			IStructureComparator ancestor= null;
			IStructureComparator left= getStructure(lr);
			IStructureComparator right= getStructure(rr);
						
			if (right == null || left == null) {
				setMessage("Selected resources must be of same type");
				return null;
			}
	
			CompareConfiguration cc= (CompareConfiguration) getCompareConfiguration();

			String leftLabel= lr.getName();
			cc.setLeftLabel(leftLabel);
			cc.setLeftImage(CompareUIPlugin.getImage(lr));
			
			String rightLabel= rr.getName();
			cc.setRightLabel(rightLabel);
			cc.setRightImage(CompareUIPlugin.getImage(rr));
			
			StringBuffer title= new StringBuffer();
			title.append("Compare (");
			if (threeWay) {
				IResource ar= selection[0];
				ancestor= getStructure(ar);
				String ancestorLabel= ar.getName();
				cc.setAncestorLabel(ancestorLabel);
				cc.setAncestorImage(CompareUIPlugin.getImage(ar));
				title.append(ancestorLabel);
				title.append("-");
			}
			title.append(leftLabel);
			title.append("-");
			title.append(rightLabel);
			title.append(")");
			setTitle(title.toString());
			
			Differencer d= new Differencer() {
				protected Object visit(Object parent, int description, Object ancestor, Object left, Object right) {
					return new MyDiffNode((IDiffContainer) parent, description, (ITypedElement)ancestor, (ITypedElement)left, (ITypedElement)right);
				}
			};
			
			return d.findDifferences(threeWay, pm, null, ancestor, left, right);
		
		} finally {
			pm.done();
		}
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
			if ("JAR".equals(type) || "ZIP".equals(type)) {	// FIXME
				
				return new ZipStructureCreator().getStructure(rn);
				
//				IStructureCreatorDescriptor scd= CompareUIPlugin.getStructureCreator(type);
//				if (scd != null) {
//					IStructureCreator sc= scd.createStructureCreator();
//					if (sc != null)
//						return sc.getStructure(rn);
//				}
			}
			return rn;
		}
		return null;
	}
	
	private static final boolean NORMALIZE_CASE= true;
	
	private static String normalizeCase(String s) {
		if (NORMALIZE_CASE && s != null)
			return s.toUpperCase();
		return s;
	}
}

