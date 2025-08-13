/*******************************************************************************
 * Copyright (c) 2010, 2025 BestSolution.at and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - adjustment to EObject
 *     Thibault Le Ouay <thibaultleouay@gmail.com> - Bug 450212
 ******************************************************************************/
package org.eclipse.e4.emf.xpath.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Iterator;
import java.util.List;

import org.eclipse.e4.emf.xpath.XPathContext;
import org.eclipse.e4.emf.xpath.XPathContextFactory;
import org.eclipse.e4.emf.xpath.XPathNotFoundException;
import org.eclipse.e4.emf.xpath.test.model.xpathtest.ExtendedNode;
import org.eclipse.e4.emf.xpath.test.model.xpathtest.Menu;
import org.eclipse.e4.emf.xpath.test.model.xpathtest.Node;
import org.eclipse.e4.emf.xpath.test.model.xpathtest.Root;
import org.eclipse.e4.emf.xpath.test.model.xpathtest.XpathtestPackage;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings({ "deprecation", "removal" })
public class ExampleQueriesTestCase {

	private ResourceSet resourceSet;
	private XPathContext xpathContext;
	private Resource resource;
	private XPathContextFactory<EObject> xpathContextFactory;

	@BeforeEach
	public void setUp() {
		resourceSet = new ResourceSetImpl();
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap()
				.put(Resource.Factory.Registry.DEFAULT_EXTENSION, new XMIResourceFactoryImpl());

		// Register the package to ensure it is available during loading.
		resourceSet.getPackageRegistry().put(XpathtestPackage.eNS_URI, XpathtestPackage.eINSTANCE);
		URI uri = URI.createPlatformPluginURI("/org.eclipse.e4.emf.xpath.test/model/Test.xmi", true);
		resource = resourceSet.getResource(uri, true);
		xpathContextFactory = XPathContextFactory.newInstance();
		xpathContext = xpathContextFactory.newContext(resource.getContents().get(0));
	}

	@AfterEach
	public void tearDown() {
		xpathContext = null;
		xpathContextFactory = null;
		resource.unload();
		resourceSet.getResources().remove(resource);
	}

	@Test
	public void testSimpleQuery() {

		Object application = xpathContext.getValue("/");
		assertThat(application).isInstanceOf(Root.class);

		Root rootApplication = xpathContext.getValue("/", Root.class);
		assertNotNull(rootApplication);

		application = xpathContext.getValue(".");
		assertThat(application).isInstanceOf(Root.class);

		assertThrows(XPathNotFoundException.class, () -> xpathContext.getValue(".[@id='nixda']"));
		assertFalse(xpathContext.iterate(".[@id='nixda']").hasNext());

		application = xpathContext.getValue(".[@id='root']");
		assertThat(application).isInstanceOf(Root.class);

		rootApplication = xpathContext.getValue(".[@id='root']", Root.class);
		assertNotNull(rootApplication);

		assertEquals("element1", xpathContext.getValue("nodes[1]/@id"));

		assertThat(xpathContext.getValue("//.[@id='element2.2']")).isInstanceOf(Node.class);
		assertThat(xpathContext.getValue("//.[ecore:eClassName(.)='ExtendedNode']")).isInstanceOf(ExtendedNode.class);

		assertNotNull(xpathContext.getValue("//.[ecore:eClassName(.)='ExtendedNode']", ExtendedNode.class));

		assertEquals(rootApplication, xpathContext.getValue("."));
	}

	@Test
	public void testMenuQuery() {
		Object application = xpathContext.getValue("/");
		assertThat(application).isInstanceOf(Root.class);

		Object node = xpathContext.getValue("//.[@id='menuContainer.1']/menus[@id='menu.1']");
		assertNotNull(node);
		assertTrue(node instanceof Menu);

		Iterator<Object> i = xpathContext.iterate("//.[@id='menu.1']");
		assertTrue(i.hasNext());
		assertThat(i.next()).isInstanceOf(Node.class);
		assertTrue(i.hasNext());
		assertThat(i.next()).isInstanceOf(Menu.class);
		// EMF model has a loop in it, it just goes back to the top
		// assertFalse(i.hasNext());

		List<Menu> list = xpathContext.stream("//.[@id='menu.1']", Menu.class).toList();
		// EMF model has a loop in it, it just goes back to the top
		assertEquals(1, list.size());
	}

	@Test
	public void testRelative() {
		EObject context = resource.getContents().get(0);
		List<EObject> eContents = context.eContents();
		EObject firstElement = eContents.get(0);
		XPathContext nestedXpathContext = xpathContextFactory.newContext(xpathContext, firstElement);

		List<Node> dotList = nestedXpathContext.stream(".", Node.class).toList();
		assertEquals(1, dotList.size());
		assertSame(firstElement, dotList.get(0));

		List<Node> followingSiblingsList = nestedXpathContext.stream("following-sibling::*", Node.class).toList();
		assertEquals(eContents.subList(1, eContents.size()), followingSiblingsList);
	}
}
