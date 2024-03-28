package org.eclipse.ui.tests;

import org.eclipse.ui.views.markers.MarkerSupportView;

/**
 * @since 3.18
 *
 */
public class NoScopeConfigTestView extends MarkerSupportView {

	public static final String ID = "org.eclipse.ui.tests.noScopeConfigTestView";

	static final String CONTENT_GEN_ID = "org.eclipse.ui.tests.noScopeConfigContentGenerator";

	public NoScopeConfigTestView() {
		super(CONTENT_GEN_ID);
	}

}
