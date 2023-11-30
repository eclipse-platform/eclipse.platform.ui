/*******************************************************************************
 * Copyright (c) 2011, 2017 EclipseSource Muenchen GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Jonas Helming- initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.tools.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.eclipse.e4.internal.tools.wizards.model.FragmentExtractHelper;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.MApplicationFactory;
import org.eclipse.e4.ui.model.application.commands.MBindingContext;
import org.eclipse.e4.ui.model.application.commands.MCategory;
import org.eclipse.e4.ui.model.application.commands.MCommand;
import org.eclipse.e4.ui.model.application.commands.MCommandsFactory;
import org.eclipse.e4.ui.model.application.commands.MHandler;
import org.eclipse.e4.ui.model.application.ui.advanced.MAdvancedFactory;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspectiveStack;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.basic.MBasicFactory;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.menu.MHandledItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuFactory;
import org.eclipse.e4.ui.model.fragment.MModelFragment;
import org.eclipse.e4.ui.model.fragment.MModelFragments;
import org.eclipse.e4.ui.model.fragment.MStringModelFragment;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Jonas
 */
public class FragmentExtractHelperTest {

	private static final String WINDOWID = "someID"; //$NON-NLS-1$
	private static final String PART1ID = "part1id"; //$NON-NLS-1$
	private static final String PART2ID = "part2id"; //$NON-NLS-1$
	private static final String HANDLER1ID = "handler1id"; //$NON-NLS-1$
	private static final String COMMANDID = "commandid"; //$NON-NLS-1$
	private static final String HANDLER2ID = "handler2id"; //$NON-NLS-1$
	private static final String APPLICATIONID = "applicationid"; //$NON-NLS-1$
	private MPart part1;
	private MWindow window;
	private MPart part2;
	private MHandler handler1;
	private MApplication application;
	private MHandler handler2;
	private MPlaceholder placeholder;
	private MPart referencedPart;

	@Before
	public void setup() {
		application = MApplicationFactory.INSTANCE.createApplication();
		application.setElementId(APPLICATIONID);
	}

	@Test
	public void testExtractSinglePart() {
		createWindowWithPart();
		final ArrayList<MApplicationElement> elementsToExtract = new ArrayList<>();
		elementsToExtract.add(part1);
		final MModelFragments initialModel = FragmentExtractHelper.createInitialModel(elementsToExtract);
		assertEquals(1, initialModel.getFragments().size());
		assertEquals(0, initialModel.getImports().size());
		final MModelFragment modelFragment = initialModel.getFragments().get(0);
		assertEquals(1, modelFragment.getElements().size());
		assertExtractedElement(part1, modelFragment.getElements().get(0));
	}

	private void assertExtractedElement(MApplicationElement originalElement, MApplicationElement extractedElement) {
		final EObject extracted = (EObject) extractedElement;
		final EObject original = (EObject) originalElement;

		EcoreUtil.equals(extracted, original);

		final MStringModelFragment fragment = (MStringModelFragment) extracted.eContainer();
		final MApplicationElement originalContainer = (MApplicationElement) original.eContainer();
		final EStructuralFeature originalContainingFeature = original.eContainingFeature();

		assertEquals(originalContainingFeature.getName(), fragment.getFeaturename());
		assertEquals(originalContainer.getElementId(), fragment.getParentElementId());

	}

	@Test
	/**
	 * Test for BR 405159, ignored until it is fixed
	 */
	public void testExtractPlaceHolder() {
		createWindowWithPlaceHolder();
		final ArrayList<MApplicationElement> elementsToExtract = new ArrayList<>();
		elementsToExtract.add(placeholder);
		final MModelFragments initialModel = FragmentExtractHelper.createInitialModel(elementsToExtract);
		assertEquals(1, initialModel.getFragments().size());
		assertEquals(1, initialModel.getImports().size());
		final MModelFragment modelFragment = initialModel.getFragments().get(0);
		assertEquals(1, modelFragment.getElements().size());
		assertTrue(initialModel.getImports().contains(((MPlaceholder) modelFragment.getElements().get(0)).getRef()));
		assertExtractedElement(placeholder, modelFragment.getElements().get(0));
		assertExtractedImport(referencedPart, initialModel.getImports().get(0));
	}

