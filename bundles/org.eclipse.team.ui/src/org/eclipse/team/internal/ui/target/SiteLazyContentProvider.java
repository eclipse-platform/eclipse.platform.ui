package org.eclipse.team.internal.ui.target;

import org.eclipse.ui.model.WorkbenchContentProvider;

public class SiteLazyContentProvider extends WorkbenchContentProvider {

	public boolean hasChildren(Object element) {
		if (element == null) {
			return false;
		}
		// the + box will always appear, but then disappear
		// if not needed after you first click on it.
		return super.hasChildren(element);
	}
}
