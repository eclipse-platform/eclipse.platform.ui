package org.eclipse.jface.binding.internal;

import org.eclipse.jface.binding.BindingException;
import org.eclipse.jface.binding.DatabindingContext;
import org.eclipse.jface.binding.IChangeEvent;
import org.eclipse.jface.binding.IChangeListener;
import org.eclipse.jface.binding.IUpdatableTable;
import org.eclipse.jface.binding.IUpdatableValue;
import org.eclipse.jface.binding.NestedTableDescription;
import org.eclipse.jface.binding.TableDescription;
import org.eclipse.jface.binding.Updatable;

/**
 * @since 3.2
 * 
 */
public class NestedUpdatableTable extends Updatable implements IUpdatableTable {

	private DatabindingContext databindingContext;

	private IUpdatableTable innerUpdatableTable;

	private NestedTableDescription tableDescription;

	private Object currentOuterValue;

	private IChangeListener innerChangeListener;

	/**
	 * @param context
	 * @param tableDescription
	 */
	public NestedUpdatableTable(DatabindingContext context,
			NestedTableDescription tableDescription) {
		this.databindingContext = context;
		this.tableDescription = tableDescription;
		final IUpdatableValue outerUpdatableValue = tableDescription
				.getUpdatableValue();
		updateInnerUpdatableValue(outerUpdatableValue);
		IChangeListener outerChangeListener = new IChangeListener() {
			public void handleChange(IChangeEvent changeEvent) {
				updateInnerUpdatableValue(outerUpdatableValue);
				fireChangeEvent(null, IChangeEvent.CHANGE, null, null, -1);
			}
		};
		outerUpdatableValue.addChangeListener(outerChangeListener);
	}

	private void updateInnerUpdatableValue(IUpdatableValue outerUpdatableValue) {
		currentOuterValue = outerUpdatableValue.getValue();
		if (innerUpdatableTable != null) {
			innerUpdatableTable.removeChangeListener(innerChangeListener);
			innerUpdatableTable.dispose();
		}
		if (currentOuterValue == null) {
			innerUpdatableTable = null;
		} else {
			try {
				this.innerUpdatableTable = (IUpdatableTable) databindingContext
						.createUpdatable2(new TableDescription(
								currentOuterValue, tableDescription
										.getPropertyID(), tableDescription
										.getColumnPropertyIDs()));
				Class innerElementType = innerUpdatableTable.getElementType();
				if (!tableDescription.getPropertyType()
						.equals(innerElementType)) {
					throw new AssertionError(
							"Cannot change element type in a nested updatable table"); //$NON-NLS-1$
				}
				Class[] innerColumnTypes = innerUpdatableTable.getColumnTypes();
				for (int i = 0; i < innerColumnTypes.length; i++) {
					if (!tableDescription.getColumnPropertyTypes()[i]
							.equals(innerColumnTypes[i])) {
						throw new AssertionError(
								"Cannot change column type in a nested updatable table (column " + i + ")"); //$NON-NLS-1$ //$NON-NLS-2$
					}
				}
			} catch (BindingException e) {
				throw new AssertionError(
						"Binding exception while updating nested updatable table"); //$NON-NLS-1$
			}
			innerUpdatableTable.addChangeListener(innerChangeListener);
		}
	}

	public Class[] getColumnTypes() {
		return tableDescription.getColumnPropertyTypes();
	}

	public Object[] getValues(int index) {
		if (innerUpdatableTable != null) {
			return innerUpdatableTable.getValues(index);
		}
		throw new IndexOutOfBoundsException();
	}

	public void setElementAndValues(int index, Object element, Object[] values) {
		if (innerUpdatableTable != null) {
			innerUpdatableTable.setElementAndValues(index, element, values);
		}
		throw new AssertionError(
				"cannot set element and values because current outer value is null"); //$NON-NLS-1$
	}

	public int addElementWithValues(int index, Object element, Object[] values) {
		if (innerUpdatableTable != null) {
			return innerUpdatableTable.addElementWithValues(index, element,
					values);
		}
		throw new AssertionError(
				"cannot add element and values because current outer value is null"); //$NON-NLS-1$
	}

	public void setValues(int index, Object[] values) {
		if (innerUpdatableTable != null) {
			innerUpdatableTable.setValues(index, values);
		}
		throw new AssertionError(
				"cannot set values because current outer value is null"); //$NON-NLS-1$
	}

	public int getSize() {
		if (innerUpdatableTable != null) {
			return innerUpdatableTable.getSize();
		}
		return 0;
	}

	public int addElement(Object value, int index) {
		if (innerUpdatableTable != null) {
			return innerUpdatableTable.addElement(value, index);
		}
		throw new AssertionError(
				"cannot add element because current outer value is null"); //$NON-NLS-1$
	}

	public void removeElement(int index) {
		if (innerUpdatableTable != null) {
			innerUpdatableTable.removeElement(index);
		}
		throw new AssertionError(
				"cannot remove element because current outer value is null"); //$NON-NLS-1$
	}

	public void setElement(int index, Object value) {
		if (innerUpdatableTable != null) {
			innerUpdatableTable.setElement(index, value);
		}
		throw new AssertionError(
				"cannot set element because current outer value is null"); //$NON-NLS-1$
	}

	public Object getElement(int index) {
		if (innerUpdatableTable != null) {
			return innerUpdatableTable.getElement(index);
		}
		throw new IndexOutOfBoundsException();
	}

	public Class getElementType() {
		return tableDescription.getPropertyType();
	}

}
