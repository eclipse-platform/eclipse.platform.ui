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

public class ContainerPlaceholder extends PartPlaceholder implements
        ILayoutContainer {
    private static int nextId = 0;

    private ILayoutContainer realContainer;

    /**
     * ContainerPlaceholder constructor comment.
     * @param id java.lang.String
     * @param label java.lang.String
     */
    public ContainerPlaceholder(String id) {
        super(((id == null) ? "Container Placeholder " + nextId++ : id)); //$NON-NLS-1$
    }

    /**
     * add method comment.
     */
    public void add(LayoutPart child) {
        if (!(child instanceof PartPlaceholder)) {
			return;
		}
        realContainer.add(child);
    }

    /**
     * See ILayoutContainer::allowBorder
     */
    public boolean allowsBorder() {
        return true;
    }

    /**
     * getChildren method comment.
     */
    public LayoutPart[] getChildren() {
        return realContainer.getChildren();
    }

    /**
     * getFocus method comment.
     */
    public LayoutPart getFocus() {
        return null;
    }

    /**
     * getFocus method comment.
     */
    public LayoutPart getRealContainer() {
        return (LayoutPart) realContainer;
    }

    /**
     * isChildVisible method comment.
     */
    public boolean isChildVisible(LayoutPart child) {
        return false;
    }

    /**
     * remove method comment.
     */
    public void remove(LayoutPart child) {
        if (!(child instanceof PartPlaceholder)) {
			return;
		}
        realContainer.remove(child);
    }

    /**
     * replace method comment.
     */
    public void replace(LayoutPart oldChild, LayoutPart newChild) {
        if (!(oldChild instanceof PartPlaceholder)
                && !(newChild instanceof PartPlaceholder)) {
			return;
		}
        realContainer.replace(oldChild, newChild);
    }

    /**
     * setChildVisible method comment.
     */
    public void setChildVisible(LayoutPart child, boolean visible) {
    }

    /**
     * setFocus method comment.
     */
    public void setFocus(LayoutPart child) {
    }

    public void setRealContainer(ILayoutContainer container) {

        if (container == null) {
            // set the parent container of the children back to the real container
            if (realContainer != null) {
                LayoutPart[] children = realContainer.getChildren();
                if (children != null) {
                    for (int i = 0, length = children.length; i < length; i++) {
                        children[i].setContainer(realContainer);
                    }
                }
            }
        } else {
            // replace the real container with this place holder
            LayoutPart[] children = container.getChildren();
            if (children != null) {
                for (int i = 0, length = children.length; i < length; i++) {
                    children[i].setContainer(this);
                }
            }
        }

        this.realContainer = container;
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

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.ILayoutContainer#isZoomed(org.eclipse.ui.internal.LayoutPart)
     */
    public boolean childIsZoomed(LayoutPart toTest) {
        return false;
    }
}
