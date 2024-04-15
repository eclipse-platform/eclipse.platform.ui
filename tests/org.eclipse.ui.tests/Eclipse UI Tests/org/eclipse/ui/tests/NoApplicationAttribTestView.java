package org.eclipse.ui.tests;

import org.eclipse.ui.views.markers.MarkerSupportView;

/**
 * @since 3.5
 *
 */
public class NoApplicationAttribTestView extends MarkerSupportView {
	public static final String ID = "org.eclipse.ui.tests.noApplicationAttribTestView";

	static final String CONTENT_GEN_ID = "org.eclipse.ui.tests.noApplicationAttribTestViewContentGenerator";

	public NoApplicationAttribTestView() {
		super(CONTENT_GEN_ID);
	}

}
