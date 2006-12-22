/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.internal.ccvs.ui;

import org.eclipse.core.resources.IResource;
import org.eclipse.team.internal.ccvs.core.resources.RemoteFile;
import org.eclipse.team.ui.history.HistoryPageSource;
import org.eclipse.ui.part.Page;

public class CVSHistoryPageSource extends HistoryPageSource {

	public Page createPage(Object object) {
		CVSHistoryPage page = new CVSHistoryPage(object);
		return page;
	}

	public boolean canShowHistoryFor(Object object) {
		if (object instanceof IResource && ((IResource) object).getType() == IResource.FILE)
			return true;
		if (object instanceof RemoteFile) {
			return true;
		}
		return false;
	}

}
