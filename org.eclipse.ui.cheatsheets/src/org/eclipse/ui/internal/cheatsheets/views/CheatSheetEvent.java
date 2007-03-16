/*******************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.cheatsheets.views;

import org.eclipse.ui.cheatsheets.ICheatSheetEvent;
import org.eclipse.ui.cheatsheets.ICheatSheetManager;

public class CheatSheetEvent implements ICheatSheetEvent {
	int type;
	String cheatsheetID;
	ICheatSheetManager csm;


	public CheatSheetEvent(int eventType, String id, ICheatSheetManager csm) {
		super();
		this.csm = csm;
		this.type = eventType;
		this.cheatsheetID = id;
	}

	/**
	 * @return
	 */
	public int getEventType() {
		return type;
	}

	/**
	 * @return
	 */
	public String getCheatSheetID() {
		return cheatsheetID;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.cheatsheets.events.ICheatSheetEvent#getCheatSheetManager()
	 */
	public ICheatSheetManager getCheatSheetManager() {
		return csm;
	}

}
