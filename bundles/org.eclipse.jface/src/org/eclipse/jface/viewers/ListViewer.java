/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jface.viewers;

import org.eclipse.jface.util.Assert;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * A concrete viewer based on an SWT <code>List</code> control.
 * <p>
 * This class is not intended to be subclassed. It is designed to be
 * instantiated with a pre-existing SWT <code>List</code> control and configured
 * with a domain-specific content provider, label provider, element filter (optional),
 * and element sorter (optional).
 * <p>
 * Note that the SWT <code>List</code> control only supports the display of strings, not icons.
 * If you need to show icons for items, use <code>TableViewer</code> instead.
 * </p>
 * 
 * @see TableViewer
 */
public class ListViewer extends AbstractListViewer {

    /**
     * This viewer's list control.
     */
    private org.eclipse.swt.widgets.List list;

    /**
     * Creates a list viewer on a newly-created list control under the given parent.
     * The list control is created using the SWT style bits <code>MULTI, H_SCROLL, V_SCROLL,</code> and <code>BORDER</code>.
     * The viewer has no input, no content provider, a default label provider, 
     * no sorter, and no filters.
     *
     * @param parent the parent control
     */
    public ListViewer(Composite parent) {
        this(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
    }

    /**
     * Creates a list viewer on a newly-created list control under the given parent.
     * The list control is created using the given SWT style bits.
     * The viewer has no input, no content provider, a default label provider, 
     * no sorter, and no filters.
     *
     * @param parent the parent control
     * @param style the SWT style bits
     */
    public ListViewer(Composite parent, int style) {
        this(new org.eclipse.swt.widgets.List(parent, style));
    }

    /**
     * Creates a list viewer on the given list control.
     * The viewer has no input, no content provider, a default label provider, 
     * no sorter, and no filters.
     *
     * @param list the list control
     */
    public ListViewer(org.eclipse.swt.widgets.List list) {
        this.list = list;
        hookControl(list);
    }

    /* (non-Javadoc)
     * Method declared on Viewer.
     */
    public Control getControl() {
        return list;
    }

    /**
     * Returns this list viewer's list control.
     *
     * @return the list control
     */
    public org.eclipse.swt.widgets.List getList() {
        return list;
    }

    /*
     * Non-Javadoc.
     * Method defined on StructuredViewer.
     */
    public void reveal(Object element) {
        Assert.isNotNull(element);
        int index = getElementIndex(element);
        if (index == -1)
            return;
        // algorithm patterned after List.showSelection()
        int count = list.getItemCount();
        if (count == 0)
            return;
        int height = list.getItemHeight();
        Rectangle rect = list.getClientArea();
        int topIndex = list.getTopIndex();
        int visibleCount = Math.max(rect.height / height, 1);
        int bottomIndex = Math.min(topIndex + visibleCount, count) - 1;
        if ((topIndex <= index) && (index <= bottomIndex))
            return;
        int newTop = Math.min(Math.max(index - (visibleCount / 2), 0),
                count - 1);
        list.setTopIndex(newTop);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.AbstractListViewer#listAdd(java.lang.String, int)
     */
    protected void listAdd(String string, int index) {
        list.add(string, index);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.AbstractListViewer#listSetItem(int, java.lang.String)
     */
    protected void listSetItem(int index, String string) {
        list.setItem(index, string);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.AbstractListViewer#listGetSelectionIndices()
     */
    protected int[] listGetSelectionIndices() {
        return list.getSelectionIndices();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.AbstractListViewer#listGetItemCount()
     */
    protected int listGetItemCount() {
        return list.getItemCount();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.AbstractListViewer#listSetItems(java.lang.String[])
     */
    protected void listSetItems(String[] labels) {
        list.setItems(labels);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.AbstractListViewer#listRemoveAll()
     */
    protected void listRemoveAll() {
        list.removeAll();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.AbstractListViewer#listRemove(int)
     */
    protected void listRemove(int index) {
        list.remove(index);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.AbstractListViewer#listSelectAndShow(int[])
     */
    protected void listSetSelection(int[] ixs) {
        list.setSelection(ixs);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.AbstractListViewer#listDeselectAll()
     */
    protected void listDeselectAll() {
        list.deselectAll();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.AbstractListViewer#listShowSelection()
     */
    protected void listShowSelection() {
        list.showSelection();
    }

}
