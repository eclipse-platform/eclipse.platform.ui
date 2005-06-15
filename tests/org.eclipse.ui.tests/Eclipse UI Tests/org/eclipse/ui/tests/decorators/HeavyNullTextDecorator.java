/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.decorators;

import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.tests.internal.ForcedException;

/**
 * @see ILabelDecorator
 */
public class HeavyNullTextDecorator implements ILabelDecorator {

    /**
     * Whether we should fail with an exception
     */
    public static boolean fail = false;

    /**
     *
     */
    public HeavyNullTextDecorator() {
    }

    /**
     * @see ILabelDecorator#addListener
     */
    public void addListener(ILabelProviderListener listener) {
    }

    /**
     * @see ILabelDecorator#dispose
     */
    public void dispose() {
    }

    /**
     * @see ILabelDecorator#isLabelProperty
     */
    public boolean isLabelProperty(Object element, String property) {
        return false;
    }

    /**
     * @see ILabelDecorator#removeListener
     */
    public void removeListener(ILabelProviderListener listener) {
    }

    /**
     * @see ILabelDecorator#decorateImage
     */
    public Image decorateImage(Image image, Object element) {
        return image;
    }

    /**
     * @see ILabelDecorator#decorateText
     */
    public String decorateText(String text, Object element) {
        if (fail) {
            fail = false;
            throw new ForcedException("Heavy text decorator boom");
        }
        return null;
    }
}
