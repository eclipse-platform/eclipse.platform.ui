/*
 * Licensed Materials - Property of IBM
 * (c) Copyright IBM Corporation 2000, 2003.
 * All Rights Reserved. 
 * Note to U.S. Government Users Restricted Rights:  Use, duplication or disclosure restricted by GSA ADP  schedule Contract with IBM Corp. 
*/

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
