/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.internal.indexing;

import org.eclipse.core.tests.harness.EclipseWorkspaceTest;

public class IntegratedTestEnvironment extends EclipseWorkspaceTest implements TestEnvironment {

	public String getFileName() {
		return getWorkspace().getRoot().getLocation().append("test.dat").toOSString();
	}

	public void print(String s) {
	}
	
	public void print(int n, int width) {
	}

	public void println(String s) {
	}
	
	public void printHeading(String s) {
	}

}
