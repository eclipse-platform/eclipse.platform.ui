/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Gunnar Wagenknecht - fix for bug 21756 [PropertiesView] property view sorting
 *     Kevin Milburn - [Bug 423214] [PropertiesView] add support for IColorProvider and IFontProvider
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 460405
 *******************************************************************************/

package org.eclipse.ui.views.properties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.common.EventManager;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellEditorListener;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

/**
 * <code>PropertySheetEntry</code> is an implementation of
 * <code>IPropertySheetEntry</code> which uses <code>IPropertySource</code>
 * and <code>IPropertyDescriptor</code> to interact with domain model objects.
 * <p>
 * Every property sheet entry has a single descriptor (except the root entry
 * which has none). This descriptor determines what property of its objects it
 * will display/edit.
 * </p>
 * <p>
 * Entries do not listen for changes in their objects. Since there is no
 * restriction on properties being independent, a change in one property may
 * affect other properties. The value of a parent's property may also change. As
 * a result we are forced to refresh the entire entry tree when a property
 * changes value.
 * </p>
 *
 * @since 3.0 (was previously internal)
 */
public class PropertySheetEntry extends EventManager implements IPropertySheetEntry {

	/**
	 * The values we are displaying/editing. These objects repesent the value of
	 * one of the properties of the values of our parent entry. Except for the
	 * root entry where they represent the input (selected) objects.
	 */
	private Object[] values = new Object[0];

	/**
	 * The property sources for the values we are displaying/editing.
	 */
	private Map<Object, IPropertySource> sources = new HashMap<>(0);

	/**
	 * The value of this entry is defined as the the first object in its value
	 * array or, if that object is an <code>IPropertySource</code>, the value
	 * it returns when sent <code>getEditableValue</code>
	 */
	private Object editValue;

	private PropertySheetEntry parent;

	private IPropertySourceProvider propertySourceProvider;

	private IPropertyDescriptor descriptor;

	private CellEditor editor;

	private String errorText;

	private PropertySheetEntry[] childEntries = null;

	/**
	 * Create the CellEditorListener for this entry. It listens for value
	 * changes in the CellEditor, and cancel and finish requests.
	 */
	private ICellEditorListener cellEditorListener = new ICellEditorListener() {
		@Override
		public void editorValueChanged(boolean oldValidState,
				boolean newValidState) {
			if (!newValidState) {
				// currently not valid so show an error message
				setErrorText(editor.getErrorMessage());
			} else {
				// currently valid
				setErrorText(null);
			}
		}

		@Override
		public void cancelEditor() {
			setErrorText(null);
		}

		@Override
		public void applyEditorValue() {
			PropertySheetEntry.this.applyEditorValue();
		}
	};

	@Override
	public void addPropertySheetEntryListener(
			IPropertySheetEntryListener listener) {
		addListenerObject(listener);
	}

	@Override
	public void applyEditorValue() {
		if (editor == null) {
			return;
		}

		// Check if editor has a valid value
		if (!editor.isValueValid()) {
			setErrorText(editor.getErrorMessage());
			return;
		}

		setErrorText(null);

		// See if the value changed and if so update
		Object newValue = editor.getValue();
		boolean changed = false;
		if (values.length > 1) {
			changed = true;
		} else if (editValue == null) {
			if (newValue != null) {
				changed = true;
			}
		} else if (!editValue.equals(newValue)) {
			changed = true;
		}

		// Set the editor value
		if (changed) {
			setValue(newValue);
		}
	}

