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
package org.eclipse.team.tests.ccvs.core.cvsresources;


import junit.extensions.TestSetup;
import junit.framework.Test;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.resources.EclipseSynchronizer;

public class BatchedTestSetup extends TestSetup {
	public BatchedTestSetup(Test test) {
		super(test);
	}

	public void setUp() throws CVSException {
		EclipseSynchronizer.getInstance().beginOperation(ResourcesPlugin.getWorkspace().getRoot(), null);
	}
	
	public void tearDown() throws CVSException {
		EclipseSynchronizer.getInstance().endOperation(null);
	}
}
