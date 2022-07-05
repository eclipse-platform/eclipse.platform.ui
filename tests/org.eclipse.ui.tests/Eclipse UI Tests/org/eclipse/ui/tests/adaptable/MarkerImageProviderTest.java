/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.adaptable;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.tests.harness.util.UITestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests the markerImageProviders extension point.
 */
@RunWith(JUnit4.class)
public class MarkerImageProviderTest extends UITestCase {

	public MarkerImageProviderTest() {
		super(MarkerImageProviderTest.class.getSimpleName());
	}

	/**
	 * Tests the static form of the extension, where just a file path is given.
	 */
	@Test
	public void testStatic() {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IMarker marker = null;
		try {
			marker = workspace.getRoot().createMarker(
					"org.eclipse.ui.tests.testmarker"); //$NON-NLS-1$
		} catch (CoreException e) {
			fail(e.getMessage());
		}
		IWorkbenchAdapter adapter = marker.getAdapter(IWorkbenchAdapter.class);
		ImageDescriptor imageDesc = adapter.getImageDescriptor(marker);
		assertNotNull(imageDesc);
		assertTrue(imageDesc.toString().contains("anything")); //$NON-NLS-1$
	}

	/**
	 * Tests the dynamic form of the extension, where an IMarkerImageProvider class is given.
	 */
	@Test
	public void testDynamic() {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IMarker marker = null;
		try {
			marker = workspace.getRoot().createMarker(
					"org.eclipse.ui.tests.testmarker2"); //$NON-NLS-1$
		} catch (CoreException e) {
			fail(e.getMessage());
		}
		IWorkbenchAdapter adapter = marker.getAdapter(IWorkbenchAdapter.class);
		ImageDescriptor imageDesc = adapter.getImageDescriptor(marker);
		assertNotNull(imageDesc);
		assertTrue(imageDesc.toString().contains("anything")); //$NON-NLS-1$
	}

}
