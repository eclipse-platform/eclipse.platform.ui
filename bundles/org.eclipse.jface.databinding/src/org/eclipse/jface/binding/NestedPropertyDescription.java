package org.eclipse.jface.binding;

public class NestedPropertyDescription {

	private final IUpdatableValue updatable;
	private final Object propertyID;
	private final Class propertyType;

	public NestedPropertyDescription(IUpdatableValue updatable, Object propertyID, Class propertyType) {
		this.updatable = updatable;
		this.propertyID = propertyID;
		this.propertyType = propertyType;
	}

	public IUpdatableValue getUpdatableValue() {
		return updatable;
	}

	public Object getPropertyID() {
		return propertyID;
	}
	
	public Class getPropertyType() {
		return propertyType;
	}
}
