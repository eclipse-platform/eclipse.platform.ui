package org.eclipse.ant.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.jface.viewers.*;

/**
 * Content provider for the tree viewer of the Ant Console.
 */

public class AntTreeContentProvider implements ITreeContentProvider {
	
	private AntConsole console;
	
public AntTreeContentProvider(AntConsole console) {
	this.console = console;
}

/**
 * @see ITreeContentProvider#getChildren(Object)
 */
public Object[] getChildren(Object parent) {
		return ((OutputStructureElement) parent).getChildren();
}

/**
 * @see ITreeContentProvider#getParent(Object)
 */
public Object getParent(Object element) {
	return ((OutputStructureElement) element).getParent();
}

/**
 * @see ITreeContentProvider#hasChildren(Object)
 */
public boolean hasChildren(Object element) {
		return ((OutputStructureElement) element).hasChildren();
}

/**
 * @see IStructuredContentProvider#getElements(Object)
 */
public Object[] getElements(Object parent) {
	return ((OutputStructureElement) parent).getChildren();
}

/**
 * @see IContentProvider#dispose()
 */
public void dispose() {
}

/**
 * @see IContentProvider#inputChanged(Viewer, Object, Object)
 */
public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
}

}

