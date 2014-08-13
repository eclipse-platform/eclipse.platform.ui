/*******************************************************************************
 * Copyright (c) Jul 30, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.tests.sourcelookup;

import junit.framework.TestCase;

import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.internal.ui.sourcelookup.SourceLookupFacility;
import org.eclipse.debug.ui.sourcelookup.ISourceLookupResult;

/**
 * Tests {@link SourceLookupFacility}
 *
 * @since 3.9.200
 */
public class SourceLookupFacilityTests extends TestCase {

	/**
	 * {@link IStackFrame} to be reused
	 */
	TestStackFrame fReusableFrame = new TestStackFrame(new TestLaunch());
	/**
	 * Testing source director
	 */
	TestSourceDirector fTestDirector = new TestSourceDirector();
	/**
	 * Test source locator
	 */
	TestSourceLocator fTestLocator = new TestSourceLocator();

	/**
	 * @param name
	 */
	public SourceLookupFacilityTests(String name) {
		super(name);
	}

	/**
	 * Tests calling
	 * {@link SourceLookupFacility#lookup(Object, org.eclipse.debug.core.model.ISourceLocator, boolean)}
	 * with simple type, no locator and no forcing
	 *
	 * @throws Exception
	 */
	public void testLookupStringNoLocatorNoForce() throws Exception {
		try {
			String artifact = "Empty"; //$NON-NLS-1$
			ISourceLookupResult result = SourceLookupFacility.getDefault().lookup(artifact, null, false);
			assertNotNull("There should be a result", result); //$NON-NLS-1$
			assertNull("Source element should be null", result.getSourceElement()); //$NON-NLS-1$
		} finally {
			SourceLookupFacility.shutdown();
		}
	}

	/**
	 * Tests calling
	 * {@link SourceLookupFacility#lookup(Object, org.eclipse.debug.core.model.ISourceLocator, boolean)}
	 * with simple type and no forcing
	 *
	 * @throws Exception
	 */
	public void testLookupStringNoForce() throws Exception {
		try {
			String artifact = "One"; //$NON-NLS-1$
			ISourceLookupResult result = SourceLookupFacility.getDefault().lookup(artifact, fTestDirector, false);
			assertNotNull("There should be a result", result); //$NON-NLS-1$
			assertTrue("The result artifact should be a String", result.getArtifact() instanceof String); //$NON-NLS-1$
			assertTrue("The result source element should be a String", result.getSourceElement() instanceof String); //$NON-NLS-1$
			String value = (String) result.getSourceElement();
			result = SourceLookupFacility.getDefault().lookup(artifact, fTestDirector, false);
			assertNotNull("There should be a result", result); //$NON-NLS-1$
			assertTrue("The result artifact should be a String", result.getArtifact() instanceof String); //$NON-NLS-1$
			assertTrue("The result source element should be a String", result.getSourceElement() instanceof String); //$NON-NLS-1$
			assertEquals("The results should be equal", value, result.getSourceElement()); //$NON-NLS-1$
		} finally {
			SourceLookupFacility.shutdown();
		}
	}

	/**
	 * Tests calling
	 * {@link SourceLookupFacility#lookup(Object, org.eclipse.debug.core.model.ISourceLocator, boolean)}
	 * with simple type and forcing
	 *
	 * @throws Exception
	 */
	public void testLookupStringForce() throws Exception {
		try {
			String artifact = "Two"; //$NON-NLS-1$
			ISourceLookupResult result = SourceLookupFacility.getDefault().lookup(artifact, fTestDirector, true);
			assertNotNull("There should be a result", result); //$NON-NLS-1$
			assertTrue("The result artifact should be a String", result.getArtifact() instanceof String); //$NON-NLS-1$
			assertTrue("The result source element should be a String", result.getSourceElement() instanceof String); //$NON-NLS-1$
			String value = (String) result.getSourceElement();
			result = SourceLookupFacility.getDefault().lookup(artifact, fTestDirector, true);
			assertNotNull("There should be a result", result); //$NON-NLS-1$
			assertTrue("The result artifact should be a String", result.getArtifact() instanceof String); //$NON-NLS-1$
			assertTrue("The result source element should be a String", result.getSourceElement() instanceof String); //$NON-NLS-1$
			assertNotSame("The results should not be equal", value, result.getSourceElement()); //$NON-NLS-1$
		} finally {
			SourceLookupFacility.shutdown();
		}
	}

