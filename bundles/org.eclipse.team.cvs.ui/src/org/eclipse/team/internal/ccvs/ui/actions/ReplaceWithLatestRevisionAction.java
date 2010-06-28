/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui.actions;

import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.ui.operations.ReplaceOperation;


public class ReplaceWithLatestRevisionAction extends ReplaceWithTagAction {

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.actions.ReplaceWithTagAction.getTag(ReplaceOperation)
	 */
	protected CVSTag getTag(ReplaceOperation replaceOperation) {
		return CVSTag.BASE;
	}
}