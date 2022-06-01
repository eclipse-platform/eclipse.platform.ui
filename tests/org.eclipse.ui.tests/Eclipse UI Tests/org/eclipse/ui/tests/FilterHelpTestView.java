package org.eclipse.ui.tests;

import org.eclipse.ui.views.markers.MarkerSupportView;

/**
 * @since 3.18
 *
 */
public class FilterHelpTestView extends MarkerSupportView {

	public static final String ID = "org.eclipse.ui.tests.filterHelpTestView";

	static final String CONTENT_GEN_ID = "org.eclipse.ui.tests.filterHelpContentGenerator";

	public FilterHelpTestView() {
		super(CONTENT_GEN_ID);
	}

}
