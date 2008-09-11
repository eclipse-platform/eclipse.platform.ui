/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.internal.text;

import org.eclipse.jface.text.IDelayedInputChangeProvider;
import org.eclipse.jface.text.IInputChangedListener;


/**
 * A delayed input change listener that forwards delayed input changes to an information control replacer.
 *
 * @since 3.4
 */
public final class DelayedInputChangeListener implements IInputChangedListener {

	private final IDelayedInputChangeProvider fChangeProvider;
	private final InformationControlReplacer fInformationControlReplacer;

	/**
	 * Creates a new listener.
	 *
	 * @param changeProvider the information control with delayed input changes
	 * @param informationControlReplacer the information control replacer, whose information control should get the new input
	 */
	public DelayedInputChangeListener(IDelayedInputChangeProvider changeProvider, InformationControlReplacer informationControlReplacer) {
		fChangeProvider= changeProvider;
		fInformationControlReplacer= informationControlReplacer;
	}

	/*
	 * @see org.eclipse.jface.text.IDelayedInputChangeListener#inputChanged(java.lang.Object)
	 */
	public void inputChanged(Object newInput) {
		fChangeProvider.setDelayedInputChangeListener(null);
		fInformationControlReplacer.setDelayedInput(newInput);
	}
}
