package org.eclipse.jface.viewers;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.swt.graphics.Image;

/**
 * Extends <code>IBaseLabelProvider</code> with the methods
 * to provide the text and/or image for the label of a given element. 
 * Used by most structured viewers, except table viewers.
 */
public interface ILabelProvider extends IBaseLabelProvider {
/**
 * Returns the image for the label of the given element.  The image
 * is owned by the label provider and must not be disposed directly.
 * Instead, dispose the label provider when no longer needed.
 *
 * @param element the element for which to provide the label image
 * @return the image used to label the element, or <code>null</code>
 *   if there is no image for the given object
 */
public Image getImage(Object element);
/**
 * Returns the text for the label of the given element.
 *
 * @param element the element for which to provide the label text
 * @return the text string used to label the element, or <code>null</code>
 *   if there is no text label for the given object
 */
public String getText(Object element);
}
