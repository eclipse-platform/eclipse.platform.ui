package org.eclipse.help.internal.ui;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.help.topics.ITopicNode;
import org.eclipse.jface.viewers.*;
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
		if (element instanceof ITopicNode) {
			return ((ITopicNode) element).getChildTopics().toArray();
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
		return null;
	}
	public boolean hasChildren(Object element) {
		if (element instanceof ITopicNode)
			return ((ITopicNode) element).getChildTopics().size() > 0;
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