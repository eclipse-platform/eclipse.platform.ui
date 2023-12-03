/*******************************************************************************
 * Copyright (c) 2007, 2015 IBM Corporation and others.
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

package org.eclipse.jface.dialogs;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * A ErrorSupportProvider defines the area to be shown in an error dialog for extra support information.
 * @since 3.3
 */
public abstract class ErrorSupportProvider {


		/**
		 * Create an area for adding support components as a child of parent.
		 * @param parent The parent {@link Composite}
		 * @param status The {@link IStatus} that is being displayed.
		 * @return Control
		 */
		public abstract Control createSupportArea(Composite parent, IStatus status);

		/**
		 * This method is called before
		 * {@link #createSupportArea(Composite, IStatus)} to check if the
		 * {@link ErrorSupportProvider} will display any significant
		 * informations. If not, then it will not be presented at all.
		 *
		 * @param status
		 *            - {@link IStatus} for which status are will be requested.
		 * @return true if provider is able to process particular {@link IStatus}
		 * @since 3.7
		 */
		public boolean validFor(IStatus status){
			return true;
		}
}
