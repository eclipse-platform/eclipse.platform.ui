/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ui.internal.statushandlers;

import java.util.Map;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.statushandlers.AbstractStatusAreaProvider;
import org.eclipse.ui.statushandlers.StatusAdapter;


/**
 * This class is responsible for managing details area.
 */
public class DetailsAreaManager {

	private Map dialogState;
	private Control control = null;

	/**
	 * @param dialogState
	 */
	public DetailsAreaManager(Map dialogState) {
		this.dialogState = dialogState;
	}

	/**
	 * Closes the details area
	 */
	public void close() {
		if (control != null && !control.isDisposed()) {
			control.dispose();
		}
	}

	/**
	 * This method is responsible for creating details area on the specified
	 * Composite and displaying specified StatusAdapter
	 * 
	 * @param parent
	 *            A composite on which should be the details area created.
	 * @param statusAdapter
	 *            StatusAdapter for which should be the details area
	 *            created.
	 */
	public void createDetailsArea(Composite parent,
			StatusAdapter statusAdapter) {
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(GridLayoutFactory.fillDefaults().create());
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		getProvider().createSupportArea(container, statusAdapter);
		control = container;
	}

	/**
	 * Returns current detail area provider.
	 * 
	 * @return current detail area provider.
	 */
	public AbstractStatusAreaProvider getProvider() {
		AbstractStatusAreaProvider provider = (AbstractStatusAreaProvider) dialogState
				.get(IStatusDialogConstants.CUSTOM_DETAILS_PROVIDER);
		if (provider == null) {
			provider = new DefaultDetailsArea(dialogState);
		}
		return provider;
	}

	/**
	 * This method allows to check if the details area is open (physically
	 * constructed).
	 * 
	 * @return true if the area is open, false otherwise
	 */
	public boolean isOpen() {
		if (control == null || control.isDisposed()) {
			return false;
		}
		return true;
	}
}