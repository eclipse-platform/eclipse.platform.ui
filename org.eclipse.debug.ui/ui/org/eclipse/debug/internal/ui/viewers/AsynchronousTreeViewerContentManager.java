/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.debug.internal.ui.viewers;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;

public class AsynchronousTreeViewerContentManager {

    private static final Object[] EMPTY = new Object[0];
    private Map fWidgetToChildElements = new HashMap();

    public void clearAll() {
        fWidgetToChildElements.clear();
    }

    public Object[] getChildElements(Widget parentWidget) {
        Object[] childElements = (Object[]) fWidgetToChildElements.get(parentWidget);
        if (childElements == null)
            childElements = EMPTY;
        return childElements;
    }

    public void addChildElement(Widget parentWidget, Object child) {
        Object[] originalChildren = getChildElements(parentWidget);
        Object[] newChildren = new Object[originalChildren.length + 1];
        System.arraycopy(originalChildren, 0, newChildren, 0, originalChildren.length);
        newChildren[originalChildren.length] = child;
        fWidgetToChildElements.put(parentWidget, newChildren);
    }

    public int getChildCount(Widget parentWidget) {
        return getChildElements(parentWidget).length;
    }

    public void setChildElements(Widget parentWidget, Object[] children) {
        fWidgetToChildElements.put(parentWidget, children);
    }

    public Object getChildElement(Widget parentWidget, int index) {
        Object[] childElements = getChildElements(parentWidget);
        if (index < childElements.length)
            return childElements[index];
        else
            return null;
    }

    public void remove(Widget widget) {
        if (widget instanceof TreeItem) {
            TreeItem treeItem = (TreeItem) widget;
            removeChildren(treeItem);

            Object data = treeItem.getData();
            if (data != null) {
                Widget parentWidget = treeItem.getParentItem();
                if (parentWidget == null)
                    parentWidget = treeItem.getParent();

                removeFromParent(parentWidget, data);
            }
        } else {
            fWidgetToChildElements.clear();
        }
    }

    private void removeFromParent(Widget parentWidget, Object data) {
        Object[] childElements = getChildElements(parentWidget);
        for (int i = 0; i < childElements.length; i++) {
            Object object = childElements[i];
            if (data.equals(object)) {
                Object[] newChildren = new Object[childElements.length - 1];
                System.arraycopy(childElements, 0, newChildren, 0, i);
                System.arraycopy(childElements, i + 1, newChildren, i, newChildren.length - i);
                fWidgetToChildElements.put(parentWidget, newChildren);
                return;
            }
        }
    }

    private void removeChildren(TreeItem item) {
        TreeItem[] items = item.getItems();
        for (int i = 0; i < items.length; i++) {
            removeChildren(items[i]);
        }

        fWidgetToChildElements.remove(item);
    }
}
