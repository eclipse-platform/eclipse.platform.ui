/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.adaptable;

/*
 * This decorator tests the null cases
 */

import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;

/**
 * @version 	1.0
 * @author
 */
public class NullLabelDecorator implements ILabelDecorator {

    /*
     * @see ILabelDecorator#decorateImage(Image, Object)
     */
    public Image decorateImage(Image image, Object element) {
        return null;
    }

    /*
     * @see ILabelDecorator#decorateText(String, Object)
     */
    public String decorateText(String text, Object element) {
        return null;
    }

    /*
     * @see IBaseLabelProvider#addListener(ILabelProviderListener)
     */
    public void addListener(ILabelProviderListener listener) {
    }

    /*
     * @see IBaseLabelProvider#dispose()
     */
    public void dispose() {
    }

    /*
     * @see IBaseLabelProvider#isLabelProperty(Object, String)
     */
    public boolean isLabelProperty(Object element, String property) {
        return false;
    }

    /*
     * @see IBaseLabelProvider#removeListener(ILabelProviderListener)
     */
    public void removeListener(ILabelProviderListener listener) {
    }

}
