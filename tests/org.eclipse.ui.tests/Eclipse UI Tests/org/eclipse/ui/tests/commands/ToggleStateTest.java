/*******************************************************************************
 * Copyright (c) 2009, 2019 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Paul Pazderski - Bug 546546: migrate to JUnit4 test
 ******************************************************************************/

package org.eclipse.ui.tests.commands;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.Parameterization;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.State;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.commands.IElementReference;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.handlers.RegistryToggleState;
import org.eclipse.ui.menus.UIElement;
import org.eclipse.ui.services.IServiceLocator;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.runners.MethodSorters;

/**
 * @since 3.5
 * @author Prakash G.R.
 */
@RunWith(JUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ToggleStateTest {

	private final IWorkbench fWorkbench = PlatformUI.getWorkbench();
	private ICommandService commandService;
	private IHandlerService handlerService;

	@Before
	public void doSetUp() throws Exception {
		commandService = fWorkbench.getService(ICommandService.class);
		handlerService = fWorkbench.getService(IHandlerService.class);
	}

	// Note: this and all other tests are numbered because they must run in a
	// specific order.
	// See https://bugs.eclipse.org/bugs/show_bug.cgi?id=369660
	// The old junit3 implementation used a custom suite(). Because junit4 provides
	// less options on test run order the tests are now numbered and run in method
	// name order.
	@Test
	public void test01DefaultValues() throws Exception {

		Command command1 = commandService.getCommand("org.eclipse.ui.tests.toggleStateCommand1");
		Command command2 = commandService.getCommand("org.eclipse.ui.tests.toggleStateCommand2");

		// check the initial values
		assertState(command1, true);
		assertState(command2, false);

		// execute and check the values have changed or not
		handlerService.executeCommand(command1.getId(), null);
		handlerService.executeCommand(command2.getId(), null);

		assertState(command1, false);
		assertState(command2, true);

	}

	@Test
	public void test02ExceptionThrown() throws Exception {

		Command command3 = commandService.getCommand("org.eclipse.ui.tests.toggleStateCommand3");
		try {
			handlerService.executeCommand(command3.getId(), null);
			fail("Command3 doesn't have any state. An exception must be thrown from the handler, when trying to change that");
		} catch (Exception e) {
			if(!(e instanceof ExecutionException)) {
				throw e;
			}
		}
	}

	static class MyUIElement extends UIElement{

		private boolean checked;
		protected MyUIElement(IServiceLocator serviceLocator){
			super(serviceLocator);
		}

		@Override
		public void setDisabledIcon(ImageDescriptor desc) {}
		@Override
		public void setHoverIcon(ImageDescriptor desc) {}
		@Override
		public void setIcon(ImageDescriptor desc) {}
		@Override
		public void setText(String text) {}
		@Override
		public void setTooltip(String text) {}

		@Override
		public void setChecked(boolean checked) {
			this.checked = checked;
		}

		public boolean isChecked() {
			return checked;
		}

	}

	@Test
	public void test03MultipleContributions() throws Exception {

		Command command1 = commandService.getCommand("org.eclipse.ui.tests.toggleStateCommand1");
		ParameterizedCommand parameterizedCommand = new ParameterizedCommand(command1, new Parameterization[0]);

		MyUIElement element1 = new MyUIElement(fWorkbench);
		MyUIElement element2 = new MyUIElement(fWorkbench);

		IElementReference reference1 = commandService.registerElementForCommand(parameterizedCommand, element1);
		IElementReference reference2 = commandService.registerElementForCommand(parameterizedCommand, element2);

		try{

			commandService.refreshElements(command1.getId(), null);
			assertEquals(element1.isChecked(), element2.isChecked());

			Boolean oldValue = (Boolean) handlerService.executeCommand(command1.getId(), null);
			//value should have changed
			assertEquals(!oldValue.booleanValue(), element1.isChecked());
			//and changed in both places
			assertEquals(element1.isChecked(), element2.isChecked());

		}finally {
			commandService.unregisterElement(reference1);
			commandService.unregisterElement(reference2);
		}

	}

	private void assertState(Command command1, boolean expectedValue) {
		State state = command1.getState(RegistryToggleState.STATE_ID);
		Object value = state.getValue();
		assertTrue(value instanceof Boolean);
		assertEquals(expectedValue, ((Boolean)value).booleanValue());
	}

}
