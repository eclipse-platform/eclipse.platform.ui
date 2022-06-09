package org.eclipse.ui.tests;

import org.eclipse.ui.views.markers.MarkerSupportView;

/**
 * @since 3.5
 *
 */

public class SubTypeOnlyTestView extends MarkerSupportView {
	public static final String ID = "org.eclipse.ui.tests.subTypeOnlyTestView";

	static final String CONTENT_GEN_ID = "org.eclipse.ui.tests.subTypeOnlyTestViewContentGenerator";

	public SubTypeOnlyTestView() {
		super(CONTENT_GEN_ID);
	}

}