	private void createWindowWithPlaceHolder() {
		createWindow();
		final MPerspectiveStack perspectiveStack = MAdvancedFactory.INSTANCE.createPerspectiveStack();
		window.getChildren().add(perspectiveStack);
		final MPerspective perspective = MAdvancedFactory.INSTANCE.createPerspective();
		perspectiveStack.getChildren().add(perspective);
		placeholder = MAdvancedFactory.INSTANCE.createPlaceholder();
		perspective.getChildren().add(placeholder);
		referencedPart = MBasicFactory.INSTANCE.createPart();
		window.getSharedElements().add(referencedPart);
		placeholder.setRef(referencedPart);
	}

	@Test
	/**
	 * Test for BR 446206, ignored until it is fixed
	 */
	public void testExtractCommandWithCategory() {
		final MCommand command = createCommand();
		final MCategory category = createCategoryForCommand(command);
		final ArrayList<MApplicationElement> elementsToExtract = new ArrayList<>();
		elementsToExtract.add(command);
		final MModelFragments initialModel = FragmentExtractHelper.createInitialModel(elementsToExtract);
		assertEquals(1, initialModel.getFragments().size());
		assertEquals(1, initialModel.getImports().size());
		final MModelFragment modelFragment = initialModel.getFragments().get(0);
		assertEquals(1, modelFragment.getElements().size());
		final MCategory importedCategory = (MCategory) initialModel.getImports().get(0);
		final MCommand extractedCommand = (MCommand) modelFragment.getElements().get(0);
		assertSame(importedCategory, extractedCommand.getCategory());
		assertExtractedElement(command, modelFragment.getElements().get(0));
		assertExtractedImport(category, initialModel.getImports().get(0));
	}

	private MCategory createCategoryForCommand(MCommand command) {
		final MCategory category = MCommandsFactory.INSTANCE.createCategory();
		application.getCategories().add(category);
		command.setCategory(category);
		return category;

	}

	private void createWindowWithPart() {
		createWindow();
		part1 = MBasicFactory.INSTANCE.createPart();
		part1.setElementId(PART1ID);
		window.getChildren().add(part1);
	}

	private void createWindow() {
		window = MBasicFactory.INSTANCE.createWindow();
		window.setElementId(WINDOWID);
		application.getChildren().add(window);
	}

	@Test

	/**
	 * Test for BR 470998, ignored until it is fixed
	 */
	public void testExtractTwoPartsSameLocation() {
		createWindowWithPart();
		addSecondPart();
		final ArrayList<MApplicationElement> elementsToExtract = new ArrayList<>();
		elementsToExtract.add(part1);
		elementsToExtract.add(part2);
		final MModelFragments initialModel = FragmentExtractHelper.createInitialModel(elementsToExtract);
		assertEquals(1, initialModel.getFragments().size());
		assertEquals(0, initialModel.getImports().size());
		final MModelFragment modelFragment = initialModel.getFragments().get(0);
		assertEquals(2, modelFragment.getElements().size());
		assertExtractedElement(part1, modelFragment.getElements().get(0));
		assertExtractedElement(part2, modelFragment.getElements().get(1));
	}

	@Test
	public void testExtractHandlerWithoutCommand() {
		createHandler();
		final ArrayList<MApplicationElement> elementsToExtract = new ArrayList<>();
		elementsToExtract.add(handler1);
		final MModelFragments initialModel = FragmentExtractHelper.createInitialModel(elementsToExtract);
		assertEquals(1, initialModel.getFragments().size());
		assertEquals(0, initialModel.getImports().size());
		final MModelFragment modelFragment = initialModel.getFragments().get(0);
		assertEquals(1, modelFragment.getElements().size());
		assertExtractedElement(handler1, modelFragment.getElements().get(0));
	}

	private void createHandler() {
		handler1 = MCommandsFactory.INSTANCE.createHandler();
		handler1.setElementId(HANDLER1ID);
		application.getHandlers().add(handler1);
	}

