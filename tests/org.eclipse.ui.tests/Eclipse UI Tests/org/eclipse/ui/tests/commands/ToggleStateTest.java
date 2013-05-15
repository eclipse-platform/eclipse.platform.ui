/*******************************************************************************
 * Copyright (c) 2009, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.tests.commands;

import junit.framework.TestSuite;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.Parameterization;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.State;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.commands.IElementReference;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.handlers.RegistryToggleState;
import org.eclipse.ui.menus.UIElement;
import org.eclipse.ui.services.IServiceLocator;
import org.eclipse.ui.tests.harness.util.UITestCase;

/**
 * @since 3.5
 * @author Prakash G.R.
 *
 */
public class ToggleStateTest extends UITestCase {

	private ICommandService commandService;
	private IHandlerService handlerService;


	public static TestSuite suite() {
		TestSuite ts = new TestSuite();
		ts.addTest(new ToggleStateTest("testDefaultValues"));
		ts.addTest(new ToggleStateTest("testExceptionThrown"));
		ts.addTest(new ToggleStateTest("testMultipleContributions"));
		return ts;
	}

	public ToggleStateTest(String testName) {
		super(testName);
	}
	
	
	protected void doSetUp() throws Exception {
		super.doSetUp();
		commandService = (ICommandService) fWorkbench.getService(ICommandService.class);
		handlerService = (IHandlerService) fWorkbench.getService(IHandlerService.class);
	}
	
	public void testDefaultValues() throws Exception {
		
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
	
	public void testExceptionThrown() throws Exception {
		
		Command command3 = commandService.getCommand("org.eclipse.ui.tests.toggleStateCommand3");
		try {
			handlerService.executeCommand(command3.getId(), null);
			fail("Command3 doesn't have any state. An exception must be thrown from the handler, when trying to change that");
		} catch (Exception e) {
			if(!(e instanceof ExecutionException))
				throw e;
		}
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
	
	public void testMultipleContributions() throws Exception{
		
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
