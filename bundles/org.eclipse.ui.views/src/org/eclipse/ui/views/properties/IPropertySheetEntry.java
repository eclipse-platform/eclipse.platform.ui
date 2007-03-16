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
package org.eclipse.ui.views.properties;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

/**
 * <code>IPropertySheetEntry</code> describes the model interface for the
 * property sheet.
 * <p>
 * May be implemented when supplying a custom root entry to a property page.
 * </p>
 */
public interface IPropertySheetEntry {

    /**
     * The filter id for expert properties
     */
    public String FILTER_ID_EXPERT = "org.eclipse.ui.views.properties.expert"; //$NON-NLS-1$

    /**
     * Adds the given listener to this entry's collection of listeners.
     * 
     * @param listener
     *            the listener to add
     */
    public void addPropertySheetEntryListener(
            IPropertySheetEntryListener listener);

    /**
     * Apply the current cell editor value.
     */
    public void applyEditorValue();

    /**
     * Called when the entry is no longer needed
     */
    public void dispose();

    /**
     * Returns the entry's category.
     * 
     * @return the entry's category
     */
    public String getCategory();

    /**
     * Returns the child entries for this entry.
     * 
     * @return the child entries for this entry
     */
    public IPropertySheetEntry[] getChildEntries();

    /**
     * Return a short description of the property sheet entry. Typically this
     * description is shown in a status line when the entry is selected.
     * 
     * @return the entry's description
     */
    public String getDescription();

    /**
     * Returns the name used to display the property.
     * 
     * @return the name used to display the property
     */
    public String getDisplayName();

    /**
     * Return the <code>CellEditor</code> used to edit the property.
     * 
     * @param parent
     *            the parent widget for the editor
     * @return the <code>CellEditor</code> used to edit the property
     */
    CellEditor getEditor(Composite parent);

    /**
     * Returns the error text to display if the value is invalid.
     * 
     * @return the error text to display when the value is invalid or
     *         <code>null</code>
     */
    public String getErrorText();

    /**
     * Return the filter ids used to group entries into levels such as Expert.
     * Valid values are defined as constants on this interface.
     * 
     * @return the filter ids used to group entries into levels such as Expert.
     */
    public String[] getFilters();

    /**
     * Returns the help context id for this entry, or <code>null</code> if
     * this entry has no help context id.
     * <p>
     * NOTE: Help support system API's changed since 2.0 and arrays of contexts
     * are no longer supported.
     * </p>
     * <p>
     * Thus the only valid non- <code>null</code> return type for this method
     * is a <code>String</code> representing a context id. The previously
     * valid return types are deprecated. The plural name for this method is
     * unfortunate.
     * </p>
     * 
     * @return the help context id for this entry
     */
    public Object getHelpContextIds();

    /**
     * Returns the image for the property value, if there is one. This image is
     * managed by the entry it came from. Callers of this method must never
     * dispose the returned image.
     * 
     * @return the image for this property value or <code>null</code>
     */
    public Image getImage();

    /**
     * Returns the value of the objects expressed as a String.
     * 
     * @return the value of the objects expressed as a String
     */
    public String getValueAsString();

    /**
     * Returns <code>true</code> if the entry has children.
     * 
     * @return <code>true</code> if the entry has children
     */
    public boolean hasChildEntries();

    /**
     * Removes the given listener from this entry's collection of listeners.
     * 
     * @param listener
     *            the listener to remove
     */
    public void removePropertySheetEntryListener(
            IPropertySheetEntryListener listener);

    /**
     * Resets the property value to its default value if it has been changed.
     * <p>
     * Does nothing if the notion of a default value is not meaningful for 
     * the property.
     * </p>
     */
    void resetPropertyValue();

    /**
     * Sets the objects which represent the property values for this entry. In
     * the case of the root entry these objects are the input to the viewer.
     * 
     * @param values the values for this entry
     */
    public void setValues(Object[] values);
}
