package org.eclipse.ui.tests.markers;

import org.eclipse.ui.views.markers.MarkerField;
import org.eclipse.ui.views.markers.MarkerItem;

public class ProblemKeyMarkerField extends MarkerField {

	@Override
	public String getValue(MarkerItem item) {
		if (item == null) {
			return "";
		}
		return item.getAttributeValue("problemKey", "");
	}

}
