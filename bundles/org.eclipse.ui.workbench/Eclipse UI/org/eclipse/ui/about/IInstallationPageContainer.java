/*******************************************************************************
 * Copyright (c) 2008, 2015 IBM Corporation and others.
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
 ******************************************************************************/

package org.eclipse.ui.about;

import org.eclipse.swt.widgets.Button;

/**
 * Interface for a container that hosts one or more installation pages (
 * {@link InstallationPage}).
 *
 * @noimplement This interface is not intended to be implemented by clients.
 *
 * @since 3.5
 */
public interface IInstallationPageContainer {

	/**
	 * Register a button as belonging to a particular page in the container. The
	 * container will manage the placement and visibility of the page's buttons.
	 *
	 * @param page   the page that created the button
	 * @param button the button to be managed
	 */
	void registerPageButton(InstallationPage page, Button button);

	/**
	 * Closes any modal containers that were used to launch this installation page.
	 * This method should be used when a page is launching a long-running task (such
	 * as a background job) that requires progress indication, in order to allow
	 * platform progress indication to behave as expected.
	 */
	void closeModalContainers();

}
