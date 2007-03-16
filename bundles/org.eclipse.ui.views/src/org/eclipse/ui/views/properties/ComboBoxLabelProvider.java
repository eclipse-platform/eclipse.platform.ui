/*******************************************************************************
 * Copyright (c) 2003, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.views.properties;

import org.eclipse.jface.viewers.LabelProvider;

/**
 * An <code>ILabelProvider</code> that assists in rendering labels for 
 * <code>ComboBoxPropertyDescriptors</code>.  The label for a given 
 * <code>Integer</code> value is the <code>String</code> at the value in 
 * the provided values array.  
 * 
 * @since 3.0
 */
public class ComboBoxLabelProvider extends LabelProvider {

    /**
     * The array of String labels.
     */
    private String[] values;

    /**
     * @param values the possible label values that this 
     * <code>ILabelProvider</code> may return.
     */
    public ComboBoxLabelProvider(String[] values) {
        this.values = values;
    }

    /**
     * @return the possible label values that this 
     * <code>ILabelProvider</code> may return.
     */
    public String[] getValues() {
        return values;
    }

    /**
     * @param values the possible label values that this 
     * <code>ILabelProvider</code> may return.
     */
    public void setValues(String[] values) {
        this.values = values;
    }

    /**
     * Returns the <code>String</code> that maps to the given 
     * <code>Integer</code> offset in the values array.
     * 
     * @param element an <code>Integer</code> object whose value is a valid 
     * location within the values array of the receiver
     * @return a <code>String</code> from the provided values array, or the 
     * empty <code>String</code> 
     * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
     */
    public String getText(Object element) {
        if (element == null) {
            return ""; //$NON-NLS-1$
        }

        if (element instanceof Integer) {
            int index = ((Integer) element).intValue();
            if (index >= 0 && index < values.length) {
                return values[index];
            } else {
                return ""; //$NON-NLS-1$
            }
        }

        return ""; //$NON-NLS-1$
    }
}
