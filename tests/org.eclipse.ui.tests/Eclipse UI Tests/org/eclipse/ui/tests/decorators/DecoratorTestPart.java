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
import org.eclipse.ui.IDecoratorManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

/**
 * The DecoratorTestPart is the abstract superclass of the ViewParts that are
 * used for decorator tests.
 * 
 */
public abstract class DecoratorTestPart extends ViewPart {

	private ILabelProviderListener listener;
	
	public boolean updateHappened = false;

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
	 * Get a listener that is informed of DecoratorManager updates.
	 * @return ILabelProviderListener
	 */
	public abstract ILabelProviderListener getDecoratorManagerListener();

	

	public void dispose() {
		PlatformUI.getWorkbench().getDecoratorManager()
				.removeListener(listener);

	}
	
	public void clearFlags() {
		updateHappened = false;
	}
}