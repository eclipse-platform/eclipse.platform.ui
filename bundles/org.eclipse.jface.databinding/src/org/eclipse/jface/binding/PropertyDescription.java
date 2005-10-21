package org.eclipse.jface.binding;

public class PropertyDescription {

	private final Object object;
	private final Object propertyID;

	public PropertyDescription(Object object, Object propertyID) {
		this.object = object;
		this.propertyID = propertyID;
	}

	public Object getObject() {
		return object;
	}

	public Object getPropertyID() {
		return propertyID;
	}
}
