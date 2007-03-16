/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.internal.ccvs.ui;

import org.eclipse.team.ui.history.HistoryPageSource;
import org.eclipse.ui.part.Page;

public class CVSHistoryPageSource extends HistoryPageSource {

	public Page createPage(Object object) {
		CVSHistoryPage page = new CVSHistoryPage(object);
		return page;
	}

	public boolean canShowHistoryFor(Object object) {
		return CVSHistoryPage.getCVSFile(object) != null;
	}

}
