/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.PlatformUI;

/**
 * The <code>SystemSummaryEditorInputFactory</code> creates
 * <code>SystemSummaryEditorInput</code> objects.
 */
public class SystemSummaryEditorInputFactory implements IElementFactory {
	/*
	 * The ID of the factory that creates this input.
	 */
	static final String FACTORY_ID = PlatformUI.PLUGIN_ID + ".SystemSummaryEditorInputFactory"; //$NON-NLS-1$

	/**
	 * Creates the factory, should not be called
	 */
	public SystemSummaryEditorInputFactory() {
		super();
	}
	
	/**
	 * @see org.eclipse.ui.IElementFactory#createElement(IMemento)
	 */
	public IAdaptable createElement(IMemento memento) {
		return new SystemSummaryEditorInput();
	}
}
