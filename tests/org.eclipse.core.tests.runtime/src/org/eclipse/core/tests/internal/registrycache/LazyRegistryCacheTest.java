/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.internal.registrycache;

import java.io.File;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.internal.registry.Factory;
import org.eclipse.core.internal.registry.RegistryCacheReader;
import org.eclipse.core.runtime.IPlatform;
import org.eclipse.core.runtime.MultiStatus;

public class LazyRegistryCacheTest extends RegistryCacheTest {
	public LazyRegistryCacheTest(String name) {
		super(name);
	}

	protected RegistryCacheReader createRegistryReader(File cacheFile) {
		return new RegistryCacheReader(cacheFile, new Factory(new MultiStatus(IPlatform.PI_RUNTIME, 0, "", null)), true);
	}

	public static Test suite() {
		return new TestSuite(LazyRegistryCacheTest.class);
	}
}