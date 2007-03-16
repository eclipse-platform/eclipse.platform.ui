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
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.widgets.Composite;

/**
 * A descriptor for a property to be presented by a standard property sheet page
 * (<code>PropertySheetPage</code>). These descriptors originate with property 
 * sources (<code>IPropertySource</code>).
 * <p>
 * A property descriptor carries the following information:
 * <ul>
 *   <li>property id (required)</li>
 *   <li>display name (required)</li>
 *   <li>brief description of the property (optional)</li>
 *   <li>category for grouping related properties (optional)</li>
 *   <li>label provider used to display the property value (optional)</li>
 *   <li>cell editor for changing the property value (optional)</li>
 *   <li>help context id (optional)</li>
 * </ul>
 * </p>
 * <p>
 * Clients may implement this interface to provide specialized property 
 * descriptors; however, there are standard implementations declared in
 * this package that take care of the most common cases:
 * <ul>
 *   <li><code>PropertyDescriptor - read-only property</li>
 *   <li><code>TextPropertyDescriptor</code> - edits with a 
 *      <code>TextCellEditor</code></li>
 *   <li><code>CheckboxPropertyDescriptor - edits with a 
 *      <code>CheckboxCellEditor</code></code></li>
 *   <li><code>ComboBoxPropertyDescriptor - edits with a
 *      <code>ComboBoxCellEditor</code></code></li>
 *   <li><code>ColorPropertyDescriptor - edits with a 
 *      <code>ColorCellEditor</code></code></li>
 * </ul>
 * </p>
 *
 * @see IPropertySource#getPropertyDescriptors
 */
public interface IPropertyDescriptor {
    /**
     * Creates and returns a new cell editor for editing this property. Returns 
     * <code>null</code> if the property is not editable.
     *
     * @param parent the parent widget for the cell editor
     * @return the cell editor for this property, or <code>null</code> if this
     *  property cannot be edited
     */
    public CellEditor createPropertyEditor(Composite parent);

    /**
     * Returns the name of the category to which this property belongs. Properties
     * belonging to the same category are grouped together visually. This localized 
     * string is shown to the user
     * 
     * @return the category name, or <code>null</code> if the default category is to
     *   be used
     */
    public String getCategory();

    /**
     * Returns a brief description of this property. This localized string is shown 
     * to the user when this property is selected.
     * 
     * @return a brief description, or <code>null</code> if none
     */
    public String getDescription();

    /**
     * Returns the display name for this property. This localized string is shown to
     * the user as the name of this property.
     * 
     * @return a displayable name
     */
    public String getDisplayName();

    /**
     * Returns a list of filter types to which this property belongs.
     * The user is able to toggle the filters to show/hide properties belonging to
     * a filter type.
     * <p>
     * Valid values for these flags are declared as constants on 
     *  <code>IPropertySheetEntry</code>
     * </p> 
     * @return a list of filter types to which this property belongs, or
     *   <code>null</code> if none
     */
    public String[] getFilterFlags();

    /**
     * Returns the help context id for this property or 
     * <code>null</code> if this property has no help context id.
     * <p>
     * NOTE: Help support system API's changed since 2.0 and arrays
     * of contexts are no longer supported.
     * </p>
     * <p>
     * Thus the only valid non-<code>null</code> return type for this method
     * is a <code>String</code> representing a context id. The previously
     * valid return types are deprecated. The plural name for this method
     * is unfortunate.
     * </p>
     * 
     * @return the help context id for this entry
     */
    public Object getHelpContextIds();

    /**
     * Returns the id for this property. This object is
     * used internally to distinguish one property descriptor 
     * from another.
     *
     * @return the property id
     */
    public Object getId();

    /**
     * Returns the label provider for this property. The label provider is used
     * to obtain the text (and possible image) for displaying the <it>value</it>
     * of this property.
     *
     * @return the label provider used to display this property
     */
    public ILabelProvider getLabelProvider();

    /**
     * Returns whether this property descriptor and the given one are compatible.
     * <p>
     * The property sheet uses this method during multiple selection to determine
     * whether two property descriptors with the same id are in fact the same 
     * property and can be displayed as a single entry in the property sheet.
     * </p>
     *
     * @param anotherProperty the other property descriptor 
     * @return <code>true</code> if the property descriptors are compatible, and 
     *   <code>false</code> otherwise
     */
    public boolean isCompatibleWith(IPropertyDescriptor anotherProperty);
}
