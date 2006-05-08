/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.progress;

import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.ui.PlatformUI;

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
    static final String PROPERTY_PREFIX = PlatformUI.PLUGIN_ID
            + ".workbench.progress"; //$NON-NLS-1$

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
     * This property is used to associate an <code>IAction</code> with a Job.
     * If the Job is shown in the UI, the action might be represented as a button or
     * hyper link to allow the user to trigger a job specific action, like showing
     * the Job's results.
     * <p>
     * The progress UI will track the enabled state of the action and its tooltip text.
     * </p>
     * <p>
     * If the action implements <code>ActionFactory.IWorkbenchAction</code>, its
     * <code>dispose</code> method will be called as soon as the Job is finally
     * removed from the set of kept jobs.
     * </p>
     * @see org.eclipse.jface.action.IAction
     * @see org.eclipse.ui.actions.ActionFactory.IWorkbenchAction
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
     * @see org.eclipse.ui.progress.IProgressService
     **/
    public static final QualifiedName ICON_PROPERTY = new QualifiedName(
            PROPERTY_PREFIX, "icon"); //$NON-NLS-1$

    /**
     * Constant for the progress view id.
     */
    public static String PROGRESS_VIEW_ID = "org.eclipse.ui.views.ProgressView"; //$NON-NLS-1$

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
}
