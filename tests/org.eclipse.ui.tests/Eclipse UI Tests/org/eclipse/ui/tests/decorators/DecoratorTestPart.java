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

	public boolean waitingForDecoration = true;

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
		// Listener for decorator manager events
		listener = event -> {
			// Decorator update occurred
		};

		return listener;
	}

	/**
	 * Process events until decorations are applied. Uses a fixed delay
	 * to allow time for decorator updates to be processed.
	 */
	public void readAndDispatchForUpdates() {
		Display display = Display.getCurrent();
		// Process events for 1 second with regular intervals
		for (int i = 0; i < 20; i++) {
			while (display.readAndDispatch()) {
				// Process all pending events
			}
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return;
			}
		}
		// Final pass to process any remaining events
		while (display.readAndDispatch()) {
			// Keep processing
		}
	}

	public void setUpForDecorators() {
		// No initialization needed for simple fixed delay approach
	}

	@Override
	public void dispose() {
		PlatformUI.getWorkbench().getDecoratorManager()
				.removeListener(listener);

	}

}
