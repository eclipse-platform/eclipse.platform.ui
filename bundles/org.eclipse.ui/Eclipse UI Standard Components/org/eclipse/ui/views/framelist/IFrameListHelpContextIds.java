package org.eclipse.ui.views.framelist;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import org.eclipse.ui.PlatformUI;

/**
 * Help context ids for the frame list.
 * <p>
 * This interface contains constants only; it is not intended to be implemented
 * or extended.
 * </p>
 * 
 */
/*package*/ interface IFrameListHelpContextIds {
	public static final String PREFIX = PlatformUI.PLUGIN_ID + "."; //$NON-NLS-1$

	// Actions
	public static final String BACK_ACTION = PREFIX + "back_action_context"; //$NON-NLS-1$
	public static final String FORWARD_ACTION = PREFIX + "forward_action_context"; //$NON-NLS-1$
	public static final String GO_INTO_ACTION = PREFIX + "go_into_action_context"; //$NON-NLS-1$
	public static final String UP_ACTION = PREFIX + "up_action_context"; //$NON-NLS-1$
}