	/**
	 * Return the unsorted intersection of all the
	 * <code>IPropertyDescriptor</code>s for the objects.
	 *
	 * @return List
	 */
	private List<IPropertyDescriptor> computeMergedPropertyDescriptors() {
		if (values.length == 0) {
			return new ArrayList<>(0);
		}

		IPropertySource firstSource = getPropertySource(values[0]);
		if (firstSource == null) {
			return new ArrayList<>(0);
		}

		if (values.length == 1) {
			return Arrays.asList(firstSource.getPropertyDescriptors());
		}

		// get all descriptors from each object
		@SuppressWarnings("unchecked")
		Map<Object, IPropertyDescriptor>[] propertyDescriptorMaps = new Map[values.length];
		for (int i = 0; i < values.length; i++) {
			Object object = values[i];
			IPropertySource source = getPropertySource(object);
			if (source == null) {
				// if one of the selected items is not a property source
				// then we show no properties
				return new ArrayList<>(0);
			}
			// get the property descriptors keyed by id
			propertyDescriptorMaps[i] = computePropertyDescriptorsFor(source);
		}

		// intersect
		Map<Object, IPropertyDescriptor> intersection = propertyDescriptorMaps[0];
		for (int i = 1; i < propertyDescriptorMaps.length; i++) {
			// get the current ids
			Object[] ids = intersection.keySet().toArray();
			for (Object id : ids) {
				Object object = propertyDescriptorMaps[i].get(id);
				if (object == null || // see if the descriptors (which have the same id) are
				// compatible
						!intersection.get(id).isCompatibleWith((IPropertyDescriptor) object)) {
					intersection.remove(id);
				}
			}
		}

		// sorting is handled in the PropertySheetViewer, return unsorted (in
		// the original order)
		ArrayList<IPropertyDescriptor> result = new ArrayList<>(intersection.size());
		IPropertyDescriptor[] firstDescs = firstSource.getPropertyDescriptors();
		for (IPropertyDescriptor desc : firstDescs) {
			if (intersection.containsKey(desc.getId())) {
				result.add(desc);
			}
		}
		return result;
	}

	/**
	 * Returns an map of property descriptors (keyed on id) for the given
	 * property source.
	 *
	 * @param source
	 *            a property source for which to obtain descriptors
	 * @return a table of descriptors keyed on their id
	 */
	private Map<Object, IPropertyDescriptor> computePropertyDescriptorsFor(IPropertySource source) {
		IPropertyDescriptor[] descriptors = source.getPropertyDescriptors();
		Map<Object, IPropertyDescriptor> result = new HashMap<>(descriptors.length * 2 + 1);
		for (IPropertyDescriptor desc : descriptors) {
			result.put(desc.getId(), desc);
		}
		return result;
	}

	/**
	 * Create our child entries.
	 */
	private void createChildEntries() {
		// get the current descriptors
		List<IPropertyDescriptor> descriptors = computeMergedPropertyDescriptors();

		// rebuild child entries using old when possible
		PropertySheetEntry[] newEntries = new PropertySheetEntry[descriptors
				.size()];
		for (int i = 0; i < descriptors.size(); i++) {
			IPropertyDescriptor d = descriptors.get(i);
			// create new entry
			PropertySheetEntry entry = createChildEntry();
			entry.setDescriptor(d);
			entry.setParent(this);
			entry.setPropertySourceProvider(propertySourceProvider);
			entry.refreshValues();
			newEntries[i] = entry;
		}
		// only assign if successful
		childEntries = newEntries;
	}

	/**
	 * Factory method to create a new child <code>PropertySheetEntry</code>
	 * instance.
	 * <p>
	 * Subclasses may overwrite to create new instances of their own class.
	 * </p>
	 *
	 * @return a new <code>PropertySheetEntry</code> instance for the
	 *         descriptor passed in
	 * @since 3.1
	 */
	protected PropertySheetEntry createChildEntry() {
		return new PropertySheetEntry();
	}

	@Override
	public void dispose() {
		if (editor != null) {
			editor.dispose();
			editor = null;
		}
		// recursive call to dispose children
		PropertySheetEntry[] entriesToDispose = childEntries;
		childEntries = null;
		if (entriesToDispose != null) {
			for (PropertySheetEntry element : entriesToDispose) {
				// an error in a property source may cause refreshChildEntries
				// to fail. Since the Workbench handles such errors we
				// can be left in a state where a child entry is null.
				if (element != null) {
					element.dispose();
				}
			}
		}
	}

	/**
	 * The child entries of this entry have changed (children added or removed).
	 * Notify all listeners of the change.
	 */
	private void fireChildEntriesChanged() {
		Object[] array = getListeners();
		for (Object element : array) {
			IPropertySheetEntryListener listener = (IPropertySheetEntryListener) element;
			listener.childEntriesChanged(this);
		}
	}

