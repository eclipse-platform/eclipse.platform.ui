/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.decorators;

import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IDecoratorManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

/**
 * The DecoratorTestPart is the abstract superclass of the ViewParts that are
 * used for decorator tests.
 * 
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
	 * 
	 * @return
	 */
	protected DecoratingLabelProvider getLabelProvider() {

		IDecoratorManager manager = PlatformUI.getWorkbench()
				.getDecoratorManager();
		manager.addListener(getDecoratorManagerListener());
		return new DecoratingLabelProvider(new TestLabelProvider(), manager);

	}

	/**
	 * Get the listener for the suite.
	 * 
	 * @return
	 */
	private ILabelProviderListener getDecoratorManagerListener() {
		listener = new ILabelProviderListener() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.ILabelProviderListener#labelProviderChanged(org.eclipse.jface.viewers.LabelProviderChangedEvent)
			 */
			public void labelProviderChanged(LabelProviderChangedEvent event) {
				// Reset the end time each time we get an update
				endTime = System.currentTimeMillis() + DELAY_TIME;

			}
		};

		return listener;
	}

	public void readAndDispatchForUpdates() {
		while (System.currentTimeMillis() < endTime)
			Display.getCurrent().readAndDispatch();

	}

	public void setUpForDecorators() {
		endTime = System.currentTimeMillis() + DELAY_TIME;

	}

	public void dispose() {
		PlatformUI.getWorkbench().getDecoratorManager()
				.removeListener(listener);

	}

}
