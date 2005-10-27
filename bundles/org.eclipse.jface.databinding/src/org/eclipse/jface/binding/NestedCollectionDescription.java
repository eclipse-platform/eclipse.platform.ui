package org.eclipse.jface.binding;

public class NestedCollectionDescription {

	private final IUpdatableValue updatable;

	private final Object propertyID;

	private final Class propertyElementType;

	public NestedCollectionDescription(IUpdatableValue updatable,
			Object propertyID, Class propertyElementType) {
		this.updatable = updatable;
		this.propertyID = propertyID;
		this.propertyElementType = propertyElementType;
	}

	public IUpdatableValue getUpdatableValue() {
		return updatable;
	}

	public Object getPropertyID() {
		return propertyID;
	}

	public Class getPropertyElementType() {
		return propertyElementType;
	}
}