	@Test
	public void testExtractHandlerWithCommand() {
		createHandler();
		final MCommand command = createCommandLinkedWithHandler(handler1);
		final ArrayList<MApplicationElement> elementsToExtract = new ArrayList<>();
		elementsToExtract.add(handler1);
		final MModelFragments initialModel = FragmentExtractHelper.createInitialModel(elementsToExtract);
		assertEquals(1, initialModel.getFragments().size());
		assertEquals(1, initialModel.getImports().size());
		final MModelFragment modelFragment = initialModel.getFragments().get(0);
		assertEquals(1, modelFragment.getElements().size());
		final MCommand importedCommand = (MCommand) initialModel.getImports().get(0);
		final MHandler extractedHandler = (MHandler) modelFragment.getElements().get(0);
		assertSame(importedCommand, extractedHandler.getCommand());
		assertExtractedElement(handler1, modelFragment.getElements().get(0));
		assertExtractedImport(command, initialModel.getImports().get(0));
	}

	private void assertExtractedImport(MApplicationElement original, MApplicationElement importedElement) {
		EcoreUtil.equals((EObject) original, (EObject) importedElement);

	}

	private MCommand createCommandLinkedWithHandler(MHandler handler) {
		final MCommand command = createCommand();
		command.setElementId(command.getElementId() + handler.getElementId());
		handler.setCommand(command);
		return command;

	}

	private MCommand createCommand() {
		final MCommand command = MCommandsFactory.INSTANCE.createCommand();
		command.setElementId(COMMANDID);
		application.getCommands().add(command);
		return command;
	}

	@Test

	/**
	 * Test for BR 470998, ignored until it is fixed
	 */
	public void testExtractTwoHandlerWithDifferentCommands() {
		createHandler();
		createHandler2();
		final MCommand command = createCommandLinkedWithHandler(handler1);
		final MCommand command2 = createCommandLinkedWithHandler(handler2);
		final ArrayList<MApplicationElement> elementsToExtract = new ArrayList<>();
		elementsToExtract.add(handler1);
		elementsToExtract.add(handler2);
		final MModelFragments initialModel = FragmentExtractHelper.createInitialModel(elementsToExtract);
		assertEquals(1, initialModel.getFragments().size());
		assertEquals(2, initialModel.getImports().size());
		final MModelFragment modelFragment = initialModel.getFragments().get(0);
		assertEquals(2, modelFragment.getElements().size());

		assertTrue(initialModel.getImports().contains(((MHandler) modelFragment.getElements().get(0)).getCommand()));
		assertTrue(initialModel.getImports().contains(((MHandler) modelFragment.getElements().get(1)).getCommand()));

		assertExtractedElement(handler1, modelFragment.getElements().get(0));
		assertExtractedElement(handler2, modelFragment.getElements().get(1));
		assertExtractedImport(command, initialModel.getImports().get(0));
		assertExtractedImport(command2, initialModel.getImports().get(1));
	}

	private void createHandler2() {
		handler2 = MCommandsFactory.INSTANCE.createHandler();
		handler2.setElementId(HANDLER2ID);
		application.getHandlers().add(handler2);

	}

	@Test
	/**
	 * Test for BR 470999
	 */
	public void testExtractResolveCommandsWithTwoHandlerWithSameCommand() {
		createHandler();
		createHandler2();
		final MCommand command = createCommandLinkedWithHandler(handler1);
		handler2.setCommand(command);
		final HashMap<MApplicationElement, MApplicationElement> importCommands = new HashMap<>();

		FragmentExtractHelper.resolveImports(handler1, importCommands);
		FragmentExtractHelper.resolveImports(handler2, importCommands);

		assertEquals(1, importCommands.size());
		final MApplicationElement resolvedCommand = importCommands.values().iterator().next();
		assertSame(resolvedCommand, handler1.getCommand());
		assertSame(resolvedCommand, handler2.getCommand());
	}

