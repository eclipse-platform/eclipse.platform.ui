package org.eclipse.ui.views.markers.internal;

import org.eclipse.core.resources.IProject;
import org.eclipse.swt.graphics.Image;

/**
 * FieldProject is the field that supplies the project.
 *
 */
public class FieldProject implements IField {

	public String getDescription() {
		return MarkerMessages.description_project;
	}

	public Image getDescriptionImage() {
		return null;
	}

	public String getColumnHeaderText() {
		return Util.EMPTY_STRING;
	}

	public Image getColumnHeaderImage() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.markers.internal.IField#getValue(java.lang.Object)
	 */
	public String getValue(Object obj) {
		if (obj instanceof ConcreteMarker) {
			IProject project = ((ConcreteMarker) obj).getMarker().getResource().getProject();
			if(project != null)
				return project.getName();
		}
		return Util.EMPTY_STRING;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.markers.internal.IField#getImage(java.lang.Object)
	 */
	public Image getImage(Object obj) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.markers.internal.IField#compare(java.lang.Object,
	 *      java.lang.Object)
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.markers.internal.IField#getCategoryValue(java.lang.Object)
	 */
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