	/**
	 * The error message of this entry has changed. Notify all listeners of the
	 * change.
	 */
	private void fireErrorMessageChanged() {
		Object[] array = getListeners();
		for (Object element : array) {
			IPropertySheetEntryListener listener = (IPropertySheetEntryListener) element;
			listener.errorMessageChanged(this);
		}
	}

	/**
	 * The values of this entry have changed. Notify all listeners of the
	 * change.
	 */
	private void fireValueChanged() {
		Object[] array = getListeners();
		for (Object element : array) {
			IPropertySheetEntryListener listener = (IPropertySheetEntryListener) element;
			listener.valueChanged(this);
		}
	}

	@Override
	public String getCategory() {
		return descriptor.getCategory();
	}

	@Override
	public IPropertySheetEntry[] getChildEntries() {
		if (childEntries == null) {
			createChildEntries();
		}
		return childEntries;
	}

	@Override
	public String getDescription() {
		return descriptor.getDescription();
	}

	/**
	 * Returns the descriptor for this entry.
	 *
	 * @return the descriptor for this entry
	 * @since 3.1 (was previously private)
	 */
	protected IPropertyDescriptor getDescriptor() {
		return descriptor;
	}

	@Override
	public String getDisplayName() {
		return descriptor.getDisplayName();
	}

	@Override
	public CellEditor getEditor(Composite parent) {

		if (editor == null) {
			editor = descriptor.createPropertyEditor(parent);
			if (editor != null) {
				editor.addListener(cellEditorListener);
			}
		}
		if (editor != null) {
			editor.setValue(editValue);
			setErrorText(editor.getErrorMessage());
		}
		return editor;
	}

	/**
	 * Returns the edit value for the object at the given index.
	 *
	 * @param index
	 *            the value object index
	 * @return the edit value for the object at the given index
	 */
	protected Object getEditValue(int index) {
		Object value = values[index];
		IPropertySource source = getPropertySource(value);
		if (source != null) {
			value = source.getEditableValue();
		}
		return value;
	}

	@Override
	public String getErrorText() {
		return errorText;
	}

	@Override
	public String getFilters()[] {
		return descriptor.getFilterFlags();
	}

	@Override
	public Object getHelpContextIds() {
		return descriptor.getHelpContextIds();
	}

	@Override
	public Image getImage() {
		ILabelProvider provider = descriptor.getLabelProvider();
		if (provider == null) {
			return null;
		}
		return provider.getImage(editValue);
	}

	/**
	 * Returns the parent of this entry.
	 *
	 * @return the parent entry, or <code>null</code> if it has no parent
	 * @since 3.1
	 */
	protected PropertySheetEntry getParent() {
		return parent;
	}

	/**
	 * Returns an property source for the given object.
	 *
	 * @param object
	 *            an object for which to obtain a property source or
	 *            <code>null</code> if a property source is not available
	 * @return an property source for the given object
	 * @since 3.1 (was previously private)
	 */
	protected IPropertySource getPropertySource(Object object) {
		if (sources.containsKey(object))
			return sources.get(object);

		IPropertySource result = null;
		IPropertySourceProvider provider = propertySourceProvider;

		if (provider == null && object != null) {
			provider = Adapters.adapt(object, IPropertySourceProvider.class);
		}

		if (provider != null) {
			result = provider.getPropertySource(object);
		} else {
			result = Adapters.adapt(object, IPropertySource.class);
		}

		sources.put(object, result);
		return result;
	}

	@Override
	public String getValueAsString() {
		if (editValue == null) {
			return "";//$NON-NLS-1$
		}
		ILabelProvider provider = descriptor.getLabelProvider();
		if (provider == null) {
			return editValue.toString();
		}
		String text = provider.getText(editValue);
		if (text == null) {
			return "";//$NON-NLS-1$
		}
		return text;
	}

	/**
	 * Returns the value objects of this entry.
	 *
	 * @return the value objects of this entry
	 * @since 3.1 (was previously private)
	 */
	public Object[] getValues() {
		return values;
	}

	@Override
	public boolean hasChildEntries() {
		if (childEntries != null && childEntries.length > 0) {
			return true;
		}
		// see if we could have entires if we were asked
		return computeMergedPropertyDescriptors().size() > 0;
	}

