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
package org.eclipse.core.tests.runtime.content;

import java.util.Arrays;
import org.eclipse.core.internal.content.ContentTypeBuilder;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager;
import org.eclipse.core.tests.runtime.DynamicPluginTest;
import org.osgi.framework.Bundle;

public class IContentTypeTest extends DynamicPluginTest {
	public IContentTypeTest(String name) {
		super(name);
	}		
	public void testAssociations() {
		IContentType text = Platform.getContentTypeManager().getContentType((IPlatform.PI_RUNTIME + ".text"));
		// associate a user-defined file spec
		text.addFileSpec("ini", IContentType.FILE_EXTENSION_SPEC);		
		// test associations
		assertTrue("0.1", text.isAssociatedWith("text.txt"));
		assertTrue("0.2", text.isAssociatedWith("text.ini"));
		assertTrue("0.3", text.isAssociatedWith("text.tkst"));		
		// check provider defined settings
		String[] providerDefinedExtensions = text.getFileSpecs(IContentType.FILE_EXTENSION_SPEC | IContentType.IGNORE_USER_DEFINED);
		assertTrue("1.0", contains(providerDefinedExtensions, "txt"));		
		assertTrue("1.1", !contains(providerDefinedExtensions, "ini"));
		assertTrue("1.2", contains(providerDefinedExtensions, "tkst"));		
		// check user defined settings
		String[] textUserDefinedExtensions = text.getFileSpecs(IContentType.FILE_EXTENSION_SPEC | IContentType.IGNORE_PRE_DEFINED); 
		assertTrue("2.0", !contains(textUserDefinedExtensions, "txt"));
		assertTrue("2.1", contains(textUserDefinedExtensions, "ini"));
		assertTrue("2.2", !contains(textUserDefinedExtensions, "tkst"));
		// removing pre-defined file specs should not do anything
		text.removeFileSpec("txt", IContentType.FILE_EXTENSION_SPEC);
		assertTrue("3.0", contains(text.getFileSpecs(IContentType.FILE_EXTENSION_SPEC | IContentType.IGNORE_USER_DEFINED), "txt"));
		assertTrue("3.1", text.isAssociatedWith("text.txt"));
		assertTrue("3.2", text.isAssociatedWith("text.ini"));
		assertTrue("3.3", text.isAssociatedWith("text.tkst"));				
		// removing user file specs is the normal case and has to work as expected
		text.removeFileSpec("ini", IContentType.FILE_EXTENSION_SPEC);
		assertTrue("4.0", !contains(text.getFileSpecs(IContentType.FILE_EXTENSION_SPEC | IContentType.IGNORE_PRE_DEFINED), "ini"));
		assertTrue("4.1", text.isAssociatedWith("text.txt"));
		assertTrue("4.2", !text.isAssociatedWith("text.ini"));
		assertTrue("4.3", text.isAssociatedWith("text.tkst"));
	}
	/**
	 * This test shows how we deal with orphan file associations (associations
	 * whose content types are missing).
	 */
	public void testOrphanContentType() throws Exception {
		IContentTypeManager contentTypeManager = Platform.getContentTypeManager();
		assertEquals("1.0", 0, contentTypeManager.findContentTypesFor("foo.orphan2").length);
		//test late addition of content type - orphan2 should become visible
		TestRegistryChangeListener listener = new TestRegistryChangeListener(Platform.PI_RUNTIME, ContentTypeBuilder.PT_CONTENTTYPES, null, null); 
		registerListener(listener, Platform.PI_RUNTIME);
		Bundle installed = installBundle("content/bundle01");
		try {
			IRegistryChangeEvent event = listener.getEvent(10000);
			assertNotNull("1.5", event);
			assertNotNull("2.0", Platform.getBundle("org.eclipse.foo"));
			IContentType newType = contentTypeManager.getContentType("org.eclipse.foo.bar");
			assertNotNull("2.1", newType);
			assertTrue("2.2", newType.isAssociatedWith("foo.orphan2"));			
			assertEquals("2.3", 1, contentTypeManager.findContentTypesFor("foo.orphan2").length);
		} finally {
			//remove installed bundle
			installed.uninstall();
		}
	}

	private boolean contains(Object[] list, Object item) {
		return Arrays.asList(list).contains(item);
	}	
}
