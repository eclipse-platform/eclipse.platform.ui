/*******************************************************************************
 * Copyright (c) 2016 EclipseSource Muenchen GmbH and others.
 *
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Alexandra Buzila - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.tests.workbench;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import jakarta.annotation.PostConstruct;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.e4.ui.internal.workbench.E4XMIResource;
import org.eclipse.e4.ui.internal.workbench.E4XMIResourceFactory;
import org.eclipse.e4.ui.internal.workbench.ModelAssembler;
import org.eclipse.e4.ui.internal.workbench.ModelFragmentComparator;
import org.eclipse.e4.ui.internal.workbench.ModelFragmentWrapper;
import org.eclipse.e4.ui.internal.workbench.swt.E4Application;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.impl.ApplicationFactoryImpl;
import org.eclipse.e4.ui.model.application.ui.basic.MBasicFactory;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimBar;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimmedWindow;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.menu.MHandledMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MHandledToolItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuFactory;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBar;
import org.eclipse.e4.ui.model.fragment.MFragmentFactory;
import org.eclipse.e4.ui.model.fragment.MModelFragments;
import org.eclipse.e4.ui.model.fragment.MStringModelFragment;
import org.eclipse.e4.ui.workbench.swt.DisplayUISynchronize;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.swt.widgets.Display;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the ordering of fragment contributions based on their declared
 * position in list.
 */
public class ModelAssemblerFragmentOrderingTests {

	final private static String APPLICATION_ID = "org.eclipse.e4.ui.tests.modelassembler.fragmentordering.app";
	private IEclipseContext appContext;
	private MApplication application;
	private E4XMIResourceFactory factory;
	private ResourceSetImpl resourceSet;
	private E4XMIResource appResource;
	private ModelAssembler assembler;
	private MToolBar toolBar;
	private static final String MAIN_TOOLBAR_ID = "org.eclipse.e4.ui.tests.modelassembler.fragmentordering.mainWindow.mainToolBar";
	private static final String MAIN_WINDOW_ID = "org.eclipse.e4.ui.tests.modelassembler.fragmentordering.mainWindow";

	@Before
	public void setup() {
		appContext = E4Application.createDefaultContext();
		application = ApplicationFactoryImpl.eINSTANCE.createApplication();
		application.setElementId(APPLICATION_ID);
		application.setContext(appContext);

		appContext.set(MApplication.class, application);
		appContext.set(UISynchronize.class, new DisplayUISynchronize(Display.getDefault()));

		factory = new E4XMIResourceFactory();
		appResource = (E4XMIResource) factory.createResource(URI.createURI("virtualuri"));
		resourceSet = new ResourceSetImpl();
		resourceSet.getResources().add(appResource);
		appResource.getContents().add((EObject) application);
		assembler = new ModelAssembler();
		ContextInjectionFactory.invoke(assembler, PostConstruct.class, appContext);
		createWindowWithToolbar();
	}

	/**
	 * Adds a trimmed window with an empty toolbar to the application.
	 */
	private void createWindowWithToolbar() {
		MTrimmedWindow window = MBasicFactory.INSTANCE.createTrimmedWindow();
		window.setElementId(MAIN_WINDOW_ID);

		MTrimBar trimBar = MBasicFactory.INSTANCE.createTrimBar();
		trimBar.setElementId(MAIN_WINDOW_ID+".trimBar");
		toolBar = MMenuFactory.INSTANCE.createToolBar();
		toolBar.setElementId(MAIN_TOOLBAR_ID);

		trimBar.getChildren().add(toolBar);
		window.getTrimBars().add(trimBar);
		application.getChildren().add(window);
	}

	private ModelFragmentWrapper createFragmentWrapper(MModelFragments fragmentsContainer, String featureName,
			String parentElementId, List<? extends MApplicationElement> contributedElements, String positionInList,
			String contributorName, String contributorURI, boolean checkExists) {
		MStringModelFragment fragment = MFragmentFactory.INSTANCE.createStringModelFragment();
		fragment.setFeaturename(featureName);
		fragment.setParentElementId(parentElementId);
		fragment.getElements().addAll(contributedElements);
		fragment.setPositionInList(positionInList);
		fragmentsContainer.getFragments().add(fragment);
		return new ModelFragmentWrapper(fragmentsContainer, fragment, contributorName,
				contributorURI, checkExists);
	}

	/**
	 * Empty application.
	 * <p>
	 * Contributed fragments:
	 * <ul>
	 * <li>fragment1 - menu item a</li>
	 * <li>fragment2 - menu item b</li>
	 * <li>fragment3 - menu item c</li>
	 * </ul>
	 * Expected result: a, b, c.
	 */
	@Test
	public void testInvalidPositionEmptyApplication() {
		assertTrue(toolBar.getChildren().isEmpty());

		// the fragments
		Resource fragmentResource = factory.createResource(URI.createURI("fragmentvirtualuri"));
		resourceSet.getResources().add(fragmentResource);
		final String contributorName = "testNoPositionEmptyApplication";
		final String contributorURI = fragmentResource.getURI().toString();

		MModelFragments fragmentsContainerA = MFragmentFactory.INSTANCE.createModelFragments();
		fragmentResource.getContents().add((EObject) fragmentsContainerA);
		MHandledToolItem a = MMenuFactory.INSTANCE.createHandledToolItem();
		a.setElementId("a");
		ModelFragmentWrapper fragmentWrapperA = createFragmentWrapper(fragmentsContainerA, "children", MAIN_TOOLBAR_ID,
				Arrays.asList(a), null, contributorName, contributorURI, false);

		MModelFragments fragmentsContainerB = MFragmentFactory.INSTANCE.createModelFragments();
		fragmentResource.getContents().add((EObject) fragmentsContainerB);
		MHandledToolItem b = MMenuFactory.INSTANCE.createHandledToolItem();
		b.setElementId("b");
		ModelFragmentWrapper fragmentWrapperB = createFragmentWrapper(fragmentsContainerB, "children", MAIN_TOOLBAR_ID,
				Arrays.asList(b), "", contributorName, contributorURI, false);

		MModelFragments fragmentsContainerC = MFragmentFactory.INSTANCE.createModelFragments();
		fragmentResource.getContents().add((EObject) fragmentsContainerC);
		MHandledToolItem c = MMenuFactory.INSTANCE.createHandledToolItem();
		c.setElementId("c");
		ModelFragmentWrapper fragmentWrapperC = createFragmentWrapper(fragmentsContainerC, "children", MAIN_TOOLBAR_ID,
				Arrays.asList(c), "qwerty", contributorName, contributorURI, false);

		Set<ModelFragmentWrapper> fragmentList = new TreeSet<>(new ModelFragmentComparator(application));
		fragmentList.addAll(Arrays.asList(fragmentWrapperA, fragmentWrapperB, fragmentWrapperC));

		Iterator<ModelFragmentWrapper> iterator = fragmentList.iterator();
		assertEquals(fragmentWrapperA, iterator.next());
		assertEquals(fragmentWrapperB, iterator.next());
		assertEquals(fragmentWrapperC, iterator.next());

		assembler.processFragmentWrappers(fragmentList);
		assertEquals(3, toolBar.getChildren().size());
		assertEquals(a, toolBar.getChildren().get(0));
		assertEquals(b, toolBar.getChildren().get(1));
		assertEquals(c, toolBar.getChildren().get(2));
	}

	@Test
	public void testInvalidPositionNotEmptyApplication() {
		// initial application elements
		MHandledToolItem x = MMenuFactory.INSTANCE.createHandledToolItem();
		x.setElementId("x");
		toolBar.getChildren().add(x);
		MHandledToolItem y = MMenuFactory.INSTANCE.createHandledToolItem();
		y.setElementId("y");
		toolBar.getChildren().add(y);
		MHandledToolItem z = MMenuFactory.INSTANCE.createHandledToolItem();
		z.setElementId("z");
		toolBar.getChildren().add(z);

		// the fragments
		Resource fragmentResource = factory.createResource(URI.createURI("fragmentvirtualuri"));
		resourceSet.getResources().add(fragmentResource);
		final String contributorName = "testNoPositionNotEmptyApplication";
		final String contributorURI = fragmentResource.getURI().toString();

		// the contributed elements
		MModelFragments fragmentsContainerA = MFragmentFactory.INSTANCE.createModelFragments();
		fragmentResource.getContents().add((EObject) fragmentsContainerA);
		MHandledToolItem a = MMenuFactory.INSTANCE.createHandledToolItem();
		a.setElementId("a");
		ModelFragmentWrapper fragmentWrapperA = createFragmentWrapper(fragmentsContainerA, "children", MAIN_TOOLBAR_ID,
				Arrays.asList(a), "", contributorName, contributorURI, false);

		MModelFragments fragmentsContainerB = MFragmentFactory.INSTANCE.createModelFragments();
		fragmentResource.getContents().add((EObject) fragmentsContainerB);
		MHandledToolItem b = MMenuFactory.INSTANCE.createHandledToolItem();
		b.setElementId("b");
		ModelFragmentWrapper fragmentWrapperB = createFragmentWrapper(fragmentsContainerB, "children", MAIN_TOOLBAR_ID,
				Arrays.asList(b), null, contributorName, contributorURI, false);

		MModelFragments fragmentsContainerC = MFragmentFactory.INSTANCE.createModelFragments();
		fragmentResource.getContents().add((EObject) fragmentsContainerC);
		MHandledToolItem c = MMenuFactory.INSTANCE.createHandledToolItem();
		c.setElementId("c");
		ModelFragmentWrapper fragmentWrapperC = createFragmentWrapper(fragmentsContainerC, "children", MAIN_TOOLBAR_ID,
				Arrays.asList(c), "qwerty", contributorName, contributorURI, false);

		Set<ModelFragmentWrapper> fragmentList = new TreeSet<>(new ModelFragmentComparator(application));
		fragmentList.addAll(Arrays.asList(fragmentWrapperA, fragmentWrapperB, fragmentWrapperC));

		Iterator<ModelFragmentWrapper> iterator = fragmentList.iterator();
		assertEquals(fragmentWrapperA, iterator.next());
		assertEquals(fragmentWrapperB, iterator.next());
		assertEquals(fragmentWrapperC, iterator.next());

		assertEquals(3, toolBar.getChildren().size());
		assertEquals(x, toolBar.getChildren().get(0));
		assertEquals(y, toolBar.getChildren().get(1));
		assertEquals(z, toolBar.getChildren().get(2));

		assembler.processFragmentWrappers(fragmentList);

		assertEquals(6, toolBar.getChildren().size());
		assertEquals(a, toolBar.getChildren().get(3));
		assertEquals(b, toolBar.getChildren().get(4));
		assertEquals(c, toolBar.getChildren().get(5));
	}

	@Test
	public void testIncorrectIndexEmptyApplication() {
		// the fragments
		Resource fragmentResource = factory.createResource(URI.createURI("fragmentvirtualuri"));
		resourceSet.getResources().add(fragmentResource);
		final String contributorName = "testIncorrectIndex1";
		final String contributorURI = fragmentResource.getURI().toString();

		// the contributed elements
		MHandledToolItem a = MMenuFactory.INSTANCE.createHandledToolItem();
		a.setElementId("a");
		MModelFragments fragmentsContainerA = MFragmentFactory.INSTANCE.createModelFragments();
		fragmentResource.getContents().add((EObject) fragmentsContainerA);
		ModelFragmentWrapper fragmentWrapperA = createFragmentWrapper(fragmentsContainerA, "children", MAIN_TOOLBAR_ID,
				Arrays.asList(a), "index:-70", contributorName, contributorURI, false);

		MHandledToolItem b = MMenuFactory.INSTANCE.createHandledToolItem();
		b.setElementId("b");
		MModelFragments fragmentsContainerB = MFragmentFactory.INSTANCE.createModelFragments();
		fragmentResource.getContents().add((EObject) fragmentsContainerB);
		ModelFragmentWrapper fragmentWrapperB = createFragmentWrapper(fragmentsContainerB, "children", MAIN_TOOLBAR_ID,
				Arrays.asList(b), "index:70", contributorName, contributorURI, false);

		assertTrue(toolBar.getChildren().isEmpty());

		Set<ModelFragmentWrapper> fragmentList = new TreeSet<>(new ModelFragmentComparator(application));
		fragmentList.addAll(Arrays.asList(fragmentWrapperA, fragmentWrapperB));

		Iterator<ModelFragmentWrapper> iterator = fragmentList.iterator();
		assertEquals(fragmentWrapperA, iterator.next());
		assertEquals(fragmentWrapperB, iterator.next());

		assembler.processFragmentWrappers(fragmentList);

		assertEquals(2, toolBar.getChildren().size());
		assertEquals(a, toolBar.getChildren().get(0));
		assertEquals(b, toolBar.getChildren().get(1));
	}

	@Test
	public void testIncorrectIndexNotEmptyApplication() {
		// initial application elements
		MHandledToolItem x = MMenuFactory.INSTANCE.createHandledToolItem();
		x.setElementId("x");
		toolBar.getChildren().add(x);
		MHandledToolItem y = MMenuFactory.INSTANCE.createHandledToolItem();
		y.setElementId("y");
		toolBar.getChildren().add(y);
		MHandledToolItem z = MMenuFactory.INSTANCE.createHandledToolItem();
		z.setElementId("z");
		toolBar.getChildren().add(z);

		// the fragments
		Resource fragmentResource = factory.createResource(URI.createURI("fragmentvirtualuri"));
		resourceSet.getResources().add(fragmentResource);
		final String contributorName = "testIncorrectIndex1";
		final String contributorURI = fragmentResource.getURI().toString();

		// the contributed elements
		MHandledToolItem a = MMenuFactory.INSTANCE.createHandledToolItem();
		a.setElementId("a");
		MModelFragments fragmentsContainerA = MFragmentFactory.INSTANCE.createModelFragments();
		fragmentResource.getContents().add((EObject) fragmentsContainerA);
		ModelFragmentWrapper fragmentWrapperA = createFragmentWrapper(fragmentsContainerA, "children", MAIN_TOOLBAR_ID,
				Arrays.asList(a), "index:-70", contributorName, contributorURI, false);

		MHandledToolItem b = MMenuFactory.INSTANCE.createHandledToolItem();
		b.setElementId("b");
		MModelFragments fragmentsContainerB = MFragmentFactory.INSTANCE.createModelFragments();
		fragmentResource.getContents().add((EObject) fragmentsContainerB);
		ModelFragmentWrapper fragmentWrapperB = createFragmentWrapper(fragmentsContainerB, "children", MAIN_TOOLBAR_ID,
				Arrays.asList(b), "index:70", contributorName, contributorURI, false);

		Set<ModelFragmentWrapper> fragmentList = new TreeSet<>(new ModelFragmentComparator(application));
		fragmentList.addAll(Arrays.asList(fragmentWrapperA, fragmentWrapperB));

		Iterator<ModelFragmentWrapper> iterator = fragmentList.iterator();
		assertEquals(fragmentWrapperA, iterator.next());
		assertEquals(fragmentWrapperB, iterator.next());

		assertEquals(3, toolBar.getChildren().size());
		assertEquals(x, toolBar.getChildren().get(0));
		assertEquals(y, toolBar.getChildren().get(1));
		assertEquals(z, toolBar.getChildren().get(2));

		assembler.processFragmentWrappers(fragmentList);

		assertEquals(5, toolBar.getChildren().size());
		assertEquals(a, toolBar.getChildren().get(3));
		assertEquals(b, toolBar.getChildren().get(4));
	}

