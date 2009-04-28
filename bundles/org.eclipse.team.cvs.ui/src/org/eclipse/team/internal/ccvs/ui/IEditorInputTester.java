/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.ICVSResource;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.core.TeamPlugin;
import org.eclipse.ui.IFileEditorInput;

public class IEditorInputTester extends PropertyTester {

	public boolean test(Object receiver, String property, Object[] args,
			Object expectedValue) {

		Boolean actual = Boolean.FALSE;
		if (property.equals("isManaged")) { //$NON-NLS-1$
			if (receiver instanceof IFileEditorInput) {
				IFileEditorInput input = (IFileEditorInput) receiver;
				IFile file = input.getFile();
				ICVSResource cvsResource = CVSWorkspaceRoot
						.getCVSResourceFor(file);
				try {
					actual = new Boolean((cvsResource != null
							&& !cvsResource.isFolder() && cvsResource
							.isManaged()));
				} catch (CVSException e) {
					actual = new Boolean(isEnabledForException(e));
				}
				return (actual.equals(expectedValue));
			}
			return false;
		}

		return false;
	}

	protected boolean isEnabledForException(TeamException exception) {
		if (exception.getStatus().getCode() == IResourceStatus.OUT_OF_SYNC_LOCAL) {
			// Enable the action to allow the user to discover the problem
			return true;
		}
		// We should not open a dialog when determining menu enablement so log
		// it instead
		TeamPlugin.log(exception);
		return false;
	}

}
