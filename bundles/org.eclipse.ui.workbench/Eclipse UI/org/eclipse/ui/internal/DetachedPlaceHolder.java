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
package org.eclipse.ui.internal;

import java.util.ArrayList;

import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.ui.IMemento;

/**
 * DetachedPlaceHolder is the placeholder for detached views.
 *
 */
public class DetachedPlaceHolder extends PartPlaceholder implements
        ILayoutContainer {
    ArrayList children = new ArrayList();

    Rectangle bounds;

    /**
     * DetachedPlaceHolder constructor comment.
     * @param id java.lang.String
     * @param bounds the size of the placeholder
     */
    public DetachedPlaceHolder(String id, Rectangle bounds) {
        super(id);
        this.bounds = bounds;
    }

    /**
     * Add a child to the container.
     */
    public void add(LayoutPart newPart) {
        if (!(newPart instanceof PartPlaceholder)) {
			return;
		}
        children.add(newPart);
    }

    /**
     * Return true if the container allows its
     * parts to show a border if they choose to,
     * else false if the container does not want
     * its parts to show a border.
     * @return boolean
     */
    public boolean allowsBorder() {
        return false;
    }

    public Rectangle getBounds() {
        return bounds;
    }

    /**
     * Returns a list of layout children.
     */
    public LayoutPart[] getChildren() {
        LayoutPart result[] = new LayoutPart[children.size()];
        children.toArray(result);
        return result;
    }

    /**
     * Remove a child from the container.
     */
    public void remove(LayoutPart part) {
        children.remove(part);
    }

    /**
     * Replace one child with another
     */
    public void replace(LayoutPart oldPart, LayoutPart newPart) {
        remove(oldPart);
        add(newPart);
    }

   
    /**
     * Restore the state from the memento.
     * @param memento
     */
    public void restoreState(IMemento memento) {
        // Read the bounds.
        Integer bigInt;
        bigInt = memento.getInteger(IWorkbenchConstants.TAG_X);
        int x = bigInt.intValue();
        bigInt = memento.getInteger(IWorkbenchConstants.TAG_Y);
        int y = bigInt.intValue();
        bigInt = memento.getInteger(IWorkbenchConstants.TAG_WIDTH);
        int width = bigInt.intValue();
        bigInt = memento.getInteger(IWorkbenchConstants.TAG_HEIGHT);
        int height = bigInt.intValue();

        bounds = new Rectangle(x, y, width, height);

        // Restore the placeholders.
        IMemento childrenMem[] = memento
                .getChildren(IWorkbenchConstants.TAG_VIEW);
        for (int i = 0; i < childrenMem.length; i++) {
            PartPlaceholder holder = new PartPlaceholder(childrenMem[i]
                    .getString(IWorkbenchConstants.TAG_ID));
            holder.setContainer(this);
            children.add(holder);
        }
    }

    /**
     * Save state to the memento.
     * @param memento
     */
    public void saveState(IMemento memento) {
        // Save the bounds.
        memento.putInteger(IWorkbenchConstants.TAG_X, bounds.x);
        memento.putInteger(IWorkbenchConstants.TAG_Y, bounds.y);
        memento.putInteger(IWorkbenchConstants.TAG_WIDTH, bounds.width);
        memento.putInteger(IWorkbenchConstants.TAG_HEIGHT, bounds.height);

        // Save the views.
        for (int i = 0; i < children.size(); i++) {
            IMemento childMem = memento
                    .createChild(IWorkbenchConstants.TAG_VIEW);
            LayoutPart child = (LayoutPart) children.get(i);
            childMem.putString(IWorkbenchConstants.TAG_ID, child.getID());
        }
    }

    public void findSashes(LayoutPart part, PartPane.Sashes sashes) {
        ILayoutContainer container = getContainer();

        if (container != null) {
            container.findSashes(this, sashes);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.ILayoutContainer#allowsAutoFocus()
     */
    public boolean allowsAutoFocus() {
        return false;
    }
}
