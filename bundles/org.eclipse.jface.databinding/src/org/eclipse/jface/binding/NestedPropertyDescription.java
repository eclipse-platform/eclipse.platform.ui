package org.eclipse.jface.binding;

/**
 * @since 3.2
 *
 */
public class NestedPropertyDescription {

	private final IUpdatableValue updatable;
	private final Object propertyID;
	private final Class propertyType;

	/**
	 * @param updatable
	 * @param propertyID
	 * @param propertyType
	 */
	public NestedPropertyDescription(IUpdatableValue updatable, Object propertyID, Class propertyType) {
		this.updatable = updatable;
		this.propertyID = propertyID;
		this.propertyType = propertyType;
	}

	/**
	 * @return
	 */
	public IUpdatableValue getUpdatableValue() {
		return updatable;
	}

	/**
	 * @return
	 */
	public Object getPropertyID() {
		return propertyID;
	}
	
	/**
	 * @return
	 */
	public Class getPropertyType() {
		return propertyType;
	}
}
