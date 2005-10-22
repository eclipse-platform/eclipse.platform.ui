package org.eclipse.jface.binding;

public class TableDescription2 {

	private final Object object;

	private final Object propertyID;

	private final Object[] columnPropertyIDs;

	public TableDescription2(Object object, Object propertyID,
			Object[] columnPropertyIDs) {
		this.object = object;
		this.propertyID = propertyID;
		this.columnPropertyIDs = columnPropertyIDs;
	}

	public Object getPropertyID() {
		return propertyID;
	}

	public Object[] getColumnPropertyIDs() {
		return columnPropertyIDs;
	}

	public Object getObject() {
		return object;
	}

}
