/*******************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.resources.saveparticipant3;

import org.eclipse.core.resources.ISaveContext;
import org.eclipse.core.runtime.*;

/**
 * This plugin is intended to the .safetable of config files.
 */
public class SaveParticipant3Plugin extends SaveParticipantPlugin {
	protected boolean shouldFail = false;
	protected static final String file1 = "file_1";

	public SaveParticipant3Plugin() {
		super();
	}

	protected IPath getFilePath(String name) {
		return getStateLocation().append(name);
	}

	protected IPath getRealPath(String name, int saveNumber) {
		return getFilePath(name + "." + saveNumber);
	}

	public void prepareToSave(ISaveContext context) throws CoreException {
		resetSaveLifecycleLog();
		context.needSaveNumber();
		IPath file = getFilePath(file1);
		if (context.lookup(file) != null) {
			validate(context);
			shouldFail = true;
			return;
		}
		IPath realPath = getRealPath(file1, context.getSaveNumber());
		context.map(file, realPath);
	}

	public void rollback(ISaveContext context) {
	}

	public void saving(ISaveContext context) throws CoreException {
		if (shouldFail)
			throw new CoreException(new Status(0, getDescriptor().getUniqueIdentifier(), 0, "fake failure", null));
	}

	protected void validate(ISaveContext context) {
		IPath file = getFilePath(file1);
		IPath realPath = getRealPath(file1, context.getPreviousSaveNumber());
		IPath value = context.lookup(file);
		if (value != null && value.equals(realPath))
			return;
		String message = "Name of configuration file is different than expected.";
		saveLifecycleLog.add(new Status(IStatus.ERROR, getDescriptor().getUniqueIdentifier(), 111, message, null));
	}
}
