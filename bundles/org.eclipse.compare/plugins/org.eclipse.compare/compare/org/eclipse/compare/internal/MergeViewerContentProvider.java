/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.compare.internal;

import org.eclipse.swt.graphics.Image;

import org.eclipse.jface.viewers.Viewer;

import org.eclipse.compare.*;
import org.eclipse.compare.structuremergeviewer.*;
import org.eclipse.compare.contentmergeviewer.IMergeViewerContentProvider;

/**
 * Adapts any <code>ContentMergeViewer</code> to work on an <code>ICompareInput</code>
 * e.g. a <code>DiffNode</code>.
 */
public class MergeViewerContentProvider implements IMergeViewerContentProvider {
	
	private CompareConfiguration fCompareConfiguration;
		
	public MergeViewerContentProvider(CompareConfiguration cc) {
		fCompareConfiguration= cc;
	}
	
	public void dispose() {
	}
	
	public void inputChanged(Viewer v, Object o1, Object o2) {
	}
	
	//---- ancestor
			
	public String getAncestorLabel(Object element) {
		return fCompareConfiguration.getAncestorLabel(element);
	}
	
	public Image getAncestorImage(Object element) {
		return fCompareConfiguration.getAncestorImage(element);
	}
	
	public Object getAncestorContent(Object element) {
		if (element instanceof ICompareInput)
			return ((ICompareInput) element).getAncestor();
		return null;
	}
	
	public boolean showAncestor(Object element) {
		if (element instanceof ICompareInput)
			return (((ICompareInput)element).getKind() & Differencer.DIRECTION_MASK) == Differencer.CONFLICTING;
		return false;
	}

	//---- left
					
	public String getLeftLabel(Object element) {
		return fCompareConfiguration.getLeftLabel(element);
	}
	
	public Image getLeftImage(Object element) {
		return fCompareConfiguration.getLeftImage(element);
	}
	
	public Object getLeftContent(Object element) {	
		if (element instanceof ICompareInput)
			return ((ICompareInput) element).getLeft();
		return null;
	}
		
	public boolean isLeftEditable(Object element) {
		if (element instanceof ICompareInput) {
			Object left= ((ICompareInput) element).getLeft();
			if (left == null) {
				IDiffElement parent= ((IDiffElement)element).getParent();
				if (parent instanceof ICompareInput)
					left= ((ICompareInput) parent).getLeft();
			}
			if (left instanceof IEditableContent)
				return ((IEditableContent)left).isEditable();
		}
		return false;
	}

	public void saveLeftContent(Object element, byte[] bytes) {
		if (element instanceof ICompareInput) {
			ICompareInput node= (ICompareInput) element;
			if (bytes != null) {
				ITypedElement left= node.getLeft();
				if (left instanceof IEditableContent)
					((IEditableContent)left).setContent(bytes);
			} else {
				node.copy(false);
			}			
		}
	}
	
	//---- right
	
	public String getRightLabel(Object element) {
		return fCompareConfiguration.getRightLabel(element);
	}
	
	public Image getRightImage(Object element) {
		return fCompareConfiguration.getRightImage(element);
	}
	
	public Object getRightContent(Object element) {
		if (element instanceof ICompareInput)
			return ((ICompareInput) element).getRight();
		return null;
	}
	
	public boolean isRightEditable(Object element) {
		if (element instanceof ICompareInput) {
			Object right= ((ICompareInput) element).getRight();
			if (right == null) {
				IDiffContainer parent= ((IDiffElement)element).getParent();
				if (parent instanceof ICompareInput)
					right= ((ICompareInput) parent).getRight();
			}
			if (right instanceof IEditableContent)
				return ((IEditableContent)right).isEditable();
		}
		return false;
	}
	
	public void saveRightContent(Object element, byte[] bytes) {
		if (element instanceof ICompareInput) {
			ICompareInput node= (ICompareInput) element;
			if (bytes != null) {
				ITypedElement right= node.getRight();
				if (right instanceof IEditableContent)
					((IEditableContent)right).setContent(bytes);
			} else {
				node.copy(true);
			}		
		}
	}
}

