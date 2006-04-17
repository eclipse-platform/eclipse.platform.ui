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
package org.eclipse.ui.internal.cheatsheets.data;

public class SubItem extends AbstractSubItem implements IExecutableItem, IPerformWhenItem {
	
	private String label;
	private boolean skip;
	private String when;

	private AbstractExecutable executable;
	private PerformWhen performWhen;

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

	/**
	 * This method returns the skip state for the sub item.
	 * @return the label
	 */
	public boolean isSkip() {
		return skip;
	}

	/**
	 * This method sets whether the sub item can be skipped.
	 * @param value the new value for skip
	 */
	public void setSkip(boolean value) {
		skip = value;
	}

	/**
	 * This method returns the when expression for the sub item.
	 * @return the label
	 */
	public String getWhen() {
		return when;
	}

	/**
	 * This method sets the when expression for the sub item.
	 * @param string the when expression to set
	 */
	public void setWhen(String string) {
		when = string;
	}

	/**
	 * @return Returns the action.
	 */
	public AbstractExecutable getExecutable() {
		return executable;
	}
	
	/**
	 * @param Executable The Executable to set.
	 */
	public void setExecutable(AbstractExecutable executable) {
		this.executable = executable;
	}
	
	/**
	 * @return Returns the performWhen.
	 */
	public PerformWhen getPerformWhen() {
		return performWhen;
	}
	
	/**
	 * @param performWhen The performWhen to set.
	 */
	public void setPerformWhen(PerformWhen performWhen) {
		this.performWhen = performWhen;
	}
}
