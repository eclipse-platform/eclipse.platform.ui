package org.eclipse.team.internal.ccvs.ui.model;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.ui.model.IWorkbenchAdapter;

public abstract class CVSModelElement implements IWorkbenchAdapter {
	/**
	 * Handles exceptions that occur in CVS model elements.
	 */
	protected void handle(Throwable t) {
		CVSUIPlugin.openError(null, null, null, t, CVSUIPlugin.LOG_NONTEAM_EXCEPTIONS);
	}
}

