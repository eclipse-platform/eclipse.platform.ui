package org.eclipse.jface.text.internal;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME= Messages.class.getPackageName() + ".messages"; //$NON-NLS-1$

	public static String RegExUtils_0;

	public static String RegExUtils_IllegalPositionForRegEx;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
