/*
 * Licensed Materials - Property of IBM
 * (c) Copyright IBM Corporation 2000, 2003.
 * All Rights Reserved. 
 * Note to U.S. Government Users Restricted Rights:  Use, duplication or disclosure restricted by GSA ADP  schedule Contract with IBM Corp. 
*/

package org.eclipse.ui.internal.cheatsheets.views;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;

public class NoopAction extends Action {

	/**
	 * Constructor for NoopAction.
	 */
	protected NoopAction() {
		super();
	}

	/**
	 * Constructor for NoopAction.
	 * @param text
	 */
	protected NoopAction(String text) {
		super(text);
	}

	/**
	 * Constructor for NoopAction.
	 * @param text
	 * @param image
	 */
	protected NoopAction(String text, ImageDescriptor image) {
		super(text, image);
	}



}
