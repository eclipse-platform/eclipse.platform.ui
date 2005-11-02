package org.eclipse.jface.binding;

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
