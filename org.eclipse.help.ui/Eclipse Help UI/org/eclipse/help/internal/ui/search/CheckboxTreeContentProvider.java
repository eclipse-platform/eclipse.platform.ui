package org.eclipse.help.internal.ui.search;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import java.util.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.help.internal.contributions.*;
import org.eclipse.help.internal.ui.*;

/**
 * A basic tree content provider
 */
public class CheckboxTreeContentProvider implements ITreeContentProvider {
	static CheckboxTreeContentProvider instance = new CheckboxTreeContentProvider();
	/**
	 * TreeContentProvider constructor comment.
	 */
	public CheckboxTreeContentProvider() {
		super();
	}
	public void dispose() {
	}
	public Object[] getChildren(Object element) {
		if (element instanceof InfoView || element instanceof InfoSet) {
			return ((Contribution) element).getChildrenList().toArray();
		} else
			return null;
	}
	public static CheckboxTreeContentProvider getDefault() {
		return instance;
	}
	public Object[] getElements(Object element) {
		if (element instanceof InfoView || element instanceof InfoSet)
			return getChildren(element);
		else
			return null;
	}
	public Object getParent(Object element) {
		if (element instanceof Contribution)
			return ((Contribution) element).getParent();
		else
			return null;
	}
	public boolean hasChildren(Object element) {
		if (element instanceof InfoView || element instanceof InfoSet)
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
