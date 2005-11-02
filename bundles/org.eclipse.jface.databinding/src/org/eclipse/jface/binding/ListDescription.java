package org.eclipse.jface.binding;

/**
 * @since 3.2
 *
 */
public class ListDescription {

	private final Object object;

	private final Object propertyID;

	private final Object labelPropertyID;

	/**
	 * @param object
	 * @param propertyID
	 * @param labelPropertyID
	 */
	public ListDescription(Object object, Object propertyID,
			Object labelPropertyID) {
		this.object = object;
		this.propertyID = propertyID;
		this.labelPropertyID = labelPropertyID;
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
	public Object getLabelPropertyID() {
		return labelPropertyID;
	}

	/**
	 * @return
	 */
	public Object getObject() {
		return object;
	}

}
