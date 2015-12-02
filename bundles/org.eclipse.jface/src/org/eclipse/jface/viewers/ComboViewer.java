/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sebastian Davids - bug 69254
 *     Lars Vogel <Lars.Vogel@gmail.com> - Bug 430873
 *******************************************************************************/

package org.eclipse.jface.viewers;

import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * A concrete viewer based either on an SWT <code>Combo</code> control or <code>CCombo</code>
 * control. This class is intended as an alternative to the JFace <code>ListViewer</code>, which displays
 * its content in a combo box rather than a list. Wherever possible, this class attempts to behave
 * like ListViewer. <p>
 *
 * This class is designed to be instantiated with a pre-existing SWT combo control
 * and configured with a domain-specific content provider, label provider, element
 * filter (optional), and element sorter (optional).
 * </p>
 *
 * @see org.eclipse.jface.viewers.ListViewer
 * @since 3.0 (made non-final in 3.4)
 */
public class ComboViewer extends AbstractListViewer {

    /**
     * This viewer's list control if this viewer is instantiated with a combo control; otherwise
     * <code>null</code>.
     *
     * @see #ComboViewer(Combo)
     */
    private Combo combo;

    /**
     * This viewer's list control if this viewer is instantiated with a CCombo control; otherwise
     * <code>null</code>.
     *
     * @see #ComboViewer(CCombo)
     * @since 3.3
     */
    private CCombo ccombo;

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

    /**
     * Creates a combo viewer on the given CCombo control.
     * The viewer has no input, no content provider, a default label provider,
     * no sorter, and no filters.
     *
     * @param list the CCombo control
     * @since 3.3
     */
    public ComboViewer(CCombo list) {
        this.ccombo = list;
        hookControl(list);
    }

    @Override
	protected void listAdd(String string, int index) {
        if (combo == null) {
            ccombo.add(string, index);
        } else {
            combo.add(string, index);
        }
    }

    @Override
	protected void listSetItem(int index, String string) {
        if (combo == null) {
            ccombo.setItem(index, string);
        } else {
            combo.setItem(index, string);
        }
    }

    @Override
	protected int[] listGetSelectionIndices() {
        if (combo == null) {
            return new int[] { ccombo.getSelectionIndex() };
        }
        return new int[] { combo.getSelectionIndex() };
    }

    @Override
	protected int listGetItemCount() {
        if (combo == null) {
            return ccombo.getItemCount();
        }
        return combo.getItemCount();
    }

    @Override
	protected void listSetItems(String[] labels) {
        if (combo == null) {
            ccombo.setItems(labels);
        } else {
            combo.setItems(labels);
        }
    }

    @Override
	protected void listRemoveAll() {
        if (combo == null) {
            ccombo.removeAll();
        } else {
            combo.removeAll();
        }
    }

    @Override
	protected void listRemove(int index) {
        if (combo == null) {
            ccombo.remove(index);
        } else {
            combo.remove(index);
        }
    }

    @Override
	public Control getControl() {
        if (combo == null) {
            return ccombo;
        }
        return combo;
    }

    /**
	 * Returns this list viewer's list control. If the viewer was not created on
	 * a CCombo control, some kind of unchecked exception is thrown.
	 *
	 * @return the list control
     * @since 3.3
	 */
    public CCombo getCCombo() {
        Assert.isNotNull(ccombo);
        return ccombo;
    }

    /**
     * Returns this list viewer's list control. If the viewer was not created on
	 * a Combo control, some kind of unchecked exception is thrown.
     *
     * @return the list control
     */
    public Combo getCombo() {
    	Assert.isNotNull(combo);
        return combo;
    }

    /*
     * Do nothing -- combos only display the selected element, so there is no way
     * we can ensure that the given element is visible without changing the selection.
     * Method defined on StructuredViewer.
     */
    @Override
	public void reveal(Object element) {
    }


    @Override
	protected void listSetSelection(int[] ixs) {
        if (combo == null) {
            for (int ix : ixs) {
                ccombo.select(ix);
            }
        } else {
            for (int ix : ixs) {
                combo.select(ix);
            }
        }
    }

    @Override
	protected void listDeselectAll() {
        if (combo == null) {
            ccombo.deselectAll();
            ccombo.clearSelection();
        } else {
            combo.deselectAll();
            combo.clearSelection();
        }
    }

    @Override
	protected void listShowSelection() {
    }
}
