/*******************************************************************************
 * Copyright (c) 2010 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - adjustment to EObject
 ******************************************************************************/
package org.eclipse.e4.emf.xpath.test;

import junit.framework.TestCase;

import org.apache.commons.jxpath.JXPathNotFoundException;
import org.eclipse.e4.emf.xpath.EcoreXPathContextFactory;
import org.eclipse.e4.emf.xpath.XPathContext;
import org.eclipse.e4.emf.xpath.XPathContextFactory;
import org.eclipse.e4.emf.xpath.test.model.xpathtest.XpathtestPackage;
import org.eclipse.e4.emf.xpath.test.model.xpathtest.impl.ExtendedNodeImpl;
import org.eclipse.e4.emf.xpath.test.model.xpathtest.impl.NodeImpl;
import org.eclipse.e4.emf.xpath.test.model.xpathtest.impl.RootImpl;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

public class ExampleQueriesTestCase extends TestCase {
	public void testSimpleQuery() {
		ResourceSet resourceSet = new ResourceSetImpl();
		resourceSet
				.getResourceFactoryRegistry()
				.getExtensionToFactoryMap()
				.put(Resource.Factory.Registry.DEFAULT_EXTENSION,
						new XMIResourceFactoryImpl());

		// Register the package to ensure it is available during loading.
		//
		resourceSet.getPackageRegistry().put(XpathtestPackage.eNS_URI,
				XpathtestPackage.eINSTANCE);
		URI uri = URI.createPlatformPluginURI(
				"/org.eclipse.e4.emf.xpath.test/model/Test.xmi", true);
		Resource resource = resourceSet.getResource(uri, true);

		XPathContextFactory<EObject> f = EcoreXPathContextFactory.newInstance();
		XPathContext context = f.newContext(resource.getContents().get(0));

		Object application = context.getValue("/");
		assertNotNull(application);
		assertSame(RootImpl.class, application.getClass());

		application = context.getValue(".");
		assertNotNull(application);
		assertSame(RootImpl.class, application.getClass());

		try {
			application = context.getValue(".[@id='nixda']");
			fail("This query should fail with JXPathNotFoundException");
		} catch (JXPathNotFoundException path) {
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

		application = context.getValue(".[@id='root']");
		assertNotNull(application);
		assertSame(RootImpl.class, application.getClass());
		
		assertEquals("element1",context.getValue("nodes[1]/@id"));
		
		assertEquals(NodeImpl.class, context.getValue("//.[@id='element2.2']").getClass());
		assertEquals(ExtendedNodeImpl.class,context.getValue("//.[ecore:eClassName(.)='ExtendedNode']").getClass());
		
	}
}