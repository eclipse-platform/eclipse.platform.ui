/*******************************************************************************
 * Copyright (c) 2008, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.statushandlers;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.ErrorSupportProvider;
import org.eclipse.jface.util.Policy;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * <p>
 * A status area provider creates an area that displays detailed information
 * about {@link StatusAdapter} or {@link IStatus}.
 * </p>
 * 
 * <p>
 * The area provider can be set in {@link WorkbenchStatusDialogManager} as well as in
 * JFace {@link Policy} since its extends {@link ErrorSupportProvider}.
 * </p>
 * 
 * @see Policy#setErrorSupportProvider(ErrorSupportProvider)
 * @see WorkbenchStatusDialogManager#setSupportAreaProvider(AbstractStatusAreaProvider)
 * @see WorkbenchStatusDialogManager#setDetailsAreaProvider(AbstractStatusAreaProvider)
 * @since 3.4
 */
public abstract class AbstractStatusAreaProvider extends ErrorSupportProvider {

	/**
	 * Create an area for detailed support area as a child of the given parent.
	 * 
	 * @param parent
	 *            A {@link Composite} that will host support area.
	 * @param statusAdapter
	 *            The {@link StatusAdapter} to be supported.
	 * @return a control, that hold all support elements.
	 */
	public abstract Control createSupportArea(Composite parent,
			StatusAdapter statusAdapter);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.ErrorSupportProvider#createSupportArea(org.eclipse.swt.widgets.Composite,
	 *      org.eclipse.core.runtime.IStatus)
	 */
	public final Control createSupportArea(Composite parent, IStatus status) {
		return createSupportArea(parent, new StatusAdapter(status));
	}

	/**
	 * This method is called before
	 * {@link #createSupportArea(Composite, StatusAdapter)} to check if the
	 * {@link AbstractStatusAreaProvider} will display any significant
	 * informations. If not, then it will not be presented at all.
	 * 
	 * @param statusAdapter
	 *            - {@link StatusAdapter} for which status are will be
	 *            requested.
	 * @return true if provider is able to process particular
	 *         {@link StatusAdapter}
	 * @since 3.6
	 */
	public boolean validFor(StatusAdapter statusAdapter) {
		return true;
	}

	/**
	 * This method is called before
	 * {@link #createSupportArea(Composite, IStatus)} to check if the
	 * {@link AbstractStatusAreaProvider} will display any significant
	 * informations. If not, then it will not be presented at all.
	 * 
	 * This implementation wraps the {@link IStatus} into {@link StatusAdapter}
	 * and calls {@link #validFor(StatusAdapter)}.
	 * 
	 * @param status
	 *            - {@link IStatus} for which status are will be requested.
	 * @return true if provider is able to process particular {@link IStatus}
	 * @since 3.7
	 */
	public final boolean validFor(IStatus status) {
		return validFor(new StatusAdapter(status));
	}
}
