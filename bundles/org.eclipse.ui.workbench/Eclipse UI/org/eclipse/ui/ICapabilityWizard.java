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
package org.eclipse.ui;


/**
 * Interface for project capability configuration wizards.
 * <p>
 * Clients should implement this interface and include the name of their class
 * in an extension contributed to the workbench's capabilities wizard extension point 
 * (named <code>"org.eclipse.ui.capabilities"</code>).
 * </p>
 * <p>
 * <b>NOTE:</b> This is experimental API, which may be changed or removed at any point
 * in time. This API should not be called, overridden or otherwise used in production code.
 * </p>
 *
 * @see org.eclipse.jface.wizard.IWizard
 * @see org.eclipse.ui.ICapabilityInstallWizard
 * @since 2.0
 * @deprecated use ICapabilityInstallWizard instead. This interface
 * 		will be deleted for M6 milestone build.
 */
public interface ICapabilityWizard extends ICapabilityInstallWizard {
}
