package org.eclipse.ui.internal.forms;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ui.internal.forms.Messages"; //$NON-NLS-1$

	private Messages() {
	}

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	public static String FormText_copy;
	public static String Form_tooltip_minimize;
	public static String Form_tooltip_restore;
}
