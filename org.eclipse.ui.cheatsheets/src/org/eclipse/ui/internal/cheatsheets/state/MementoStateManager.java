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

package org.eclipse.ui.internal.cheatsheets.state;

import java.util.Hashtable;
import java.util.Properties;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.cheatsheets.ICheatSheetManager;
import org.eclipse.ui.internal.cheatsheets.data.CheatSheetSaveHelper;
import org.eclipse.ui.internal.cheatsheets.data.IParserTags;
import org.eclipse.ui.internal.cheatsheets.registry.CheatSheetElement;
import org.eclipse.ui.internal.cheatsheets.views.CheatSheetManager;

/**
 * A state manager used to open a cheat sheet whose state comes from a memento.
 * This is used by the children of composite cheat sheets.
 */

public class MementoStateManager implements ICheatSheetStateManager {

	private IMemento memento;
	private CheatSheetElement element;
	private CheatSheetSaveHelper saveHelper = new CheatSheetSaveHelper();
	private Properties props;
	private ICheatSheetManager parentCsm;

	/**
	 * @param memento The memento which will be used to initialize the state. May be
	 * null to indicate that the state should be initialized.
	 */
	public MementoStateManager(IMemento memento, ICheatSheetManager parentCsm) {
		this.memento = memento;
		this.parentCsm = parentCsm;
	}

	/**
	 * Load properties from the memento.
	 */
	public Properties getProperties() {
		if (memento == null) {
			return null;
		}
		if (props == null) {
			props = saveHelper.loadFromMemento(memento);
		}
		return props;
	}

	public CheatSheetManager getCheatSheetManager() {
		CheatSheetManager result = new CheatSheetManager(element);
		if (getProperties() != null) {
		    result.setData((Hashtable) getProperties().get(IParserTags.MANAGERDATA));
		}
		result.setParent(parentCsm);
		return result;
	}

	public void setElement(CheatSheetElement element) {
		this.element = element;
	}

	public IStatus saveState(Properties properties, CheatSheetManager manager) {
		// The real save will use a memento, this is an empty method
		return Status.OK_STATUS;
	}

}
