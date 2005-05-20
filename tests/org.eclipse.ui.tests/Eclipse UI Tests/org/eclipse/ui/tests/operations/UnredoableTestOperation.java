/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.operations;


/**
 * @since 3.1
 */
public class UnredoableTestOperation extends TestOperation {
	UnredoableTestOperation(String name) {
		super(name);
	}

	boolean disposed = false;
	
	public boolean canRedo() {
		return false;
	}
	
	public void dispose() {
		disposed = true;
	}

}
