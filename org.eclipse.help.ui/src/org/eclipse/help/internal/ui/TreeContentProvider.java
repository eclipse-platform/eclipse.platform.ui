package org.eclipse.help.internal.ui;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jface.viewers.*;
import org.eclipse.help.*;
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
		if (element instanceof ITopic) 
			return ((ITopic) element).getSubtopics();
		else if (element instanceof IToc) 
			return ((IToc) element).getTopics();
		else
			return null;
	}
	public static TreeContentProvider getDefault() {
		return instance;
	}
	public Object[] getElements(Object element) {
		return getChildren(element);
	}
	public Object getParent(Object element) {
		return null;
	}
	public boolean hasChildren(Object element) {
		if (element instanceof ITopic)
			return ((ITopic) element).getSubtopics().length > 0;
		else if (element instanceof IToc)
			return ((IToc) element).getTopics().length > 0;
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