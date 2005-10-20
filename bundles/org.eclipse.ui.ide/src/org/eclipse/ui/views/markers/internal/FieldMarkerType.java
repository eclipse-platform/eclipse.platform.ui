package org.eclipse.ui.views.markers.internal;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.graphics.Image;

/**
 * FieldMarkerType is the field entry for marker types
 *
 */
public class FieldMarkerType implements IField {

	private MarkerTypesModel typesModel;

	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.IField#getDescription()
	 */
	public String getDescription() {
		return MarkerMessages.description_type;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.IField#getDescriptionImage()
	 */
	public Image getDescriptionImage() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.IField#getColumnHeaderText()
	 */
	public String getColumnHeaderText() {
		return Util.EMPTY_STRING;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.IField#getColumnHeaderImage()
	 */
	public Image getColumnHeaderImage() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.IField#getValue(java.lang.Object)
	 */
	public String getValue(Object obj) {
		if (obj instanceof ConcreteMarker) {
			try {
				String typeId =  ((ConcreteMarker) obj).getMarker().getType();
				MarkerType type = getMarkerTypesModel().getType(typeId);
				return type.getLabel();
			} catch (CoreException e) {
				return e.getLocalizedMessage();
			}
		}
		return Util.EMPTY_STRING;
	}

	/**
	 * Get the types model for the receiver.
	 * @return MarkerTypesModel
	 */
	private MarkerTypesModel getMarkerTypesModel() {
		if(typesModel == null)
			typesModel = new MarkerTypesModel();
		return typesModel;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.IField#getImage(java.lang.Object)
	 */
	public Image getImage(Object obj) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.IField#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(Object obj1, Object obj2) {
		if (obj1 == null || obj2 == null || !(obj1 instanceof ConcreteMarker)
				|| !(obj2 instanceof ConcreteMarker)) {
			return 0;
		}

		ConcreteMarker marker1 = (ConcreteMarker) obj1;
		ConcreteMarker marker2 = (ConcreteMarker) obj2;

		return getValue(marker1).compareTo(getValue(marker2));
	}

	public String getCategoryValue(Object obj) {
		return getValue(obj);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.IField#isCategoryField()
	 */
	public boolean isCategoryField() {
		return true;
	}

}
