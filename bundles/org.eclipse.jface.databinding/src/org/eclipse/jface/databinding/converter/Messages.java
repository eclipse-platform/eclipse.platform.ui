package org.eclipse.jface.databinding.converter;

import org.eclipse.osgi.util.NLS;

/**
 * Messages used by the converter framework
 * 
 * @since 3.2
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.jface.databinding.converter.messages"; //$NON-NLS-1$

	private Messages() {
	}

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	public static String Boolean_Yes;

	public static String Boolean_yes;

	public static String Boolean_true;

	public static String Boolean_True;

	public static String Boolean_No;

	public static String Boolean_no;

	public static String Boolean_false;

	public static String Boolean_False;

	public static String Boolean_ERROR;

}
