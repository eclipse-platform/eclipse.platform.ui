package org.eclipse.jface.databinding;

/**
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will remain
 * unchanged during the 3.2 release cycle. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * 
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
