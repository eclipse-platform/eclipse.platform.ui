package org.eclipse.jface.binding;

public class NestedTableDescription {

	private final IUpdatableValue updatableValue;

	private final Object propertyID;

	private final Object[] columnPropertyIDs;

	private final Class propertyType;

	private final Class[] columnPropertyTypes;

	public NestedTableDescription(IUpdatableValue updatableValue, Object propertyID, Class propertyType,
			Object[] columnPropertyIDs, Class[] columnPropertyTypes) {
		this.updatableValue = updatableValue;
		this.propertyID = propertyID;
		this.propertyType = propertyType;
		this.columnPropertyIDs = columnPropertyIDs;
		this.columnPropertyTypes = columnPropertyTypes;
	}

	public Object getPropertyID() {
		return propertyID;
	}

	public Object[] getColumnPropertyIDs() {
		return columnPropertyIDs;
	}

	public IUpdatableValue getUpdatableValue() {
		return updatableValue;
	}

	public Class[] getColumnPropertyTypes() {
		return columnPropertyTypes;
	}

	public Class getPropertyType() {
		return propertyType;
	}

}
