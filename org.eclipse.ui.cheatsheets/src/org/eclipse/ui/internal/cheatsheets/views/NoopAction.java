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