	/**
	 * Tests calling
	 * {@link SourceLookupFacility#lookup(Object, org.eclipse.debug.core.model.ISourceLocator, boolean)}
	 * with simple type and no forcing
	 *
	 * @throws Exception
	 */
	public void testLookupStringLocatorNoForce() throws Exception {
		try {
			String artifact = "Three"; //$NON-NLS-1$
			ISourceLookupResult result = SourceLookupFacility.getDefault().lookup(artifact, fTestLocator, false);
			assertNotNull("There should be a result", result); //$NON-NLS-1$
			assertNull("The source element should be null", result.getSourceElement()); //$NON-NLS-1$
		} finally {
			SourceLookupFacility.shutdown();
		}
	}

	/**
	 * Tests calling
	 * {@link SourceLookupFacility#lookup(Object, org.eclipse.debug.core.model.ISourceLocator, boolean)}
	 * with an {@link IStackFrame} impl and no forcing
	 *
	 * @throws Exception
	 */
	public void testLookupStackframeNoForce() throws Exception {
		try {
			ISourceLookupResult result = SourceLookupFacility.getDefault().lookup(fReusableFrame, fTestDirector, false);
			assertNotNull("There should be a result", result); //$NON-NLS-1$
			assertTrue("The result source element should be a String", result.getArtifact() instanceof IStackFrame); //$NON-NLS-1$
			assertTrue("The result source element should be a String", result.getSourceElement() instanceof String); //$NON-NLS-1$
			String value = (String) result.getSourceElement();
			result = SourceLookupFacility.getDefault().lookup(fReusableFrame, fTestDirector, false);
			assertNotNull("There should be a result", result); //$NON-NLS-1$
			assertTrue("The result artifact should be a String", result.getArtifact() instanceof IStackFrame); //$NON-NLS-1$
			assertTrue("The result source element should be a String", result.getSourceElement() instanceof String); //$NON-NLS-1$
			assertEquals("The results should not be equal", value, result.getSourceElement()); //$NON-NLS-1$
		} finally {
			SourceLookupFacility.shutdown();
		}
	}

	/**
	 * Tests calling
	 * {@link SourceLookupFacility#lookup(Object, org.eclipse.debug.core.model.ISourceLocator, boolean)}
	 * with an {@link IStackFrame} impl and forcing
	 *
	 * @throws Exception
	 */
	public void testLookupStackframeForce() throws Exception {
		try {
			ISourceLookupResult result = SourceLookupFacility.getDefault().lookup(fReusableFrame, fTestDirector, true);
			assertNotNull("There should be a result", result); //$NON-NLS-1$
			assertTrue("The result source element should be a String", result.getArtifact() instanceof IStackFrame); //$NON-NLS-1$
			assertTrue("The result source element should be a String", result.getSourceElement() instanceof String); //$NON-NLS-1$
			String value = (String) result.getSourceElement();
			result = SourceLookupFacility.getDefault().lookup(fReusableFrame, fTestDirector, true);
			assertNotNull("There should be a result", result); //$NON-NLS-1$
			assertTrue("The result artifact should be a String", result.getArtifact() instanceof IStackFrame); //$NON-NLS-1$
			assertTrue("The result source element should be a String", result.getSourceElement() instanceof String); //$NON-NLS-1$
			assertNotSame("The results should not be equal", value, result.getSourceElement()); //$NON-NLS-1$
		} finally {
			SourceLookupFacility.shutdown();
		}
	}

	/**
	 * Tests calling
	 * {@link SourceLookupFacility#lookup(Object, org.eclipse.debug.core.model.ISourceLocator, boolean)}
	 * with an {@link IStackFrame} impl, no forcing, no locator, no launch
	 *
	 * @throws Exception
	 */
	public void testLookupStackframeWithDebugElement1() throws Exception {
		try {
			ISourceLookupResult result = SourceLookupFacility.getDefault().lookup(new TestStackFrame(null), null, false);
			assertNotNull("There should be a result", result); //$NON-NLS-1$
			assertNull("Source element should be null", result.getSourceElement()); //$NON-NLS-1$
		} finally {
			SourceLookupFacility.shutdown();
		}
	}

	/**
	 * Tests calling
	 * {@link SourceLookupFacility#lookup(Object, org.eclipse.debug.core.model.ISourceLocator, boolean)}
	 * with an {@link IStackFrame} impl, no forcing, no locator
	 *
	 * @throws Exception
	 */
	public void testLookupStackframeWithDebugElement2() throws Exception {
		try {
			ISourceLookupResult result = SourceLookupFacility.getDefault().lookup(fReusableFrame, null, false);
			assertNotNull("There should be a result", result); //$NON-NLS-1$
			assertTrue("The result source element should be a String", result.getArtifact() instanceof IStackFrame); //$NON-NLS-1$
			assertTrue("The result source element should be a String", result.getSourceElement() instanceof String); //$NON-NLS-1$
			String value = (String) result.getSourceElement();
			result = SourceLookupFacility.getDefault().lookup(fReusableFrame, null, false);
			assertNotNull("There should be a result", result); //$NON-NLS-1$
			assertTrue("The result artifact should be a String", result.getArtifact() instanceof IStackFrame); //$NON-NLS-1$
			assertTrue("The result source element should be a String", result.getSourceElement() instanceof String); //$NON-NLS-1$
			assertEquals("The results should be equal", value, result.getSourceElement()); //$NON-NLS-1$
		} finally {
			SourceLookupFacility.shutdown();
		}
	}

