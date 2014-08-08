/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.tests.commands;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.Parameterization;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.State;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.commands.IElementReference;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.handlers.RadioState;
import org.eclipse.ui.menus.UIElement;
import org.eclipse.ui.services.IServiceLocator;
import org.eclipse.ui.tests.harness.util.UITestCase;

/**
 * @since 3.5
 * @author Prakash G.R.
 * 
 */
public class RadioStateTest extends UITestCase {

	private ICommandService commandService;
	private IHandlerService handlerService;

	public RadioStateTest(String testName) {
		super(testName);
	}

	protected void doSetUp() throws Exception {
		super.doSetUp();
		commandService = fWorkbench
				.getService(ICommandService.class);
		handlerService = fWorkbench
				.getService(IHandlerService.class);
	}

	public void testRadioValues() throws Exception {

		Command command1 = commandService
				.getCommand("org.eclipse.ui.tests.radioStateCommand1");

		// check the initial values
		assertState(command1, "value2");

		// execute with value1
		Parameterization radioParam = new Parameterization(command1
				.getParameter(RadioState.PARAMETER_ID), "value1");
		ParameterizedCommand parameterizedCommand = new ParameterizedCommand(
				command1, new Parameterization[] { radioParam });
		handlerService.executeCommand(parameterizedCommand, null);

		// check if updated
		assertState(command1, "value1");
		
		handlerService.executeCommand(parameterizedCommand, null);
		assertState(command1, "value1");
		
		Parameterization radioParam2 = new Parameterization(command1
				.getParameter(RadioState.PARAMETER_ID), "value2");
		ParameterizedCommand parameterizedCommand2 = new ParameterizedCommand(
				command1, new Parameterization[] { radioParam2 });
		handlerService.executeCommand(parameterizedCommand2, null);
		assertState(command1, "value2");
	}
	
	
	static class MyUIElement extends UIElement{

		private boolean checked;
		protected MyUIElement(IServiceLocator serviceLocator){
			super(serviceLocator);
		}

		public void setDisabledIcon(ImageDescriptor desc) {}
		public void setHoverIcon(ImageDescriptor desc) {}
		public void setIcon(ImageDescriptor desc) {}
		public void setText(String text) {}
		public void setTooltip(String text) {}

		public void setChecked(boolean checked) {
			this.checked = checked;
		}
		
		public boolean isChecked() {
			return checked;
		}

	}

	MyUIElement element1a;
	MyUIElement element2a;
	MyUIElement element3a;
	
	MyUIElement element1b;
	MyUIElement element2b;
	MyUIElement element3b;

	public void testMultipleContributions() throws Exception{
		
		Command command1 = commandService.getCommand("org.eclipse.ui.tests.radioStateCommand1");
		
		// group 1
		Parameterization radioParam1a = new Parameterization(command1.getParameter(RadioState.PARAMETER_ID), "value1");
		ParameterizedCommand parameterizedCommand1a = new ParameterizedCommand(command1, new Parameterization[] { radioParam1a });
		
		Parameterization radioParam2a = new Parameterization(command1.getParameter(RadioState.PARAMETER_ID), "value2");
		ParameterizedCommand parameterizedCommand2a = new ParameterizedCommand(command1, new Parameterization[] { radioParam2a });
		
		Parameterization radioParam3a = new Parameterization(command1.getParameter(RadioState.PARAMETER_ID), "value3");
		ParameterizedCommand parameterizedCommand3a = new ParameterizedCommand(command1, new Parameterization[] { radioParam3a });
		
		element1a = new MyUIElement(fWorkbench);
		element2a = new MyUIElement(fWorkbench);
		element3a = new MyUIElement(fWorkbench);

		IElementReference reference1a = commandService.registerElementForCommand(parameterizedCommand1a, element1a);
		IElementReference reference2a = commandService.registerElementForCommand(parameterizedCommand2a, element2a);
		IElementReference reference3a = commandService.registerElementForCommand(parameterizedCommand3a, element3a);
		
		// group 2
		Parameterization radioParam1b = new Parameterization(command1.getParameter(RadioState.PARAMETER_ID), "value1");
		ParameterizedCommand parameterizedCommand1b = new ParameterizedCommand(command1, new Parameterization[] { radioParam1b });
		
		Parameterization radioParam2b = new Parameterization(command1.getParameter(RadioState.PARAMETER_ID), "value2");
		ParameterizedCommand parameterizedCommand2b = new ParameterizedCommand(command1, new Parameterization[] { radioParam2b });
		
		Parameterization radioParam3b = new Parameterization(command1.getParameter(RadioState.PARAMETER_ID), "value3");
		ParameterizedCommand parameterizedCommand3b = new ParameterizedCommand(command1, new Parameterization[] { radioParam3b });
		
		element1b = new MyUIElement(fWorkbench);
		element2b = new MyUIElement(fWorkbench);
		element3b = new MyUIElement(fWorkbench);

		IElementReference reference1b = commandService.registerElementForCommand(parameterizedCommand1b, element1b);
		IElementReference reference2b = commandService.registerElementForCommand(parameterizedCommand2b, element2b);
		IElementReference reference3b = commandService.registerElementForCommand(parameterizedCommand3b, element3b);
		
		try{

			// first set the state to value1
			handlerService.executeCommand(parameterizedCommand1a, null);
			commandService.refreshElements(command1.getId(), null);
			
			assertChecked(element1a);
			assertBothGroupsUpdated();
			
			// then set the state to value2
			handlerService.executeCommand(parameterizedCommand2a, null);
			
			// only value 2 is checked
			assertChecked(element2a);
			assertBothGroupsUpdated();
			
			
		}finally {
			commandService.unregisterElement(reference1a);
			commandService.unregisterElement(reference2a);
			commandService.unregisterElement(reference3a);
			commandService.unregisterElement(reference1b);
			commandService.unregisterElement(reference2b);
			commandService.unregisterElement(reference3b);
		}
		
	}

	private void assertChecked(MyUIElement checkedElement) {
		
		// only the element passed is checked and other two should be unchecked
		assertTrue(checkedElement == element1a? element1a.isChecked():!element1a.isChecked());
		assertTrue(checkedElement == element2a? element2a.isChecked():!element2a.isChecked());
		assertTrue(checkedElement == element3a? element3a.isChecked():!element3a.isChecked());
	}

	private void assertBothGroupsUpdated() {
		assertEquals(element1a.isChecked(), element1b.isChecked());
		assertEquals(element2a.isChecked(), element2b.isChecked());
		assertEquals(element3a.isChecked(), element3b.isChecked());
	}

	private void assertState(Command command, String expectedValue) {
		State state = command.getState(RadioState.STATE_ID);
		Object value = state.getValue();
		assertTrue(value instanceof String);
		assertEquals(expectedValue, value);
	}

}
