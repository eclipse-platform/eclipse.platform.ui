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
package org.eclipse.ui.internal.cheatsheets.views;

import org.eclipse.swt.widgets.*;

public class SubItemCompositeHolder {
	private Label iconLabel;
	private boolean skipped;
	private boolean completed;
	protected Button startButton;
	
	/**
	 * 
	 */
	/*package*/ SubItemCompositeHolder(Label l, Button startb) {
		super();
		iconLabel = l;
		startButton = startb;
	}

	/**
	 * @return Label
	 */
	/*package*/ Label getIconLabel() {
		return iconLabel;
	}

	/**
	 * @return
	 */
	public boolean isCompleted() {
		return completed;
	}

	/**
	 * @return
	 */
	public boolean isSkipped() {
		return skipped;
	}

	/**
	 * @param b
	 */
	/*package*/ void setCompleted(boolean b) {
		completed = b;
	}

	/**
	 * @param b
	 */
	/*package*/ void setSkipped(boolean b) {
		skipped = b;
	}

	/**
	 * @return
	 */
	/*package*/ Button getStartButton() {
		return startButton;
	}

}