	/**
	 * Empty application.
	 * <p>
	 * Contributed fragments:
	 * <ul>
	 * <li>fragment1 - menu item a - index:1</li>
	 * <li>fragment2 - menu item b - index:2</li>
	 * <li>fragment3 - menu item c - index:3</li>
	 * </ul>
	 * Expected result: a, b, c.
	 */
	@Test
	public void testIndex1EmptyApplication() {
		// the fragments
		Resource fragmentResource = factory.createResource(URI.createURI("fragmentvirtualuri"));
		resourceSet.getResources().add(fragmentResource);
		final String contributorName = "testIndex1";
		final String contributorURI = fragmentResource.getURI().toString();

		// the contributed elements
		MModelFragments fragmentsContainerA = MFragmentFactory.INSTANCE.createModelFragments();
		fragmentResource.getContents().add((EObject) fragmentsContainerA);
		MHandledToolItem a = MMenuFactory.INSTANCE.createHandledToolItem();
		a.setElementId("a");
		MModelFragments fragmentsContainerB = MFragmentFactory.INSTANCE.createModelFragments();
		fragmentResource.getContents().add((EObject) fragmentsContainerB);
		MHandledToolItem b = MMenuFactory.INSTANCE.createHandledToolItem();
		b.setElementId("b");
		MModelFragments fragmentsContainerC = MFragmentFactory.INSTANCE.createModelFragments();
		fragmentResource.getContents().add((EObject) fragmentsContainerC);
		MHandledToolItem c = MMenuFactory.INSTANCE.createHandledToolItem();
		c.setElementId("c");
		ModelFragmentWrapper fragmentWrapper1 = createFragmentWrapper(fragmentsContainerA, "children", MAIN_TOOLBAR_ID,
				Arrays.asList(a), "index:1", contributorName, contributorURI, false);
		ModelFragmentWrapper fragmentWrapper2 = createFragmentWrapper(fragmentsContainerB, "children", MAIN_TOOLBAR_ID,
				Arrays.asList(b), "index:2", contributorName, contributorURI, false);
		ModelFragmentWrapper fragmentWrapper3 = createFragmentWrapper(fragmentsContainerC, "children", MAIN_TOOLBAR_ID,
				Arrays.asList(c), "index:3", contributorName, contributorURI, false);

		assertTrue(toolBar.getChildren().isEmpty());

		Set<ModelFragmentWrapper> fragmentList = new TreeSet<>(new ModelFragmentComparator(application));
		fragmentList.addAll(Arrays.asList(fragmentWrapper1, fragmentWrapper2, fragmentWrapper3));

		Iterator<ModelFragmentWrapper> iterator = fragmentList.iterator();
		assertEquals(fragmentWrapper1, iterator.next());
		assertEquals(fragmentWrapper2, iterator.next());
		assertEquals(fragmentWrapper3, iterator.next());

		assembler.processFragmentWrappers(fragmentList);

		assertEquals(a, toolBar.getChildren().get(0));
		assertEquals(b, toolBar.getChildren().get(1));
		assertEquals(c, toolBar.getChildren().get(2));
	}

	/**
	 * Empty application.
	 * <p>
	 * Contributed fragments:
	 * <ul>
	 * <li>fragment1 - menu item a - index:3</li>
	 * <li>fragment2 - menu item b - index:2</li>
	 * <li>fragment3 - menu item c - index:1</li>
	 * </ul>
	 * Expected result: c, b, a.
	 */
	@Test
	public void testIndex2EmptyApplication() {
		// the fragments
		Resource fragmentResource = factory.createResource(URI.createURI("fragmentvirtualuri"));
		resourceSet.getResources().add(fragmentResource);
		final String contributorName = "testIndex2";
		final String contributorURI = fragmentResource.getURI().toString();

		// the contributed elements
		MModelFragments fragmentsContainerA = MFragmentFactory.INSTANCE.createModelFragments();
		fragmentResource.getContents().add((EObject) fragmentsContainerA);
		MHandledToolItem a = MMenuFactory.INSTANCE.createHandledToolItem();
		a.setElementId("a");
		MModelFragments fragmentsContainerB = MFragmentFactory.INSTANCE.createModelFragments();
		fragmentResource.getContents().add((EObject) fragmentsContainerB);
		MHandledToolItem b = MMenuFactory.INSTANCE.createHandledToolItem();
		b.setElementId("b");
		MModelFragments fragmentsContainerC = MFragmentFactory.INSTANCE.createModelFragments();
		fragmentResource.getContents().add((EObject) fragmentsContainerC);
		MHandledToolItem c = MMenuFactory.INSTANCE.createHandledToolItem();
		c.setElementId("c");

		ModelFragmentWrapper fragmentWrapper1 = createFragmentWrapper(fragmentsContainerA, "children", MAIN_TOOLBAR_ID,
				Arrays.asList(a), "index:3", contributorName, contributorURI, false);
		ModelFragmentWrapper fragmentWrapper2 = createFragmentWrapper(fragmentsContainerB, "children", MAIN_TOOLBAR_ID,
				Arrays.asList(b), "index:2", contributorName, contributorURI, false);
		ModelFragmentWrapper fragmentWrapper3 = createFragmentWrapper(fragmentsContainerC, "children", MAIN_TOOLBAR_ID,
				Arrays.asList(c), "index:1", contributorName, contributorURI, false);

		assertTrue(toolBar.getChildren().isEmpty());

		Set<ModelFragmentWrapper> fragmentList = new TreeSet<>(new ModelFragmentComparator(application));
		fragmentList.addAll(Arrays.asList(fragmentWrapper1, fragmentWrapper2, fragmentWrapper3));

		Iterator<ModelFragmentWrapper> iterator = fragmentList.iterator();
		assertEquals(fragmentWrapper3, iterator.next());
		assertEquals(fragmentWrapper2, iterator.next());
		assertEquals(fragmentWrapper1, iterator.next());

		assembler.processFragmentWrappers(fragmentList);

		assertEquals(c, toolBar.getChildren().get(0));
		assertEquals(b, toolBar.getChildren().get(1));
		assertEquals(a, toolBar.getChildren().get(2));
	}

	/**
	 * Empty application.
	 * <p>
	 * Contributed fragments:
	 * <ul>
	 * <li>fragment1 - menu item a - index:3</li>
	 * <li>fragment2 - menu item b - index:1</li>
	 * <li>fragment3 - menu item c - index:2</li>
	 * </ul>
	 * Expected result: c, b, a.
	 */
	@Test
	public void testIndex3EmptyApplication() {
		// the fragments
		Resource fragmentResource = factory.createResource(URI.createURI("fragmentvirtualuri"));
		resourceSet.getResources().add(fragmentResource);
		final String contributorName = "testIndex3";
		final String contributorURI = fragmentResource.getURI().toString();

		// the contributed elements
		MModelFragments fragmentsContainerA = MFragmentFactory.INSTANCE.createModelFragments();
		fragmentResource.getContents().add((EObject) fragmentsContainerA);
		MHandledToolItem a = MMenuFactory.INSTANCE.createHandledToolItem();
		a.setElementId("a");
		MModelFragments fragmentsContainerB = MFragmentFactory.INSTANCE.createModelFragments();
		fragmentResource.getContents().add((EObject) fragmentsContainerB);
		MHandledToolItem b = MMenuFactory.INSTANCE.createHandledToolItem();
		b.setElementId("b");
		MModelFragments fragmentsContainerC = MFragmentFactory.INSTANCE.createModelFragments();
		fragmentResource.getContents().add((EObject) fragmentsContainerC);
		MHandledToolItem c = MMenuFactory.INSTANCE.createHandledToolItem();
		c.setElementId("c");

		ModelFragmentWrapper fragmentWrapper1 = createFragmentWrapper(fragmentsContainerA, "children", MAIN_TOOLBAR_ID,
				Arrays.asList(a), "index:3", contributorName, contributorURI, false);
		ModelFragmentWrapper fragmentWrapper2 = createFragmentWrapper(fragmentsContainerB, "children", MAIN_TOOLBAR_ID,
				Arrays.asList(b), "index:1", contributorName, contributorURI, false);
		ModelFragmentWrapper fragmentWrapper3 = createFragmentWrapper(fragmentsContainerC, "children", MAIN_TOOLBAR_ID,
				Arrays.asList(c), "index:2", contributorName, contributorURI, false);

		assertTrue(toolBar.getChildren().isEmpty());

		Set<ModelFragmentWrapper> fragmentList = new TreeSet<>(new ModelFragmentComparator(application));
		fragmentList.addAll(Arrays.asList(fragmentWrapper1, fragmentWrapper2, fragmentWrapper3));

		Iterator<ModelFragmentWrapper> iterator = fragmentList.iterator();
		assertEquals(fragmentWrapper2, iterator.next());
		assertEquals(fragmentWrapper3, iterator.next());
		assertEquals(fragmentWrapper1, iterator.next());

		assembler.processFragmentWrappers(fragmentList);

		assertEquals(b, toolBar.getChildren().get(0));
		assertEquals(c, toolBar.getChildren().get(1));
		assertEquals(a, toolBar.getChildren().get(2));
	}

	/**
	 * Application with menu items x y z .
	 * <p>
	 * Contributed fragments:
	 * <ul>
	 * <li>fragment1 - menu item a - index:9</li>
	 * <li>fragment2 - menu item b - index:1</li>
	 * <li>fragment3 - menu item c - index:0</li>
	 * <li>fragment4 - menu item d - index:3</li>
	 * </ul>
	 * Expected result: c, b, x, d, y, z, a
	 */
	@Test
	public void testIndex4NotEmptyApplication() {
		// initial application elements
		MHandledToolItem x = MMenuFactory.INSTANCE.createHandledToolItem();
		x.setElementId("x");
		toolBar.getChildren().add(x);
		MHandledToolItem y = MMenuFactory.INSTANCE.createHandledToolItem();
		y.setElementId("y");
		toolBar.getChildren().add(y);
		MHandledToolItem z = MMenuFactory.INSTANCE.createHandledToolItem();
		z.setElementId("z");
		toolBar.getChildren().add(z);

		// the fragments
		Resource fragmentResource = factory.createResource(URI.createURI("fragmentvirtualuri"));
		resourceSet.getResources().add(fragmentResource);
		final String contributorName = "testIndex4";
		final String contributorURI = fragmentResource.getURI().toString();

		// the contributed elements
		MModelFragments fragmentsContainerA = MFragmentFactory.INSTANCE.createModelFragments();
		fragmentResource.getContents().add((EObject) fragmentsContainerA);
		MHandledToolItem a = MMenuFactory.INSTANCE.createHandledToolItem();
		a.setElementId("a");
		MModelFragments fragmentsContainerB = MFragmentFactory.INSTANCE.createModelFragments();
		fragmentResource.getContents().add((EObject) fragmentsContainerB);
		MHandledToolItem b = MMenuFactory.INSTANCE.createHandledToolItem();
		b.setElementId("b");
		MModelFragments fragmentsContainerC = MFragmentFactory.INSTANCE.createModelFragments();
		fragmentResource.getContents().add((EObject) fragmentsContainerC);
		MHandledToolItem c = MMenuFactory.INSTANCE.createHandledToolItem();
		c.setElementId("c");
		MModelFragments fragmentsContainerD = MFragmentFactory.INSTANCE.createModelFragments();
		fragmentResource.getContents().add((EObject) fragmentsContainerD);
		MHandledToolItem d = MMenuFactory.INSTANCE.createHandledToolItem();
		d.setElementId("d");
		ModelFragmentWrapper fragmentWrapper1 = createFragmentWrapper(fragmentsContainerA, "children", MAIN_TOOLBAR_ID,
				Arrays.asList(a), "index:9", contributorName, contributorURI, false);
		ModelFragmentWrapper fragmentWrapper2 = createFragmentWrapper(fragmentsContainerB, "children", MAIN_TOOLBAR_ID,
				Arrays.asList(b), "index:1", contributorName, contributorURI, false);
		ModelFragmentWrapper fragmentWrapper3 = createFragmentWrapper(fragmentsContainerC, "children", MAIN_TOOLBAR_ID,
				Arrays.asList(c), "index:0", contributorName, contributorURI, false);
		ModelFragmentWrapper fragmentWrapper4 = createFragmentWrapper(fragmentsContainerD, "children", MAIN_TOOLBAR_ID,
				Arrays.asList(d), "index:3", contributorName, contributorURI, false);

		assertEquals(3, toolBar.getChildren().size());
		assertEquals(x, toolBar.getChildren().get(0));
		assertEquals(y, toolBar.getChildren().get(1));
		assertEquals(z, toolBar.getChildren().get(2));

		Set<ModelFragmentWrapper> fragmentList = new TreeSet<>(new ModelFragmentComparator(application));
		fragmentList.addAll(Arrays.asList(fragmentWrapper1, fragmentWrapper2, fragmentWrapper3, fragmentWrapper4));

		Iterator<ModelFragmentWrapper> iterator = fragmentList.iterator();
		assertEquals(fragmentWrapper3, iterator.next());
		assertEquals(fragmentWrapper2, iterator.next());
		assertEquals(fragmentWrapper4, iterator.next());
		assertEquals(fragmentWrapper1, iterator.next());

		assembler.processFragmentWrappers(fragmentList);

		assertEquals(c, toolBar.getChildren().get(0));
		assertEquals(b, toolBar.getChildren().get(1));
		assertEquals(x, toolBar.getChildren().get(2));
		assertEquals(d, toolBar.getChildren().get(3));
		assertEquals(y, toolBar.getChildren().get(4));
		assertEquals(z, toolBar.getChildren().get(5));
		assertEquals(a, toolBar.getChildren().get(6));
	}

	@Test
	public void testFirstEmptyApplication() {
		// the fragments
		Resource fragmentResource = factory.createResource(URI.createURI("fragmentvirtualuri"));
		resourceSet.getResources().add(fragmentResource);
		final String contributorName = "testFirstEmptyApplication";
		final String contributorURI = fragmentResource.getURI().toString();

		// the contributed elements
		MModelFragments fragmentsContainer = MFragmentFactory.INSTANCE.createModelFragments();
		fragmentResource.getContents().add((EObject) fragmentsContainer);
		MHandledToolItem a = MMenuFactory.INSTANCE.createHandledToolItem();
		a.setElementId("a");
		ModelFragmentWrapper fragmentWrapper = createFragmentWrapper(fragmentsContainer, "children", MAIN_TOOLBAR_ID,
				Arrays.asList(a), "first", contributorName, contributorURI, false);

		Set<ModelFragmentWrapper> fragmentList = new TreeSet<>(new ModelFragmentComparator(application));
		fragmentList.addAll(Arrays.asList(fragmentWrapper));

		assertEquals(0, toolBar.getChildren().size());
		assembler.processFragmentWrappers(fragmentList);

		assertEquals(1, toolBar.getChildren().size());
		assertEquals(a, toolBar.getChildren().get(0));
	}

	@Test
	public void testFirstNotEmptyApplication() {
		// initial application elements
		MHandledToolItem x = MMenuFactory.INSTANCE.createHandledToolItem();
		x.setElementId("x");
		toolBar.getChildren().add(x);
		MHandledToolItem y = MMenuFactory.INSTANCE.createHandledToolItem();
		y.setElementId("y");
		toolBar.getChildren().add(y);
		MHandledToolItem z = MMenuFactory.INSTANCE.createHandledToolItem();
		z.setElementId("z");
		toolBar.getChildren().add(z);

		// the fragments
		Resource fragmentResource = factory.createResource(URI.createURI("fragmentvirtualuri"));
		resourceSet.getResources().add(fragmentResource);
		final String contributorName = "testFirstNotEmptyApplication";
		final String contributorURI = fragmentResource.getURI().toString();

		// the contributed elements
		MModelFragments fragmentsContainer = MFragmentFactory.INSTANCE.createModelFragments();
		fragmentResource.getContents().add((EObject) fragmentsContainer);
		MHandledToolItem a = MMenuFactory.INSTANCE.createHandledToolItem();
		a.setElementId("a");
		ModelFragmentWrapper fragmentWrapper = createFragmentWrapper(fragmentsContainer, "children", MAIN_TOOLBAR_ID,
				Arrays.asList(a), "first", contributorName, contributorURI, false);

		Set<ModelFragmentWrapper> fragmentList = new TreeSet<>(new ModelFragmentComparator(application));
		fragmentList.addAll(Arrays.asList(fragmentWrapper));

		assertEquals(3, toolBar.getChildren().size());
		assertEquals(x, toolBar.getChildren().get(0));
		assertEquals(y, toolBar.getChildren().get(1));
		assertEquals(z, toolBar.getChildren().get(2));

		assembler.processFragmentWrappers(fragmentList);

		assertEquals(a, toolBar.getChildren().get(0));
		assertEquals(x, toolBar.getChildren().get(1));
		assertEquals(y, toolBar.getChildren().get(2));
		assertEquals(z, toolBar.getChildren().get(3));
	}

