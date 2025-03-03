/*******************************************************************************
 * Copyright (c) 2004, 2025 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.e4.ui.progress;

import org.eclipse.core.runtime.QualifiedName;

/**
 * Constants relating to progress UI functionality of the workbench plug-in.
 * <p>
 * The four constants define property keys that are used to associate
 * UI related information with Jobs (<code>org.eclipse.core.runtime.jobs.Job</code>).
 *
 * @see org.eclipse.core.runtime.jobs.Job#setProperty
 * @since 3.0
 */
public interface IProgressConstants {

	/**
	 * Common prefix for properties defined in this interface.
	 */
	static final String PROPERTY_PREFIX = "org.eclipse.ui"; //$NON-NLS-1$

	static final String PLUGIN_ID = "org.eclipse.e4.ui.progress"; //$NON-NLS-1$


	/**
	 * This property provides a hint to the progress UI to keep Jobs
	 * in the UI after they have finished. This can be used to communicate results of a Job
	 * back to the user.
	 * <p>
	 * The property must be of type <code>Boolean</code> and the hint is used
	 * if its value is <code>true</code>.
	 * </p>
	 */
	public static final QualifiedName KEEP_PROPERTY = new QualifiedName(
			PROPERTY_PREFIX, "keep"); //$NON-NLS-1$

	/**
	 * The KEEPONE_PROPERTY is an extension to the KEEP_PROPERTY, that provides a hint
	 * to the progress UI to ensure that only a single Job of a Job family is kept in the
	 * set of kept Jobs. That is, whenever a Job that has the KEEPONE_PROPERTY starts or finishes,
	 * all other kept Jobs of the same family are removed first.
	 * <p>
	 * Membership to family is determined using a Job's <code>belongsTo</code>
	 * method. The progress service will pass each job that currently exists in the
	 * view to the <code>belongsTo</code> method of a newly added job. Clients who
	 * set the <code>KEEPONE_PROPERTY</code> must implement a <code>belongsTo</code>
	 * method that determines if the passed job is of the same family as their job
	 * and return <code>true</code> if it is.
	 * </p>
	 * <p>
	 * Please note that other Jobs of the same family are only removed if they have finished.
	 * Non finished jobs of the same family are left alone.
	 * </p>
	 **/
	public static final QualifiedName KEEPONE_PROPERTY = new QualifiedName(
			PROPERTY_PREFIX, "keepone"); //$NON-NLS-1$

	/**
	 * This property is used to associate an <code>IAction</code> with a Job. If
	 * the Job is shown in the UI, the action might be represented as a button
	 * or hyper link to allow the user to trigger a job specific action, like
	 * showing the Job's results.
	 * <p>
	 * The progress UI will track the enabled state of the action and its
	 * tooltip text.
	 * </p>
	 * <p>
	 * If the action implements <code>ActionFactory.IWorkbenchAction</code>, its
	 * <code>dispose</code> method will be called as soon as the Job is finally
	 * removed from the set of kept jobs.
	 * </p>
	 * <p>
	 * Note: Only one of <code>ACTION_PROPERTY</code> or
	 * <code>COMMAND_PROPERTY</code> should be used
	 * </p>
	 *
	 * @see org.eclipse.jface.action.IAction
	 **/
	public static final QualifiedName ACTION_PROPERTY = new QualifiedName(
			PROPERTY_PREFIX, "action"); //$NON-NLS-1$

	/**
	 * This property is used to associate an <code>ImageDescriptor</code> with a Job.
	 * If the Job is shown in the UI, this descriptor is used to create an icon that
	 * represents the Job.
	 * <p>
	 * Please note, that this property is only used if no <code>ImageDescriptor</code> has been
	 * registered for the Job family with the <code>IProgressService</code>.
	 * </p>
	 * @see org.eclipse.jface.resource.ImageDescriptor
	 * @see org.eclipse.e4.ui.progress.IProgressService
	 **/
	public static final QualifiedName ICON_PROPERTY = new QualifiedName(
			PROPERTY_PREFIX, "icon"); //$NON-NLS-1$

	/**
	 * This is a property set on a user job if the user has not decided to
	 * run the job in the background.
	 * The value is set to <code>true</code> when the job starts and set to
	 * <code>false</code> if the user subsequently decides to complete the job in the
	 * background.
	 * <p>
	 * This property is not intended to be set by clients.
	 * </p>
	 * @see org.eclipse.core.runtime.jobs.Job#isUser()
	 */
	public static final QualifiedName PROPERTY_IN_DIALOG = new QualifiedName(
			IProgressConstants.PROPERTY_PREFIX, "inDialog"); //$NON-NLS-1$

	/**
	 * This property provides a hint to the progress UI to not prompt on errors
	 * immediately but instead make the errors available through the progress UI.
	 * <p>
	 * The property must be of type <code>Boolean</code> and the hint is used
	 * if its value is <code>true</code>.
	 * </p>
	 * @since 3.1
	 */
	public static final QualifiedName NO_IMMEDIATE_ERROR_PROMPT_PROPERTY = new QualifiedName(
			PROPERTY_PREFIX, "delayErrorPrompt"); //$NON-NLS-1$

	/**
	 * Stores whether or not system jobs are being shown.
	 *
	 * @since 3.1
	 */
	public static final String SHOW_SYSTEM_JOBS = "SHOW_SYSTEM_JOBS";//$NON-NLS-1$

	/**
	 * This property is used to associate a <code>ParameterizedCommand</code>
	 * with a Job. If the Job is shown in the UI, the command might be
	 * represented as a button or hyper link to allow the user to trigger a job
	 * specific action, like showing the Job's results.
	 * <p>
	 * Note: Only one of <code>ACTION_PROPERTY</code> or
	 * <code>COMMAND_PROPERTY</code> should be used
	 * </p>
	 *
	 * @see org.eclipse.core.commands.ParameterizedCommand
	 **/
	public static final QualifiedName COMMAND_PROPERTY = new QualifiedName(
			PROPERTY_PREFIX, "command"); //$NON-NLS-1$

	/**
	 * This property provides a hint to the progress UI to show the progress of
	 * the job in the application TaskBar
	 * <p>
	 * The property must be of type <code>Boolean</code> and the hint is used if
	 * its value is <code>true</code>.
	 * </p>
	 */
	public static final QualifiedName SHOW_IN_TASKBAR_ICON_PROPERTY = new QualifiedName(
			PROPERTY_PREFIX, "inTaskBarIcon"); //$NON-NLS-1$

	public static final String RUN_IN_BACKGROUND = "RUN_IN_BACKGROUND"; //$NON-NLS-1$

	/**
	 * This property provides a hint to the progress UI to hide the "Cancel" button,
	 * effectively making the job uncancellable by the user.
	 * <p>
	 * The property must be of type {@code Boolean}.
	 * </p>
	 *
	 * @since 0.4.700
	 */
	QualifiedName CANCELLABLE = new QualifiedName(PROPERTY_PREFIX, "cancellable"); //$NON-NLS-1$
}
