package org.eclipse.help.ui.internal.search;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.help.*;
import org.eclipse.jface.viewers.ITreeContentProvider;
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
		if (element instanceof IToc)
			return ((IToc) element).getTopics();
		else if (element instanceof ITopic)
			return ((ITopic) element).getSubtopics();
		else
			return null;
	}
	public static CheckboxTreeContentProvider getDefault() {
		return instance;
	}
	public Object[] getElements(Object element) {
		if (element instanceof IToc)
			return ((IToc) element).getTopics();
		else if (element instanceof ITopic)
			return ((ITopic) element).getSubtopics();
		else
			return null;
	}
	public Object getParent(Object element) {
		return null;
	}
	public boolean hasChildren(Object element) {
		return getChildren(element) != null && getChildren(element).length > 0;
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