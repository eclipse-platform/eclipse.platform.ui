package org.eclipse.ui.internal.dialogs;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.IPerspectiveRegistry;

public class PerspContentProvider implements IStructuredContentProvider {
/**
 * PerspContentProvider constructor comment.
 */
public PerspContentProvider() {
	super();
}
public void dispose() {
}
public Object[] getElements(Object element) {
	if (element instanceof IPerspectiveRegistry) {
		IPerspectiveRegistry reg = (IPerspectiveRegistry)element;
		return reg.getPerspectives();
	}
	return null;
}
public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
}
public boolean isDeleted(Object element) {
	return false;
}
}
