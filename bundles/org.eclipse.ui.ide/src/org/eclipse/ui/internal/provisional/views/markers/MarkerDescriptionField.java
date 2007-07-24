package org.eclipse.ui.internal.provisional.views.markers;

import org.eclipse.core.resources.IMarker;

import com.ibm.icu.text.CollationKey;

public class MarkerDescriptionField extends MarkerField {

	/**
	 * Create a new instance of the receiver.
	 */
	public MarkerDescriptionField() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.provisional.views.markers.MarkerField#compare(org.eclipse.ui.internal.provisional.views.markers.MarkerItem,
	 *      org.eclipse.ui.internal.provisional.views.markers.MarkerItem)
	 */
	public int compare(MarkerItem item1, MarkerItem item2) {
		return getDescriptionKey(item1).compareTo(getDescriptionKey(item2));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.provisional.views.markers.MarkerField#getColumnWeight()
	 */
	public float getColumnWeight() {
		return 4;
	}

	/**
	 * Return the collation key for the description.
	 * 
	 * @param element
	 * @return
	 */
	private CollationKey getDescriptionKey(Object element) {
		if (element instanceof MarkerEntry)
			return ((MarkerItem) element).getCollationKey(IMarker.MESSAGE,
					MarkerUtilities.EMPTY_STRING);
		return MarkerUtilities.EMPTY_COLLATION_KEY;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.provisional.views.markers.MarkerField#getValue(org.eclipse.ui.internal.provisional.views.markers.MarkerItem)
	 */
	public String getValue(MarkerItem item) {
		return item.getAttributeValue(IMarker.MESSAGE,
				MarkerUtilities.EMPTY_STRING);
	}

}