	@Test
	public void testFirstMultipleElements() {
		// initial application elements
		MHandledToolItem x = MMenuFactory.INSTANCE.createHandledToolItem();
		x.setElementId("x");
		toolBar.getChildren().add(x);

		// the fragments
		Resource fragmentResource = factory.createResource(URI.createURI("fragmentvirtualuri"));
		resourceSet.getResources().add(fragmentResource);
		final String contributorName = "testFirstMultipleElements";
		final String contributorURI = fragmentResource.getURI().toString();

		// the contributed elements
		MModelFragments fragmentsContainerA = MFragmentFactory.INSTANCE.createModelFragments();
		fragmentResource.getContents().add((EObject) fragmentsContainerA);
		MHandledToolItem a = MMenuFactory.INSTANCE.createHandledToolItem();
		a.setElementId("a");
		MModelFragments fragmentsContainerB = MFragmentFactory.INSTANCE.createModelFragments();
		fragmentResource.getContents().add((EObject) fragmentsContainerB);
		MHandledToolItem b = MMenuFactory.INSTANCE.createHandledToolItem();
		b.setElementId("b");
		MModelFragments fragmentsContainerC = MFragmentFactory.INSTANCE.createModelFragments();
		fragmentResource.getContents().add((EObject) fragmentsContainerC);
		MHandledToolItem c = MMenuFactory.INSTANCE.createHandledToolItem();
		c.setElementId("c");

		ModelFragmentWrapper fragmentWrapper1 = createFragmentWrapper(fragmentsContainerA, "children", MAIN_TOOLBAR_ID,
				Arrays.asList(a), "first", contributorName, contributorURI, false);
		ModelFragmentWrapper fragmentWrapper2 = createFragmentWrapper(fragmentsContainerB, "children", MAIN_TOOLBAR_ID,
				Arrays.asList(b), "first", contributorName, contributorURI, false);
		ModelFragmentWrapper fragmentWrapper3 = createFragmentWrapper(fragmentsContainerC, "children", MAIN_TOOLBAR_ID,
				Arrays.asList(c), "first", contributorName, contributorURI, false);

		Set<ModelFragmentWrapper> fragmentList = new TreeSet<>(new ModelFragmentComparator(application));
		fragmentList.addAll(Arrays.asList(fragmentWrapper1, fragmentWrapper2, fragmentWrapper3));

		Iterator<ModelFragmentWrapper> iterator = fragmentList.iterator();
		assertEquals(fragmentWrapper1, iterator.next());
		assertEquals(fragmentWrapper2, iterator.next());
		assertEquals(fragmentWrapper3, iterator.next());

		assertEquals(1, toolBar.getChildren().size());
		assertEquals(x, toolBar.getChildren().get(0));

		assembler.processFragmentWrappers(fragmentList);

		assertEquals(4, toolBar.getChildren().size());
		assertEquals(c, toolBar.getChildren().get(0));
		assertEquals(b, toolBar.getChildren().get(1));
		assertEquals(a, toolBar.getChildren().get(2));
		assertEquals(x, toolBar.getChildren().get(3));
	}

	@Test
	public void testLastEmptyApplication() {
		// the fragments
		Resource fragmentResource = factory.createResource(URI.createURI("fragmentvirtualuri"));
		resourceSet.getResources().add(fragmentResource);
		final String contributorName = "testLastEmptyApplication";
		final String contributorURI = fragmentResource.getURI().toString();

		// the contributed elements
		MModelFragments fragmentsContainer = MFragmentFactory.INSTANCE.createModelFragments();
		fragmentResource.getContents().add((EObject) fragmentsContainer);
		MHandledToolItem a = MMenuFactory.INSTANCE.createHandledToolItem();
		a.setElementId("a");

		ModelFragmentWrapper fragmentWrapper = createFragmentWrapper(fragmentsContainer, "children", MAIN_TOOLBAR_ID,
				Arrays.asList(a), "last", contributorName, contributorURI, false);

		assertEquals(0, toolBar.getChildren().size());

		Set<ModelFragmentWrapper> fragmentList = new TreeSet<>(new ModelFragmentComparator(application));
		fragmentList.addAll(Arrays.asList(fragmentWrapper));

		assembler.processFragmentWrappers(fragmentList);

		assertEquals(1, toolBar.getChildren().size());
		assertEquals(a, toolBar.getChildren().get(0));
	}

	@Test
	public void testLastNotEmptyApplication() {
		// initial application elements
		MHandledToolItem x = MMenuFactory.INSTANCE.createHandledToolItem();
		x.setElementId("x");
		toolBar.getChildren().add(x);
		MHandledToolItem y = MMenuFactory.INSTANCE.createHandledToolItem();
		y.setElementId("y");
		toolBar.getChildren().add(y);
		MHandledToolItem z = MMenuFactory.INSTANCE.createHandledToolItem();
		z.setElementId("z");
		toolBar.getChildren().add(z);

		// the fragments
		Resource fragmentResource = factory.createResource(URI.createURI("fragmentvirtualuri"));
		resourceSet.getResources().add(fragmentResource);
		final String contributorName = "testLastNotEmptyApplication";
		final String contributorURI = fragmentResource.getURI().toString();

		// the contributed elements
		MModelFragments fragmentsContainer = MFragmentFactory.INSTANCE.createModelFragments();
		fragmentResource.getContents().add((EObject) fragmentsContainer);
		MHandledToolItem a = MMenuFactory.INSTANCE.createHandledToolItem();
		a.setElementId("a");

		ModelFragmentWrapper fragmentWrapper = createFragmentWrapper(fragmentsContainer, "children", MAIN_TOOLBAR_ID,
				Arrays.asList(a), "last", contributorName, contributorURI, false);

		Set<ModelFragmentWrapper> fragmentList = new TreeSet<>(new ModelFragmentComparator(application));
		fragmentList.addAll(Arrays.asList(fragmentWrapper));

		assertEquals(3, toolBar.getChildren().size());
		assertEquals(x, toolBar.getChildren().get(0));
		assertEquals(y, toolBar.getChildren().get(1));
		assertEquals(z, toolBar.getChildren().get(2));

		assembler.processFragmentWrappers(fragmentList);

		assertEquals(x, toolBar.getChildren().get(0));
		assertEquals(y, toolBar.getChildren().get(1));
		assertEquals(z, toolBar.getChildren().get(2));
		assertEquals(a, toolBar.getChildren().get(3));
	}


	@Test
	public void testLastMultipleElements() {
		// initial application elements
		MHandledToolItem x = MMenuFactory.INSTANCE.createHandledToolItem();
		x.setElementId("x");
		toolBar.getChildren().add(x);
		MHandledToolItem y = MMenuFactory.INSTANCE.createHandledToolItem();
		y.setElementId("y");
		toolBar.getChildren().add(y);
		MHandledToolItem z = MMenuFactory.INSTANCE.createHandledToolItem();
		z.setElementId("z");
		toolBar.getChildren().add(z);

		// the fragments
		Resource fragmentResource = factory.createResource(URI.createURI("fragmentvirtualuri"));
		resourceSet.getResources().add(fragmentResource);
		final String contributorName = "testLastMultipleElements";
		final String contributorURI = fragmentResource.getURI().toString();

		// the contributed elements
		MModelFragments fragmentsContainerA = MFragmentFactory.INSTANCE.createModelFragments();
		fragmentResource.getContents().add((EObject) fragmentsContainerA);
		MHandledToolItem a = MMenuFactory.INSTANCE.createHandledToolItem();
		a.setElementId("a");
		MModelFragments fragmentsContainerB = MFragmentFactory.INSTANCE.createModelFragments();
		fragmentResource.getContents().add((EObject) fragmentsContainerB);
		MHandledToolItem b = MMenuFactory.INSTANCE.createHandledToolItem();
		b.setElementId("b");

		ModelFragmentWrapper fragmentWrapper1 = createFragmentWrapper(fragmentsContainerA, "children", MAIN_TOOLBAR_ID,
				Arrays.asList(a), "last", contributorName, contributorURI, false);
		ModelFragmentWrapper fragmentWrapper2 = createFragmentWrapper(fragmentsContainerB, "children", MAIN_TOOLBAR_ID,
				Arrays.asList(b), "last", contributorName, contributorURI, false);

		Set<ModelFragmentWrapper> fragmentList = new TreeSet<>(new ModelFragmentComparator(application));
		fragmentList.addAll(Arrays.asList(fragmentWrapper1, fragmentWrapper2));

		Iterator<ModelFragmentWrapper> iterator = fragmentList.iterator();
		assertEquals(fragmentWrapper1, iterator.next());
		assertEquals(fragmentWrapper2, iterator.next());

		assertEquals(3, toolBar.getChildren().size());
		assertEquals(x, toolBar.getChildren().get(0));
		assertEquals(y, toolBar.getChildren().get(1));
		assertEquals(z, toolBar.getChildren().get(2));

		assembler.processFragmentWrappers(fragmentList);

		assertEquals(5, toolBar.getChildren().size());
		assertEquals(x, toolBar.getChildren().get(0));
		assertEquals(y, toolBar.getChildren().get(1));
		assertEquals(z, toolBar.getChildren().get(2));
		assertEquals(a, toolBar.getChildren().get(3));
		assertEquals(b, toolBar.getChildren().get(4));
	}

	@Test
	public void testBeforeApplicationReferences() {
		// initial application elements
		MHandledToolItem x = MMenuFactory.INSTANCE.createHandledToolItem();
		x.setElementId("x");
		toolBar.getChildren().add(x);
		MHandledToolItem y = MMenuFactory.INSTANCE.createHandledToolItem();
		y.setElementId("y");
		toolBar.getChildren().add(y);
		MHandledToolItem z = MMenuFactory.INSTANCE.createHandledToolItem();
		z.setElementId("z");
		toolBar.getChildren().add(z);

		// the fragments
		Resource fragmentResource = factory.createResource(URI.createURI("fragmentvirtualuri"));
		resourceSet.getResources().add(fragmentResource);
		final String contributorName = "testBefore";
		final String contributorURI = fragmentResource.getURI().toString();

		// the contributed elements
		MModelFragments fragmentsContainer = MFragmentFactory.INSTANCE.createModelFragments();
		fragmentResource.getContents().add((EObject) fragmentsContainer);
		MHandledToolItem a = MMenuFactory.INSTANCE.createHandledToolItem();
		a.setElementId("a");
		MHandledToolItem b = MMenuFactory.INSTANCE.createHandledToolItem();
		b.setElementId("b");
		MHandledToolItem c = MMenuFactory.INSTANCE.createHandledToolItem();
		c.setElementId("c");

		ModelFragmentWrapper fragmentWrapper1 = createFragmentWrapper(fragmentsContainer, "children", MAIN_TOOLBAR_ID,
				Arrays.asList(a), "before:x", contributorName, contributorURI, false);
		ModelFragmentWrapper fragmentWrapper2 = createFragmentWrapper(fragmentsContainer, "children", MAIN_TOOLBAR_ID,
				Arrays.asList(b), "before:y", contributorName, contributorURI, false);
		ModelFragmentWrapper fragmentWrapper3 = createFragmentWrapper(fragmentsContainer, "children", MAIN_TOOLBAR_ID,
				Arrays.asList(c), "before:z", contributorName, contributorURI, false);

		Set<ModelFragmentWrapper> fragmentList = new TreeSet<>(new ModelFragmentComparator(application));
		fragmentList.addAll(Arrays.asList(fragmentWrapper1, fragmentWrapper2, fragmentWrapper3));

		Iterator<ModelFragmentWrapper> iterator = fragmentList.iterator();
		assertEquals(fragmentWrapper3, iterator.next());
		assertEquals(fragmentWrapper2, iterator.next());
		assertEquals(fragmentWrapper1, iterator.next());

		assertEquals(3, toolBar.getChildren().size());
		assertEquals(x, toolBar.getChildren().get(0));
		assertEquals(y, toolBar.getChildren().get(1));
		assertEquals(z, toolBar.getChildren().get(2));

		assembler.processFragmentWrappers(fragmentList);

		assertEquals(a, toolBar.getChildren().get(0));
		assertEquals(x, toolBar.getChildren().get(1));
		assertEquals(b, toolBar.getChildren().get(2));
		assertEquals(y, toolBar.getChildren().get(3));
		assertEquals(c, toolBar.getChildren().get(4));
		assertEquals(z, toolBar.getChildren().get(5));
	}

	@Test
	public void testBeforeNoReference() {
		// initial application elements
		MHandledToolItem x = MMenuFactory.INSTANCE.createHandledToolItem();
		x.setElementId("x");
		toolBar.getChildren().add(x);
		MHandledToolItem y = MMenuFactory.INSTANCE.createHandledToolItem();
		y.setElementId("y");
		toolBar.getChildren().add(y);
		MHandledToolItem z = MMenuFactory.INSTANCE.createHandledToolItem();
		z.setElementId("z");
		toolBar.getChildren().add(z);

		// the fragments
		Resource fragmentResource = factory.createResource(URI.createURI("fragmentvirtualuri"));
		resourceSet.getResources().add(fragmentResource);
		final String contributorName = "testBeforeEmptyTag";
		final String contributorURI = fragmentResource.getURI().toString();

		// the contributed elements
		MModelFragments fragmentsContainer = MFragmentFactory.INSTANCE.createModelFragments();
		fragmentResource.getContents().add((EObject) fragmentsContainer);
		MHandledToolItem a = MMenuFactory.INSTANCE.createHandledToolItem();
		a.setElementId("a");

		ModelFragmentWrapper fragmentWrapper1 = createFragmentWrapper(fragmentsContainer, "children", MAIN_TOOLBAR_ID,
				Arrays.asList(a), "before:", contributorName, contributorURI, false);

		assertEquals(3, toolBar.getChildren().size());
		assertEquals(x, toolBar.getChildren().get(0));
		assertEquals(y, toolBar.getChildren().get(1));
		assertEquals(z, toolBar.getChildren().get(2));

		Set<ModelFragmentWrapper> fragmentList = new TreeSet<>(new ModelFragmentComparator(application));
		fragmentList.addAll(Arrays.asList(fragmentWrapper1));
		assembler.processFragmentWrappers(fragmentList);

		assertEquals(4, toolBar.getChildren().size());
		assertEquals(a, toolBar.getChildren().get(3));
	}
	@Test
	public void testBeforeIncorrectRefElement() {
		// initial application elements
		MHandledToolItem x = MMenuFactory.INSTANCE.createHandledToolItem();
		x.setElementId("x");
		toolBar.getChildren().add(x);
		MHandledToolItem y = MMenuFactory.INSTANCE.createHandledToolItem();
		y.setElementId("y");
		toolBar.getChildren().add(y);
		MHandledToolItem z = MMenuFactory.INSTANCE.createHandledToolItem();
		z.setElementId("z");
		toolBar.getChildren().add(z);

		// the fragments
		Resource fragmentResource = factory.createResource(URI.createURI("fragmentvirtualuri"));
		resourceSet.getResources().add(fragmentResource);
		final String contributorName = "testBeforeIncorrectRefElement";
		final String contributorURI = fragmentResource.getURI().toString();

		// the contributed elements
		MModelFragments fragmentsContainer = MFragmentFactory.INSTANCE.createModelFragments();
		fragmentResource.getContents().add((EObject) fragmentsContainer);
		MHandledToolItem a = MMenuFactory.INSTANCE.createHandledToolItem();
		a.setElementId("a");

		ModelFragmentWrapper fragmentWrapper1 = createFragmentWrapper(fragmentsContainer, "children", MAIN_TOOLBAR_ID,
				Arrays.asList(a), "before:w", contributorName, contributorURI, false);

		assertEquals(3, toolBar.getChildren().size());
		assertEquals(x, toolBar.getChildren().get(0));
		assertEquals(y, toolBar.getChildren().get(1));
		assertEquals(z, toolBar.getChildren().get(2));

		Set<ModelFragmentWrapper> fragmentList = new TreeSet<>(new ModelFragmentComparator(application));
		fragmentList.addAll(Arrays.asList(fragmentWrapper1));
		assembler.processFragmentWrappers(fragmentList);

		assertEquals(4, toolBar.getChildren().size());
		assertEquals(a, toolBar.getChildren().get(3));
	}

