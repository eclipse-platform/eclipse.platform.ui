/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Hendrik Still <hendrik.still@gammas.de> - bug 412273
 *******************************************************************************/
package org.eclipse.jface.viewers;

import org.eclipse.swt.graphics.Image;

/**
 * Extends <code>IBaseLabelProvider</code> with the methods
 * to provide the text and/or image for the label of a given element. 
 * Used by most structured viewers, except table viewers.
 * @param <E> Type of an element of the model
 */
public interface ILabelProvider<E> extends IBaseLabelProvider<E> {
    /**
     * Returns the image for the label of the given element.  The image
     * is owned by the label provider and must not be disposed directly.
     * Instead, dispose the label provider when no longer needed.
     *
     * @param element the element for which to provide the label image
     * @return the image used to label the element, or <code>null</code>
     *   if there is no image for the given object
     */
    public Image getImage(E element);

    /**
     * Returns the text for the label of the given element.
     *
     * @param element the element for which to provide the label text
     * @return the text string used to label the element, or <code>null</code>
     *   if there is no text label for the given object
     */
    public String getText(E element);
}
