/*******************************************************************************
 * Copyright (c) 2015 EclipseSource Muenchen GmbH and others.
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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.eclipse.e4.internal.tools.wizards.model.FragmentMergeHelper;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.commands.MCommand;
import org.eclipse.e4.ui.model.application.commands.MCommandsFactory;
import org.eclipse.e4.ui.model.application.commands.MHandler;
import org.eclipse.e4.ui.model.fragment.MFragmentFactory;
import org.eclipse.e4.ui.model.fragment.MModelFragments;
import org.eclipse.e4.ui.model.fragment.MStringModelFragment;
import org.junit.Before;
import org.junit.Test;

public class FragmentMergeHelperTest {

	private static final String TESTID = "commandID"; //$NON-NLS-1$
	private static final String OTHERTESTID = "anotherID"; //$NON-NLS-1$
	private MModelFragments sourceFragments;
	private MModelFragments targetFragments;

	@Before
	public void setUpFragments() {
		sourceFragments = MFragmentFactory.INSTANCE.createModelFragments();
		targetFragments = MFragmentFactory.INSTANCE.createModelFragments();
	}

	@Test
	public void testMergeEmptyFragments() {
		FragmentMergeHelper.merge(sourceFragments, targetFragments);
		assertTrue(sourceFragments.getFragments().isEmpty());
		assertTrue(targetFragments.getFragments().isEmpty());
	}

	@Test
	public void testMergeHandlerWithoutCommandEmptyTarget() {
		final MHandler existingHandler = addHandler(sourceFragments);

		FragmentMergeHelper.merge(sourceFragments, targetFragments);

		assertTrue(sourceFragments.getFragments().isEmpty());
		final MApplicationElement handler = targetFragments.getFragments().get(0).getElements().get(0);
		assertSame(existingHandler, handler);
	}

	private MHandler addHandler(MModelFragments sourceFragments) {
		final MStringModelFragment modelFragment = MFragmentFactory.INSTANCE.createStringModelFragment();
		final MHandler testHandler = MCommandsFactory.INSTANCE.createHandler();
		modelFragment.getElements().add(testHandler);
		sourceFragments.getFragments().add(modelFragment);
		return testHandler;
	}

	private MCommand addCommandToImports(MModelFragments sourceFragments) {
		final MCommand command = createCommand();
		sourceFragments.getImports().add(command);
		return command;
	}

	private MCommand createCommand() {
		final MCommand command = MCommandsFactory.INSTANCE.createCommand();
		command.setElementId(TESTID);
		return command;
	}

	@Test
	public void testMergeHandlerWithCommandEmptyTarget() {
		final MHandler existingHandler = addHandler(sourceFragments);
		final MCommand existingCommand = addCommandToImports(sourceFragments);

		existingHandler.setCommand(existingCommand);

		FragmentMergeHelper.merge(sourceFragments, targetFragments);
		assertTrue(sourceFragments.getFragments().isEmpty());

		final MHandler handler = (MHandler) targetFragments.getFragments().get(0).getElements().get(0);
		assertSame(existingHandler, handler);
		assertTrue(targetFragments.getImports().contains(existingCommand));
		assertSame(existingCommand, handler.getCommand());
	}

	@Test
	public void testMergeHandlerWithCommandExistingImport() {
		final MHandler existingHandler = addHandler(sourceFragments);
		final MCommand existingCommand = addCommandToImports(sourceFragments);

		existingHandler.setCommand(existingCommand);

		final MCommand existingImport = addCommandToImports(targetFragments);

		FragmentMergeHelper.merge(sourceFragments, targetFragments);
		assertTrue(sourceFragments.getFragments().isEmpty());

		final MHandler handler = (MHandler) targetFragments.getFragments().get(0).getElements().get(0);
		assertSame(existingHandler, handler);
		assertFalse(targetFragments.getImports().contains(existingCommand));
		assertTrue(targetFragments.getImports().contains(existingImport));
		assertSame(existingImport, handler.getCommand());
	}

	@Test
	public void testMergeCommandWithExistingImport() {
		final MCommand existingCommand = addCommand(sourceFragments);

		final MHandler existingHandler = addHandler(targetFragments);
		final MCommand existingImport = addCommandToImports(targetFragments);
		existingHandler.setCommand(existingImport);


		FragmentMergeHelper.merge(sourceFragments, targetFragments);
		assertTrue(sourceFragments.getFragments().isEmpty());

		final MHandler handler = (MHandler) targetFragments.getFragments().get(0).getElements().get(0);
		final MCommand command = (MCommand) targetFragments.getFragments().get(1).getElements().get(0);
		assertSame(existingCommand, command);
		assertSame(command, handler.getCommand());
		assertSame(existingHandler, handler);
		assertTrue(targetFragments.getImports().isEmpty());
	}

	private MCommand addCommand(MModelFragments fragments) {
		final MStringModelFragment modelFragment = MFragmentFactory.INSTANCE.createStringModelFragment();
		final MCommand command = createCommand();
		modelFragment.getElements().add(command);
		sourceFragments.getFragments().add(modelFragment);
		return command;
	}

	@Test
	public void testhaveSameIDWithFirstNullID(){
		final MApplicationElement firstElement = MCommandsFactory.INSTANCE.createCommand();
		final MApplicationElement secondElement = MCommandsFactory.INSTANCE.createCommand();

		firstElement.setElementId(null);
		secondElement.setElementId(TESTID);
		assertFalse(FragmentMergeHelper.haveSameID(firstElement, secondElement));

	}

	@Test
	public void testhaveSameIDWithSecondNullID(){
		final MApplicationElement firstElement = MCommandsFactory.INSTANCE.createCommand();
		final MApplicationElement secondElement = MCommandsFactory.INSTANCE.createCommand();

		firstElement.setElementId(TESTID);
		secondElement.setElementId(null);
		assertFalse(FragmentMergeHelper.haveSameID(firstElement, secondElement));
	}

	@Test
	public void testhaveSameIDWithBothNullID(){
		final MApplicationElement firstElement = MCommandsFactory.INSTANCE.createCommand();
		final MApplicationElement secondElement = MCommandsFactory.INSTANCE.createCommand();

		firstElement.setElementId(null);
		secondElement.setElementId(null);
		assertFalse(FragmentMergeHelper.haveSameID(firstElement, secondElement));
	}

	@Test
	public void testhaveSameIDWithFirstEmptyID(){
		final MApplicationElement firstElement = MCommandsFactory.INSTANCE.createCommand();
		final MApplicationElement secondElement = MCommandsFactory.INSTANCE.createCommand();

		firstElement.setElementId(""); //$NON-NLS-1$
		secondElement.setElementId(TESTID);
		assertFalse(FragmentMergeHelper.haveSameID(firstElement, secondElement));

	}

	@Test
	public void testhaveSameIDWithSecondEmptyID(){
		final MApplicationElement firstElement = MCommandsFactory.INSTANCE.createCommand();
		final MApplicationElement secondElement = MCommandsFactory.INSTANCE.createCommand();

		firstElement.setElementId(TESTID);
		secondElement.setElementId(""); //$NON-NLS-1$
		assertFalse(FragmentMergeHelper.haveSameID(firstElement, secondElement));
	}

	@Test
	public void testhaveSameIDWithBothEmptyID(){
		final MApplicationElement firstElement = MCommandsFactory.INSTANCE.createCommand();
		final MApplicationElement secondElement = MCommandsFactory.INSTANCE.createCommand();

		firstElement.setElementId(""); //$NON-NLS-1$
		secondElement.setElementId(""); //$NON-NLS-1$
		assertFalse(FragmentMergeHelper.haveSameID(firstElement, secondElement));
	}

	@Test
	public void testhaveSameIDWithEqualID(){
		final MApplicationElement firstElement = MCommandsFactory.INSTANCE.createCommand();
		final MApplicationElement secondElement = MCommandsFactory.INSTANCE.createCommand();

		firstElement.setElementId(TESTID);
		secondElement.setElementId(TESTID);
		assertTrue(FragmentMergeHelper.haveSameID(firstElement, secondElement));
	}

	@Test
	public void testhaveSameIDWithNonEqualID(){
		final MApplicationElement firstElement = MCommandsFactory.INSTANCE.createCommand();
		final MApplicationElement secondElement = MCommandsFactory.INSTANCE.createCommand();

		firstElement.setElementId(TESTID);
		secondElement.setElementId(OTHERTESTID);
		assertFalse(FragmentMergeHelper.haveSameID(firstElement, secondElement));
	}





}
