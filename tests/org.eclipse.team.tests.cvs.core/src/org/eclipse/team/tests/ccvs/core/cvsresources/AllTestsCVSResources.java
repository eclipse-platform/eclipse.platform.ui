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
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.team.tests.ccvs.core.EclipseTest;

public class AllTestsCVSResources extends EclipseTest {
	public static Test suite() {	
		TestSuite suite = new TestSuite();
		suite.addTest(ResourceSyncInfoTest.suite());
		suite.addTest(EclipseSynchronizerTest.suite());
		suite.addTest(EclipseFolderTest.suite());
		suite.addTest(ResourceSyncBytesTest.suite());
    	return suite; 	
	}	
	
	public AllTestsCVSResources(String name) {
		super(name);
	}
	
	public AllTestsCVSResources() {
		super();
	}
}


