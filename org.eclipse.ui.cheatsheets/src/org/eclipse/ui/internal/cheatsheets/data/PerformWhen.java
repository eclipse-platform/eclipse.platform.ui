/*******************************************************************************
 * Copyright (c) 2002, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.cheatsheets.data;

import java.util.*;
import org.eclipse.ui.internal.cheatsheets.views.CheatSheetManager;

public class PerformWhen implements IActionItem {
	private String condition;
	private ArrayList actions;
	private Action selectedAction;

	/**
	 * Constructor for PerformWhen.
	 */
	public PerformWhen() {
		super();
	}

	public PerformWhen(String condition) {
		super();
		this.condition = condition;
	}
	
	/**
	 * Returns the condition.
	 * @return String
	 */
	public String getCondition() {
		return condition;
	}

	/**
	 * Sets the condition.
	 * @param newCondition The new condition to set
	 */
	public void setCondition(String newCondition) {
		this.condition = newCondition;
	}

	/**
	 * @return Returns the actions.
	 */
	public ArrayList getActions() {
		return actions;
	}
	
	/**
	 * @param action the Action to add.
	 */
	public void addAction(Action action) {
		if(actions == null) {
			actions = new ArrayList();
		}
		actions.add(action);
	}


	/**
	 * This method always returns <code>null</code>, it is only here aid in parsing.
	 * @return Returns the actions.
	 */
	public Action getAction() {
		return null;
	}

	/**
	 * Delegate to the addAction metod.
	 * @param action the Action to add.
	 */
	public void setAction(Action action) {
		addAction(action);
	}

	public Action getSelectedAction() {
		return selectedAction;
	}

	public void setSelectedAction(CheatSheetManager csm) {
		String conditionValue = csm.getVariableData(condition);

		for (Iterator iter = actions.iterator(); iter.hasNext();) {
			Action action = (Action) iter.next();
			if(action.getWhen() != null && action.getWhen().equals(conditionValue)) {
				selectedAction = action;
				break;
			}
		}
	}
}