	@Test
	public void testBeforeFragmentReference1() {
		// initial application elements
		MHandledToolItem x = MMenuFactory.INSTANCE.createHandledToolItem();
		x.setElementId("x");
		toolBar.getChildren().add(x);
		MHandledToolItem y = MMenuFactory.INSTANCE.createHandledToolItem();
		y.setElementId("y");
		toolBar.getChildren().add(y);
		MHandledToolItem z = MMenuFactory.INSTANCE.createHandledToolItem();
		z.setElementId("z");
		toolBar.getChildren().add(z);

		// the fragments
		Resource fragmentResource = factory.createResource(URI.createURI("fragmentvirtualuri"));
		resourceSet.getResources().add(fragmentResource);
		final String contributorName = "testBeforeFragmentReference";
		final String contributorURI = fragmentResource.getURI().toString();

		// the contributed elements
		MModelFragments fragmentsContainerA = MFragmentFactory.INSTANCE.createModelFragments();
		fragmentResource.getContents().add((EObject) fragmentsContainerA);
		MHandledToolItem a = MMenuFactory.INSTANCE.createHandledToolItem();
		a.setElementId("a");
		MModelFragments fragmentsContainerB = MFragmentFactory.INSTANCE.createModelFragments();
		fragmentResource.getContents().add((EObject) fragmentsContainerB);
		MHandledToolItem b = MMenuFactory.INSTANCE.createHandledToolItem();
		b.setElementId("b");

		ModelFragmentWrapper fragmentWrapper1 = createFragmentWrapper(fragmentsContainerA, "children", MAIN_TOOLBAR_ID,
				Arrays.asList(a), "before:y", contributorName, contributorURI, false);
		ModelFragmentWrapper fragmentWrapper2 = createFragmentWrapper(fragmentsContainerB, "children", MAIN_TOOLBAR_ID,
				Arrays.asList(b), "before:a", contributorName, contributorURI, false);

		Set<ModelFragmentWrapper> fragmentList = new TreeSet<>(new ModelFragmentComparator(application));
		fragmentList.addAll(Arrays.asList(fragmentWrapper1, fragmentWrapper2));

		Iterator<ModelFragmentWrapper> iterator = fragmentList.iterator();
		assertEquals(fragmentWrapper1, iterator.next());
		assertEquals(fragmentWrapper2, iterator.next());

		assertEquals(3, toolBar.getChildren().size());
		assertEquals(x, toolBar.getChildren().get(0));
		assertEquals(y, toolBar.getChildren().get(1));
		assertEquals(z, toolBar.getChildren().get(2));

		assembler.processFragmentWrappers(fragmentList);

		assertEquals(5, toolBar.getChildren().size());
		assertEquals(x, toolBar.getChildren().get(0));
		assertEquals(b, toolBar.getChildren().get(1));
		assertEquals(a, toolBar.getChildren().get(2));
		assertEquals(y, toolBar.getChildren().get(3));
		assertEquals(z, toolBar.getChildren().get(4));
	}

	/**
	 * Same test as testBeforeFragmentReference, but the fragments are
	 * contributed in a different order.
	 */
	@Test
	public void testBeforeFragmentReference2() {
		// initial application elements
		MHandledToolItem x = MMenuFactory.INSTANCE.createHandledToolItem();
		x.setElementId("x");
		toolBar.getChildren().add(x);
		MHandledToolItem y = MMenuFactory.INSTANCE.createHandledToolItem();
		y.setElementId("y");
		toolBar.getChildren().add(y);
		MHandledToolItem z = MMenuFactory.INSTANCE.createHandledToolItem();
		z.setElementId("z");
		toolBar.getChildren().add(z);

		// the fragments
		Resource fragmentResource = factory.createResource(URI.createURI("fragmentvirtualuri"));
		resourceSet.getResources().add(fragmentResource);
		final String contributorName = "testBeforeFragmentReference2";
		final String contributorURI = fragmentResource.getURI().toString();

		// the contributed elements
		MModelFragments fragmentsContainerA = MFragmentFactory.INSTANCE.createModelFragments();
		fragmentResource.getContents().add((EObject) fragmentsContainerA);
		MHandledToolItem a = MMenuFactory.INSTANCE.createHandledToolItem();
		a.setElementId("a");
		MModelFragments fragmentsContainerB = MFragmentFactory.INSTANCE.createModelFragments();
		fragmentResource.getContents().add((EObject) fragmentsContainerB);
		MHandledToolItem b = MMenuFactory.INSTANCE.createHandledToolItem();
		b.setElementId("b");

		ModelFragmentWrapper fragmentWrapper1 = createFragmentWrapper(fragmentsContainerA, "children", MAIN_TOOLBAR_ID,
				Arrays.asList(b), "before:a", contributorName, contributorURI, false);
		ModelFragmentWrapper fragmentWrapper2 = createFragmentWrapper(fragmentsContainerB, "children", MAIN_TOOLBAR_ID,
				Arrays.asList(a), "before:y", contributorName, contributorURI, false);

		Set<ModelFragmentWrapper> fragmentList = new TreeSet<>(new ModelFragmentComparator(application));
		fragmentList.addAll(Arrays.asList(fragmentWrapper1, fragmentWrapper2));

		Iterator<ModelFragmentWrapper> iterator = fragmentList.iterator();
		assertEquals(fragmentWrapper2, iterator.next());
		assertEquals(fragmentWrapper1, iterator.next());

		assertEquals(3, toolBar.getChildren().size());
		assertEquals(x, toolBar.getChildren().get(0));
		assertEquals(y, toolBar.getChildren().get(1));
		assertEquals(z, toolBar.getChildren().get(2));

		assembler.processFragmentWrappers(fragmentList);

		assertEquals(5, toolBar.getChildren().size());
		assertEquals(x, toolBar.getChildren().get(0));
		assertEquals(b, toolBar.getChildren().get(1));
		assertEquals(a, toolBar.getChildren().get(2));
		assertEquals(y, toolBar.getChildren().get(3));
		assertEquals(z, toolBar.getChildren().get(4));
	}

	@Test
	public void testBeforeFragmentReference3() {
		// initial application elements
		MHandledToolItem x = MMenuFactory.INSTANCE.createHandledToolItem();
		x.setElementId("x");
		toolBar.getChildren().add(x);
		MHandledToolItem y = MMenuFactory.INSTANCE.createHandledToolItem();
		y.setElementId("y");
		toolBar.getChildren().add(y);
		MHandledToolItem z = MMenuFactory.INSTANCE.createHandledToolItem();
		z.setElementId("z");
		toolBar.getChildren().add(z);

		// the fragments
		Resource fragmentResource = factory.createResource(URI.createURI("fragmentvirtualuri"));
		resourceSet.getResources().add(fragmentResource);
		final String contributorName = "testBeforeFragmentReference3";
		final String contributorURI = fragmentResource.getURI().toString();

		// the contributed elements
		MModelFragments fragmentsContainerA = MFragmentFactory.INSTANCE.createModelFragments();
		fragmentResource.getContents().add((EObject) fragmentsContainerA);
		MHandledToolItem a = MMenuFactory.INSTANCE.createHandledToolItem();
		a.setElementId("a");
		MModelFragments fragmentsContainerB = MFragmentFactory.INSTANCE.createModelFragments();
		fragmentResource.getContents().add((EObject) fragmentsContainerB);
		MHandledToolItem b = MMenuFactory.INSTANCE.createHandledToolItem();
		b.setElementId("b");
		MModelFragments fragmentsContainerC = MFragmentFactory.INSTANCE.createModelFragments();
		fragmentResource.getContents().add((EObject) fragmentsContainerC);
		MHandledToolItem c = MMenuFactory.INSTANCE.createHandledToolItem();
		c.setElementId("c");

		ModelFragmentWrapper fragmentWrapper1 = createFragmentWrapper(fragmentsContainerA, "children", MAIN_TOOLBAR_ID,
				Arrays.asList(a), "before:b", contributorName, contributorURI, false);
		ModelFragmentWrapper fragmentWrapper2 = createFragmentWrapper(fragmentsContainerB, "children", MAIN_TOOLBAR_ID,
				Arrays.asList(b), "before:c", contributorName, contributorURI, false);
		ModelFragmentWrapper fragmentWrapper3 = createFragmentWrapper(fragmentsContainerC, "children", MAIN_TOOLBAR_ID,
				Arrays.asList(c), "before:y", contributorName, contributorURI, false);

		Set<ModelFragmentWrapper> fragmentList = new TreeSet<>(new ModelFragmentComparator(application));
		fragmentList.addAll(Arrays.asList(fragmentWrapper1, fragmentWrapper2, fragmentWrapper3));

		Iterator<ModelFragmentWrapper> iterator = fragmentList.iterator();
		assertEquals(fragmentWrapper3, iterator.next());
		assertEquals(fragmentWrapper2, iterator.next());
		assertEquals(fragmentWrapper1, iterator.next());

		assertEquals(3, toolBar.getChildren().size());
		assertEquals(x, toolBar.getChildren().get(0));
		assertEquals(y, toolBar.getChildren().get(1));
		assertEquals(z, toolBar.getChildren().get(2));

		assembler.processFragmentWrappers(fragmentList);

		assertEquals(6, toolBar.getChildren().size());
		assertEquals(x, toolBar.getChildren().get(0));
		assertEquals(a, toolBar.getChildren().get(1));
		assertEquals(b, toolBar.getChildren().get(2));
		assertEquals(c, toolBar.getChildren().get(3));
		assertEquals(y, toolBar.getChildren().get(4));
		assertEquals(z, toolBar.getChildren().get(5));
	}

	@Test
	public void testBeforeCrossReference() {
		// initial application elements
		MHandledToolItem x = MMenuFactory.INSTANCE.createHandledToolItem();
		x.setElementId("x");
		toolBar.getChildren().add(x);
		MHandledToolItem y = MMenuFactory.INSTANCE.createHandledToolItem();
		y.setElementId("y");
		toolBar.getChildren().add(y);
		MHandledToolItem z = MMenuFactory.INSTANCE.createHandledToolItem();
		z.setElementId("z");
		toolBar.getChildren().add(z);

		// the fragments
		Resource fragmentResource = factory.createResource(URI.createURI("fragmentvirtualuri"));
		resourceSet.getResources().add(fragmentResource);
		final String contributorName = "testBeforeCrossReference";
		final String contributorURI = fragmentResource.getURI().toString();

		// the contributed elements
		MModelFragments fragmentsContainerA = MFragmentFactory.INSTANCE.createModelFragments();
		fragmentResource.getContents().add((EObject) fragmentsContainerA);
		MHandledToolItem a = MMenuFactory.INSTANCE.createHandledToolItem();
		a.setElementId("a");
		MModelFragments fragmentsContainerB = MFragmentFactory.INSTANCE.createModelFragments();
		fragmentResource.getContents().add((EObject) fragmentsContainerB);
		MHandledToolItem b = MMenuFactory.INSTANCE.createHandledToolItem();
		b.setElementId("b");

		ModelFragmentWrapper fragmentWrapper1 = createFragmentWrapper(fragmentsContainerA, "children", MAIN_TOOLBAR_ID,
				Arrays.asList(a), "before:b", contributorName, contributorURI, false);
		ModelFragmentWrapper fragmentWrapper2 = createFragmentWrapper(fragmentsContainerB, "children", MAIN_TOOLBAR_ID,
				Arrays.asList(b), "before:a", contributorName, contributorURI, false);

		Set<ModelFragmentWrapper> fragmentList = new TreeSet<>(new ModelFragmentComparator(application));
		fragmentList.addAll(Arrays.asList(fragmentWrapper1, fragmentWrapper2));

		Iterator<ModelFragmentWrapper> iterator = fragmentList.iterator();
		assertEquals(fragmentWrapper1, iterator.next());
		assertEquals(fragmentWrapper2, iterator.next());

		assertEquals(3, toolBar.getChildren().size());
		assertEquals(x, toolBar.getChildren().get(0));
		assertEquals(y, toolBar.getChildren().get(1));
		assertEquals(z, toolBar.getChildren().get(2));

		assembler.processFragmentWrappers(fragmentList);

		assertEquals(5, toolBar.getChildren().size());
		assertEquals(x, toolBar.getChildren().get(0));
		assertEquals(y, toolBar.getChildren().get(1));
		assertEquals(z, toolBar.getChildren().get(2));
		assertEquals(b, toolBar.getChildren().get(3));
		assertEquals(a, toolBar.getChildren().get(4));

	}

	/**
	 * Tests merging of a fragment declared to be before a fragment with no
	 * position.
	 */
	@Test
	public void testBeforeFragmentNoPosition() {
		// initial application elements
		MHandledToolItem x = MMenuFactory.INSTANCE.createHandledToolItem();
		x.setElementId("x");
		toolBar.getChildren().add(x);
		MHandledToolItem y = MMenuFactory.INSTANCE.createHandledToolItem();
		y.setElementId("y");
		toolBar.getChildren().add(y);
		MHandledToolItem z = MMenuFactory.INSTANCE.createHandledToolItem();
		z.setElementId("z");
		toolBar.getChildren().add(z);

		// the fragments
		Resource fragmentResource = factory.createResource(URI.createURI("fragmentvirtualuri"));
		resourceSet.getResources().add(fragmentResource);
		final String contributorName = "testBeforeFragmentNoPosition";
		final String contributorURI = fragmentResource.getURI().toString();

		// the contributed elements
		MModelFragments fragmentsContainerA = MFragmentFactory.INSTANCE.createModelFragments();
		fragmentResource.getContents().add((EObject) fragmentsContainerA);
		MHandledToolItem a = MMenuFactory.INSTANCE.createHandledToolItem();
		a.setElementId("a");
		MModelFragments fragmentsContainerB = MFragmentFactory.INSTANCE.createModelFragments();
		fragmentResource.getContents().add((EObject) fragmentsContainerB);
		MHandledToolItem b = MMenuFactory.INSTANCE.createHandledToolItem();
		b.setElementId("b");
		MModelFragments fragmentsContainerC = MFragmentFactory.INSTANCE.createModelFragments();
		fragmentResource.getContents().add((EObject) fragmentsContainerC);
		MHandledToolItem c = MMenuFactory.INSTANCE.createHandledToolItem();
		c.setElementId("c");

		ModelFragmentWrapper fragmentWrapper1 = createFragmentWrapper(fragmentsContainerA, "children", MAIN_TOOLBAR_ID,
				Arrays.asList(a), "before:b", contributorName, contributorURI, false);
		ModelFragmentWrapper fragmentWrapper2 = createFragmentWrapper(fragmentsContainerB, "children", MAIN_TOOLBAR_ID,
				Arrays.asList(b), null, contributorName, contributorURI, false);
		ModelFragmentWrapper fragmentWrapper3 = createFragmentWrapper(fragmentsContainerC, "children", MAIN_TOOLBAR_ID,
				Arrays.asList(c), "last", contributorName, contributorURI, false);

		Set<ModelFragmentWrapper> fragmentList = new TreeSet<>(new ModelFragmentComparator(application));
		fragmentList.addAll(Arrays.asList(fragmentWrapper1, fragmentWrapper2, fragmentWrapper3));

		Iterator<ModelFragmentWrapper> iterator = fragmentList.iterator();
		assertEquals(fragmentWrapper2, iterator.next());
		assertEquals(fragmentWrapper3, iterator.next());
		assertEquals(fragmentWrapper1, iterator.next());

		assertEquals(3, toolBar.getChildren().size());
		assertEquals(x, toolBar.getChildren().get(0));
		assertEquals(y, toolBar.getChildren().get(1));
		assertEquals(z, toolBar.getChildren().get(2));

		assembler.processFragmentWrappers(fragmentList);

		assertEquals(6, toolBar.getChildren().size());
		assertEquals(x, toolBar.getChildren().get(0));
		assertEquals(y, toolBar.getChildren().get(1));
		assertEquals(z, toolBar.getChildren().get(2));
		assertEquals(a, toolBar.getChildren().get(3));
		assertEquals(b, toolBar.getChildren().get(4));
		assertEquals(c, toolBar.getChildren().get(5));
	}

