/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.runtime.content;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.internal.content.ContentTypeManager;
import org.eclipse.core.internal.preferences.EclipsePreferences;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.content.*;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.tests.runtime.RuntimeTest;

/**
 * Tests content type matcher with a non-default context for user preferences.
 */
public class SpecificContextTest extends RuntimeTest {

	/**
	 * A minimal scope implementation. 
	 */
	private class SingleNodeScope implements IScopeContext {
		private IEclipsePreferences node;

		SingleNodeScope() {
			this.node = new EclipsePreferences();
		}

		public IPath getLocation() {
			return null;
		}

		public String getName() {
			return "";
		}

		public IEclipsePreferences getNode(String qualifier) {
			assertEquals(ContentTypeManager.CONTENT_TYPE_PREF_NODE, qualifier);
			return this.node;
		}
	}

	public static Test suite() {
		return new TestSuite(SpecificContextTest.class);
	}

	public SpecificContextTest(String name) {
		super(name);
	}

	public void testContentTypeLookup() {
		IContentTypeManager global = Platform.getContentTypeManager();
		final SingleNodeScope scope = new SingleNodeScope();
		IContentTypeMatcher local = global.getMatcher(new LocalSelectionPolicy(), scope);
		IContentType textContentType = global.getContentType(Platform.PI_RUNTIME + '.' + "text");
		try {
			// added "<test case name>.global" to the text content type as a global file spec
			textContentType.addFileSpec(getName() + ".global", IContentType.FILE_NAME_SPEC);
		} catch (CoreException e) {
			fail("0.1", e);
		}
		try {
			// added "<test case name>.local" to the text content type as a local (scope-specific) file spec			
			textContentType.getSettings(scope).addFileSpec(getName() + ".local", IContentType.FILE_NAME_SPEC);
		} catch (CoreException e) {
			fail("0.2", e);
		}
		// make ensure associations are properly recognized when doing content type lookup
		assertEquals("1.0", textContentType, global.findContentTypeFor(getName() + ".global"));
		assertEquals("1.1", null, local.findContentTypeFor(getName() + ".global"));
		assertEquals("2.0", textContentType, local.findContentTypeFor(getName() + ".local"));
		assertEquals("2.1", null, global.findContentTypeFor(getName() + ".local"));
	}

	public void testIsAssociatedWith() {
		IContentTypeManager contentTypeManager = Platform.getContentTypeManager();
		final SingleNodeScope scope = new SingleNodeScope();
		IContentType textContentType = contentTypeManager.getContentType(Platform.PI_RUNTIME + '.' + "text");
		IContentTypeSettings localSettings = null;
		try {
			localSettings = textContentType.getSettings(scope);
		} catch (CoreException e) {
			fail("0.1", e);
		}
		// haven't added association yet
		assertTrue("1.0", !textContentType.isAssociatedWith("hello.foo", scope));
		assertTrue("1.1", !textContentType.isAssociatedWith("hello.foo"));
		try {
			// associate at the scope level 
			localSettings.addFileSpec("foo", IContentType.FILE_EXTENSION_SPEC);
		} catch (CoreException e) {
			fail("1.5", e);
		}
		try {
			localSettings = textContentType.getSettings(scope);
		} catch (CoreException e) {
			fail("2.1", e);
		}
		// scope-specific settings should contain the filespec we just added
		String[] fileSpecs = localSettings.getFileSpecs(IContentType.FILE_EXTENSION_SPEC);
		assertEquals("2.2", 1, fileSpecs.length);
		assertEquals("2.3", "foo", fileSpecs[0]);
		// now it is associated at the scope level...
		assertTrue("2.5", textContentType.isAssociatedWith("hello.foo", scope));
		// ...but not at the global level
		assertTrue("2.6", !textContentType.isAssociatedWith("hello.foo"));
	}

}