	@Test
	/**
	 * Test for BR 470999
	 */
	public void testExtractResolveCommandsWithTwoHandledItemsWithSameCommand() {
		final MHandledItem handledItem1 = createHandledItem();
		final MHandledItem handledItem2 = createHandledItem();
		final MCommand command = createCommand();
		handledItem1.setCommand(command);
		handledItem2.setCommand(command);
		final HashMap<MApplicationElement, MApplicationElement> importCommands = new HashMap<>();

		FragmentExtractHelper.resolveImports(handledItem1, importCommands);
		FragmentExtractHelper.resolveImports(handledItem2, importCommands);

		assertEquals(1, importCommands.size());
		final MApplicationElement resolvedCommand = importCommands.values().iterator().next();
		assertSame(resolvedCommand, handledItem1.getCommand());
		assertSame(resolvedCommand, handledItem2.getCommand());
	}

	@Test
	/**
	 * Test for referenced BindingContexts BR 474037
	 */
	public void testExtractTwoPartsWithTwoBindingContexts() {
		final MBindingContext bindingContext1 = MCommandsFactory.INSTANCE.createBindingContext();
		final MBindingContext bindingContext2 = MCommandsFactory.INSTANCE.createBindingContext();

		final MPart part = MBasicFactory.INSTANCE.createPart();
		part.getBindingContexts().add(bindingContext1);
		part.getBindingContexts().add(bindingContext2);

		final MPart part2 = MBasicFactory.INSTANCE.createPart();
		part2.getBindingContexts().add(bindingContext1);


		final HashMap<MApplicationElement, MApplicationElement> importCommands = new LinkedHashMap<>();

		FragmentExtractHelper.resolveImports(part, importCommands);
		FragmentExtractHelper.resolveImports(part2, importCommands);

		assertEquals(2, importCommands.size());
		final MApplicationElement resolvedBindingContext = importCommands.values().iterator().next();
		final MApplicationElement resolvedBindingContext2 = importCommands.values().iterator().next();
		assertTrue(part.getBindingContexts().contains(resolvedBindingContext));
		assertTrue(part.getBindingContexts().contains(resolvedBindingContext2));
		assertTrue(part2.getBindingContexts().contains(resolvedBindingContext));
		assertEquals(2, part.getBindingContexts().size());
		assertEquals(1, part2.getBindingContexts().size());
	}

	private MHandledItem createHandledItem() {
		final MHandledItem handledItem = MMenuFactory.INSTANCE.createHandledMenuItem();
		handledItem.setElementId(HANDLER1ID);
		application.getSnippets().add(handledItem);
		return handledItem;
	}

	@Test
	/**
	 * Test for BR 470998, ignored until it is fixed
	 */
	public void testExtractTwoHandlerWithSameCommand() {
		createHandler();
		createHandler2();
		final MCommand command = createCommandLinkedWithHandler(handler1);
		handler2.setCommand(command);
		final ArrayList<MApplicationElement> elementsToExtract = new ArrayList<>();
		elementsToExtract.add(handler1);
		elementsToExtract.add(handler2);
		final MModelFragments initialModel = FragmentExtractHelper.createInitialModel(elementsToExtract);
		assertEquals(1, initialModel.getFragments().size());
		assertEquals(1, initialModel.getImports().size());
		final MModelFragment modelFragment = initialModel.getFragments().get(0);
		assertEquals(2, modelFragment.getElements().size());

		final MCommand importedCommand = (MCommand) initialModel.getImports().get(0);
		final MHandler importedHandler1 = (MHandler) modelFragment.getElements().get(0);
		final MHandler importedHandler2 = (MHandler) modelFragment.getElements().get(1);

		assertSame(importedCommand, importedHandler1.getCommand());
		assertSame(importedCommand, importedHandler2.getCommand());

		assertExtractedElement(handler1, importedHandler1);
		assertExtractedElement(handler2, importedHandler2);
		assertExtractedImport(command, initialModel.getImports().get(0));
	}

	private void addSecondPart() {
		part2 = MBasicFactory.INSTANCE.createPart();
		part2.setElementId(PART2ID);
		window.getChildren().add(part2);

	}

}
