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
package org.eclipse.ui.tests.cheatsheets.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;

public class NoopAction extends Action {

	private static int count;
	
	/**
	 * Constructor for NoopAction.
	 */
	public NoopAction() {
		super();
	}

	/**
	 * Constructor for NoopAction.
	 * @param text
	 */
	public NoopAction(String text) {
		super(text);
	}

	/**
	 * Constructor for NoopAction.
	 * @param text
	 * @param image
	 */
	public NoopAction(String text, ImageDescriptor image) {
		super(text, image);
	}

	public void run() {
		System.out.println("Running NoopAction: "+count++);
	}


}
