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
package org.eclipse.core.tests.resources;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.internal.resources.ContentDescriptionManager;
import org.eclipse.core.internal.resources.Workspace;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.content.*;

public class ContentDescriptionManagerTest extends ResourceTest {

	private static final String CONTENT_TYPE_RELATED_NATURE2 = "org.eclipse.core.tests.resources.contentTypeRelated2";
	private static final String CONTENT_TYPE_RELATED_NATURE1 = "org.eclipse.core.tests.resources.contentTypeRelated1";

	public static Test suite() {
		return new TestSuite(ContentDescriptionManagerTest.class);
	}

	public ContentDescriptionManagerTest(String name) {
		super(name);
	}

	protected InputStream projectDescriptionWithNatures(String project, String[] natures) {
		StringBuffer contents = new StringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\"?><projectDescription><name>" + project + "</name><natures>");
		for (int i = 0; i < natures.length; i++)
			contents.append("<nature>" + natures[i] + "</nature>");
		contents.append("</natures></projectDescription>");
		return new ByteArrayInputStream(contents.toString().getBytes());
	}

	public void testNatureContentTypeAssociation() {
		IContentTypeManager contentTypeManager = Platform.getContentTypeManager();
		IContentType baseType = contentTypeManager.getContentType("org.eclipse.core.tests.resources.nature_associated_1");
		IContentType derivedType = contentTypeManager.getContentType("org.eclipse.core.tests.resources.nature_associated_2");
		assertNotNull("0.1", baseType);
		assertNotNull("0.2", derivedType);
		IProject project = getWorkspace().getRoot().getProject("proj1");
		IFile file = project.getFile("file.nature-associated");
		IFile descFile = project.getFile(IProjectDescription.DESCRIPTION_FILE_NAME);
		ensureExistsInWorkspace(file, "it really does not matter");
		IContentDescription description = null;

		// originally, project description has no natures		
		try {
			descFile.setContents(projectDescriptionWithNatures(project.getName(), new String[0]), IResource.FORCE, getMonitor());
		} catch (CoreException e) {
			fail("1.0", e);
		}
		waitForCacheFlush();
		try {
			description = file.getContentDescription();
		} catch (CoreException e) {
			fail("1.1", e);
		}
		assertNotNull("1.2", description);
		assertSame("1.3", baseType, description.getContentType());

		// change project description to include one of the natures		
		try {
			descFile.setContents(projectDescriptionWithNatures(project.getName(), new String[] {CONTENT_TYPE_RELATED_NATURE1}), IResource.FORCE, getMonitor());
		} catch (CoreException e) {
			fail("2.0", e);
		}
		waitForCacheFlush();
		try {
			description = file.getContentDescription();
		} catch (CoreException e) {
			fail("2.1", e);
		}
		assertNotNull("2.2", description);
		assertSame("2.3", baseType, description.getContentType());

		// change project description to include the other nature		
		try {
			descFile.setContents(projectDescriptionWithNatures(project.getName(), new String[] {CONTENT_TYPE_RELATED_NATURE2}), IResource.FORCE, getMonitor());
		} catch (CoreException e) {
			fail("3.0", e);
		}
		waitForCacheFlush();
		try {
			description = file.getContentDescription();
		} catch (CoreException e) {
			fail("3.1", e);
		}
		assertNotNull("3.2", description);
		assertSame("3.3", derivedType, description.getContentType());

		// change project description to include both of the natures
		try {
			descFile.setContents(projectDescriptionWithNatures(project.getName(), new String[] {CONTENT_TYPE_RELATED_NATURE1, CONTENT_TYPE_RELATED_NATURE2}), IResource.FORCE, getMonitor());
		} catch (CoreException e) {
			fail("4.0", e);
		}
		waitForCacheFlush();
		try {
			description = file.getContentDescription();
		} catch (CoreException e) {
			fail("4.1", e);
		}
		assertNotNull("4.2", description);
		assertSame("4.3", baseType, description.getContentType());

		// back to no natures
		try {
			descFile.setContents(projectDescriptionWithNatures(project.getName(), new String[0]), IResource.FORCE, getMonitor());
		} catch (CoreException e) {
			fail("5.0", e);
		}
		waitForCacheFlush();
		try {
			description = file.getContentDescription();
		} catch (CoreException e) {
			fail("5.1", e);
		}
		assertNotNull("5.2", description);
		assertSame("5.3", baseType, description.getContentType());

	}

	public void testBug79151() {
		IWorkspace workspace = getWorkspace();
		IProject project = workspace.getRoot().getProject("MyProject");
		IContentTypeManager contentTypeManager = Platform.getContentTypeManager();
		IContentType xml = contentTypeManager.getContentType("org.eclipse.core.runtime.xml");
		String newExtension = "xml_bug_79151";
		IFile file1 = project.getFile("file.xml");
		IFile file2 = project.getFile("file." + newExtension);
		ensureExistsInWorkspace(file1, getContents(CharsetTest.SAMPLE_XML_ISO_8859_1_ENCODING));
		ensureExistsInWorkspace(file2, getContents(CharsetTest.SAMPLE_XML_US_ASCII_ENCODING));
		// ensure we start in a known state
		((Workspace) workspace).getContentDescriptionManager().invalidateCache(true, null);
		// wait for cache flush to finish
		waitForCacheFlush();
		// cache is new at this point
		assertEquals("0.9", ContentDescriptionManager.EMPTY_CACHE, ((Workspace) workspace).getContentDescriptionManager().getCacheState());

		IContentDescription description1a = null, description1b = null, description1c = null, description1d = null;
		IContentDescription description2 = null;
		try {
			description1a = file1.getContentDescription();
			description2 = file2.getContentDescription();
		} catch (CoreException e) {
			fail("1.0", e);
		}
		assertNotNull("1.1", description1a);
		assertEquals("1.2", xml, description1a.getContentType());
		assertNull("1.3", description2);
		try {
			description1b = file1.getContentDescription();
			// ensure it comes from the cache (should be the very same object)
			assertNotNull(" 2.0", description1b);
			assertSame("2.1", description1a, description1b);
		} catch (CoreException e) {
			fail("2.2", e);
		}
		try {
			// change the content type
			xml.addFileSpec(newExtension, IContentType.FILE_EXTENSION_SPEC);
		} catch (CoreException e) {
			fail("3.0", e);
		}
		try {
			try {
				description1c = file1.getContentDescription();
				description2 = file2.getContentDescription();
			} catch (CoreException e) {
				fail("4.0", e);
			}
			// ensure it does *not* come from the cache (should be a different object)
			assertNotNull("4.1", description1c);
			assertNotSame("4.2", description1a, description1c);
			assertEquals("4.3", xml, description1c.getContentType());
			assertNotNull("4.4", description2);
			assertEquals("4.5", xml, description2.getContentType());
		} finally {
			try {
				// dissociate the xml2 extension from the XML content type
				xml.removeFileSpec(newExtension, IContentType.FILE_EXTENSION_SPEC);
			} catch (CoreException e) {
				fail("4.99", e);
			}
		}
		try {
			description1d = file1.getContentDescription();
			description2 = file2.getContentDescription();
		} catch (CoreException e) {
			fail("5.0", e);
		}
		// ensure it does *not* come from the cache (should be a different object)
		assertNotNull("5.1", description1d);
		assertNotSame("5.2", description1c, description1d);
		assertEquals("5.3", xml, description1d.getContentType());
		assertNull("5.4", description2);
	}

	/**
	 * Blocks the calling thread until the cache flush job completes.
	 */
	protected void waitForCacheFlush() {
		try {
			Platform.getJobManager().join(ContentDescriptionManager.FAMILY_DESCRIPTION_CACHE_FLUSH, null);
		} catch (OperationCanceledException e) {
			//ignore
		} catch (InterruptedException e) {
			//ignore
		}
	}

}
