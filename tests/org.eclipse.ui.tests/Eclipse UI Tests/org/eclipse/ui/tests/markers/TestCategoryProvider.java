package org.eclipse.ui.tests.markers;

import org.eclipse.core.resources.IMarker;
import org.eclipse.ui.views.markers.ISubCategoryProvider;

/**
 * TestCategoryProvider is the test for categories.
 *
 */
public class TestCategoryProvider implements ISubCategoryProvider {

	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.ICategoryProvider#categoryFor(org.eclipse.core.resources.IMarker)
	 */
	public String categoryFor(IMarker marker) {
		return marker.getAttribute("category", "No category");
	}

}
