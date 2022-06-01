package org.eclipse.ui.tests;

import org.eclipse.ui.views.markers.MarkerSupportView;

/**
 * @since 3.5
 *
 */

public class FilterNoHelpTestView extends MarkerSupportView {

	public static final String ID = "org.eclipse.ui.tests.filterNoHelpTestView";

	static final String CONTENT_GEN_ID = "org.eclipse.ui.tests.filterNoHelpContentGenerator";

	public FilterNoHelpTestView() {
		super(CONTENT_GEN_ID);
	}

}
