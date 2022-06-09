package org.eclipse.ui.tests;

import org.eclipse.ui.views.markers.MarkerSupportView;

/**
 * @since 3.5
 *
 */

public class TypeAndSubTypeTestView extends MarkerSupportView {
	public static final String ID = "org.eclipse.ui.tests.typeAndSubTypeTestView";

	static final String CONTENT_GEN_ID = "org.eclipse.ui.tests.typeAndSubTypeTestViewContentGenerator";

	public TypeAndSubTypeTestView() {
		super(CONTENT_GEN_ID);
	}

}
