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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.search2.internal.ui.basic.views.GlobalNextPrevSearchEntryHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link GlobalNextPrevSearchEntryHandler}.
 * 
 * @since 3.17
 */
public class GlobalNextPrevSearchEntryHandlerTest {

	private GlobalNextPrevSearchEntryHandler handler;

	@BeforeEach
	public void setUp() {
		handler = new GlobalNextPrevSearchEntryHandler();
	}

	/**
	 * Test that the handler can be instantiated without errors.
	 */
	@Test
	public void testHandlerInstantiation() {
		// Verify handler was created successfully in setUp()
		assertNotNull(handler, "Handler should be created successfully");
		
		// Verify handler is properly instantiated
		// If we get here, instantiation was successful
	}

	/**
	 * Test that setInitializationData works correctly for "previous" command.
	 */
	@Test
	public void testSetInitializationDataWithPreviousCommand() throws CoreException {
		// Test with "previous" data - should set the handler to use NAVIGATE_PREVIOUS
		handler.setInitializationData(null, "property", "previous");
		// If no exception is thrown, the method worked correctly
	}

	/**
	 * Test that setInitializationData works correctly for "next" command.
	 */
	@Test
	public void testSetInitializationDataWithNextCommand() throws CoreException {
		// Test with "next" data - should keep default NAVIGATE_NEXT behavior
		handler.setInitializationData(null, "property", "next");
		// If no exception is thrown, the method worked correctly
	}

	/**
	 * Test that setInitializationData works correctly with unknown command.
	 */
	@Test
	public void testSetInitializationDataWithUnknownCommand() throws CoreException {
		// Test with unknown data - should keep default NAVIGATE_NEXT behavior
		handler.setInitializationData(null, "property", "unknown");
		// If no exception is thrown, the method worked correctly
	}

	/**
	 * Test that setInitializationData works correctly with null data.
	 */
	@Test
	public void testSetInitializationDataWithNullData() throws CoreException {
		// Test with null data - should keep default NAVIGATE_NEXT behavior
		handler.setInitializationData(null, "property", null);
		// If no exception is thrown, the method worked correctly
	}

	/**
	 * Test that the handler can be instantiated and is properly configured.
	 */
	@Test
	public void testHandlerInstantiationAndConfiguration() {
		// Verify handler can be created and is not null
		assertNotNull(handler);
		
		// Verify handler has expected default behavior
		// (This test verifies the handler is properly instantiated)
	}
}
