package org.eclipse.jface.viewers;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.swt.graphics.Image;

/**
 * Extends <code>IBaseLabelProvider</code> with the methods
 * to provide the text and/or image for each column of a given element.  
 * Used by table viewers.
 *
 * @see TableViewer
 */
public interface ITableLabelProvider extends IBaseLabelProvider {
/**
 * Returns the label image for the given column of the given element.
 *
 * @param element the object representing the entire row, or 
 *    <code>null</code> indicating that no input object is set
 *    in the viewer
 * @param columnIndex the zero-based index of the column in which
 *   the label appears
 */
public Image getColumnImage(Object element, int columnIndex);
/**
 * Returns the label text for the given column of the given element.
 *
 * @param element the object representing the entire row, or
 *   <code>null</code> indicating that no input object is set
 *   in the viewer
 * @param columnIndex the zero-based index of the column in which the label appears
 */
public String getColumnText(Object element, int columnIndex);
}
