package org.eclipse.jface.binding;

public class TableDescription2 {

	private final Object object;

	private final Object propertyID;

	private final Object[] columnPropertyIDs;

	private IConverter[] columnConverters;

	private IValidator[] columnValidators;

	public TableDescription2(Object object, Object propertyID,
			Object[] columnPropertyIDs, IConverter[] columnConverters, IValidator[] columnValidators) {
		this.object = object;
		this.propertyID = propertyID;
		this.columnPropertyIDs = columnPropertyIDs;
		this.columnConverters = columnConverters;
		this.columnValidators = columnValidators;
	}
	
	public TableDescription2(Object object, Object propertyID,
			Object[] columnPropertyIDs) {
		this(object,propertyID,columnPropertyIDs,null,null);
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
