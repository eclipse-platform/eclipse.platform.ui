/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.examples.views.properties.tabbed.article.views;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.PropertyDescriptor;

/**
 * A font property descriptor.
 * 
 * @author Anthony Hunter
 */
public class FontPropertyDescriptor
    extends PropertyDescriptor {

    /**
     * Creates an property descriptor with the given id and display name.
     * 
     * @param id
     *            the id of the property
     * @param displayName
     *            the name to display for the property
     */
    public FontPropertyDescriptor(Object id, String displayName) {
        super(id, displayName);
    }

    /**
     * @see org.eclipse.ui.views.properties.IPropertyDescriptor#createPropertyEditor(Composite)
     */
    public CellEditor createPropertyEditor(Composite parent) {
        CellEditor editor = new FontDialogCellEditor(parent);
        if (getValidator() != null)
            editor.setValidator(getValidator());
        return editor;
    }

}
