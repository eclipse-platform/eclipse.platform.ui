/*******************************************************************************
 * Copyright (c) 2018 Remain Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     wim.jongman@remainsoftware.com - initial API and implementation
 *******************************************************************************/
package org.eclipse.tips.ui;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.tips.core.Tip;

/**
 * A decoration of {@link Tip} class that provides the opportunity to
 * create a rich SWT UI.
 *
 */
public interface ISwtTip {

	/**
	 * Creates the top level control for this tip on the given parent composite. You
	 * can implement this method to build your the UI for your Tip in SWT. You can
	 * find and example implementation in the tips example bundle.
	 *
	 * @param parent
	 *            the parent composite
	 */
	public abstract void createControl(Composite parent);
}