	@Test
	public void testAfterApplicationReferences() {
		// initial application elements
		MHandledToolItem x = MMenuFactory.INSTANCE.createHandledToolItem();
		x.setElementId("x");
		toolBar.getChildren().add(x);
		MHandledToolItem y = MMenuFactory.INSTANCE.createHandledToolItem();
		y.setElementId("y");
		toolBar.getChildren().add(y);
		MHandledToolItem z = MMenuFactory.INSTANCE.createHandledToolItem();
		z.setElementId("z");
		toolBar.getChildren().add(z);

		// the fragments
		Resource fragmentResource = factory.createResource(URI.createURI("fragmentvirtualuri"));
		resourceSet.getResources().add(fragmentResource);
		final String contributorName = "testAfter";
		final String contributorURI = fragmentResource.getURI().toString();

		// the contributed elements
		MModelFragments fragmentsContainerA = MFragmentFactory.INSTANCE.createModelFragments();
		fragmentResource.getContents().add((EObject) fragmentsContainerA);
		MHandledToolItem a = MMenuFactory.INSTANCE.createHandledToolItem();
		a.setElementId("a");
		MModelFragments fragmentsContainerB = MFragmentFactory.INSTANCE.createModelFragments();
		fragmentResource.getContents().add((EObject) fragmentsContainerB);
		MHandledToolItem b = MMenuFactory.INSTANCE.createHandledToolItem();
		b.setElementId("b");
		MModelFragments fragmentsContainerC = MFragmentFactory.INSTANCE.createModelFragments();
		fragmentResource.getContents().add((EObject) fragmentsContainerC);
		MHandledToolItem c = MMenuFactory.INSTANCE.createHandledToolItem();
		c.setElementId("c");

		ModelFragmentWrapper fragmentWrapper1 = createFragmentWrapper(fragmentsContainerA, "children", MAIN_TOOLBAR_ID,
				Arrays.asList(a), "after:x", contributorName, contributorURI, false);
		ModelFragmentWrapper fragmentWrapper2 = createFragmentWrapper(fragmentsContainerB, "children", MAIN_TOOLBAR_ID,
				Arrays.asList(b), "after:y", contributorName, contributorURI, false);
		ModelFragmentWrapper fragmentWrapper3 = createFragmentWrapper(fragmentsContainerC, "children", MAIN_TOOLBAR_ID,
				Arrays.asList(c), "after:z", contributorName, contributorURI, false);

		Set<ModelFragmentWrapper> fragmentList = new TreeSet<>(new ModelFragmentComparator(application));
		fragmentList.addAll(Arrays.asList(fragmentWrapper1, fragmentWrapper2, fragmentWrapper3));

		Iterator<ModelFragmentWrapper> iterator = fragmentList.iterator();
		assertEquals(fragmentWrapper3, iterator.next());
		assertEquals(fragmentWrapper2, iterator.next());
		assertEquals(fragmentWrapper1, iterator.next());

		assertEquals(3, toolBar.getChildren().size());
		assertEquals(x, toolBar.getChildren().get(0));
		assertEquals(y, toolBar.getChildren().get(1));
		assertEquals(z, toolBar.getChildren().get(2));

		assembler.processFragmentWrappers(fragmentList);

		assertEquals(x, toolBar.getChildren().get(0));
		assertEquals(a, toolBar.getChildren().get(1));
		assertEquals(y, toolBar.getChildren().get(2));
		assertEquals(b, toolBar.getChildren().get(3));
		assertEquals(z, toolBar.getChildren().get(4));
		assertEquals(c, toolBar.getChildren().get(5));
	}

	@Test
	public void testAfterNoReference() {
		// initial application elements
		MHandledToolItem x = MMenuFactory.INSTANCE.createHandledToolItem();
		x.setElementId("x");
		toolBar.getChildren().add(x);
		MHandledToolItem y = MMenuFactory.INSTANCE.createHandledToolItem();
		y.setElementId("y");
		toolBar.getChildren().add(y);
		MHandledToolItem z = MMenuFactory.INSTANCE.createHandledToolItem();
		z.setElementId("z");
		toolBar.getChildren().add(z);

		// the fragments
		Resource fragmentResource = factory.createResource(URI.createURI("fragmentvirtualuri"));
		resourceSet.getResources().add(fragmentResource);
		final String contributorName = "testAfterEmptyTag";
		final String contributorURI = fragmentResource.getURI().toString();

		// the contributed elements
		MModelFragments fragmentsContainer = MFragmentFactory.INSTANCE.createModelFragments();
		fragmentResource.getContents().add((EObject) fragmentsContainer);
		MHandledToolItem a = MMenuFactory.INSTANCE.createHandledToolItem();
		a.setElementId("a");

		ModelFragmentWrapper fragmentWrapper = createFragmentWrapper(fragmentsContainer, "children", MAIN_TOOLBAR_ID,
				Arrays.asList(a), "after:", contributorName, contributorURI, false);

		Set<ModelFragmentWrapper> fragmentList = new TreeSet<>(new ModelFragmentComparator(application));
		fragmentList.addAll(Arrays.asList(fragmentWrapper));

		assertEquals(3, toolBar.getChildren().size());
		assertEquals(x, toolBar.getChildren().get(0));
		assertEquals(y, toolBar.getChildren().get(1));
		assertEquals(z, toolBar.getChildren().get(2));

		assembler.processFragmentWrappers(fragmentList);

		assertEquals(4, toolBar.getChildren().size());
		assertEquals(a, toolBar.getChildren().get(3));
	}

	@Test
	public void testAfterIncorrectRefElement() {
		// initial application elements
		MHandledToolItem x = MMenuFactory.INSTANCE.createHandledToolItem();
		x.setElementId("x");
		toolBar.getChildren().add(x);
		MHandledToolItem y = MMenuFactory.INSTANCE.createHandledToolItem();
		y.setElementId("y");
		toolBar.getChildren().add(y);
		MHandledToolItem z = MMenuFactory.INSTANCE.createHandledToolItem();
		z.setElementId("z");
		toolBar.getChildren().add(z);

		// the fragments
		Resource fragmentResource = factory.createResource(URI.createURI("fragmentvirtualuri"));
		resourceSet.getResources().add(fragmentResource);
		final String contributorName = "testAfterIncorrectRefElement";
		final String contributorURI = fragmentResource.getURI().toString();

		// the contributed elements
		MModelFragments fragmentsContainer = MFragmentFactory.INSTANCE.createModelFragments();
		fragmentResource.getContents().add((EObject) fragmentsContainer);
		MHandledToolItem a = MMenuFactory.INSTANCE.createHandledToolItem();
		a.setElementId("a");

		ModelFragmentWrapper fragmentWrapper1 = createFragmentWrapper(fragmentsContainer, "children", MAIN_TOOLBAR_ID,
				Arrays.asList(a), "after:w", contributorName, contributorURI, false);

		Set<ModelFragmentWrapper> fragmentList = new TreeSet<>(new ModelFragmentComparator(application));
		fragmentList.addAll(Arrays.asList(fragmentWrapper1));

		assertEquals(3, toolBar.getChildren().size());
		assertEquals(x, toolBar.getChildren().get(0));
		assertEquals(y, toolBar.getChildren().get(1));
		assertEquals(z, toolBar.getChildren().get(2));

		assembler.processFragmentWrappers(fragmentList);

		assertEquals(4, toolBar.getChildren().size());
		assertEquals(a, toolBar.getChildren().get(3));
	}

	@Test
	public void testAfterFragmentReference1() {
		// initial application elements
		MHandledToolItem x = MMenuFactory.INSTANCE.createHandledToolItem();
		x.setElementId("x");
		toolBar.getChildren().add(x);
		MHandledToolItem y = MMenuFactory.INSTANCE.createHandledToolItem();
		y.setElementId("y");
		toolBar.getChildren().add(y);
		MHandledToolItem z = MMenuFactory.INSTANCE.createHandledToolItem();
		z.setElementId("z");
		toolBar.getChildren().add(z);

		// the fragments
		Resource fragmentResource = factory.createResource(URI.createURI("fragmentvirtualuri"));
		resourceSet.getResources().add(fragmentResource);
		final String contributorName = "testAfterFragmentReference";
		final String contributorURI = fragmentResource.getURI().toString();

		// the contributed elements
		MModelFragments fragmentsContainerA = MFragmentFactory.INSTANCE.createModelFragments();
		fragmentResource.getContents().add((EObject) fragmentsContainerA);
		MHandledToolItem a = MMenuFactory.INSTANCE.createHandledToolItem();
		a.setElementId("a");
		MModelFragments fragmentsContainerB = MFragmentFactory.INSTANCE.createModelFragments();
		fragmentResource.getContents().add((EObject) fragmentsContainerB);
		MHandledToolItem b = MMenuFactory.INSTANCE.createHandledToolItem();
		b.setElementId("b");

		ModelFragmentWrapper fragmentWrapper1 = createFragmentWrapper(fragmentsContainerA, "children", MAIN_TOOLBAR_ID,
				Arrays.asList(a), "after:y", contributorName, contributorURI, false);
		ModelFragmentWrapper fragmentWrapper2 = createFragmentWrapper(fragmentsContainerB, "children", MAIN_TOOLBAR_ID,
				Arrays.asList(b), "after:a", contributorName, contributorURI, false);

		Set<ModelFragmentWrapper> fragmentList = new TreeSet<>(new ModelFragmentComparator(application));
		fragmentList.addAll(Arrays.asList(fragmentWrapper1, fragmentWrapper2));

		Iterator<ModelFragmentWrapper> iterator = fragmentList.iterator();
		assertEquals(fragmentWrapper1, iterator.next());
		assertEquals(fragmentWrapper2, iterator.next());

		assertEquals(3, toolBar.getChildren().size());
		assertEquals(x, toolBar.getChildren().get(0));
		assertEquals(y, toolBar.getChildren().get(1));
		assertEquals(z, toolBar.getChildren().get(2));

		assembler.processFragmentWrappers(fragmentList);

		assertEquals(5, toolBar.getChildren().size());
		assertEquals(x, toolBar.getChildren().get(0));
		assertEquals(y, toolBar.getChildren().get(1));
		assertEquals(a, toolBar.getChildren().get(2));
		assertEquals(b, toolBar.getChildren().get(3));
		assertEquals(z, toolBar.getChildren().get(4));
	}

	/**
	 * Same test as testAfterFragmentReference1, but the fragments are
	 * contributed in a different order.
	 */
	@Test
	public void testAfterFragmentReference2() {
		// initial application elements
		MHandledToolItem x = MMenuFactory.INSTANCE.createHandledToolItem();
		x.setElementId("x");
		toolBar.getChildren().add(x);
		MHandledToolItem y = MMenuFactory.INSTANCE.createHandledToolItem();
		y.setElementId("y");
		toolBar.getChildren().add(y);
		MHandledToolItem z = MMenuFactory.INSTANCE.createHandledToolItem();
		z.setElementId("z");
		toolBar.getChildren().add(z);

		// the contributed elements
		MHandledToolItem a = MMenuFactory.INSTANCE.createHandledToolItem();
		a.setElementId("a");
		MHandledToolItem b = MMenuFactory.INSTANCE.createHandledToolItem();
		b.setElementId("b");

		// the fragments
		MModelFragments fragmentsContainer = MFragmentFactory.INSTANCE.createModelFragments();
		Resource fragmentResource = factory.createResource(URI.createURI("fragmentvirtualuri"));
		resourceSet.getResources().add(fragmentResource);
		fragmentResource.getContents().add((EObject) fragmentsContainer);

		final String contributorName = "testAfterFragmentReference1";
		final String contributorURI = fragmentResource.getURI().toString();

		ModelFragmentWrapper fragmentWrapper1 = createFragmentWrapper(fragmentsContainer, "children", MAIN_TOOLBAR_ID,
				Arrays.asList(b), "after:a", contributorName, contributorURI, false);
		ModelFragmentWrapper fragmentWrapper2 = createFragmentWrapper(fragmentsContainer, "children", MAIN_TOOLBAR_ID,
				Arrays.asList(a), "after:y", contributorName, contributorURI, false);

		assertEquals(3, toolBar.getChildren().size());
		assertEquals(x, toolBar.getChildren().get(0));
		assertEquals(y, toolBar.getChildren().get(1));
		assertEquals(z, toolBar.getChildren().get(2));

		Set<ModelFragmentWrapper> fragmentList = new TreeSet<>(new ModelFragmentComparator(application));
		fragmentList.addAll(Arrays.asList(fragmentWrapper1, fragmentWrapper2));

		Iterator<ModelFragmentWrapper> iterator = fragmentList.iterator();
		assertEquals(fragmentWrapper2, iterator.next());
		assertEquals(fragmentWrapper1, iterator.next());

		assembler.processFragmentWrappers(fragmentList);

		assertEquals(5, toolBar.getChildren().size());
		assertEquals(x, toolBar.getChildren().get(0));
		assertEquals(y, toolBar.getChildren().get(1));
		assertEquals(a, toolBar.getChildren().get(2));
		assertEquals(b, toolBar.getChildren().get(3));
		assertEquals(z, toolBar.getChildren().get(4));
	}

