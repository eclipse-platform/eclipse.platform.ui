package org.eclipse.ui.views.internal.framelist;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;

public class TreeFrame extends Frame {
	private AbstractTreeViewer viewer;
	private Object input;
	private ISelection selection;
	private Object[] expandedElements;
public TreeFrame(AbstractTreeViewer viewer, Object input) {
	this.viewer = viewer;
	this.input = input;
	ILabelProvider provider = (ILabelProvider) viewer.getLabelProvider();
	String name = provider.getText(input);
	setName(name);
	setToolTipText(name);
}
public Object[] getExpandedElements() {
	return expandedElements;
}
public Object getInput() {
	return input;
}
public ISelection getSelection() {
	return selection;
}
public AbstractTreeViewer getViewer() {
	return viewer;
}
public void setExpandedElements(Object[] expandedElements) {
	this.expandedElements = expandedElements;
}
public void setSelection(ISelection selection) {
	this.selection = selection;
}
}
