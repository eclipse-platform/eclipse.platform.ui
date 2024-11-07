/*******************************************************************************
 * Copyright (c) 2010, 2020 IBM Corporation and others.
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
 *     Tasktop Technologies - Bug 302529 [UX] [Progress] Show Eclipse IDE progress in the Eclipse icon on the Windows 7 Task Bar
 *******************************************************************************/
package org.eclipse.ui.progress;

import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.ui.PlatformUI;

/**
 * Constants relating to progress UI functionality of the workbench plug-in.
 * <p>
 * The constants define property keys that are used to associate UI related
 * information with Jobs (<code>org.eclipse.core.runtime.jobs.Job</code>). This
 * class is a superset of all previously defined progress constants.
 *
 * @see org.eclipse.core.runtime.jobs.Job#setProperty
 * @see IProgressConstants
 * @since 3.6
 *
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IProgressConstants2 extends IProgressConstants {

	/**
	 * Common prefix for properties defined in this interface.
	 */
	String PROPERTY_PREFIX = PlatformUI.PLUGIN_ID + ".workbench.progress"; //$NON-NLS-1$

	/**
	 * This property is used to associate a <code>ParameterizedCommand</code> with a
	 * Job. If the Job is shown in the UI, the command might be represented as a
	 * button or hyper link to allow the user to trigger a job specific action, like
	 * showing the Job's results.
	 * <p>
	 * Note: Only one of <code>ACTION_PROPERTY</code> or
	 * <code>COMMAND_PROPERTY</code> should be used
	 * </p>
	 *
	 * @see org.eclipse.core.commands.ParameterizedCommand
	 **/
	QualifiedName COMMAND_PROPERTY = new QualifiedName(PROPERTY_PREFIX, "command"); //$NON-NLS-1$

	/**
	 * This property provides a hint to the progress UI to show the progress of the
	 * job in the application TaskBar
	 * <p>
	 * The property must be of type <code>Boolean</code> and the hint is used if its
	 * value is <code>true</code>.
	 * </p>
	 */
	QualifiedName SHOW_IN_TASKBAR_ICON_PROPERTY = new QualifiedName(PROPERTY_PREFIX, "inTaskBarIcon"); //$NON-NLS-1$
}
