package org.eclipse.jface.binding;

public class ListDescription {

	private final Object object;

	private final Object propertyID;

	private final Object labelPropertyID;

	public ListDescription(Object object, Object propertyID,
			Object labelPropertyID) {
		this.object = object;
		this.propertyID = propertyID;
		this.labelPropertyID = labelPropertyID;
	}

	public Object getPropertyID() {
		return propertyID;
	}

	public Object getLabelPropertyID() {
		return labelPropertyID;
	}

	public Object getObject() {
		return object;
	}

}
