package org.eclipse.ant.internal.ui.model;

import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;


public class AntModelContentProvider implements ITreeContentProvider {

	private static final Object[] EMPTY_ARRAY= new Object[0];
	
	/**
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	public void dispose() {
	}
    
	/**
	 * do nothing
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(Viewer, Object, Object)
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(Object)
	 */
	public Object[] getChildren(Object parentNode) {
		AntElementNode parentElement = (AntElementNode)parentNode;
		if (parentElement.hasChildren()) {
			List children= parentElement.getChildNodes();
			return children.toArray();
		} 
		return EMPTY_ARRAY;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(Object)
	 */
	public Object getParent(Object aNode) {
		AntElementNode tempElement = (AntElementNode)aNode;
		return tempElement.getParentNode();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(Object)
	 */
	public boolean hasChildren(Object aNode) {
		return ((AntElementNode)aNode).hasChildren();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(Object)
	 */
	public Object[] getElements(Object anInputElement) {
		if (anInputElement instanceof AntModel ) {
			return ((AntModel) anInputElement).getRootElements();
		}
		if (anInputElement instanceof AntTargetNode[]) {
			return (Object[])anInputElement;
		}
		return EMPTY_ARRAY;
	}

}