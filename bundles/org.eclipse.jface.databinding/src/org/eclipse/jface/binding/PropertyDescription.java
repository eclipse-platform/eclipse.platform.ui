package org.eclipse.jface.binding;

/**
 * @since 3.2
 *
 */
public class PropertyDescription {

	private final Object object;
	private final Object propertyID;

	/**
	 * @param object
	 * @param propertyID
	 */
	public PropertyDescription(Object object, Object propertyID) {
		this.object = object;
		this.propertyID = propertyID;
	}

	/**
	 * @return the object
	 */
	public Object getObject() {
		return object;
	}

	/**
	 * @return the property identifier
	 */
	public Object getPropertyID() {
		return propertyID;
	}
}
