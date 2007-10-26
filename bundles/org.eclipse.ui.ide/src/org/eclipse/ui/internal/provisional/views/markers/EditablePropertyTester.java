package org.eclipse.ui.internal.provisional.views.markers;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IMarker;

/**
 * EditablePropertyTester is a property tester for the editable property of the
 * selected marker.
 * 
 * @since 3.4
 * 
 */
public class EditablePropertyTester extends PropertyTester {

	private static final Object EDITABLE = "editable"; //$NON-NLS-1$

	/**
	 * Create a new instance of the receiver.
	 */
	public EditablePropertyTester() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.expressions.IPropertyTester#test(java.lang.Object, java.lang.String, java.lang.Object[], java.lang.Object)
	 */
	public boolean test(Object receiver, String property, Object[] args,
			Object expectedValue) {
		if (property.equals(EDITABLE)) {
			IMarker marker = ((MarkerEntry) receiver).getMarker();
			if (marker != null)
				return marker.getAttribute(IMarker.USER_EDITABLE, false);
		}
		return false;
	}

}
