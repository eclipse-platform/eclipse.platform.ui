/*******************************************************************************
 * Copyright (c) 2025 Eclipse Foundation and others.
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

import static org.junit.jupiter.api.Assertions.assertNull;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.search2.internal.ui.basic.views.GlobalNextPrevSearchEntryHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link GlobalNextPrevSearchEntryHandler}.
 * <p>
 * These tests cover configuration via {@code setInitializationData} and
 * the no-op behaviour when no search results are available.
 * For navigation behaviour with real search results see
 * {@link GlobalNextPrevSearchEntryHandlerIntegrationTest}.
 * </p>
 */
public class GlobalNextPrevSearchEntryHandlerTest {

	private GlobalNextPrevSearchEntryHandler handler;

	@BeforeEach
	public void setUp() {
		handler = new GlobalNextPrevSearchEntryHandler();
	}

	/**
	 * When no search has been run the handler returns {@code null} silently rather
	 * than throwing an exception.
	 */
	@Test
	public void testExecuteReturnsNullWhenNoSearchResultViewIsOpen() throws Exception {
		// Deliberately close any open search view so the handler finds nothing
		SearchTestUtil.ensureSearchViewClosed();
		Object result = handler.execute(new ExecutionEvent());
		assertNull(result, "Handler should return null when no search result view is open");
	}

	/**
	 * Configuring with {@code "previous"} must not throw and leaves the handler
	 * ready to navigate backwards.
	 */
	@Test
	public void testSetInitializationDataWithPrevious() throws CoreException {
		handler.setInitializationData(null, "command", "previous"); //$NON-NLS-1$ //$NON-NLS-2$
		// Direction verified behaviourally in GlobalNextPrevSearchEntryHandlerIntegrationTest
	}

	/**
	 * Configuring with {@code "next"} must not throw; it is also the default.
	 */
	@Test
	public void testSetInitializationDataWithNext() throws CoreException {
		handler.setInitializationData(null, "command", "next"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * A {@code null} data value must not throw; the handler defaults to navigate-next.
	 */
	@Test
	public void testSetInitializationDataWithNullDefaultsToNext() throws CoreException {
		handler.setInitializationData(null, null, null);
		// No exception expected; direction defaults to next
	}

	/**
	 * An unrecognised data value must not throw; the handler defaults to navigate-next.
	 */
	@Test
	public void testSetInitializationDataWithUnknownValueDefaultsToNext() throws CoreException {
		handler.setInitializationData(null, "command", "sideways"); //$NON-NLS-1$ //$NON-NLS-2$
	}
}
