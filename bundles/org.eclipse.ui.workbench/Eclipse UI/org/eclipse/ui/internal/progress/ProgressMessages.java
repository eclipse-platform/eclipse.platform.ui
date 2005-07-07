/**********************************************************************
 * Copyright (c) 2005 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.ui.internal.progress;

import org.eclipse.osgi.util.NLS;

public class ProgressMessages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ui.internal.progress.messages";//$NON-NLS-1$

	public static String AnimatedCanvas_JobInvocationError;
	public static String ErrorNotificationDialog_ClearButtonTitle;
	public static String ErrorNotificationDialog_ShowButtonTitle;
	public static String PendingUpdateAdapter_PendingLabel;
	public static String JobInfo_DoneMessage;
	public static String JobInfo_DoneNoProgressMessage;
	public static String JobInfo_NoTaskNameDoneMessage;
	public static String JobsViewPreferenceDialog_Note;
	public static String JobErrorDialog_CustomJobText;
	public static String JobInfo_UnknownProgress;
	public static String JobInfo_Waiting;
	public static String JobInfo_Sleeping;
	public static String JobInfo_System;
	public static String JobInfo_Cancelled;
	public static String JobInfo_Error;
	public static String JobInfo_Blocked;
	public static String JobInfo_Finished;
	public static String JobInfo_FinishedAt;
	public static String JobInfo_Percent;
	public static String JobInfo_Percent2;
	public static String JobInfo_Format;
	public static String JobInfo_TaskFormat;
	public static String JobInfo_TaskFormat2;
	public static String JobInfo_BlocksUserOperationFormat;
	public static String JobErrorDialog_CloseDialogMessage;
	public static String Error;
	public static String UIJob_displayNotSet;
	public static String DeferredTreeContentManager_NotDeferred;
	public static String DeferredTreeContentManager_AddingChildren;
	public static String DeferredTreeContentManager_FetchingName;
	public static String AnimateJob_JobName;
	public static String AnimationItem_InProgressStatus;
	public static String AnimationItem_NotRunningStatus;
	public static String ProgressView_CancelAction;
	public static String ProgressView_ShowErrorAction;
	public static String ProgressView_ClearAllAction;
	public static String NewProgressView_RemoveJobToolTip;
	public static String NewProgressView_RemoveAllJobsToolTip;
	public static String NewProgressView_CancelJobToolTip;
	public static String NewProgressView_errorDialogTitle;
	public static String NewProgressView_errorDialogMessage;
	public static String ProgressAnimationItem_tasks;
	public static String ProgressAnimationItem_ok;
	public static String ProgressAnimationItem_error;
	public static String AnimationItem_RedrawJob;
	public static String SubTaskInfo_UndefinedTaskName;
	public static String DeferredTreeContentManager_ClearJob;
	public static String ProgressContentProvider_UpdateProgressJob;
	public static String JobProgressManager_OpenProgressJob;
	public static String JobErrorDialog_MultipleErrorsTitle;
	public static String AnimationItem_HoverHelp;
	public static String StatusLineProgressListener_Refresh;
	public static String ProgressFeedbackManager_OpenFeedbackJob;
	public static String ProgressFeedbackDialog_DialogTitle;
	public static String ProgressManager_openJobName;
	public static String ProgressManager_showInDialogName;
	public static String CancelJobsButton_title;
	public static String BlockedJobsDialog_CancelBlocking_title;

	public static String MonitorProvider_oneValueMessage;
	public static String MonitorProvider_twoValueMessage;
	public static String MonitorProvider_twoValueUnknownMessage;
	public static String ProgressFeedbackManager_invalidThreadMessage;
	public static String ProgressMonitorJobsDialog_DetailsTitle;
	public static String ProgressMonitorJobsDialog_HideTitle;
	public static String ErrorNotificationDialog_ErrorNotificationTitle;
	public static String ErrorNotificationDialog_ClearSelectionAction;
	public static String ErrorNotificationManager_RefreshErrorDialogJob;
	public static String ErrorNotificationManager_OpenErrorDialogJob;
	public static String AnimationItem_openFloatingWindowJob;
	public static String AnimationManager_AnimationCleanUp;
	public static String AnimationManager_AnimationStart;
	public static String ProgressView_ToggleWindowMessage;
	public static String ProgressFloatingWindow_EllipsisValue;
	public static String ProgressFloatingWindow_CloseToolTip;
	public static String ProgressFloatingWindow_OpenToolTip;

	public static String BlockedJobsDialog_UserInterfaceTreeElement;
	public static String BlockedJobsDialog_BlockedTitle;
	public static String AnimationItem_CloseWindowJob;
	public static String WorkbenchSiteProgressService_CursorJob;
	public static String AnimationManager_DoneJobName;
	public static String ProgressMonitorFocusJobDialog_OpenDialogJob;
	public static String ProgressMonitorFocusJobDialog_UserDialogJob;
	public static String ProgressMonitorFocusJobDialog_CLoseDialogJob;
	public static String ProgressMonitorFocusJobDialog_RunInBackgroundButton;

	public static String JobErrorDialog_MultipleErrorsMessage;
	public static String JobErrorDialog_CloseDialogTitle;
	public static String JobsViewPreferenceDialog_Title;
	public static String JobErrorDialog_DoNotShowAgainMessage;

	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, ProgressMessages.class);
	}
}