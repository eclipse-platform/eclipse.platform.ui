/*******************************************************************************
 * Copyright (c) 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.internal.preferences;

import java.util.Properties;
import org.eclipse.core.internal.preferences.EclipsePreferences;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.tests.harness.CoreTest;
import org.osgi.service.prefs.BackingStoreException;

/*
 * Test scope used for testing the file format on disk. 
 * Specifically in EclipsePreferencesTest.test_342709.
 */
public class TestScope2 extends EclipsePreferences implements IScopeContext {

	public static final String SCOPE = "test2"; //$NON-NLS-1$
	private String qualifier;
	private int segmentCount;
	private IEclipsePreferences loadLevel;

	public static IPath baseLocation;
	private IPath location;

	static {
		baseLocation = new CoreTest().getRandomLocation();
	}

	public TestScope2() {
		this(null, null);
	}

	private TestScope2(EclipsePreferences parent, String key) {
		super(parent, key);
		// cache the segment count
		IPath path = new Path(absolutePath());
		segmentCount = path.segmentCount();
		if (segmentCount < 2)
			return;

		// cache the qualifier
		String scope = path.segment(0);
		if (TestScope2.SCOPE.equals(scope))
			qualifier = path.segment(1);

		// cache the location
		if (qualifier == null)
			return;
	}

	protected IEclipsePreferences getLoadLevel() {
		if (loadLevel == null) {
			if (qualifier == null)
				return null;
			// Make it relative to this node rather than navigating to it from the root.
			// Walk backwards up the tree starting at this node.
			// This is important to avoid a chicken/egg thing on startup.
			IEclipsePreferences node = this;
			for (int i = 2; i < segmentCount; i++)
				node = (IEclipsePreferences) node.parent();
			loadLevel = node;
		}
		return loadLevel;
	}

	public IPath getLocation() {
		if (location == null) {
			location = computeLocation(baseLocation, qualifier);
		}
		return location;
	}

	public String getName() {
		return SCOPE;
	}

	public IEclipsePreferences getNode(String qualifier) {
		return (IEclipsePreferences) Platform.getPreferencesService().getRootNode().node(SCOPE).node(qualifier);
	}

	protected EclipsePreferences internalCreate(EclipsePreferences nodeParent, String nodeName, Object context) {
		return new TestScope2(nodeParent, nodeName);
	}

	void setDirty(boolean value) {
		dirty = value;
	}

	Properties toProperties() throws BackingStoreException {
		return convertToProperties(new Properties(), "");
	}
}
