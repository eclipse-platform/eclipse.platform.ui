package org.eclipse.ui.internal;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import org.eclipse.ui.*;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.graphics.Image;

public class WorkbenchPartLabelProvider extends LabelProvider implements ITableLabelProvider {
/**
 * @see ITableLabelProvider#getColumnImage
 */
public Image getColumnImage(Object element, int columnIndex) {
	if (element instanceof IWorkbenchPart) {
		return ((IWorkbenchPart)element).getTitleImage();
	}
	return null;
}
/**
 * @see ITableLabelProvider#getColumnText
 */
public String getColumnText(Object element, int columnIndex) {
	if (element instanceof IWorkbenchPart) {
		return ((IWorkbenchPart)element).getTitle();
	}
	return null;
}
}
