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
package org.eclipse.team.internal.ccvs.ui.actions;

import java.lang.reflect.InvocationTargetException;
import org.eclipse.jface.action.IAction;
import org.eclipse.team.internal.ccvs.core.client.Command;
import org.eclipse.team.internal.ccvs.ui.operations.UpdateOperation;

/**
 * Action that runs an update without prompting the user for a tag.
 * 
 * @since 3.1
 */
public class UpdateSilentAction extends UpdateAction {
	public void execute(IAction action) throws InterruptedException, InvocationTargetException {
			new UpdateOperation(getTargetPart(), getSelectedResources(), Command.NO_LOCAL_OPTIONS, null /* no tag */).run();
	}
}
