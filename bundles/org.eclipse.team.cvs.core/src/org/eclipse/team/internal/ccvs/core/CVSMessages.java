/**********************************************************************
 * Copyright (c) 2005 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.team.internal.ccvs.core;

import org.eclipse.osgi.util.NLS;

public class CVSMessages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.team.internal.ccvs.core.messages";//$NON-NLS-1$
	//
	// Copyright (c) 2000, 2005 IBM Corporation and others.
	// All rights reserved. This program and the accompanying materials
	// are made available under the terms of the Eclipse Public License v1.0
	// which accompanies this distribution, and is available at
	// http://www.eclipse.org/legal/epl-v10.html
	//
	// Contributors:
	//     IBM Corporation - initial API and implementation
	//
	public static String org_eclipse_team_internal_provider_cvs_CVSException;

	public static String ok;
	public static String internal;

	public static String AbstractStructureVisitor_sendingFolder;
	public static String AbstractStructureVisitor_sendingFile;
	public static String AbstractStructureVisitor_noRemote;

	public static String AddDeleteMoveListener_deletedResource;
	public static String AddDeleteMoveListener_Error_creating_deletion_marker_1;
	public static String AddDeleteMoveListener_Local_addition_not_under_CVS_control_2;
	public static String AddDeleteMoveListener_Error_creating_addition_marker_3;
	public static String AddDeleteMoveListener_Error_updating_marker_state_4;

	public static String CVSAuthenticationException_detail;
	public static String CVSCommunicationException_io;
	public static String CVSCommunicationException_interruptCause;
	public static String CVSCommunicationException_interruptSolution;
	public static String CVSCommunicationException_alternateInterruptCause;
	public static String CVSCommunicationException_alternateInterruptSolution;
	public static String CVSFileException_io;
	public static String CVSDiffException_message;
	public static String CVSStatus_messageWithRoot;

	public static String CVSTag_nullName;
	public static String CVSTag_emptyName;
	public static String CVSTag_beginName;
	public static String CVSTag_badCharName;

	public static String CVSWorkspaceRoot_notCVSFolder;

	public static String java_io_IOException;
	public static String java_io_EOFException;
	public static String java_io_FileNotFoundException;
	public static String java_io_InterruptedIOException;
	public static String java_net_UnknownHostException;
	public static String java_net_ConnectException;
	public static String java_net_SocketException;
	public static String java_net_NoRouteToHostException;

	public static String Connection_cannotClose;
	public static String Connection_readUnestablishedConnection;
	public static String Connection_writeUnestablishedConnection;
	public static String Connection_0;

	public static String PServerConnection_invalidChars;
	public static String PServerConnection_hostInvalid;
	public static String PServerConnection_loginRefused;
	public static String PServerConnection_invalidUser;
	public static String PServerConnection_socket;
	public static String PServerConnection_connectionRefused;
	public static String PServerConnection_stream;
	public static String PServerConnection_noResponse;
	public static String PServerConnection_authenticating;

	public static String CVSProviderPlugin_cannotUpdateDescription;
	public static String CVSProviderPlugin_errorDeletingCache;
	public static String CVSProviderPlugin_errorCreatingCache;
	public static String CVSProviderPlugin_unknownStateFileVersion;

	public static String CVSProvider_exception;
	public static String CVSProvider_invalidResource;
	public static String CVSProvider_initialImport;
	public static String CVSProvider_alreadyExists;
	public static String CVSProvider_rename;
	public static String CVSProvider_save;
	public static String CVSProvider_ioException;
	public static String CVSProvider_errorSaving;
	public static String CVSProvider_errorLoading;
	public static String CVSProvider_infoMismatch;

	public static String CVSTeamProvider_noFolderInfo;
	public static String CVSTeamProvider_deconfigureProblem;
	public static String CVSTeamProvider_initializationFailed;
	public static String CVSTeamProvider_visitError;
	public static String CVSTeamProvider_invalidResource;
	public static String CVSTeamProvider_checkinProblems;
	public static String CVSTeamProvider_invalidProjectState;
	public static String CVSTeamProvider_typesDiffer;
	public static String CVSTeamProvider_connectionInfo;
	public static String CVSTeamProvider_scrubbingResource;
	public static String CVSTeamProvider_preparingToSetKSubst;
	public static String CVSTeamProvider_settingKSubst;
	public static String CVSTeamProvider_cleanLineDelimitersException;
	public static String CVSTeamProvider_changingKeywordComment;
	public static String CVSTeamProvider_errorGettingFetchProperty;
	public static String CVSTeamProvider_errorSettingFetchProperty;
	public static String CVSTeamProvider_overlappingRemoteFolder;
	public static String CVSTeamProvider_overlappingFileDeletion;
	public static String CVSTeamProvider_errorGettingWatchEdit;
	public static String CVSTeamProvider_errorSettingWatchEdit;
	public static String CVSTeamProvider_errorAddingFileToDiff;
	public static String CVSTeamProvider_updatingFolder;

	public static String ResourceDeltaVisitor_visitError;

	public static String ResponseDispatcher_serverError;
	public static String ResponseDispatcher_problemsReported;
	public static String ResponseDispatcher_receiving;

	public static String FileProperties_invalidEntryLine;

	public static String EclipseResource_invalidResourceClass;

	public static String RemoteResource_invalidResourceClass;
	public static String RemoteResource_invalidOperation;
	public static String RemoteFolder_invalidChild;
	public static String RemoteFolder_errorFetchingRevisions;
	public static String RemoteFolder_errorFetchingMembers;
	public static String RemoteFolder_doesNotExist;

	public static String RemoteFile_noContentsReceived;
	public static String RemoteFile_errorRetrievingFromCache;

	public static String RemoteFolderTreeBuilder_buildingBase;
	public static String RemoteFolderTreeBuilder_0;
	public static String RemoteFolderTreeBuilder_receivingDelta;
	public static String RemoteFolderTreeBuilder_receivingRevision;
	public static String RemoteFolderTreeBuilder_missingParent;
	public static String RemoteFolderTreeBuild_folderDeletedFromServer;

	public static String ReplaceWithBaseVisitor_replacing;

	public static String Session_badInt;
	public static String Session_receiving;
	public static String Session_sending;
	public static String Session_transfer;
	public static String Session_transferNoSize;
	public static String Session_calculatingCompressedSize;
	public static String Session_dot_2;
	public static String Session_0;

	public static String Command_receivingResponses;
	public static String Command_warnings;
	public static String Command_serverError;
	public static String Command_noMoreInfoAvailable;
	public static String Command_add;
	public static String Command_admin;
	public static String Command_annotate;
	public static String Command_co;
	public static String Command_ci;
	public static String Command_diff;
	public static String Command_editors;
	public static String Command_import;
	public static String Command_log;
	public static String Command_remove;
	public static String Command_status;
	public static String Command_tag;
	public static String Command_rtag;
	public static String Command_update;
	public static String Command_version;
	public static String Command_rdiff;
	public static String Command_valid_requests;
	public static String Command_expand_modules;
	public static String Command_unsupportedResponse;
	public static String Command_argumentNotManaged;
	public static String Command_invalidTag;
	public static String Command_noOpenSession;
	public static String Command_seriousServerError;

    public static String Add_invalidParent;
    
	public static String Commit_syncInfoMissing;
	public static String Commit_timestampReset;

	public static String Diff_serverError;

	public static String Tag_notVersionOrBranchError;

	public static String DefaultHandler_connectionClosed;
	public static String ModTimeHandler_invalidFormat;
	public static String Updated_numberFormat;
	public static String UpdateListener_0;
	public static String UnsupportedHandler_message;
	public static String RemovedHandler_invalid;
	public static String RemovedHandler_0;
	public static String CheckInHandler_checkedIn;

	public static String KSubstOption__kb_short;
	public static String KSubstOption__kb_long;
	public static String KSubstOption__ko_short;
	public static String KSubstOption__ko_long;
	public static String KSubstOption__kkv_short;
	public static String KSubstOption__kkv_long;
	public static String KSubstOption__kkvl_short;
	public static String KSubstOption__kkvl_long;
	public static String KSubstOption__kv_short;
	public static String KSubstOption__kv_long;
	public static String KSubstOption__kk_short;
	public static String KSubstOption__kk_long;
	public static String KSubstOption_unknown_short;
	public static String KSubstOption_unknown_long;

	public static String AdminKSubstListener_expectedRCSFile;
	public static String AdminKSubstListener_commandRootNotManaged;
	public static String AdminKSubstListener_expectedChildOfCommandRoot;
	public static String AdminKSubstListener_couldNotSetResourceSyncInfo;

	public static String CVSRepositoryLocation_nullLocation;
	public static String CVSRepositoryLocation_emptyLocation;
	public static String CVSRepositoryLocation_endWhitespace;
	public static String CVSRepositoryLocation_locationForm;
	public static String CVSRepositoryLocation_startOfLocation;
	public static String CVSRepositoryLocation_methods;
	public static String CVSRepositoryLocation_parsingMethod;
	public static String CVSRepositoryLocation_parsingUser;
	public static String CVSRepositoryLocation_parsingPassword;
	public static String CVSRepositoryLocation_parsingHost;
	public static String CVSRepositoryLocation_parsingPort;
	public static String CVSRepositoryLocation_parsingRoot;
	public static String CVSRepositoryLocation_invalidFormat;
	public static String CVSRepositoryLocation_authenticationCanceled;
	public static String CVSRepositoryLocation_errorCaching;
	public static String CVSRepositoryLocation_openingConnection;
	public static String CVSRepositoryLocation_usernameRequired;
    public static String CVSRepositoryLocation_hostRequired;
    public static String CVSRepositoryLocation_rootRequired;
    public static String CVSRepositoryLocation_noAuthenticator;

	public static String ProjectDescriptionContentHandler_xml;

	public static String Util_invalidResource;
	public static String Util_timeout;
	public static String Util_processTimeout;
	public static String Util_truncatedPath;

	public static String ResourceSyncInfo_malformedSyncBytes;
	public static String Synchronizer_reload;
	public static String Checking_out_from_CVS____5;
	public static String FileSystemSynchronizer_Error_loading_from_CVS_Entries_file_1;
	public static String FileSystemSynchronizer_Error_loading_from__cvsignore_file_2;
	public static String FileSystemSynchronizer_Error_loading_from_CVS_Root_Repository_files_3;
	public static String FileSystemSynchronizer_Error_reloading_sync_information_5;
	public static String Malformed_entry_line___11;
	public static String Malformed_entry_line__missing_name___12;
	public static String Malformed_entry_line__missing_revision___13;
	public static String FolderSyncInfo_Maleformed_root_4;
	public static String SyncFileUtil_Error_writing_to_Entries_log_48;
	public static String SyncFileUtil_Cannot_close_Entries_log_49;
	public static String SyncFileUtil_Error_reloading_sync_information_58;
	public static String SyncFileUtil_Error_writing_to__cvsignore_61;
	public static String SyncFileUtil_Cannot_close__cvsignore_62;
	public static String SyncFileWriter_baseNotAvailable;
	public static String BaseRevInfo_malformedEntryLine;

	public static String FileModificationValidator_isReadOnly;

	public static String EXTServerConnection_invalidPort;
	public static String EXTServerConnection_varsNotSet;
	public static String EXTServerConnection_ioError;

	public static String CVSRemoteSyncElement_rootDiffers;
	public static String CVSRemoteSyncElement_repositoryDiffers;
	public static String Util_Internal_error__resource_does_not_start_with_root_3;

	public static String CVSProvider_Scrubbing_local_project_1;
	public static String CVSProvider_Scrubbing_projects_1;
	public static String CVSProvider_Creating_projects_2;

	public static String EclipseFile_Problem_deleting_resource;
	public static String EclipseFile_Problem_accessing_resource;
	public static String EclipseFile_Problem_creating_resource;
	public static String EclipseFile_Problem_writing_resource;
	public static String EclipseFolder_problem_creating;
	public static String EclipseFolder_isModifiedProgress;

	public static String EclipseSynchronizer_UpdatingSyncEndOperation;
	public static String EclipseSynchronizer_UpdatingSyncEndOperationCancelled;
	public static String EclipseSynchronizer_NotifyingListeners;
	public static String EclipseSynchronizer_ErrorSettingFolderSync;
	public static String EclipseSynchronizer_ErrorSettingResourceSync;
	public static String EclipseSynchronizer_ErrorSettingIgnorePattern;
	public static String EclipseSynchronizer_ErrorCommitting;
	public static String EclipseSynchronizer_folderSyncInfoMissing;
	public static String EclipseSynchronizer_workspaceClosedForResource;

	public static String SynchrnoizerSyncInfoCache_failedToSetSyncBytes;

	public static String SyncFileChangeListener_errorSettingTeamPrivateFlag;

	public static String RemoteFile_getContents;
	public static String RemoteFile_getLogEntries;
	public static String RemoteFolder_exists;
	public static String RemoteFolder_getMembers;
	public static String RemoteModule_getRemoteModules;
	public static String RemoteModule_invalidDefinition;

	public static String PruneFolderVisitor_caseVariantsExist;
	public static String PruneFolderVisitor_caseVariantExists;

	public static String Version_unsupportedVersion;
	public static String Version_unknownVersionFormat;
	public static String Version_versionNotValidRequest;

	public static String LogListener_invalidRevisionFormat;
	public static String RemoteFile_Could_not_cache_remote_contents_to_disk__Caching_remote_file_in_memory_instead__1;

	public static String NotifyInfo_MalformedLine;
	public static String NotifyInfo_MalformedNotificationType;
	public static String NotifyInfo_MalformedNotifyDate;

	public static String ResourceSynchronizer_missingParentBytesOnGet;
	public static String ResourceSynchronizer_missingParentBytesOnSet;
	public static String CVSAnnotateBlock_4;
	public static String CVSAnnotateBlock_5;
	public static String CVSAnnotateBlock_6;
	public static String CVSMergeSubscriber_2;
	public static String CVSMergeSubscriber_4;
	public static String CVSMergeSubscriber_13;
	public static String CVSMergeSubscriber_19;
	public static String CVSMergeSubscriber_21;
	public static String CVSMergeSubscriber_22;
	public static String CVSProviderPlugin_20;
	public static String CVSProviderPlugin_21;
	public static String CVSRevisionNumberCompareCriteria_1;
	public static String ReentrantLock_9;
	public static String CRLFDetectInputStream_0;
	public static String SynchronizerSyncInfoCache_0;
	public static String DeferredResourceChangeHandler_0;
	public static String DeferredResourceChangeHandler_1;
	public static String CVSWorkspaceRoot_11;
	public static String RemoveEntryHandler_2;
	public static String ServerMessageLineMatcher_5;
	public static String ServerMessageLineMatcher_6;
	public static String ServerMessageLineMatcher_7;
	public static String CVSSyncInfo_7;
	public static String CVSSyncInfo_8;
	public static String CVSSyncInfo_9;
	public static String CVSSyncInfo_10;
	public static String CVSCompareSubscriber_2;
	public static String CVSCompareSubscriber_3;
	public static String CompareDiffListener_11;
	public static String CompareDiffListener_12;
	public static String AnnotateListener_3;
	public static String AnnotateListener_4;
	public static String CVSWorkspaceSubscriber_1;
	public static String CVSWorkspaceSubscriber_2;
	public static String KnownRepositories_0;
	public static String CVSRepositoryLocation_73;
	public static String CVSRepositoryLocation_74;
	public static String CVSRepositoryLocation_75;
	public static String SyncFileWriter_0;
	public static String ResponseHandler_0;

	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, CVSMessages.class);
	}
}