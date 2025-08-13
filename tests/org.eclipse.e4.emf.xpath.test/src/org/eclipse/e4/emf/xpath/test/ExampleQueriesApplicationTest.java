/*******************************************************************************
 * Copyright (c) 2018, 2025 vogella GmbH and others.
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.eclipse.e4.emf.xpath.XPathContext;
import org.eclipse.e4.emf.xpath.XPathContextFactory;
import org.eclipse.e4.ui.internal.workbench.E4XMIResourceFactory;
import org.eclipse.e4.ui.model.application.MAddon;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.commands.impl.CommandsPackageImpl;
import org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl;
import org.eclipse.e4.ui.model.application.ui.advanced.impl.AdvancedPackageImpl;
import org.eclipse.e4.ui.model.application.ui.basic.impl.BasicPackageImpl;
import org.eclipse.e4.ui.model.application.ui.impl.UiPackageImpl;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.impl.MenuPackageImpl;
import org.eclipse.e4.ui.model.fragment.MModelFragments;
import org.eclipse.e4.ui.model.fragment.MStringModelFragment;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings({ "deprecation", "removal" })
public class ExampleQueriesApplicationTest {

	private ResourceSet resourceSet;
	private XPathContext xpathContext;
	private XPathContext xpathChildContext;
	private Resource resource;
	private Resource childResource;

	@SuppressWarnings("restriction")
	@BeforeEach
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
		XPathContextFactory<EObject> f = XPathContextFactory.newInstance();
		xpathContext = f.newContext(resource.getContents().get(0));
		URI childUri = URI.createPlatformPluginURI("/org.eclipse.e4.emf.xpath.test/model/fragment.e4xmi", true);
		childResource = resourceSet.getResource(childUri, true);
		xpathChildContext = f.newContext(childResource.getContents().get(0));
	}

	@AfterEach
	public void tearDown() {
		xpathContext = null;
		resource.unload();
		resourceSet.getResources().remove(resource);
		xpathChildContext = null;
		childResource.unload();
		resourceSet.getResources().remove(childResource);
	}

	@Test
	public void testAccessingTheApplication() {
		Object application = xpathContext.getValue("/");
		assertThat(application).isInstanceOf(MApplication.class);
	}

	@Test
	public void testAccessingTheMainMenu() {
		assertThat(xpathContext.getValue("//mainMenu")).isInstanceOf(MMenu.class);
		assertNotNull(xpathContext.getValue("//mainMenu", MMenu.class));

		assertNotNull(xpathContext.getValue("/children/mainMenu", MMenu.class));
		assertThat(xpathContext.getValue("/children/mainMenu")).isInstanceOf(MMenu.class);
	}

	@Test
	public void testAccessingAllMenus() {
		Object menuEntries = xpathContext.getValue("//mainMenu/children");
		assertThat(menuEntries).isInstanceOf(List.class);
		List<?> list = (List<?>) menuEntries;
		assertEquals(2, list.size());
		assertThat(list).allMatch(MMenu.class::isInstance, "Is instanceof of MMenu") //
				.anyMatch(e -> "File".equals(((MMenu) e).getLabel()))
				.anyMatch(e -> "Help".equals(((MMenu) e).getLabel()));
	}

	@Test
	public void testAccessingTheModelFragments() {
		Object modelFragments = xpathChildContext.getValue("/");
		assertThat(modelFragments).isInstanceOf(MModelFragments.class);
	}

	@Test
	public void testAccessingTheStringModelFragment() {
		Object modelFragment = xpathChildContext.getValue("//fragments[1]");
		assertThat(modelFragment).isInstanceOf(MStringModelFragment.class);

		MStringModelFragment mModelFragment = xpathChildContext.getValue("//fragments[1]", MStringModelFragment.class);
		assertNotNull(mModelFragment);

		Object modelFragment2 = xpathChildContext.getValue("/fragments[1]");
		assertThat(modelFragment2).isInstanceOf(MStringModelFragment.class);
	}

	@Test
	public void testAccessingTheAddons() {
		Object addon = xpathChildContext.getValue("//elements[1]");
		assertThat(addon).isInstanceOf(MAddon.class);

		MAddon mAddon = xpathChildContext.getValue("//elements[1]", MAddon.class);
		assertNotNull(mAddon);
	}
}
