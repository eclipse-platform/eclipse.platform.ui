package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IWorkbenchPart;

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
		IWorkbenchPart part = (IWorkbenchPart)element;
		String path = part.getTitleToolTip();
		if (path.length() == 0) {
			return part.getTitle();
		} else {
			return part.getTitle() + "  [" + part.getTitleToolTip() + "]"; //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
	return null;
}
}