	/**
	 * Update our child entries. This implementation tries to reuse child
	 * entries if possible (if the id of the new descriptor matches the
	 * descriptor id of the old entry).
	 */
	private void refreshChildEntries() {
		if (childEntries == null) {
			// no children to refresh
			return;
		}

		// get the current descriptors
		List<IPropertyDescriptor> descriptors = computeMergedPropertyDescriptors();

		// cache old entries by their descriptor id
		Map<Object, PropertySheetEntry> entryCache = new HashMap<>(childEntries.length * 2 + 1);
		for (PropertySheetEntry childEntry : childEntries) {
			if (childEntry != null) {
				entryCache.put(childEntry.getDescriptor().getId(), childEntry);
			}
		}

		// create a list of entries to dispose
		List<PropertySheetEntry> entriesToDispose = new ArrayList<>(Arrays.asList(childEntries));

		// clear the old entries
		this.childEntries = null;

		// rebuild child entries using old when possible
		PropertySheetEntry[] newEntries = new PropertySheetEntry[descriptors.size()];
		boolean entriesChanged = descriptors.size() != entryCache.size();
		for (int i = 0; i < descriptors.size(); i++) {
			IPropertyDescriptor d = descriptors.get(i);
			// see if we have an entry matching this descriptor
			PropertySheetEntry entry = entryCache.get(d.getId());
			if (entry != null) {
				// reuse old entry
				entry.setDescriptor(d);
				entriesToDispose.remove(entry);
			} else {
				// create new entry
				entry = createChildEntry();
				entry.setDescriptor(d);
				entry.setParent(this);
				entry.setPropertySourceProvider(propertySourceProvider);
				entriesChanged = true;
			}
			entry.refreshValues();
			newEntries[i] = entry;
		}

		// only assign if successful
		this.childEntries = newEntries;

		if (entriesChanged) {
			fireChildEntriesChanged();
		}

		// Dispose of entries which are no longer needed
		for (PropertySheetEntry element : entriesToDispose) {
			element.dispose();
		}
	}

	/**
	 * Refresh the entry tree from the root down.
	 *
	 * @since 3.1 (was previously private)
	 */
	protected void refreshFromRoot() {
		if (parent == null) {
			refreshChildEntries();
		} else {
			parent.refreshFromRoot();
		}
	}

	/**
	 * Update our value objects. We ask our parent for the property values based
	 * on our descriptor.
	 */
	private void refreshValues() {
		// get our parent's value objects
		Object[] currentSources = parent.getValues();

		// loop through the objects getting our property value from each
		Object[] newValues = new Object[currentSources.length];
		for (int i = 0; i < currentSources.length; i++) {
			IPropertySource source = parent.getPropertySource(currentSources[i]);
			newValues[i] = source.getPropertyValue(descriptor.getId());
		}

		// set our new values
		setValues(newValues);
	}

	@Override
	public void removePropertySheetEntryListener(IPropertySheetEntryListener listener) {
		removeListenerObject(listener);
	}

	@Override
	public void resetPropertyValue() {
		if (parent == null) {
			// root does not have a default value
			return;
		}

		// Use our parent's values to reset our values.
		boolean change = false;
		Object[] objects = parent.getValues();
		for (Object object : objects) {
			IPropertySource source = getPropertySource(object);
			if (source.isPropertySet(descriptor.getId())) {
				// fix for https://bugs.eclipse.org/bugs/show_bug.cgi?id=21756
				if (source instanceof IPropertySource2) {
					IPropertySource2 extendedSource = (IPropertySource2) source;
					// continue with next if property is not resettable
					if (!extendedSource.isPropertyResettable(descriptor.getId())) {
						continue;
					}
				}
				source.resetPropertyValue(descriptor.getId());
				change = true;
			}
		}
		if (change) {
			refreshFromRoot();
		}
	}

	/**
	 * Set the descriptor.
	 */
	private void setDescriptor(IPropertyDescriptor newDescriptor) {
		// if our descriptor is changing, we have to get rid
		// of our current editor if there is one
		if (descriptor != newDescriptor && editor != null) {
			editor.dispose();
			editor = null;
		}
		descriptor = newDescriptor;
	}

