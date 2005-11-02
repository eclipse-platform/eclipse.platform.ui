package org.eclipse.jface.databinding;

/**
 * A standard description type representing a property of an updatable object.
 * Description objects can be passed to <code>DatabindingContext.bind()</code>
 * or <code>DatabindingContext.createUpdatable()</code>. They are passed to
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
public class NestedPropertyDescription {

	private final IUpdatableValue updatable;

	private final Object propertyID;

	private final Class propertyType;

	/**
	 * Creates a nested property description from the given updatable value, a
	 * property identifier that applies to possible object values of that
	 * updatable, and the type of that property. The updatable value's value
	 * type must be assignable to <code>java.lang.Object</code>. The given
	 * property must exist for all possible values of the updatable value, and
	 * the property type of the property must match the given property type.
	 * 
	 * @param updatable
	 * @param propertyID
	 * @param propertyType
	 */
	public NestedPropertyDescription(IUpdatableValue updatable,
			Object propertyID, Class propertyType) {
		this.updatable = updatable;
		this.propertyID = propertyID;
		this.propertyType = propertyType;
	}

	/**
	 * Returns the updatable value of this property description.
	 * 
	 * @return the updatable value
	 */
	public IUpdatableValue getUpdatableValue() {
		return updatable;
	}

	/**
	 * Returns the property identifier of this property description.
	 * 
	 * @return the property identifier
	 */
	public Object getPropertyID() {
		return propertyID;
	}

	/**
	 * Returns the property type of this property description.
	 * 
	 * @return the property type
	 */
	public Class getPropertyType() {
		return propertyType;
	}
}
