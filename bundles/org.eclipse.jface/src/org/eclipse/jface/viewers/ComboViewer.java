/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jface.viewers;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * A concrete viewer based on an SWT <code>Combo</code> control. This class is intended
 * as an alternative to the JFace <code>ListViewer</code>, which displays its content
 * in a combo box rather than a list. Wherever possible, this class attempts to behave
 * like ListViewer. <p>
 * 
 * This class is designed to be instantiated with a pre-existing SWT combo control 
 * and configured with a domain-specific content provider, label provider, element
 * filter (optional), and element sorter (optional).
 * </p>
 * 
 * @see org.eclipse.jface.viewers.ListViewer
 * @since 3.0
 */
public final class ComboViewer extends AbstractListViewer {

    /**
     * This viewer's list control.
     */
    private Combo combo;

    /**
     * Creates a combo viewer on a newly-created combo control under the given parent.
     * The viewer has no input, no content provider, a default label provider, 
     * no sorter, and no filters.
     *
     * @param parent the parent control
     */
    public ComboViewer(Composite parent) {
        this(parent, SWT.READ_ONLY | SWT.BORDER);
    }

    /**
     * Creates a combo viewer on a newly-created combo control under the given parent.
     * The combo control is created using the given SWT style bits.
     * The viewer has no input, no content provider, a default label provider, 
     * no sorter, and no filters.
     *
     * @param parent the parent control
     * @param style the SWT style bits
     */
    public ComboViewer(Composite parent, int style) {
        this(new Combo(parent, style));
    }

    /**
     * Creates a combo viewer on the given combo control.
     * The viewer has no input, no content provider, a default label provider, 
     * no sorter, and no filters.
     *
     * @param list the combo control
     */
    public ComboViewer(Combo list) {
        this.combo = list;
        hookControl(list);
    }

    protected void listAdd(String string, int index) {
        combo.add(string, index);
    }

    protected void listSetItem(int index, String string) {
        combo.setItem(index, string);
    }

    protected int[] listGetSelectionIndices() {
        return new int[] { combo.getSelectionIndex() };
    }

    protected int listGetItemCount() {
        return combo.getItemCount();
    }

    protected void listSetItems(String[] labels) {
        combo.setItems(labels);
    }

    protected void listRemoveAll() {
        combo.removeAll();
    }

    protected void listRemove(int index) {
        combo.remove(index);
    }

    /* (non-Javadoc)
     * Method declared on Viewer.
     */
    public Control getControl() {
        return combo;
    }

    /**
     * Returns this list viewer's list control.
     *
     * @return the list control
     */
    public Combo getCombo() {
        return combo;
    }

    /*
     * Do nothing -- combos only display the selected element, so there is no way
     * we can ensure that the given element is visible without changing the selection.
     * Method defined on StructuredViewer.
     */
    public void reveal(Object element) {
        return;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.AbstractListViewer#listSelectAndShow(int[])
     */
    protected void listSetSelection(int[] ixs) {
        for (int idx = 0; idx < ixs.length; idx++) {
            combo.select(ixs[idx]);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.AbstractListViewer#listDeselectAll()
     */
    protected void listDeselectAll() {
        combo.deselectAll();
        combo.clearSelection();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.AbstractListViewer#listShowSelection()
     */
    protected void listShowSelection() {

    }
}