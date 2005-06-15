/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Gunnar Wagenknecht - fix for bug 21756 (https://bugs.eclipse.org/bugs/show_bug.cgi?id=21756)
 *******************************************************************************/
package org.eclipse.ui.views.properties;

/**
 * Interface to an object which is capable of supplying properties for display
 * by the standard property sheet page implementation (<code>PropertySheetPage</code>).
 * <p>
 * This interface should be implemented by clients.
 * <code>PropertySheetPage</code> discovers the properties to display from
 * currently selected elements. Elements that implement
 * <code>IPropertySource</code> directly are included, as are elements that
 * implement <code>IAdaptable</code> and have an <code>IPropertySource</code>
 * adapter. Clients should implement this interface for any newly-defined
 * elements that are to have properties displayable by
 * <code>PropertySheetPage</code>. Note that in the latter case, the client
 * will also need to register a suitable adapter factory with the platform's
 * adapter manager (<code>Platform.getAdapterManager</code>).
 * </p>
 * 
 * @see org.eclipse.core.runtime.IAdaptable
 * @see org.eclipse.core.runtime.Platform#getAdapterManager()
 * @see org.eclipse.ui.views.properties.PropertySheetPage
 * @see org.eclipse.ui.views.properties.IPropertySource2
 */
public interface IPropertySource {

	/**
	 * Returns a value for this property source that can be edited in a property
	 * sheet.
	 * <p>
	 * This value is used when this <code>IPropertySource</code> is appearing
	 * in the property sheet as the value of a property of some other
	 * <code>IPropertySource</code>
	 * </p>
	 * <p>
	 * This value is passed as the input to a cell editor opening on an
	 * <code>IPropertySource</code>.
	 * </p>
	 * <p>
	 * This value is also used when an <code>IPropertySource</code> is being
	 * used as the value in a <code>setPropertyValue</code> message. The
	 * reciever of the message would then typically use the editable value to
	 * update the original property source or construct a new instance.
	 * </p>
	 * <p>
	 * For example an email address which is a property source may have an
	 * editable value which is a string so that it can be edited in a text cell
	 * editor. The email address would also have a constructor or setter that
	 * takes the edited string so that an appropriate instance can be created or
	 * the original instance modified when the edited value is set.
	 * </p>
	 * <p>
	 * This behavior is important for another reason. When the property sheet is
	 * showing properties for more than one object (multiple selection), a
	 * property sheet entry will display and edit a single value (typically
	 * coming from the first selected object). After a property has been edited
	 * in a cell editor, the same value is set as the property value for all of
	 * the objects. This is fine for primitive types but otherwise all of the
	 * objects end up with a reference to the same value. Thus by creating an
	 * editable value and using it to update the state of the original property
	 * source object, one is able to edit several property source objects at
	 * once (multiple selection).
	 * 
	 * @return a value that can be edited
	 */
	public Object getEditableValue();

	/**
	 * Returns the list of property descriptors for this property source. The
	 * <code>getPropertyValue</code> and <code>setPropertyValue</code>
	 * methods are used to read and write the actual property values by
	 * specifying the property ids from these property descriptors.
	 * <p>
	 * Implementors should cache the descriptors as they will be asked for the
	 * descriptors with any edit/update. Since descriptors provide cell editors,
	 * returning the same descriptors if possible allows for efficient updating.
	 * </p>
	 * 
	 * @return the property descriptors
	 */
	public IPropertyDescriptor[] getPropertyDescriptors();

	/**
	 * Returns the value of the property with the given id if it has one.
	 * Returns <code>null</code> if the property's value is <code>null</code>
	 * value or if this source does not have the specified property.
	 * 
	 * @see #setPropertyValue
	 * @param id
	 *            the id of the property being set
	 * @return the value of the property, or <code>null</code>
	 */
	public Object getPropertyValue(Object id);

	/**
	 * Returns whether the value of the property with the given id has changed
	 * from its default value. Returns <code>false</code> if this source does
	 * not have the specified property.
	 * <p>
	 * If the notion of default value is not meaningful for the specified
	 * property then <code>false</code> is returned.
	 * </p>
	 * 
	 * @param id
	 *            the id of the property
	 * @return <code>true</code> if the value of the specified property has
	 *         changed from its original default value, <code>false</code> if
	 *         the specified property does not have a meaningful default value,
	 *         and <code>false</code> if this source does not have the
	 *         specified property
	 * @see IPropertySource2#isPropertyResettable(Object)
	 * @see #resetPropertyValue(Object)
	 */
	public boolean isPropertySet(Object id);

	/**
	 * Resets the property with the given id to its default value if possible.
	 * <p>
	 * Does nothing if the notion of a default value is not meaningful for the
	 * specified property, or if the property's value cannot be changed, or if
	 * this source does not have the specified property.
	 * </p>
	 * <p>
	 * Callers will check if this <code>IPropertySource</code> implements
	 * <code>IPropertySource2</code> and this method will only be called if
	 * <code>IPropertySource2#isPropertyResettable(Object)</code> returns
	 * <code>true</code> for the property with the given id.
	 * </p>
	 * 
	 * @param id
	 *            the id of the property being reset
	 * @see #isPropertySet(Object)
	 * @see IPropertySource2#isPropertyResettable(Object)
	 */
	public void resetPropertyValue(Object id);

	/**
	 * Sets the property with the given id if possible. Does nothing if the
	 * property's value cannot be changed or if this source does not have the
	 * specified property.
	 * <p>
	 * In general, a property source should not directly reference the value
	 * parameter unless it is an atomic object that can be shared, such as a
	 * string.
	 * </p>
	 * <p>
	 * An important reason for this is that several property sources with
	 * compatible descriptors could be appearing in the property sheet at the
	 * same time. An editor produces a single edited value which is passed as
	 * the value parameter of this message to all the property sources. Thus to
	 * avoid a situation where all of the property sources reference the same
	 * value they should use the value parameter to create a new instance of the
	 * real value for the given property.
	 * </p>
	 * <p>
	 * There is another reason why a level of indirection is useful. The real
	 * value of property may be a type that cannot be edited with a standard
	 * cell editor. However instead of returning the real value in
	 * <code>getPropertyValue</code>, the value could be converted to a
	 * <code>String</code> which could be edited with a standard cell editor.
	 * The edited value will be passed to this method which can then turn it
	 * back into the real property value.
	 * </p>
	 * <p>
	 * Another variation on returning a value other than the real property value
	 * in <code>getPropertyValue</code> is to return a value which is an
	 * <code>IPropertySource</code> (or for which the property sheet can
	 * obtain an <code>IPropertySource</code>). In this case the value to
	 * edit is obtained from the child property source using
	 * <code>getEditableValue</code>. It is this editable value that will be
	 * passed back via this method when it has been editted
	 * </p>
	 * 
	 * @see #getPropertyValue
	 * @see #getEditableValue
	 * @param id
	 *            the id of the property being set
	 * @param value
	 *            the new value for the property; <code>null</code> is allowed
	 */
	public void setPropertyValue(Object id, Object value);
}
