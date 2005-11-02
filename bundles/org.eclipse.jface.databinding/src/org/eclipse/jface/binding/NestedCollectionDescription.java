package org.eclipse.jface.binding;

/**
 * @since 3.2
 *
 */
public class NestedCollectionDescription {

	private final IUpdatableValue updatable;

	private final Object propertyID;

	private final Class propertyElementType;

	/**
	 * @param updatable
	 * @param propertyID
	 * @param propertyElementType
	 */
	public NestedCollectionDescription(IUpdatableValue updatable,
			Object propertyID, Class propertyElementType) {
		this.updatable = updatable;
		this.propertyID = propertyID;
		this.propertyElementType = propertyElementType;
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
	public Class getPropertyElementType() {
		return propertyElementType;
	}
}
