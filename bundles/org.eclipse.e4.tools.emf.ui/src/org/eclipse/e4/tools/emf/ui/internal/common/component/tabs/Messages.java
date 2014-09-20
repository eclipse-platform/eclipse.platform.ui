package org.eclipse.e4.tools.emf.ui.internal.common.component.tabs;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.e4.tools.emf.ui.internal.common.component.tabs.messages"; //$NON-NLS-1$
	public static String TitleAreaFilterDialog_enterFilterText;
	public static String TitleAreaFilterDialogWithEmptyOptions_emptyValueDescription;
	public static String TitleAreaFilterDialogWithEmptyOptions_excludeEmptyValues;
	public static String TitleAreaFilterDialogWithEmptyOptions_includeEmptyValues;
	public static String TitleAreaFilterDialogWithEmptyOptions_onlyEmptyValues;
	public static String EmfUtil_ex_attribute_not_found;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
