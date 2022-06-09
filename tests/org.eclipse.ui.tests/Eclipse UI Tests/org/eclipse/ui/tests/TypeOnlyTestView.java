package org.eclipse.ui.tests;

import org.eclipse.ui.views.markers.MarkerSupportView;

public class TypeOnlyTestView extends MarkerSupportView {

	public static final String ID = "org.eclipse.ui.tests.typeOnlyTestView";

	static final String CONTENT_GEN_ID = "org.eclipse.ui.tests.typeOnlyTestViewContentGenerator";

	public TypeOnlyTestView() {
		super(CONTENT_GEN_ID);
	}

}
