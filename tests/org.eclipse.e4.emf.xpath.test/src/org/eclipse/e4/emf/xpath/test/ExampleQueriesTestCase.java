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
import org.eclipse.e4.ui.model.application.impl.ApplicationImpl;
import org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl;
import org.eclipse.e4.ui.model.application.ui.basic.impl.PartImpl;
import org.eclipse.e4.ui.model.application.ui.basic.impl.TrimmedWindowImpl;
import org.eclipse.e4.ui.model.application.ui.menu.impl.MenuImpl;
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
		resourceSet.getPackageRegistry().put(ApplicationPackageImpl.eNS_URI,
				ApplicationPackageImpl.eINSTANCE);
		URI uri = URI.createPlatformPluginURI("/org.eclipse.e4.emf.xpath.test/Application.e4xmi", true);
		Resource resource = resourceSet.getResource(uri, true);
				
		XPathContextFactory<EObject> f = EcoreXPathContextFactory.newInstance();
		XPathContext context = f.newContext(resource.getContents().get(0));
		
		Object application = context.getValue("/");
		assertNotNull(application);
		assertSame(ApplicationImpl.class, application.getClass());
		
		application = context.getValue(".");
		assertNotNull(application);
		assertSame(ApplicationImpl.class, application.getClass());
		
		try {
			application = context.getValue(".[@elementId='nixda']");
			fail("This query should fail with JXPathNotFoundException");
		} catch ( JXPathNotFoundException path) {
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		application = context.getValue(".[@elementId='TestVersion']");
		assertNotNull(application);
		assertSame(ApplicationImpl.class, application.getClass());
		
		Object window = context.getValue("children[1]");
		assertNotNull(window);
		assertSame(TrimmedWindowImpl.class, window.getClass());
		
		Object menu = context.getValue("children[1]/mainMenu[1]");
		assertNotNull(menu);
		assertSame(MenuImpl.class, menu.getClass());
		
		Object detailsView = context.getValue("//.[@elementId='DetailsView']");
		assertNotNull(detailsView);
		assertSame(PartImpl.class, detailsView.getClass());
		
		
	}
}