	@Test
	public void testAfterFragmentReference3() {
		// initial application elements
		MHandledToolItem x = MMenuFactory.INSTANCE.createHandledToolItem();
		x.setElementId("x");
		toolBar.getChildren().add(x);
		MHandledToolItem y = MMenuFactory.INSTANCE.createHandledToolItem();
		y.setElementId("y");
		toolBar.getChildren().add(y);
		MHandledToolItem z = MMenuFactory.INSTANCE.createHandledToolItem();
		z.setElementId("z");
		toolBar.getChildren().add(z);

		// the fragments
		Resource fragmentResource = factory.createResource(URI.createURI("fragmentvirtualuri"));
		resourceSet.getResources().add(fragmentResource);
		final String contributorName = "testAfterFragmentReference3";
		final String contributorURI = fragmentResource.getURI().toString();

		// the contributed elements
		MModelFragments fragmentsContainerA = MFragmentFactory.INSTANCE.createModelFragments();
		fragmentResource.getContents().add((EObject) fragmentsContainerA);
		MHandledToolItem a = MMenuFactory.INSTANCE.createHandledToolItem();
		a.setElementId("a");
		MModelFragments fragmentsContainerB = MFragmentFactory.INSTANCE.createModelFragments();
		fragmentResource.getContents().add((EObject) fragmentsContainerB);
		MHandledToolItem b = MMenuFactory.INSTANCE.createHandledToolItem();
		b.setElementId("b");
		MModelFragments fragmentsContainerC = MFragmentFactory.INSTANCE.createModelFragments();
		fragmentResource.getContents().add((EObject) fragmentsContainerC);
		MHandledToolItem c = MMenuFactory.INSTANCE.createHandledToolItem();
		c.setElementId("c");

		ModelFragmentWrapper fragmentWrapper1 = createFragmentWrapper(fragmentsContainerA, "children", MAIN_TOOLBAR_ID,
				Arrays.asList(a), "after:b", contributorName, contributorURI, false);
		ModelFragmentWrapper fragmentWrapper2 = createFragmentWrapper(fragmentsContainerB, "children", MAIN_TOOLBAR_ID,
				Arrays.asList(b), "after:c", contributorName, contributorURI, false);
		ModelFragmentWrapper fragmentWrapper3 = createFragmentWrapper(fragmentsContainerC, "children", MAIN_TOOLBAR_ID,
				Arrays.asList(c), "after:y", contributorName, contributorURI, false);

		Set<ModelFragmentWrapper> fragmentList = new TreeSet<>(new ModelFragmentComparator(application));
		fragmentList.addAll(Arrays.asList(fragmentWrapper1, fragmentWrapper2, fragmentWrapper3));

		Iterator<ModelFragmentWrapper> iterator = fragmentList.iterator();
		assertEquals(fragmentWrapper3, iterator.next());
		assertEquals(fragmentWrapper2, iterator.next());
		assertEquals(fragmentWrapper1, iterator.next());

		assertEquals(3, toolBar.getChildren().size());
		assertEquals(x, toolBar.getChildren().get(0));
		assertEquals(y, toolBar.getChildren().get(1));
		assertEquals(z, toolBar.getChildren().get(2));

		assembler.processFragmentWrappers(fragmentList);

		assertEquals(6, toolBar.getChildren().size());
		assertEquals(x, toolBar.getChildren().get(0));
		assertEquals(y, toolBar.getChildren().get(1));
		assertEquals(c, toolBar.getChildren().get(2));
		assertEquals(b, toolBar.getChildren().get(3));
		assertEquals(a, toolBar.getChildren().get(4));
		assertEquals(z, toolBar.getChildren().get(5));
	}

	/**
	 * Tests merging of a fragment declared to be after a fragment with no
	 * position.
	 */
	@Test
	public void testAfterFragmentNoPosition() {
		// initial application elements
		MHandledToolItem x = MMenuFactory.INSTANCE.createHandledToolItem();
		x.setElementId("x");
		toolBar.getChildren().add(x);
		MHandledToolItem y = MMenuFactory.INSTANCE.createHandledToolItem();
		y.setElementId("y");
		toolBar.getChildren().add(y);
		MHandledToolItem z = MMenuFactory.INSTANCE.createHandledToolItem();
		z.setElementId("z");
		toolBar.getChildren().add(z);

		// the fragments
		Resource fragmentResource = factory.createResource(URI.createURI("fragmentvirtualuri"));
		resourceSet.getResources().add(fragmentResource);
		final String contributorName = "testAfterFragmentNoPosition";
		final String contributorURI = fragmentResource.getURI().toString();

		// the contributed elements
		MModelFragments fragmentsContainerA = MFragmentFactory.INSTANCE.createModelFragments();
		fragmentResource.getContents().add((EObject) fragmentsContainerA);
		MHandledToolItem a = MMenuFactory.INSTANCE.createHandledToolItem();
		a.setElementId("a");
		MModelFragments fragmentsContainerB = MFragmentFactory.INSTANCE.createModelFragments();
		fragmentResource.getContents().add((EObject) fragmentsContainerB);
		MHandledToolItem b = MMenuFactory.INSTANCE.createHandledToolItem();
		b.setElementId("b");
		MModelFragments fragmentsContainerC = MFragmentFactory.INSTANCE.createModelFragments();
		fragmentResource.getContents().add((EObject) fragmentsContainerC);
		MHandledToolItem c = MMenuFactory.INSTANCE.createHandledToolItem();
		c.setElementId("c");

		ModelFragmentWrapper fragmentWrapper1 = createFragmentWrapper(fragmentsContainerA, "children", MAIN_TOOLBAR_ID,
				Arrays.asList(a), "after:b", contributorName, contributorURI, false);
		ModelFragmentWrapper fragmentWrapper2 = createFragmentWrapper(fragmentsContainerB, "children", MAIN_TOOLBAR_ID,
				Arrays.asList(b), "", contributorName, contributorURI, false);
		ModelFragmentWrapper fragmentWrapper3 = createFragmentWrapper(fragmentsContainerC, "children", MAIN_TOOLBAR_ID,
				Arrays.asList(c), "last", contributorName, contributorURI, false);

		Set<ModelFragmentWrapper> fragmentList = new TreeSet<>(new ModelFragmentComparator(application));
		fragmentList.addAll(Arrays.asList(fragmentWrapper3, fragmentWrapper1, fragmentWrapper2));

		Iterator<ModelFragmentWrapper> iterator = fragmentList.iterator();
		assertEquals(fragmentWrapper2, iterator.next());
		assertEquals(fragmentWrapper3, iterator.next());
		assertEquals(fragmentWrapper1, iterator.next());

		assertEquals(3, toolBar.getChildren().size());
		assertEquals(x, toolBar.getChildren().get(0));
		assertEquals(y, toolBar.getChildren().get(1));
		assertEquals(z, toolBar.getChildren().get(2));

		assembler.processFragmentWrappers(fragmentList);

		assertEquals(6, toolBar.getChildren().size());
		assertEquals(x, toolBar.getChildren().get(0));
		assertEquals(y, toolBar.getChildren().get(1));
		assertEquals(z, toolBar.getChildren().get(2));
		assertEquals(b, toolBar.getChildren().get(3));
		assertEquals(a, toolBar.getChildren().get(4));
		assertEquals(c, toolBar.getChildren().get(5));
	}

	@Test
	public void testAfterCrossReference() {
		// initial application elements
		MHandledToolItem x = MMenuFactory.INSTANCE.createHandledToolItem();
		x.setElementId("x");
		toolBar.getChildren().add(x);
		MHandledToolItem y = MMenuFactory.INSTANCE.createHandledToolItem();
		y.setElementId("y");
		toolBar.getChildren().add(y);
		MHandledToolItem z = MMenuFactory.INSTANCE.createHandledToolItem();
		z.setElementId("z");
		toolBar.getChildren().add(z);

		// the fragments
		Resource fragmentResource = factory.createResource(URI.createURI("fragmentvirtualuri"));
		resourceSet.getResources().add(fragmentResource);
		final String contributorName = "testAfterCrossReference";
		final String contributorURI = fragmentResource.getURI().toString();

		// the contributed elements
		MModelFragments fragmentsContainerA = MFragmentFactory.INSTANCE.createModelFragments();
		fragmentResource.getContents().add((EObject) fragmentsContainerA);
		MHandledToolItem a = MMenuFactory.INSTANCE.createHandledToolItem();
		a.setElementId("a");
		MModelFragments fragmentsContainerB = MFragmentFactory.INSTANCE.createModelFragments();
		fragmentResource.getContents().add((EObject) fragmentsContainerB);
		MHandledToolItem b = MMenuFactory.INSTANCE.createHandledToolItem();
		b.setElementId("b");

		ModelFragmentWrapper fragmentWrapper1 = createFragmentWrapper(fragmentsContainerA, "children", MAIN_TOOLBAR_ID,
				Arrays.asList(a), "after:b", contributorName, contributorURI, false);
		ModelFragmentWrapper fragmentWrapper2 = createFragmentWrapper(fragmentsContainerB, "children", MAIN_TOOLBAR_ID,
				Arrays.asList(b), "after:a", contributorName, contributorURI, false);

		Set<ModelFragmentWrapper> fragmentList = new TreeSet<>(new ModelFragmentComparator(application));
		fragmentList.addAll(Arrays.asList(fragmentWrapper1, fragmentWrapper2));

		Iterator<ModelFragmentWrapper> iterator = fragmentList.iterator();
		assertEquals(fragmentWrapper1, iterator.next());
		assertEquals(fragmentWrapper2, iterator.next());

		assertEquals(3, toolBar.getChildren().size());
		assertEquals(x, toolBar.getChildren().get(0));
		assertEquals(y, toolBar.getChildren().get(1));
		assertEquals(z, toolBar.getChildren().get(2));

		assembler.processFragmentWrappers(fragmentList);

		assertEquals(5, toolBar.getChildren().size());
		assertEquals(x, toolBar.getChildren().get(0));
		assertEquals(y, toolBar.getChildren().get(1));
		assertEquals(z, toolBar.getChildren().get(2));
		assertEquals(a, toolBar.getChildren().get(3));
		assertEquals(b, toolBar.getChildren().get(4));
	}

	@Test
	public void testAfterBeforeCrossReference() {
		// initial application elements
		MHandledToolItem x = MMenuFactory.INSTANCE.createHandledToolItem();
		x.setElementId("x");
		toolBar.getChildren().add(x);
		MHandledToolItem y = MMenuFactory.INSTANCE.createHandledToolItem();
		y.setElementId("y");
		toolBar.getChildren().add(y);
		MHandledToolItem z = MMenuFactory.INSTANCE.createHandledToolItem();
		z.setElementId("z");
		toolBar.getChildren().add(z);

		// the fragments
		Resource fragmentResource = factory.createResource(URI.createURI("fragmentvirtualuri"));
		resourceSet.getResources().add(fragmentResource);
		final String contributorName = "testAfterBeforeCrossReference";
		final String contributorURI = fragmentResource.getURI().toString();

		// the contributed elements
		MModelFragments fragmentsContainerA = MFragmentFactory.INSTANCE.createModelFragments();
		fragmentResource.getContents().add((EObject) fragmentsContainerA);
		MHandledToolItem a = MMenuFactory.INSTANCE.createHandledToolItem();
		a.setElementId("a");
		MModelFragments fragmentsContainerB = MFragmentFactory.INSTANCE.createModelFragments();
		fragmentResource.getContents().add((EObject) fragmentsContainerB);
		MHandledToolItem b = MMenuFactory.INSTANCE.createHandledToolItem();
		b.setElementId("b");

		ModelFragmentWrapper fragmentWrapper1 = createFragmentWrapper(fragmentsContainerA, "children", MAIN_TOOLBAR_ID,
				Arrays.asList(a), "after:b", contributorName, contributorURI, false);
		ModelFragmentWrapper fragmentWrapper2 = createFragmentWrapper(fragmentsContainerB, "children", MAIN_TOOLBAR_ID,
				Arrays.asList(b), "before:a", contributorName, contributorURI, false);

		Set<ModelFragmentWrapper> fragmentList = new TreeSet<>(new ModelFragmentComparator(application));
		fragmentList.addAll(Arrays.asList(fragmentWrapper1, fragmentWrapper2));

		Iterator<ModelFragmentWrapper> iterator = fragmentList.iterator();
		assertEquals(fragmentWrapper1, iterator.next());
		assertEquals(fragmentWrapper2, iterator.next());

		assertEquals(3, toolBar.getChildren().size());
		assertEquals(x, toolBar.getChildren().get(0));
		assertEquals(y, toolBar.getChildren().get(1));
		assertEquals(z, toolBar.getChildren().get(2));

		assembler.processFragmentWrappers(fragmentList);

		assertEquals(5, toolBar.getChildren().size());
		assertEquals(x, toolBar.getChildren().get(0));
		assertEquals(y, toolBar.getChildren().get(1));
		assertEquals(z, toolBar.getChildren().get(2));
		assertEquals(b, toolBar.getChildren().get(3));
		assertEquals(a, toolBar.getChildren().get(4));
	}

	@Test
	public void testFirstBefore() {
		// initial application elements
		MHandledToolItem x = MMenuFactory.INSTANCE.createHandledToolItem();
		x.setElementId("x");
		toolBar.getChildren().add(x);
		MHandledToolItem y = MMenuFactory.INSTANCE.createHandledToolItem();
		y.setElementId("y");
		toolBar.getChildren().add(y);
		MHandledToolItem z = MMenuFactory.INSTANCE.createHandledToolItem();
		z.setElementId("z");
		toolBar.getChildren().add(z);

		// the fragments
		Resource fragmentResource = factory.createResource(URI.createURI("fragmentvirtualuri"));
		resourceSet.getResources().add(fragmentResource);
		final String contributorName = "testFirstBefore";
		final String contributorURI = fragmentResource.getURI().toString();

		// the contributed elements
		MModelFragments fragmentsContainerA = MFragmentFactory.INSTANCE.createModelFragments();
		fragmentResource.getContents().add((EObject) fragmentsContainerA);
		MHandledToolItem a = MMenuFactory.INSTANCE.createHandledToolItem();
		a.setElementId("a");
		MModelFragments fragmentsContainerB = MFragmentFactory.INSTANCE.createModelFragments();
		fragmentResource.getContents().add((EObject) fragmentsContainerB);
		MHandledToolItem b = MMenuFactory.INSTANCE.createHandledToolItem();
		b.setElementId("b");

		ModelFragmentWrapper fragmentWrapper1 = createFragmentWrapper(fragmentsContainerA, "children", MAIN_TOOLBAR_ID,
				Arrays.asList(a), "first", contributorName, contributorURI, false);
		ModelFragmentWrapper fragmentWrapper2 = createFragmentWrapper(fragmentsContainerB, "children", MAIN_TOOLBAR_ID,
				Arrays.asList(b), "before:a", contributorName, contributorURI, false);

		Set<ModelFragmentWrapper> fragmentList = new TreeSet<>(new ModelFragmentComparator(application));
		fragmentList.addAll(Arrays.asList(fragmentWrapper1, fragmentWrapper2));

		Iterator<ModelFragmentWrapper> iterator = fragmentList.iterator();
		assertEquals(fragmentWrapper1, iterator.next());
		assertEquals(fragmentWrapper2, iterator.next());

		assertEquals(3, toolBar.getChildren().size());
		assertEquals(x, toolBar.getChildren().get(0));
		assertEquals(y, toolBar.getChildren().get(1));
		assertEquals(z, toolBar.getChildren().get(2));

		assembler.processFragmentWrappers(fragmentList);

		assertEquals(5, toolBar.getChildren().size());
		assertEquals(b, toolBar.getChildren().get(0));
		assertEquals(a, toolBar.getChildren().get(1));
		assertEquals(x, toolBar.getChildren().get(2));
		assertEquals(y, toolBar.getChildren().get(3));
		assertEquals(z, toolBar.getChildren().get(4));
	}

