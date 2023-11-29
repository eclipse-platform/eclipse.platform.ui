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

	private static final int DELAY_TIME = 2000;// Wait 2 seconds

	public boolean waitingForDecoration = true;

	private long endTime;

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
		// Reset the end time each time we get an update
		listener = event -> endTime = System.currentTimeMillis() + DELAY_TIME;

		return listener;
	}

	public void readAndDispatchForUpdates() {
		while (System.currentTimeMillis() < endTime) {
			Display.getCurrent().readAndDispatch();
		}

	}

	public void setUpForDecorators() {
		endTime = System.currentTimeMillis() + DELAY_TIME;

	}

	@Override
	public void dispose() {
		PlatformUI.getWorkbench().getDecoratorManager()
				.removeListener(listener);

	}

}
