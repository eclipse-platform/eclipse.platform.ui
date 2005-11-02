package org.eclipse.jface.databinding;

/**
 * A standard description type representing a property of an object. Description
 * objects can be passed to <code>DatabindingContext.bind()</code> or
 * <code>DatabindingContext.createUpdatable()</code>. They are passed to
 * <code>IUpdatableFactory.createUpdatable()</code> to create an updatable
 * object. It is up to the IUpdatableFactory objects to interpret this
 * description.
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
	 * Creates a new property description from the given object and property
	 * identifier.
	 * 
	 * @param object
	 * @param propertyID
	 */
	public PropertyDescription(Object object, Object propertyID) {
		this.object = object;
		this.propertyID = propertyID;
	}

	/**
	 * Returns the object of this property description.
	 * 
	 * @return the object
	 */
	public Object getObject() {
		return object;
	}

	/**
	 * Returns the property identifier of this property description.
	 * 
	 * @return the property identifier
	 */
	public Object getPropertyID() {
		return propertyID;
	}
}