	/**
	 * Tests calling
	 * {@link SourceLookupFacility#lookup(Object, org.eclipse.debug.core.model.ISourceLocator, boolean)}
	 * with an {@link IStackFrame} impl, forcing, no locator
	 *
	 * @throws Exception
	 */
	public void testLookupStackframeWithDebugElement3() throws Exception {
		try {
			ISourceLookupResult result = SourceLookupFacility.getDefault().lookup(fReusableFrame, null, true);
			assertNotNull("There should be a result", result); //$NON-NLS-1$
			assertTrue("The result source element should be a String", result.getArtifact() instanceof IStackFrame); //$NON-NLS-1$
			assertTrue("The result source element should be a String", result.getSourceElement() instanceof String); //$NON-NLS-1$
			String value = (String) result.getSourceElement();
			result = SourceLookupFacility.getDefault().lookup(fReusableFrame, null, true);
			assertNotNull("There should be a result", result); //$NON-NLS-1$
			assertTrue("The result artifact should be a String", result.getArtifact() instanceof IStackFrame); //$NON-NLS-1$
			assertTrue("The result source element should be a String", result.getSourceElement() instanceof String); //$NON-NLS-1$
			assertNotSame("The results should not be equal", value, result.getSourceElement()); //$NON-NLS-1$
		} finally {
			SourceLookupFacility.shutdown();
		}
	}

	/**
	 * Tests calling
	 * {@link SourceLookupFacility#lookup(Object, org.eclipse.debug.core.model.ISourceLocator, boolean)}
	 * with an {@link IStackFrame} impl, no forcing, ISourceLocator impl
	 *
	 * @throws Exception
	 */
	public void testLookupStackframeWithDebugElement4() throws Exception {
		try {
			ISourceLookupResult result = SourceLookupFacility.getDefault().lookup(fReusableFrame, fTestLocator, false);
			assertNotNull("There should be a result", result); //$NON-NLS-1$
			assertTrue("The result source element should be a String", result.getArtifact() instanceof IStackFrame); //$NON-NLS-1$
			assertTrue("The result source element should be a String", result.getSourceElement() instanceof String); //$NON-NLS-1$
			String value = (String) result.getSourceElement();
			result = SourceLookupFacility.getDefault().lookup(fReusableFrame, fTestLocator, false);
			assertNotNull("There should be a result", result); //$NON-NLS-1$
			assertTrue("The result artifact should be a String", result.getArtifact() instanceof IStackFrame); //$NON-NLS-1$
			assertTrue("The result source element should be a String", result.getSourceElement() instanceof String); //$NON-NLS-1$
			assertEquals("The results should not be equal", value, result.getSourceElement()); //$NON-NLS-1$
		} finally {
			SourceLookupFacility.shutdown();
		}
	}

	/**
	 * Tests calling
	 * {@link SourceLookupFacility#lookup(Object, org.eclipse.debug.core.model.ISourceLocator, boolean)}
	 * with an {@link IStackFrame} impl, forcing, ISourceLocator impl
	 *
	 * @throws Exception
	 */
	public void testLookupStackframeWithDebugElement5() throws Exception {
		try {
			ISourceLookupResult result = SourceLookupFacility.getDefault().lookup(fReusableFrame, fTestLocator, true);
			assertNotNull("There should be a result", result); //$NON-NLS-1$
			assertTrue("The result source element should be a String", result.getArtifact() instanceof IStackFrame); //$NON-NLS-1$
			assertTrue("The result source element should be a String", result.getSourceElement() instanceof String); //$NON-NLS-1$
			String value = (String) result.getSourceElement();
			result = SourceLookupFacility.getDefault().lookup(fReusableFrame, fTestLocator, true);
			assertNotNull("There should be a result", result); //$NON-NLS-1$
			assertTrue("The result artifact should be a String", result.getArtifact() instanceof IStackFrame); //$NON-NLS-1$
			assertTrue("The result source element should be a String", result.getSourceElement() instanceof String); //$NON-NLS-1$
			assertNotSame("The results should not be equal", value, result.getSourceElement()); //$NON-NLS-1$
		} finally {
			SourceLookupFacility.shutdown();
		}
	}
}
