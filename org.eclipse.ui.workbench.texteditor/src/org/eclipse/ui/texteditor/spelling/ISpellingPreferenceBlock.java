/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.texteditor.spelling;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;


/**
 * Contributors to the <code>org.eclipse.ui.texteditor.spellingEngine</code>
 * extension point can specify an implementation of this interface to be
 * displayed on the spelling preference page, if the corresponding engine is
 * selected.
 * <p>
 * This interface is intended to be implemented by clients.
 * </p>
 *
 * @since 3.1
 */
public interface ISpellingPreferenceBlock {

	/**
	 * Creates the control that will be displayed on the preference page.
	 *
	 * @param parent the parent composite to which to add the preferences control
	 * @return the control that was added to <code>parent</code>
	 */
	Control createControl(Composite parent);

	/**
	 * Called after creating the control. Implementations should load the
	 * preferences values and update the controls accordingly. A status
	 * monitor is supplied to allow for status reporting to the user.
	 *
	 * @param statusMonitor the status monitor
	 */
	void initialize(IPreferenceStatusMonitor statusMonitor);

	/**
	 * Sets the enablement of all controls of this preference block.
	 *
	 * @param enabled <code>true</code> iff the controls should be enabled
	 */
	void setEnabled(boolean enabled);

	/**
	 * Returns <code>true</code> iff {@link #performOk()} may be called. A
	 * preference block may, for example, return <code>false</code> if
	 * some user supplied value is not valid (and validation is an expensive
	 * operation, use {@link IPreferenceStatusMonitor} to report validation
	 * results on-the-fly). A preference block may also request additional
	 * user input and cancel the initiated {@link #performOk()}, based on
	 * that input.
	 * <p>
	 * Note that this method is guaranteed to be called only on an enabled
	 * spelling engine, any spelling engine should be prepared to store its
	 * settings on {@link #performOk()} without a preceding call to this
	 * method.
	 * </p>
	 *
	 * @return <code>true</code> iff <code>performOk()</code> may be
	 *         called
	 */
	boolean canPerformOk();

	/**
	 * Called when the <code>OK</code> button is pressed on the preference
	 * page. Implementations should commit the configured preference
	 * settings into their form of preference storage.
	 */
	void performOk();

	/**
	 * Called when the <code>Defaults</code> button is pressed on the
	 * preference page. Implementation should reset any preference settings to
	 * their default values and adjust the controls accordingly.
	 */
	void performDefaults();

	/**
	 * Called when the user decided to dismiss all changes. Implementation
	 * should reset any working copy changes to their previous values and
	 * adjust the controls accordingly.
	 */
	void performRevert();

	/**
	 * Called when the preference page is being disposed. Implementations should
	 * free any resources they are holding on to.
	 */
	void dispose();

}
