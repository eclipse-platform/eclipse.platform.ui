/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.dialogs;

import org.eclipse.ui.IFileEditorMapping;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.graphics.Image;
import java.util.*;

/**
 * A label provider for displaying of <code>IFileEditorMapping</code>
 * objects in viewers.
 * <p>
 * This class has a singleton instance, 
 * <code>FileEditorMappingLabelProvider.INSTANCE</code>,
 * which can be used any place this kind of label provider is needed.
 * </p>
 * <p>
 * The singleton instance hangs on to images, which get freed up when 
 * <code>dispose</code> is called.
 * </p>
 *
 * @see org.eclipse.jface.viewers.ILabelProvider
 */ 
public class FileEditorMappingLabelProvider extends LabelProvider implements ITableLabelProvider {

	/**
	 * Singleton instance accessor.
	 */
	public final static FileEditorMappingLabelProvider INSTANCE = new FileEditorMappingLabelProvider();

	/**
	 * Images that will require disposal.
	 */
	private List imagesToDispose = new ArrayList();
/**
 * Creates an instance of this class.  The private visibility of this
 * constructor ensures that this class is only usable as a singleton.
 */
private FileEditorMappingLabelProvider() {
	super();
}
/* (non-Javadoc)
 * Method declared on ILabelProvider.
 */
public void dispose() {
	super.dispose();
	for (Iterator e = imagesToDispose.iterator(); e.hasNext();) {
		((Image) e.next()).dispose();
	}
	imagesToDispose.clear();
}
/**
 * The <code>ResourceTypeEditorMappingLabelProvider</code> implementation of this
 * <code>ITableLabelProvider</code> method creates and returns an new image. The 
 * image is remembered internally and will be deallocated by
 * <code>dispose</code>.
 */
public Image getColumnImage(Object element, int row) {
	return getImage(element);
}
/* (non-Javadoc)
 * Method declared on ITableLabelProvider.
 */
public String getColumnText(Object element, int row) {
	return getText(element);
}
/**
 * The <code>ResourceTypeEditorMappingLabelProvider</code> implementation of this
 * <code>ILabelProvider</code> method creates and returns an new image. The image
 * is remembered internally and will be deallocated by <code>dispose</code>.
 */
public Image getImage(Object element) {
	if (element instanceof IFileEditorMapping) {
		Image image = ((IFileEditorMapping)element).getImageDescriptor().createImage();
		imagesToDispose.add(image);
		return image;
	}
	return null;
}
/* (non-Javadoc)
 * Method declared on ILabelProvider.
 */
public String getText(Object element) {
	if (element instanceof IFileEditorMapping)
		return ((IFileEditorMapping)element).getLabel();

	return null;
}
}
