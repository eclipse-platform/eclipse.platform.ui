package org.eclipse.team.tests.ccvs.core.cvsresources;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import junit.extensions.TestSetup;
import junit.framework.Test;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.resources.EclipseSynchronizer;

public class BatchedTestSetup extends TestSetup {
	public BatchedTestSetup(Test test) {
		super(test);
	}

	public void setUp() throws CVSException {
		EclipseSynchronizer.getInstance().beginOperation(null);
	}
	
	public void tearDown() throws CVSException {
		EclipseSynchronizer.getInstance().endOperation(null);
	}
}
