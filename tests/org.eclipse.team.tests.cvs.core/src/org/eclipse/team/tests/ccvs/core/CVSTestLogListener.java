/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.tests.ccvs.core;

import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.team.internal.ccvs.core.CVSException;

/**
 * Listener that accumulates test errors
 */
public class CVSTestLogListener implements ILogListener {

	Map errors = new HashMap();
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.ILogListener#logging(org.eclipse.core.runtime.IStatus, java.lang.String)
	 */
	public void logging(IStatus status, String plugin) {
		List pluginErrors = (List)errors.get(plugin);
		if (pluginErrors == null) {
			pluginErrors = new ArrayList();
			errors.put(plugin, pluginErrors);
		}
		pluginErrors.add(status);
	}

	public void checkErrors() throws CoreException {
		if (errors.isEmpty()) return;
		List allErrors = new ArrayList();
		for (Iterator iter = errors.values().iterator(); iter.hasNext();) {
			allErrors.addAll((List)iter.next());
		}
		errors.clear();
		if (allErrors.isEmpty()) return;
		IStatus status = null;
		if (allErrors.size() == 1) {
			status = (IStatus)allErrors.get(0);
			if (!status.isMultiStatus()) {
				throw new CVSException(status);
			}
		}
		if (status == null) {
			status = new MultiStatus("org.eclipse.team.tests.cvs.core", 0, 
					(IStatus[]) allErrors.toArray(new IStatus[allErrors.size()]), 
					"Errors were logged during this test. Check the log file for details", null);
		}
		throw new CoreException(status);
	}
}
