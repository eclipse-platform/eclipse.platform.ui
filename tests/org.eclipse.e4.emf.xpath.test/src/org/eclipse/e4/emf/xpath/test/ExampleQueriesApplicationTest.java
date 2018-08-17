/*******************************************************************************
 * Copyright (c) 2018 vogella GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Lars Vogel <Lars.Vogel@vogella.com> - initial contribution
 ******************************************************************************/
package org.eclipse.e4.emf.xpath.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.e4.emf.xpath.EcoreXPathContextFactory;
import org.eclipse.e4.emf.xpath.XPathContext;
import org.eclipse.e4.emf.xpath.XPathContextFactory;
import org.eclipse.e4.ui.internal.workbench.E4XMIResourceFactory;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.commands.impl.CommandsPackageImpl;
import org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl;
import org.eclipse.e4.ui.model.application.ui.advanced.impl.AdvancedPackageImpl;
import org.eclipse.e4.ui.model.application.ui.basic.impl.BasicPackageImpl;
import org.eclipse.e4.ui.model.application.ui.impl.UiPackageImpl;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.impl.MenuPackageImpl;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ExampleQueriesApplicationTest {

	private ResourceSet resourceSet;
	private XPathContext xpathContext;
	private Resource resource;

	@SuppressWarnings("restriction")
	@Before
	public void setUp() {

		resourceSet = new ResourceSetImpl();
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap()
				.put(Resource.Factory.Registry.DEFAULT_EXTENSION, new E4XMIResourceFactory());
		resourceSet.getPackageRegistry().put(ApplicationPackageImpl.eNS_URI, ApplicationPackageImpl.eINSTANCE);
		resourceSet.getPackageRegistry().put(CommandsPackageImpl.eNS_URI, CommandsPackageImpl.eINSTANCE);
		resourceSet.getPackageRegistry().put(UiPackageImpl.eNS_URI, UiPackageImpl.eINSTANCE);
		resourceSet.getPackageRegistry().put(MenuPackageImpl.eNS_URI, MenuPackageImpl.eINSTANCE);
		resourceSet.getPackageRegistry().put(BasicPackageImpl.eNS_URI, BasicPackageImpl.eINSTANCE);
		resourceSet.getPackageRegistry().put(AdvancedPackageImpl.eNS_URI, AdvancedPackageImpl.eINSTANCE);
		resourceSet.getPackageRegistry().put(
				org.eclipse.e4.ui.model.application.descriptor.basic.impl.BasicPackageImpl.eNS_URI,
				org.eclipse.e4.ui.model.application.descriptor.basic.impl.BasicPackageImpl.eINSTANCE);

		URI uri = URI.createPlatformPluginURI("/org.eclipse.e4.emf.xpath.test/model/Application.e4xmi", true);
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

	@Test
	public void testAccessingTheApplication() {
		Object application = xpathContext.getValue("/");
		assertNotNull(application);
		assertTrue(application instanceof MApplication);
	}

	@Test
	public void testAccessingTheMainMenu() {
		Object menu = xpathContext.getValue("//mainMenu");
		assertNotNull(menu);
		assertTrue(menu instanceof MMenu);
	}

	@Test
	public void testAccessingAllMenus() {
		Object menuEntries = xpathContext.getValue("//mainMenu/children");
		assertNotNull(menuEntries);
	}


}