	/**
	 * Set the error text. This should be set to null when the current value is
	 * valid, otherwise it should be set to a error string
	 */
	private void setErrorText(String newErrorText) {
		errorText = newErrorText;
		// inform listeners
		fireErrorMessageChanged();
	}

	/**
	 * Sets the parent of the entry to be propertySheetEntry.
	 */
	private void setParent(PropertySheetEntry propertySheetEntry) {
		parent = propertySheetEntry;
	}

	/**
	 * Sets a property source provider for this entry. This provider is used to
	 * obtain an <code>IPropertySource</code> for each of this entries
	 * objects. If no provider is set then a default provider is used.
	 *
	 * @param provider
	 *            IPropertySourceProvider
	 */
	public void setPropertySourceProvider(IPropertySourceProvider provider) {
		propertySourceProvider = provider;
	}

	/**
	 * Set the value for this entry.
	 * <p>
	 * We set the given value as the value for all our value objects. We then
	 * call our parent to update the property we represent with the given value.
	 * We then trigger a model refresh.
	 * <p>
	 *
	 * @param newValue
	 *            the new value
	 */
	private void setValue(Object newValue) {
		// Set the value
		for (int i = 0; i < values.length; i++) {
			values[i] = newValue;
		}

		// Inform our parent
		parent.valueChanged(this);

		// Refresh the model
		refreshFromRoot();
	}

	/**
	 * The <code>PropertySheetEntry</code> implmentation of this method
	 * declared on<code>IPropertySheetEntry</code> will obtain an editable
	 * value for the given objects and update the child entries.
	 * <p>
	 * Updating the child entries will typically call this method on the child
	 * entries and thus the entire entry tree is updated
	 * </p>
	 *
	 * @param objects
	 *            the new values for this entry
	 */
	@Override
	public void setValues(Object[] objects) {
		values = objects;
		sources = new HashMap<>(values.length * 2 + 1);

		if (values.length == 0) {
			editValue = null;
		} else {
			// set the first value object as the entry's value
			Object newValue = values[0];

			// see if we should convert the value to an editable value
			IPropertySource source = getPropertySource(newValue);
			if (source != null) {
				newValue = source.getEditableValue();
			}
			editValue = newValue;
		}

		// update our child entries
		refreshChildEntries();

		// inform listeners that our value changed
		fireValueChanged();
	}

	/**
	 * The value of the given child entry has changed. Therefore we must set
	 * this change into our value objects.
	 * <p>
	 * We must inform our parent so that it can update its value objects
	 * </p>
	 * <p>
	 * Subclasses may override to set the property value in some custom way.
	 * </p>
	 *
	 * @param child
	 *            the child entry that changed its value
	 */
	protected void valueChanged(PropertySheetEntry child) {
		for (int i = 0; i < values.length; i++) {
			IPropertySource source = getPropertySource(values[i]);
			source.setPropertyValue(child.getDescriptor().getId(), child.getEditValue(i));
		}

		// inform our parent
		if (parent != null) {
			parent.valueChanged(this);
		}
	}

	/**
	 * Returns the foreground color for the entry.
	 *
	 * @return the foreground color for the entry, or <code>null</code> to use the default
	 *         foreground color
	 * @since 3.7
	 */
	protected Color getForeground() {
		ILabelProvider provider = descriptor.getLabelProvider();
		if (provider instanceof IColorProvider) {
			return ((IColorProvider) provider).getForeground(this);
		}
		return null;
	}

	/**
	 * Returns the background color for the entry.
	 *
	 * @return the background color for the entry, or <code>null</code> to use the default
	 *         background color
	 * @since 3.7
	 */
	protected Color getBackground() {
		ILabelProvider provider = descriptor.getLabelProvider();
		if (provider instanceof IColorProvider) {
			return ((IColorProvider) provider).getBackground(this);
		}
		return null;
	}

	/**
	 * Returns the font for the entry.
	 *
	 * @return the font for the entry, or <code>null</code> to use the default font
	 * @since 3.7
	 */
	protected Font getFont() {
		ILabelProvider provider = descriptor.getLabelProvider();
		if (provider instanceof IFontProvider) {
			return ((IFontProvider) provider).getFont(this);
		}
		return null;
	}
}
