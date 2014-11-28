/*******************************************************************************
 * Copyright (c) 2010, 2014 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - adjustment to EObject
 *     Thibault Le Ouay <thibaultleouay@gmail.com> - Bug 450212
 ******************************************************************************/
package org.eclipse.e4.emf.xpath.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Iterator;

import org.apache.commons.jxpath.JXPathNotFoundException;
import org.eclipse.e4.emf.xpath.EcoreXPathContextFactory;
import org.eclipse.e4.emf.xpath.XPathContext;
import org.eclipse.e4.emf.xpath.XPathContextFactory;
import org.eclipse.e4.emf.xpath.test.model.xpathtest.XpathtestPackage;
import org.eclipse.e4.emf.xpath.test.model.xpathtest.impl.ExtendedNodeImpl;
import org.eclipse.e4.emf.xpath.test.model.xpathtest.impl.MenuImpl;
import org.eclipse.e4.emf.xpath.test.model.xpathtest.impl.NodeImpl;
import org.eclipse.e4.emf.xpath.test.model.xpathtest.impl.RootImpl;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ExampleQueriesTestCase {
	private ResourceSet resourceSet;
	private XPathContext xpathContext;
	private Resource resource;



	@Test
	public void testSimpleQuery() {

		Object application = xpathContext.getValue("/");
		assertNotNull(application);
		assertSame(RootImpl.class, application.getClass());

		application = xpathContext.getValue(".");
		assertNotNull(application);
		assertSame(RootImpl.class, application.getClass());

		try {
			application = xpathContext.getValue(".[@id='nixda']");
			fail("This query should fail with JXPathNotFoundException");
		} catch (JXPathNotFoundException path) {
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

		application = xpathContext.getValue(".[@id='root']");
		assertNotNull(application);
		assertSame(RootImpl.class, application.getClass());

		assertEquals("element1",xpathContext.getValue("nodes[1]/@id"));

		assertEquals(NodeImpl.class, xpathContext.getValue("//.[@id='element2.2']").getClass());
		assertEquals(ExtendedNodeImpl.class,xpathContext.getValue("//.[ecore:eClassName(.)='ExtendedNode']").getClass());

	}


	@Test
	public void testMenuQuery() {
		Object application = xpathContext.getValue("/");
		assertNotNull(application);
		assertSame(RootImpl.class, application.getClass());

		Object node = xpathContext.getValue("//.[@id='menuContainer.1']/menus[@id='menu.1']");
		assertNotNull(node);

		Iterator<Object> i  = xpathContext.iterate("//.[@id='menu.1']");
		assertTrue(i.hasNext());
		assertSame(NodeImpl.class, i.next().getClass());
		assertTrue(i.hasNext());
		assertSame(MenuImpl.class, i.next().getClass());
		// EMF model has a loop in it, it just goes back to the top
		//assertFalse(i.hasNext());
	}

	@Before
	public void setUp() {
		resourceSet = new ResourceSetImpl();
		resourceSet
				.getResourceFactoryRegistry()
				.getExtensionToFactoryMap()
				.put(Resource.Factory.Registry.DEFAULT_EXTENSION,
						new XMIResourceFactoryImpl());

		// Register the package to ensure it is available during loading.
		resourceSet.getPackageRegistry().put(XpathtestPackage.eNS_URI,
				XpathtestPackage.eINSTANCE);
		URI uri = URI.createPlatformPluginURI(
				"/org.eclipse.e4.emf.xpath.test/model/Test.xmi", true);
		resource = resourceSet.getResource(uri, true);
		XPathContextFactory<EObject> f = EcoreXPathContextFactory.newInstance();
		xpathContext = f.newContext(resource.getContents().get(0));
	}

	@After
	public void tearDown() {
		xpathContext = null;
		resource.unload();
		resourceSet.getResources().remove(resource);
	}

}
