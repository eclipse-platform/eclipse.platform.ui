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
package org.eclipse.team.tests.ccvs.ui;

import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.ui.operations.CVSOperation;
import org.eclipse.team.tests.ccvs.core.EclipseTest;

public abstract class CVSOperationTest extends EclipseTest {
	
	protected CVSOperationTest() {
		super();
	}

	protected CVSOperationTest(String name) {
		super(name);
	}

	protected void run(CVSOperation op) throws CVSException {
		executeHeadless(op);
	}
}
