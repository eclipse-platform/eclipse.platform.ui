package org.eclipse.team.internal.ccvs.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
 
import org.eclipse.jface.viewers.*;

/**
 * A default content provider to prevent subclasses from
 * having to implement methods they don't need.
 */
public class SimpleContentProvider implements IStructuredContentProvider {

	/**
	 * SimpleContentProvider constructor.
	 */
	public SimpleContentProvider() {
		super();
	}
	
	/*
	 * @see SimpleContentProvider#dispose()
	 */
	public void dispose() {
	}
	
	/*
	 * @see SimpleContentProvider#getElements()
	 */
	public Object[] getElements(Object element) {
		return new Object[0];
	}
	
	/*
	 * @see SimpleContentProvider#inputChanged()
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}
}
