/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ua.tests.doc.internal.linkchecker;

import java.net.URL;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

public class PrebuiltIndexChecker extends TestCase {
	
	private static final String PLATFORM_USER = "org.eclipse.platform.doc.user";
	private static final String PLATFORM_ISV = "org.eclipse.platform.doc.isv";
	private static final String PDE_USER = "org.eclipse.pde.doc.user";
	private static final String JDT_USER = "org.eclipse.jdt.doc.user";
	private static final String JDT_ISV = "org.eclipse.jdt.doc.isv";
	
	public static Test suite() {
		return new TestSuite(PrebuiltIndexChecker.class);
	}
	
	public void testPlatformUserIndex() throws Exception {
		validateIndex(PLATFORM_USER, "index");
	}
	
	public void testPlatformIsvIndex() throws Exception {
		validateIndex(PLATFORM_ISV, "index");
	}
	
	public void testPdeUserIndex() throws Exception {
		validateIndex(PDE_USER, "index");
	}
	
	public void testJdtUserIndex() throws Exception {
		validateIndex(JDT_USER, "index");
	}
	
	public void testJdtIsvIndex() throws Exception {
		validateIndex(JDT_ISV, "index");
	}

	private void validateIndex(String plugin, String filepath) {
		Bundle bundle = Platform.getBundle(plugin);
		assertNotNull(bundle);
		
		String[] suffixes = { "", "/indexed_contributions", "/indexed_docs", "/indexed_dependencies" };
		for (int i = 0; i < suffixes.length; i++) {
			String fullPath = filepath + suffixes[i];
			IPath path = new Path(fullPath);
			URL url = FileLocator.find(bundle, path, null);
			assertNotNull("could not open: " + fullPath, url);
		}
	}

}
