/*******************************************************************************
 * Copyright (c) 2002, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.cheatsheets.data;

/**
 *  <p>An ISubItem represents a sub step of a step in the cheat sheet that has sub steps.  
 *  A step in the cheat sheet that contains sub steps is represented by an IItemWithSubItems.
 *  By calling getItem on ICheatSheetManager and passing the id of a step in the cheat sheet 
 *  with sub steps, you get an IAbstractItem that may be casted to an IItemWithSubItems.  </p>
 *
 * <p>This IItemWithSubItems can be used to  access info about the sub steps for that step in the cheat sheet.
 * ISubItem can be implemented to add sub steps to a step in the cheat sheet.</p>
 * 
 * <p>Each sub step in the cheat sheet has a label, as well as the same buttons and actions that a retular
 * step in the cheat sheet (represented by IItem) has.</p>
 *  
  */
public class SubItem extends ActionItem {
	
	private String label;

	public SubItem() {
		super();
	}

	/**
	 * This method returns the label to be shown for the sub item.
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * This method sets the label that will be shown for the sub item.
	 * @param label the label to be shown
	 */
	public void setLabel(String string) {
		label = string;
	}

}