	@Test
	public void testLastAfter() {
		// initial application elements
		MHandledToolItem x = MMenuFactory.INSTANCE.createHandledToolItem();
		x.setElementId("x");
		toolBar.getChildren().add(x);
		MHandledToolItem y = MMenuFactory.INSTANCE.createHandledToolItem();
		y.setElementId("y");
		toolBar.getChildren().add(y);
		MHandledToolItem z = MMenuFactory.INSTANCE.createHandledToolItem();
		z.setElementId("z");
		toolBar.getChildren().add(z);

		// the fragments
		Resource fragmentResource = factory.createResource(URI.createURI("fragmentvirtualuri"));
		resourceSet.getResources().add(fragmentResource);
		final String contributorName = "testLastAfter";
		final String contributorURI = fragmentResource.getURI().toString();

		// the contributed elements
		MModelFragments fragmentsContainerA = MFragmentFactory.INSTANCE.createModelFragments();
		fragmentResource.getContents().add((EObject) fragmentsContainerA);
		MHandledToolItem a = MMenuFactory.INSTANCE.createHandledToolItem();
		a.setElementId("a");
		MModelFragments fragmentsContainerB = MFragmentFactory.INSTANCE.createModelFragments();
		fragmentResource.getContents().add((EObject) fragmentsContainerB);
		MHandledToolItem b = MMenuFactory.INSTANCE.createHandledToolItem();
		b.setElementId("b");

		ModelFragmentWrapper fragmentWrapper1 = createFragmentWrapper(fragmentsContainerA, "children", MAIN_TOOLBAR_ID,
				Arrays.asList(a), "last", contributorName, contributorURI, false);
		ModelFragmentWrapper fragmentWrapper2 = createFragmentWrapper(fragmentsContainerB, "children", MAIN_TOOLBAR_ID,
				Arrays.asList(b), "after:a", contributorName, contributorURI, false);

		Set<ModelFragmentWrapper> fragmentList = new TreeSet<>(new ModelFragmentComparator(application));
		fragmentList.addAll(Arrays.asList(fragmentWrapper1, fragmentWrapper2));

		Iterator<ModelFragmentWrapper> iterator = fragmentList.iterator();
		assertEquals(fragmentWrapper1, iterator.next());
		assertEquals(fragmentWrapper2, iterator.next());

		assertEquals(3, toolBar.getChildren().size());
		assertEquals(x, toolBar.getChildren().get(0));
		assertEquals(y, toolBar.getChildren().get(1));
		assertEquals(z, toolBar.getChildren().get(2));

		assembler.processFragmentWrappers(fragmentList);

		assertEquals(5, toolBar.getChildren().size());
		assertEquals(x, toolBar.getChildren().get(0));
		assertEquals(y, toolBar.getChildren().get(1));
		assertEquals(z, toolBar.getChildren().get(2));
		assertEquals(a, toolBar.getChildren().get(3));
		assertEquals(b, toolBar.getChildren().get(4));
	}

	@Test
	public void testOrderAndMerge() {
		// initial application elements
		MHandledToolItem x = MMenuFactory.INSTANCE.createHandledToolItem();
		x.setElementId("x");
		toolBar.getChildren().add(x);
		MHandledToolItem y = MMenuFactory.INSTANCE.createHandledToolItem();
		y.setElementId("y");
		toolBar.getChildren().add(y);
		MHandledToolItem z = MMenuFactory.INSTANCE.createHandledToolItem();
		z.setElementId("z");
		toolBar.getChildren().add(z);

		// the fragments
		Resource fragmentResource = factory.createResource(URI.createURI("fragmentvirtualuri"));
		resourceSet.getResources().add(fragmentResource);
		final String contributorName = "testFirstNotEmptyApplication";
		final String contributorURI = fragmentResource.getURI().toString();

		// the contributed elements
		MModelFragments fragmentsContainerA = MFragmentFactory.INSTANCE.createModelFragments();
		fragmentResource.getContents().add((EObject) fragmentsContainerA);
		MHandledToolItem a = MMenuFactory.INSTANCE.createHandledToolItem();
		a.setElementId("a");
		MModelFragments fragmentsContainerB = MFragmentFactory.INSTANCE.createModelFragments();
		fragmentResource.getContents().add((EObject) fragmentsContainerB);
		MHandledToolItem b = MMenuFactory.INSTANCE.createHandledToolItem();
		b.setElementId("b");
		MModelFragments fragmentsContainerC = MFragmentFactory.INSTANCE.createModelFragments();
		fragmentResource.getContents().add((EObject) fragmentsContainerC);
		MHandledToolItem c = MMenuFactory.INSTANCE.createHandledToolItem();
		c.setElementId("c");
		MModelFragments fragmentsContainerD = MFragmentFactory.INSTANCE.createModelFragments();
		fragmentResource.getContents().add((EObject) fragmentsContainerD);
		MHandledToolItem d = MMenuFactory.INSTANCE.createHandledToolItem();
		d.setElementId("d");
		MModelFragments fragmentsContainerE = MFragmentFactory.INSTANCE.createModelFragments();
		fragmentResource.getContents().add((EObject) fragmentsContainerE);
		MHandledToolItem e = MMenuFactory.INSTANCE.createHandledToolItem();
		e.setElementId("e");
		MModelFragments fragmentsContainerF = MFragmentFactory.INSTANCE.createModelFragments();
		fragmentResource.getContents().add((EObject) fragmentsContainerF);
		MHandledToolItem f = MMenuFactory.INSTANCE.createHandledToolItem();
		f.setElementId("f");
		MModelFragments fragmentsContainerG = MFragmentFactory.INSTANCE.createModelFragments();
		fragmentResource.getContents().add((EObject) fragmentsContainerG);
		MHandledToolItem g = MMenuFactory.INSTANCE.createHandledToolItem();
		g.setElementId("g");
		MModelFragments fragmentsContainerH = MFragmentFactory.INSTANCE.createModelFragments();
		fragmentResource.getContents().add((EObject) fragmentsContainerH);
		MHandledToolItem h = MMenuFactory.INSTANCE.createHandledToolItem();
		h.setElementId("h");
		MModelFragments fragmentsContainerI = MFragmentFactory.INSTANCE.createModelFragments();
		fragmentResource.getContents().add((EObject) fragmentsContainerI);
		MHandledToolItem i = MMenuFactory.INSTANCE.createHandledToolItem();
		i.setElementId("i");
		MModelFragments fragmentsContainerJ = MFragmentFactory.INSTANCE.createModelFragments();
		fragmentResource.getContents().add((EObject) fragmentsContainerJ);
		MHandledToolItem j = MMenuFactory.INSTANCE.createHandledToolItem();
		j.setElementId("j");
		MModelFragments fragmentsContainerK = MFragmentFactory.INSTANCE.createModelFragments();
		fragmentResource.getContents().add((EObject) fragmentsContainerK);
		MHandledToolItem k = MMenuFactory.INSTANCE.createHandledToolItem();
		k.setElementId("k");
		ModelFragmentWrapper fragmentWrapperA = createFragmentWrapper(fragmentsContainerA, "children", MAIN_TOOLBAR_ID,
				Arrays.asList(a), "first", contributorName, contributorURI, false);
		ModelFragmentWrapper fragmentWrapperB = createFragmentWrapper(fragmentsContainerB, "children", MAIN_TOOLBAR_ID,
				Arrays.asList(b), "last", contributorName, contributorURI, false);
		ModelFragmentWrapper fragmentWrapperC = createFragmentWrapper(fragmentsContainerC, "children", MAIN_TOOLBAR_ID,
				Arrays.asList(c), null, contributorName, contributorURI, false);
		ModelFragmentWrapper fragmentWrapperD = createFragmentWrapper(fragmentsContainerD, "children", MAIN_TOOLBAR_ID,
				Arrays.asList(d), "", contributorName, contributorURI, false);
		ModelFragmentWrapper fragmentWrapperE = createFragmentWrapper(fragmentsContainerE, "children", MAIN_TOOLBAR_ID,
				Arrays.asList(e), "qwert", contributorName, contributorURI, false);
		ModelFragmentWrapper fragmentWrapperF = createFragmentWrapper(fragmentsContainerF, "children", MAIN_TOOLBAR_ID,
				Arrays.asList(f), "index:1", contributorName, contributorURI, false);
		ModelFragmentWrapper fragmentWrapperG = createFragmentWrapper(fragmentsContainerG, "children", MAIN_TOOLBAR_ID,
				Arrays.asList(g), "index:-1", contributorName, contributorURI, false);
		ModelFragmentWrapper fragmentWrapperH = createFragmentWrapper(fragmentsContainerH, "children", MAIN_TOOLBAR_ID,
				Arrays.asList(h), "before:x", contributorName, contributorURI, false);
		ModelFragmentWrapper fragmentWrapperI = createFragmentWrapper(fragmentsContainerI, "children", MAIN_TOOLBAR_ID,
				Arrays.asList(i), "before:a", contributorName, contributorURI, false);
		ModelFragmentWrapper fragmentWrapperJ = createFragmentWrapper(fragmentsContainerJ, "children", MAIN_TOOLBAR_ID,
				Arrays.asList(j), "after:z", contributorName, contributorURI, false);
		ModelFragmentWrapper fragmentWrapperK = createFragmentWrapper(fragmentsContainerK, "children", MAIN_TOOLBAR_ID,
				Arrays.asList(k), "after:b", contributorName, contributorURI, false);

		Set<ModelFragmentWrapper> fragmentList = new TreeSet<>(new ModelFragmentComparator(application));
		fragmentList.addAll(Arrays.asList(fragmentWrapperA, fragmentWrapperB, fragmentWrapperC, fragmentWrapperD,
				fragmentWrapperE, fragmentWrapperF, fragmentWrapperG, fragmentWrapperH, fragmentWrapperI,
				fragmentWrapperJ, fragmentWrapperK));

		Iterator<ModelFragmentWrapper> iterator = fragmentList.iterator();
		assertEquals(fragmentWrapperG, iterator.next());
		assertEquals(fragmentWrapperF, iterator.next());
		assertEquals(fragmentWrapperC, iterator.next());
		assertEquals(fragmentWrapperD, iterator.next());
		assertEquals(fragmentWrapperE, iterator.next());
		assertEquals(fragmentWrapperA, iterator.next());
		assertEquals(fragmentWrapperB, iterator.next());
		assertEquals(fragmentWrapperJ, iterator.next());
		assertEquals(fragmentWrapperH, iterator.next());
		assertEquals(fragmentWrapperI, iterator.next());
		assertEquals(fragmentWrapperK, iterator.next());

		assertEquals(3, toolBar.getChildren().size());
		assertEquals(x, toolBar.getChildren().get(0));
		assertEquals(y, toolBar.getChildren().get(1));
		assertEquals(z, toolBar.getChildren().get(2));

		assembler.processFragmentWrappers(fragmentList);

		assertEquals(i, toolBar.getChildren().get(0));
		assertEquals(a, toolBar.getChildren().get(1));
		assertEquals(h, toolBar.getChildren().get(2));
		assertEquals(x, toolBar.getChildren().get(3));
		assertEquals(f, toolBar.getChildren().get(4));
		assertEquals(y, toolBar.getChildren().get(5));
		assertEquals(z, toolBar.getChildren().get(6));
		assertEquals(j, toolBar.getChildren().get(7));
		assertEquals(g, toolBar.getChildren().get(8));
		assertEquals(c, toolBar.getChildren().get(9));
		assertEquals(d, toolBar.getChildren().get(10));
		assertEquals(e, toolBar.getChildren().get(11));
		assertEquals(b, toolBar.getChildren().get(12));
		assertEquals(k, toolBar.getChildren().get(13));
	}

	/**
	 * Test Model:
	 *
	 * <pre>
	 * Menu 0
	 * 	Menu 0.0 index:1
	 * 		MenuItem 0.0.0 index:0
	 * 		MenuItem 0.0.1 index:10
	 * 	Menu 0.1 index:10
	 * 	Menu 0.2 index:20
	 * </pre>
	 */
	@Test
	public void testMenuAndItemsInFragments() {
		// initial application elements
		MWindow window = application.getChildren().get(0);
		MMenu menu0 = MMenuFactory.INSTANCE.createMenu();
		window.setMainMenu(menu0);
		menu0.setElementId("menu0");

		Set<ModelFragmentWrapper> fragmentList = new HashSet<>();
		// fragment preparations
		MMenu menu00 = MMenuFactory.INSTANCE.createMenu();
		menu00.setElementId("menu00");
		MMenu menu01 = MMenuFactory.INSTANCE.createMenu();
		menu01.setElementId("menu01");
		MMenu menu02 = MMenuFactory.INSTANCE.createMenu();
		menu02.setElementId("menu02");
		MHandledMenuItem menuitem000 = MMenuFactory.INSTANCE.createHandledMenuItem();
		menuitem000.setElementId("menuitem000");
		MHandledMenuItem menuitem001 = MMenuFactory.INSTANCE.createHandledMenuItem();
		menuitem001.setElementId("menuitem001");
		// the fragments
		{
			Resource fragmentResource = factory.createResource(URI.createURI("fragmentvirtualuri1"));
			resourceSet.getResources().add(fragmentResource);
			final String contributorName = "menu00";
			final String contributorURI = fragmentResource.getURI().toString();

			// the contributed elements
			MModelFragments fragmentsContainer = MFragmentFactory.INSTANCE.createModelFragments();
			fragmentResource.getContents().add((EObject) fragmentsContainer);

			ModelFragmentWrapper fragmentWrapper = createFragmentWrapper(fragmentsContainer, "children", "menu0",
					Arrays.asList(menu00), "index:1", contributorName, contributorURI, false);
			fragmentList.add(fragmentWrapper);
		}
		{
			Resource fragmentResource = factory.createResource(URI.createURI("fragmentvirtualuri2"));
			resourceSet.getResources().add(fragmentResource);
			final String contributorName = "menu01";
			final String contributorURI = fragmentResource.getURI().toString();

			// the contributed elements
			MModelFragments fragmentsContainer = MFragmentFactory.INSTANCE.createModelFragments();
			fragmentResource.getContents().add((EObject) fragmentsContainer);

			ModelFragmentWrapper fragmentWrapper = createFragmentWrapper(fragmentsContainer, "children", "menu0",
					Arrays.asList(menu01), "index:10", contributorName, contributorURI, false);
			fragmentList.add(fragmentWrapper);
		}
		{
			Resource fragmentResource = factory.createResource(URI.createURI("fragmentvirtualuri3"));
			resourceSet.getResources().add(fragmentResource);
			final String contributorName = "menu02";
			final String contributorURI = fragmentResource.getURI().toString();

			// the contributed elements
			MModelFragments fragmentsContainer = MFragmentFactory.INSTANCE.createModelFragments();
			fragmentResource.getContents().add((EObject) fragmentsContainer);

			ModelFragmentWrapper fragmentWrapper = createFragmentWrapper(fragmentsContainer, "children", "menu0",
					Arrays.asList(menu02), "index:20", contributorName, contributorURI, false);
			fragmentList.add(fragmentWrapper);
		}
		{
			Resource fragmentResource = factory.createResource(URI.createURI("fragmentvirtualuri4"));
			resourceSet.getResources().add(fragmentResource);
			final String contributorName = "menuitem000";
			final String contributorURI = fragmentResource.getURI().toString();

			// the contributed elements
			MModelFragments fragmentsContainer = MFragmentFactory.INSTANCE.createModelFragments();
			fragmentResource.getContents().add((EObject) fragmentsContainer);

			ModelFragmentWrapper fragmentWrapper = createFragmentWrapper(fragmentsContainer, "children", "menu00",
					Arrays.asList(menuitem000), "index:0", contributorName, contributorURI, false);
			fragmentList.add(fragmentWrapper);
		}
		Resource fragmentResource = factory.createResource(URI.createURI("fragmentvirtualuri5"));
		resourceSet.getResources().add(fragmentResource);
		final String contributorName = "menuitem001";
		final String contributorURI = fragmentResource.getURI().toString();

		// the contributed elements
		MModelFragments fragmentsContainer = MFragmentFactory.INSTANCE.createModelFragments();
		fragmentResource.getContents().add((EObject) fragmentsContainer);

		ModelFragmentWrapper fragmentWrapper = createFragmentWrapper(fragmentsContainer, "children", "menu00",
				Arrays.asList(menuitem001), "index:10", contributorName, contributorURI, false);
		fragmentList.add(fragmentWrapper);

		assembler.processFragmentWrappers(fragmentList);

		assertEquals(3, menu0.getChildren().size());
		assertEquals(menu00, menu0.getChildren().get(0));
		assertEquals(menu01, menu0.getChildren().get(1));
		assertEquals(menu02, menu0.getChildren().get(2));
		assertEquals(2, menu00.getChildren().size());
		assertEquals(menuitem000, menu00.getChildren().get(0));
		assertEquals(menuitem001, menu00.getChildren().get(1));
	}

