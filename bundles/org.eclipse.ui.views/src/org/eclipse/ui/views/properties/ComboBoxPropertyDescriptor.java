/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.views.properties;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/**
 * Descriptor for a property that has a value which should be edited
 * with a combo box cell editor.  This class provides a default 
 * <code>ILabelProvider</code> that will render the label of the given 
 * descriptor as the <code>String</code> found in the value array at the 
 * currently selected index.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 * <p>
 * Example:
 * <pre>
 * String[] values = {"Top left", "Top right", "Bottom left", "Bottom right"};
 * IPropertyDescriptor pd = new ComboBoxPropertyDescriptor("origin", "Origin", values);
 * </pre>
 * </p>
 */
public class ComboBoxPropertyDescriptor extends PropertyDescriptor {

    /**
     * The list of possible values to display in the combo box
     */
    private String[] values;

    /**
     * Creates an property descriptor with the given id, display name, and list
     * of value labels to display in the combo box cell editor.
     * 
     * @param id the id of the property
     * @param displayName the name to display for the property
     * @param valuesArray the list of possible values to display in the combo box
     */
    public ComboBoxPropertyDescriptor(Object id, String displayName,
            String[] valuesArray) {
        super(id, displayName);
        values = valuesArray;
    }

    /**
     * The <code>ComboBoxPropertyDescriptor</code> implementation of this 
     * <code>IPropertyDescriptor</code> method creates and returns a new
     * <code>ComboBoxCellEditor</code>.
     * <p>
     * The editor is configured with the current validator if there is one.
     * </p>
     */
    public CellEditor createPropertyEditor(Composite parent) {
        CellEditor editor = new ComboBoxCellEditor(parent, values,
                SWT.READ_ONLY);
        if (getValidator() != null)
            editor.setValidator(getValidator());
        return editor;
    }

    /**
     * The <code>ComboBoxPropertyDescriptor</code> implementation of this 
     * <code>IPropertyDescriptor</code> method returns the value set by
     * the <code>setProvider</code> method or, if no value has been set
     * it returns a <code>ComboBoxLabelProvider</code> created from the 
     * valuesArray of this <code>ComboBoxPropertyDescriptor</code>.
     *
     * @see #setLabelProvider
     */
    public ILabelProvider getLabelProvider() {
        if (isLabelProviderSet())
            return super.getLabelProvider();
        else
            return new ComboBoxLabelProvider(values);
    }
}