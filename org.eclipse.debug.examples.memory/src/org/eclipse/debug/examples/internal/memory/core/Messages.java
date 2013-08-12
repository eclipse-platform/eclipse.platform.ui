package org.eclipse.debug.examples.internal.memory.core;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.debug.examples.internal.memory.core.messages"; //$NON-NLS-1$
	public static String SampleDebugTarget_0;
	public static String SampleDebugTarget_1;
	public static String SampleMemoryBlock_0;
	public static String SampleRegisterGroup_0;
	public static String SampleStackFrame_0;
	public static String SampleThread_0;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
