package org.eclipse.help.internal.ui;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import java.util.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.help.internal.contributions.*;

/**
 * A basic tree content provider
 */
public class TreeContentProvider implements ITreeContentProvider {
	static TreeContentProvider instance = new TreeContentProvider();
	/**
	 * TreeContentProvider constructor comment.
	 */
	public TreeContentProvider() {
		super();
	}
	public void dispose() {
	}
	public Object[] getChildren(Object element) {
		if (element instanceof Contribution) {
			return ((Contribution) element).getChildrenList().toArray();
		} else
			return null;
	}
	public static TreeContentProvider getDefault() {
		return instance;
	}
	public Object[] getElements(Object element) {
		return getChildren(element);
	}
	public Object getParent(Object element) {
		if (element instanceof Contribution)
			return ((Contribution) element).getParent();
		else
			return null;
	}
	public boolean hasChildren(Object element) {
		if (element instanceof Contribution)
			return ((Contribution) element).getChildren().hasNext();
		else
			return false;
	}
	public void inputChanged(
		org.eclipse.jface.viewers.Viewer viewer,
		Object oldInput,
		Object newInput) {
	}
	public boolean isDeleted(Object element) {
		return false;
	}
}
