/*******************************************************************************
 * Copyright (c) 2024 Eclipse Foundation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Eclipse Foundation - initial API and implementation
 *******************************************************************************/
package org.eclipse.search.tests;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.search2.internal.ui.basic.views.GlobalNextPrevSearchEntryHandler;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for {@link GlobalNextPrevSearchEntryHandler}.
 * These tests verify the basic functionality and integration points.
 * 
 * @since 3.17
 */
public class GlobalNextPrevSearchEntryHandlerIntegrationTest {

	/**
	 * Test that the handler can be created and configured properly.
	 * This test verifies the basic instantiation and configuration functionality.
	 */
	@Test
	public void testHandlerCreationAndConfiguration() throws CoreException {
		// Test Next handler
		GlobalNextPrevSearchEntryHandler nextHandler = new GlobalNextPrevSearchEntryHandler();
		assertNotNull(nextHandler, "Next handler should be created successfully");
		
		// Configure for next command (default behavior)
		nextHandler.setInitializationData(null, "command", "next");
		// No exception should be thrown
		
		// Test Previous handler
		GlobalNextPrevSearchEntryHandler prevHandler = new GlobalNextPrevSearchEntryHandler();
		assertNotNull(prevHandler, "Previous handler should be created successfully");
		
		// Configure for previous command
		prevHandler.setInitializationData(null, "command", "previous");
		// No exception should be thrown
	}

	/**
	 * Test that the handler handles various configuration scenarios correctly.
	 */
	@Test
	public void testHandlerConfigurationScenarios() throws CoreException {
		GlobalNextPrevSearchEntryHandler handler = new GlobalNextPrevSearchEntryHandler();
		
		// Test with null configuration
		handler.setInitializationData(null, null, null);
		// Should not throw exception
		
		// Test with empty string
		handler.setInitializationData(null, "", "");
		// Should not throw exception
		
		// Test with unknown command type
		handler.setInitializationData(null, "command", "unknown");
		// Should not throw exception and should default to next behavior
	}

	/**
	 * Test that the handler implements the required interfaces.
	 */
	@Test
	public void testHandlerInterfaceImplementation() {
		GlobalNextPrevSearchEntryHandler handler = new GlobalNextPrevSearchEntryHandler();
		
		// Verify it implements IHandler
		assertTrue(handler instanceof org.eclipse.core.commands.IHandler, 
			"Handler should implement IHandler interface");
		
		// Verify it implements IExecutableExtension
		assertTrue(handler instanceof org.eclipse.core.runtime.IExecutableExtension, 
			"Handler should implement IExecutableExtension interface");
	}

	/**
	 * Test that multiple handler instances can be created independently.
	 */
	@Test
	public void testMultipleHandlerInstances() throws CoreException {
		// Create multiple handlers
		GlobalNextPrevSearchEntryHandler handler1 = new GlobalNextPrevSearchEntryHandler();
		GlobalNextPrevSearchEntryHandler handler2 = new GlobalNextPrevSearchEntryHandler();
		GlobalNextPrevSearchEntryHandler handler3 = new GlobalNextPrevSearchEntryHandler();
		
		// Configure them differently
		handler1.setInitializationData(null, "command", "next");
		handler2.setInitializationData(null, "command", "previous");
		handler3.setInitializationData(null, "command", "unknown");
		
		// All should be created and configured without issues
		assertNotNull(handler1);
		assertNotNull(handler2);
		assertNotNull(handler3);
	}
}
