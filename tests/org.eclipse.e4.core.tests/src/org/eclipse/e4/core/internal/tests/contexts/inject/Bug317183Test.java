/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.core.internal.tests.contexts.inject;

import junit.framework.TestCase;

import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.contexts.RunAndTrack;

public class Bug317183Test extends TestCase {

	public void testX() {
		IEclipseContext appContext = EclipseContextFactory.create();
		IEclipseContext windowContext = appContext.createChild();
		IEclipseContext partContextA = windowContext.createChild();
		IEclipseContext partContextB = windowContext.createChild();

		partContextA.activateBranch();

		RunAndTrackImpl impl = new RunAndTrackImpl();
		windowContext.runAndTrack(impl);

		impl.called = false;

		partContextA.dispose();
		partContextB.activate();
		assertTrue(impl.called); // this fails
	}

	public void testY() {
		IEclipseContext appContext = EclipseContextFactory.create();
		IEclipseContext windowContext = appContext.createChild();
		IEclipseContext partContextA = windowContext.createChild();
		IEclipseContext partContextB = windowContext.createChild();
		IEclipseContext partContextC = windowContext.createChild();

		partContextA.activateBranch();

		RunAndTrackImpl impl = new RunAndTrackImpl();
		windowContext.runAndTrack(impl);

		partContextB.activate();
		partContextA.dispose();

		impl.called = false;

		partContextC.activate();
		assertTrue(impl.called); // this fails
	}

	static class RunAndTrackImpl extends RunAndTrack {

		boolean called = false;

		@Override
		public boolean changed(IEclipseContext context) {
			context.getActiveLeaf();
			called = true;
			return true;
		}

	}

}

