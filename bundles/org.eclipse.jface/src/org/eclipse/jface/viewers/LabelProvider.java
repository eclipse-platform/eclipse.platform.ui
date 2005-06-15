/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.viewers;

import org.eclipse.jface.util.ListenerList;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.graphics.Image;

/**
 * A label provider implementation which, by default, uses an element's 
 * <code>toString</code> value for its text and <code>null</code> for its image.
 * <p>
 * This class may be used as is, or subclassed to provide richer labels. 
 * Subclasses may override any of the following methods:
 * <ul>
 *   <li><code>isLabelProperty</code></li> 
 *   <li><code>getImage</code></li> 
 *   <li><code>getText</code></li> 
 *   <li><code>dispose</code></li> 
 * </ul>
 * </p>
 */
public class LabelProvider implements ILabelProvider {
    private ListenerList listeners = new ListenerList(1);

    /**
     * Creates a new label provider.
     */
    public LabelProvider() {
    }

    /* (non-Javadoc)
     * Method declared on IBaseLabelProvider.
     */
    public void addListener(ILabelProviderListener listener) {
        listeners.add(listener);
    }

    /**
     * The <code>LabelProvider</code> implementation of this 
     * <code>IBaseLabelProvider</code> method does nothing. Subclasses may extend.
     */
    public void dispose() {
    }

    /**
     * Fires a label provider changed event to all registered listeners
     * Only listeners registered at the time this method is called are notified.
     *
     * @param event a label provider changed event
     *
     * @see ILabelProviderListener#labelProviderChanged
     */
    protected void fireLabelProviderChanged(
            final LabelProviderChangedEvent event) {
        Object[] listeners = this.listeners.getListeners();
        for (int i = 0; i < listeners.length; ++i) {
            final ILabelProviderListener l = (ILabelProviderListener) listeners[i];
            SafeRunnable.run(new SafeRunnable() {
                public void run() {
                    l.labelProviderChanged(event);
                }
            });

        }
    }

    /**
     * The <code>LabelProvider</code> implementation of this 
     * <code>ILabelProvider</code> method returns <code>null</code>. Subclasses may 
     * override.
     */
    public Image getImage(Object element) {
        return null;
    }

    /**
     * The <code>LabelProvider</code> implementation of this 
     * <code>ILabelProvider</code> method returns the element's <code>toString</code>
     * string. Subclasses may override.
     */
    public String getText(Object element) {
        return element == null ? "" : element.toString();//$NON-NLS-1$
    }

    /**
     * The <code>LabelProvider</code> implementation of this 
     * <code>IBaseLabelProvider</code> method returns <code>true</code>. Subclasses may 
     * override.
     */
    public boolean isLabelProperty(Object element, String property) {
        return true;
    }

    /* (non-Javadoc)
     * Method declared on IBaseLabelProvider.
     */
    public void removeListener(ILabelProviderListener listener) {
        listeners.remove(listener);
    }
}
