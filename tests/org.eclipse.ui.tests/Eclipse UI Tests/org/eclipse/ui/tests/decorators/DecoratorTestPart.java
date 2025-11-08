/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.ui.tests.decorators;

import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IDecoratorManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

/**
 * The DecoratorTestPart is the abstract superclass of the ViewParts that are
 * used for decorator tests.
 */
public abstract class DecoratorTestPart extends ViewPart {

	private static final int INITIAL_DELAY_TIME = 500;// Initial wait time in milliseconds
	private static final int MAX_WAIT_TIME = 10000;// Maximum wait time (10 seconds)
	private static final int IDLE_TIME = 200;// Time to wait after last update

	public boolean waitingForDecoration = true;

	private volatile long lastUpdateTime;
	private long startTime;

	private ILabelProviderListener listener;

	public DecoratorTestPart() {
		super();
	}

	/**
	 * Get the label provider for the receiver.
	 */
	protected DecoratingLabelProvider getLabelProvider() {

		IDecoratorManager manager = PlatformUI.getWorkbench()
				.getDecoratorManager();
		manager.addListener(getDecoratorManagerListener());
		return new DecoratingLabelProvider(new TestLabelProvider(), manager);

	}

	/**
	 * Get the listener for the suite.
	 */
	private ILabelProviderListener getDecoratorManagerListener() {
		// Record the time each time we get an update
		listener = event -> lastUpdateTime = System.currentTimeMillis();

		return listener;
	}

	/**
	 * Process events until decorations are applied. This waits for updates to settle
	 * by ensuring no new updates occur for IDLE_TIME milliseconds, with a maximum
	 * wait of MAX_WAIT_TIME.
	 */
	public void readAndDispatchForUpdates() {
		Display display = Display.getCurrent();
		long elapsed = System.currentTimeMillis() - startTime;
		
		// Process events and wait for updates to settle
		while (elapsed < MAX_WAIT_TIME) {
			// Process any pending UI events
			while (display.readAndDispatch()) {
				// Keep processing
			}
			
			long timeSinceLastUpdate = System.currentTimeMillis() - lastUpdateTime;
			
			// If we haven't received an update in IDLE_TIME, we're done
			if (timeSinceLastUpdate >= IDLE_TIME && elapsed >= INITIAL_DELAY_TIME) {
				break;
			}
			
			// Small sleep to avoid busy waiting
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				break;
			}
			
			elapsed = System.currentTimeMillis() - startTime;
		}
		
		// Final event processing pass
		while (display.readAndDispatch()) {
			// Keep processing
		}
	}

	public void setUpForDecorators() {
		startTime = System.currentTimeMillis();
		lastUpdateTime = System.currentTimeMillis();
	}

	@Override
	public void dispose() {
		PlatformUI.getWorkbench().getDecoratorManager()
				.removeListener(listener);

	}

}