	/**
	 * Test Model:
	 *
	 * <pre>
	 * Menu 0
	 * 	Menu 0.0 index:1
	 * 		MenuItem 0.0.0 index:0
	 * 		MenuItem 0.0.1 index:3
	 * 		MenuItem 0.0.2 index:20
	 * 	Menu 0.1 index:10
	 * 	Menu 0.2 index:20
	 * </pre>
	 *
	 * <p>
	 * Menu0 is contained in the application model
	 * <p>
	 * fragment1 contributes MenuItem 0.0.0, MenuItem 0.0.2, Menu 0.1 and
	 * imports Menu 0.0 from fragment2
	 * <p>
	 * fragment2 contributes Menu0.0, Menu 0.2 and MenuItem 0.0.1
	 */
	@Test
	public void testMenuAndItemsInFragmentsWithImports() {
		// initial application elements
		MWindow window = application.getChildren().get(0);
		MMenu menu0 = MMenuFactory.INSTANCE.createMenu();
		window.setMainMenu(menu0);
		menu0.setElementId("menu0");

		Set<ModelFragmentWrapper> fragmentList = new HashSet<>();
		// fragment preparations
		MMenu menu00 = MMenuFactory.INSTANCE.createMenu();
		menu00.setElementId("menu00");
		MMenu menu01 = MMenuFactory.INSTANCE.createMenu();
		menu01.setElementId("menu01");
		MMenu menu02 = MMenuFactory.INSTANCE.createMenu();
		menu02.setElementId("menu02");
		MHandledMenuItem menuitem000 = MMenuFactory.INSTANCE.createHandledMenuItem();
		menuitem000.setElementId("menuitem000");
		MHandledMenuItem menuitem001 = MMenuFactory.INSTANCE.createHandledMenuItem();
		menuitem001.setElementId("menuitem001");
		MHandledMenuItem menuitem002 = MMenuFactory.INSTANCE.createHandledMenuItem();
		menuitem002.setElementId("menuitem002");
		// the fragments
		{
			Resource fragmentResource = factory.createResource(URI.createURI("fragmentvirtualuri1"));
			resourceSet.getResources().add(fragmentResource);
			final String contributorName = "fragment1";
			final String contributorURI = fragmentResource.getURI().toString();

			// the contributed elements
			MModelFragments fragmentsContainer = MFragmentFactory.INSTANCE.createModelFragments();
			fragmentResource.getContents().add((EObject) fragmentsContainer);

			ModelFragmentWrapper fragmentWrapperMenuItem000 = createFragmentWrapper(fragmentsContainer, "children",
					"menu00", Arrays.asList(menuitem000), "index:0", contributorName, contributorURI, false);
			fragmentList.add(fragmentWrapperMenuItem000);
			ModelFragmentWrapper fragmentWrapperMenuItem002 = createFragmentWrapper(fragmentsContainer, "children",
					"menu00", Arrays.asList(menuitem002), "index:20", contributorName, contributorURI, false);
			fragmentList.add(fragmentWrapperMenuItem002);
			ModelFragmentWrapper fragmentWrapperMenu01 = createFragmentWrapper(fragmentsContainer, "children", "menu0",
					Arrays.asList(menu01), "index:10", contributorName, contributorURI, false);
			fragmentList.add(fragmentWrapperMenu01);

			fragmentsContainer.getImports().add(menu00);
		}
		Resource fragmentResource = factory.createResource(URI.createURI("fragmentvirtualuri2"));
		resourceSet.getResources().add(fragmentResource);
		final String contributorName = "fragment2";
		final String contributorURI = fragmentResource.getURI().toString();

		// the contributed elements
		MModelFragments fragmentsContainer = MFragmentFactory.INSTANCE.createModelFragments();
		fragmentResource.getContents().add((EObject) fragmentsContainer);

		ModelFragmentWrapper fragmentWrapperMenu00 = createFragmentWrapper(fragmentsContainer, "children", "menu0",
				Arrays.asList(menu00), "index:1", contributorName, contributorURI, false);
		fragmentList.add(fragmentWrapperMenu00);
		ModelFragmentWrapper fragmentWrapperMenu02 = createFragmentWrapper(fragmentsContainer, "children", "menu0",
				Arrays.asList(menu02), "index:20", contributorName, contributorURI, false);
		fragmentList.add(fragmentWrapperMenu02);
		ModelFragmentWrapper fragmentWrapperMenuItem001 = createFragmentWrapper(fragmentsContainer, "children",
				"menu00", Arrays.asList(menuitem001), "index:3", contributorName, contributorURI, false);
		fragmentList.add(fragmentWrapperMenuItem001);

		assembler.processFragmentWrappers(fragmentList);

		assertEquals(3, menu0.getChildren().size());
		assertEquals(menu00, menu0.getChildren().get(0));
		assertEquals(menu01, menu0.getChildren().get(1));
		assertEquals(menu02, menu0.getChildren().get(2));
		assertEquals(3, menu00.getChildren().size());
		assertEquals(menuitem000, menu00.getChildren().get(0));
		assertEquals(menuitem001, menu00.getChildren().get(1));
		assertEquals(menuitem002, menu00.getChildren().get(2));
	}

	/**
	 * Test Model:
	 *
	 * <pre>
	 * Menu
	 * 	Menu 1 index:10
	 * 		Menu 2 index:20
	 * 			Menu 3 index:0
	 * </pre>
	 *
	 * merge Menu 3 -> Menu 1 -> Menu 2
	 */
	@Test
	public void testMenuAndItemsInFragments2() {
		// initial application elements
		MWindow window = application.getChildren().get(0);
		MMenu menu = MMenuFactory.INSTANCE.createMenu();
		window.setMainMenu(menu);
		menu.setElementId("menu");

		Set<ModelFragmentWrapper> fragmentList = new HashSet<>();
		// fragment preparations
		MMenu menu1 = MMenuFactory.INSTANCE.createMenu();
		menu1.setElementId("menu1");
		MMenu menu2 = MMenuFactory.INSTANCE.createMenu();
		menu2.setElementId("menu2");
		MMenu menu3 = MMenuFactory.INSTANCE.createMenu();
		menu3.setElementId("menu3");
		// the fragments
		{
			Resource fragmentResource = factory.createResource(URI.createURI("fragmentvirtualuri3"));
			resourceSet.getResources().add(fragmentResource);
			final String contributorName = "menu3";
			final String contributorURI = fragmentResource.getURI().toString();

			// the contributed elements
			MModelFragments fragmentsContainer = MFragmentFactory.INSTANCE.createModelFragments();
			fragmentResource.getContents().add((EObject) fragmentsContainer);

			ModelFragmentWrapper fragmentWrapper = createFragmentWrapper(fragmentsContainer, "children", "menu2",
					Arrays.asList(menu3), "index:0", contributorName, contributorURI, false);
			fragmentList.add(fragmentWrapper);
		}
		{
			Resource fragmentResource = factory.createResource(URI.createURI("fragmentvirtualuri1"));
			resourceSet.getResources().add(fragmentResource);
			final String contributorName = "menu1";
			final String contributorURI = fragmentResource.getURI().toString();

			// the contributed elements
			MModelFragments fragmentsContainer = MFragmentFactory.INSTANCE.createModelFragments();
			fragmentResource.getContents().add((EObject) fragmentsContainer);

			ModelFragmentWrapper fragmentWrapper = createFragmentWrapper(fragmentsContainer, "children", "menu",
					Arrays.asList(menu1), "index:10", contributorName, contributorURI, false);
			fragmentList.add(fragmentWrapper);
		}
		Resource fragmentResource = factory.createResource(URI.createURI("fragmentvirtualuri2"));
		resourceSet.getResources().add(fragmentResource);
		final String contributorName = "menu2";
		final String contributorURI = fragmentResource.getURI().toString();

		// the contributed elements
		MModelFragments fragmentsContainer = MFragmentFactory.INSTANCE.createModelFragments();
		fragmentResource.getContents().add((EObject) fragmentsContainer);

		ModelFragmentWrapper fragmentWrapper = createFragmentWrapper(fragmentsContainer, "children", "menu1",
				Arrays.asList(menu2), "index:20", contributorName, contributorURI, false);
		fragmentList.add(fragmentWrapper);

		assembler.processFragmentWrappers(fragmentList);

		assertEquals(1, menu.getChildren().size());
		assertEquals(menu1, menu.getChildren().get(0));
		assertEquals(menu2, menu1.getChildren().get(0));
		assertEquals(menu3, menu2.getChildren().get(0));
	}

	/**
	 * Test Model:
	 *
	 * <pre>
	 * Menu
	 * 	Menu 1 index:10
	 * 		Menu 2 index:20
	 * 			Menu 3 index:0
	 * </pre>
	 *
	 * merge Menu 1 -> Menu 3 -> Menu 2
	 */
	@Test
	public void testMenuAndItemsInFragments3() {
		// initial application elements
		MWindow window = application.getChildren().get(0);
		MMenu menu = MMenuFactory.INSTANCE.createMenu();
		window.setMainMenu(menu);
		menu.setElementId("menu");

		Set<ModelFragmentWrapper> fragmentList = new HashSet<>();
		// fragment preparations
		MMenu menu1 = MMenuFactory.INSTANCE.createMenu();
		menu1.setElementId("menu1");
		MMenu menu2 = MMenuFactory.INSTANCE.createMenu();
		menu2.setElementId("menu2");
		MMenu menu3 = MMenuFactory.INSTANCE.createMenu();
		menu3.setElementId("menu3");
		// the fragments
		{
			Resource fragmentResource = factory.createResource(URI.createURI("fragmentvirtualuri1"));
			resourceSet.getResources().add(fragmentResource);
			final String contributorName = "menu1";
			final String contributorURI = fragmentResource.getURI().toString();

			// the contributed elements
			MModelFragments fragmentsContainer = MFragmentFactory.INSTANCE.createModelFragments();
			fragmentResource.getContents().add((EObject) fragmentsContainer);

			ModelFragmentWrapper fragmentWrapper = createFragmentWrapper(fragmentsContainer, "children", "menu",
					Arrays.asList(menu1), "index:10", contributorName, contributorURI, false);
			fragmentList.add(fragmentWrapper);
		}
		{
			Resource fragmentResource = factory.createResource(URI.createURI("fragmentvirtualuri3"));
			resourceSet.getResources().add(fragmentResource);
			final String contributorName = "menu3";
			final String contributorURI = fragmentResource.getURI().toString();

			// the contributed elements
			MModelFragments fragmentsContainer = MFragmentFactory.INSTANCE.createModelFragments();
			fragmentResource.getContents().add((EObject) fragmentsContainer);

			ModelFragmentWrapper fragmentWrapper = createFragmentWrapper(fragmentsContainer, "children", "menu2",
					Arrays.asList(menu3), "index:0", contributorName, contributorURI, false);
			fragmentList.add(fragmentWrapper);
		}
		Resource fragmentResource = factory.createResource(URI.createURI("fragmentvirtualuri2"));
		resourceSet.getResources().add(fragmentResource);
		final String contributorName = "menu2";
		final String contributorURI = fragmentResource.getURI().toString();

		// the contributed elements
		MModelFragments fragmentsContainer = MFragmentFactory.INSTANCE.createModelFragments();
		fragmentResource.getContents().add((EObject) fragmentsContainer);

		ModelFragmentWrapper fragmentWrapper = createFragmentWrapper(fragmentsContainer, "children", "menu1",
				Arrays.asList(menu2), "index:20", contributorName, contributorURI, false);
		fragmentList.add(fragmentWrapper);

		assembler.processFragmentWrappers(fragmentList);

		assertEquals(1, menu.getChildren().size());
		assertEquals(menu1, menu.getChildren().get(0));
		assertEquals(menu2, menu1.getChildren().get(0));
		assertEquals(menu3, menu2.getChildren().get(0));
	}

	@Test
	public void testModelFragmentComparatorWithCorrectlySortedList() {
		// initial application elements
		MHandledToolItem x = MMenuFactory.INSTANCE.createHandledToolItem();
		x.setElementId("x");
		toolBar.getChildren().add(x);
		MHandledToolItem y = MMenuFactory.INSTANCE.createHandledToolItem();
		y.setElementId("y");
		toolBar.getChildren().add(y);
		MHandledToolItem z = MMenuFactory.INSTANCE.createHandledToolItem();
		z.setElementId("z");
		toolBar.getChildren().add(z);

		// the fragments
		Resource fragmentResource = factory.createResource(URI.createURI("fragmentvirtualuri"));
		resourceSet.getResources().add(fragmentResource);
		final String contributorName = "testAfterFragmentReference3";
		final String contributorURI = fragmentResource.getURI().toString();

		// the contributed elements
		MModelFragments fragmentsContainerA = MFragmentFactory.INSTANCE.createModelFragments();
		fragmentResource.getContents().add((EObject) fragmentsContainerA);
		MHandledToolItem a = MMenuFactory.INSTANCE.createHandledToolItem();
		a.setElementId("a");
		MModelFragments fragmentsContainerB = MFragmentFactory.INSTANCE.createModelFragments();
		fragmentResource.getContents().add((EObject) fragmentsContainerB);
		MHandledToolItem b = MMenuFactory.INSTANCE.createHandledToolItem();
		b.setElementId("b");
		MModelFragments fragmentsContainerC = MFragmentFactory.INSTANCE.createModelFragments();
		fragmentResource.getContents().add((EObject) fragmentsContainerC);
		MHandledToolItem c = MMenuFactory.INSTANCE.createHandledToolItem();
		c.setElementId("c");

		ModelFragmentWrapper fragmentWrapper1 = createFragmentWrapper(fragmentsContainerA, "children", MAIN_TOOLBAR_ID,
				Arrays.asList(a), "after:b", contributorName, contributorURI, false);
		ModelFragmentWrapper fragmentWrapper2 = createFragmentWrapper(fragmentsContainerB, "children", MAIN_TOOLBAR_ID,
				Arrays.asList(b), "after:c", contributorName, contributorURI, false);
		ModelFragmentWrapper fragmentWrapper3 = createFragmentWrapper(fragmentsContainerC, "children", MAIN_TOOLBAR_ID,
				Arrays.asList(c), "after:y", contributorName, contributorURI, false);

		Set<ModelFragmentWrapper> fragmentList = new TreeSet<>(new ModelFragmentComparator(application));
		fragmentList.addAll(Arrays.asList(fragmentWrapper1, fragmentWrapper2, fragmentWrapper3));

		{
			Iterator<ModelFragmentWrapper> iterator = fragmentList.iterator();
			assertEquals(fragmentWrapper3, iterator.next());
			assertEquals(fragmentWrapper2, iterator.next());
			assertEquals(fragmentWrapper1, iterator.next());
		}

		// adding the elements to the second list will sort them again
		Set<ModelFragmentWrapper> fragmentList2 = new TreeSet<>(new ModelFragmentComparator(application));
		fragmentList2.addAll(fragmentList);

		Iterator<ModelFragmentWrapper> iterator = fragmentList2.iterator();
		assertEquals(fragmentWrapper3, iterator.next());
		assertEquals(fragmentWrapper2, iterator.next());
		assertEquals(fragmentWrapper1, iterator.next());

	}
}
