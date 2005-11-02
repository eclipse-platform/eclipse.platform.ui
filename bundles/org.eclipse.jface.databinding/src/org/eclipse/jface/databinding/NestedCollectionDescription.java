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
