/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
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
 ******************************************************************************/

package org.eclipse.ui.tests.statushandlers;

import java.lang.reflect.Field;

import org.eclipse.ui.internal.WorkbenchErrorHandlerProxy;
import org.eclipse.ui.statushandlers.AbstractStatusHandler;
import org.eclipse.ui.statushandlers.StatusAdapter;
import org.eclipse.ui.statushandlers.StatusManager;

/**
 * The handler should be used during tests. It allows for checking the status
 * and style used during last handling.
 * <p>
 * <b>Note:</b>This handler is <b>not</b> registered via the
 * <code>statusHandler</code> extension point, because it is not possible to
 * reliably replace the workbench default status handler that way (the
 * <code>statusHandlerProductBinding</code> extension point can only bind it to
 * a product, which is not necessarily used while executing tests, e.g. on the
 * build server).
 * <p>
 * Instead, individual tests can activate this handler on-demand via
 * {@link #install()} and should be de-activate it after the test (e.g. in
 * teardown) via {@link #uninstall()}.
 *
 * @since 3.3
 */
public class TestStatusHandler extends AbstractStatusHandler {

	private static StatusAdapter lastHandledStatusAdapter;

	private static int lastHandledStyle;

	private static AbstractStatusHandler workbenchHandler;

	private static boolean isInstalled;
	private static Object originalInstance;

	private TestStatusHandler() {
	}

	@Override
	public void handle(StatusAdapter statusAdapter, int style) {
		lastHandledStatusAdapter = statusAdapter;
		lastHandledStyle = style;

		if (workbenchHandler == null) {
			workbenchHandler = new WorkbenchErrorHandlerProxy();
		}

		// Forward to the workbench handler
		workbenchHandler.handle(statusAdapter, style);
	}

	/**
	 * Injects a {@link TestStatusHandler} into {@link StatusManager}'s cache,
	 * remembering the current instance, if any.
	 */
	public static void install() throws Exception {
		if (isInstalled) {
			return;
		}
		Field fieldStatusHandler = StatusManager.class.getDeclaredField("statusHandler");
		fieldStatusHandler.setAccessible(true);
		StatusManager statusManager = StatusManager.getManager();
		originalInstance = fieldStatusHandler.get(statusManager);
		fieldStatusHandler.set(statusManager, new TestStatusHandler());
		fieldStatusHandler.setAccessible(false);
		isInstalled = true;
	}

	/**
	 * Removes the injected {@link TestStatusHandler} instance in
	 * {@link StatusManager}'s cache, restoring the previous instance remembered, by
	 * {@link #install()}, if any.
	 */
	public static void uninstall() throws Exception {
		if (!isInstalled) {
			return;
		}
		Field fieldStatusHandler = StatusManager.class.getDeclaredField("statusHandler");
		fieldStatusHandler.setAccessible(true);
		StatusManager statusManager = StatusManager.getManager();
		fieldStatusHandler.set(statusManager, originalInstance);
		fieldStatusHandler.setAccessible(false);
		isInstalled = false;
		originalInstance = null;
	}

	/**
	 * Returns the status used during last handling
	 *
	 * @return the status
	 */
	public static StatusAdapter getLastHandledStatusAdapter() {
		return lastHandledStatusAdapter;
	}

	/**
	 * Returns the style used during last handling
	 *
	 * @return the style
	 */
	public static int getLastHandledStyle() {
		return lastHandledStyle;
	}
}
