package org.eclipse.jface.text.source;

import org.eclipse.osgi.util.NLS;

/**
 * @since 3.14
 */
public class Messages extends NLS {

	/**
	 * @since 3.14
	 */
	private static final String BUNDLE_NAME= Messages.class.getPackageName() + ".messages"; //$NON-NLS-1$

	/**
	 * @since 3.14
	 */
	public static String AnnotationModel_FireModelChangedEventJobTitle;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
