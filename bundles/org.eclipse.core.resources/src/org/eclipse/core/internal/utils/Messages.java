/**********************************************************************
 * Copyright (c) 2005 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.internal.utils;

import org.eclipse.core.runtime.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.core.internal.utils.messages"; //$NON-NLS-1$
	
	// dtree
	public static String dtree_immutable;
	public static String dtree_malformedTree;
	public static String dtree_missingChild;
	public static String dtree_notFound;
	public static String dtree_notImmutable;
	public static String dtree_parentsNotImmutable;
	public static String dtree_reverse;
	public static String dtree_subclassImplement;
	public static String dtree_switchError;

	// events
	public static String events_builderError;
	public static String events_building_0;
	public static String events_building_1;
	public static String events_errors;
	public static String events_instantiate_1;
	public static String events_invoking_1;
	public static String events_invoking_2;
	public static String events_skippingBuilder;
	public static String events_unknown;

	// history store
	public static String history_conversionFailed;
	public static String history_conversionSucceeded;
	public static String history_copyToNull;
	public static String history_copyToSelf;
	public static String history_corrupt;
	public static String history_couldNotAdd;
	public static String history_errorContentDescription;

	public static String history_conversionTransitional;
	public static String history_interalPathErrors;
	public static String history_notValid;
	public static String history_problemCopying;
	public static String history_problemsAccessing;
	public static String history_problemsCleaning;
	public static String history_problemsPurging;
	public static String history_problemsRemoving;
	public static String history_specificProblemsCleaning;
	public static String history_tooManySimUpdates;

	// properties
	public static String indexed_couldNotClose;
	public static String indexed_couldNotCommit;
	public static String indexed_couldNotCreate;
	public static String indexed_couldNotCreateCursor;
	public static String indexed_couldNotCreateIndex;
	public static String indexed_couldNotDelete;
	public static String indexed_couldNotGetIndex;
	public static String indexed_couldNotOpen;
	public static String indexed_couldNotRead;
	public static String indexed_couldNotWrite;

	// indexing
	public static String indexedStore_contextNotAvailable;
	public static String indexedStore_entryKeyLengthError;
	public static String indexedStore_entryNotRemoved;
	public static String indexedStore_entryRemoved;
	public static String indexedStore_entryValueLengthError;
	public static String indexedStore_entryValueNotUpdated;
	public static String indexedStore_genericError;
	public static String indexedStore_indexExists;
	public static String indexedStore_indexNodeNotCreated;
	public static String indexedStore_indexNodeNotRetrieved;
	public static String indexedStore_indexNodeNotSplit;
	public static String indexedStore_indexNodeNotStored;
	public static String indexedStore_indexNotCreated;
	public static String indexedStore_indexNotFound;
	public static String indexedStore_indexNotRemoved;
	public static String indexedStore_metadataRequestError;

	public static String indexedStore_objectExists;
	public static String indexedStore_objectIDInvalid;
	public static String indexedStore_objectNotAcquired;
	public static String indexedStore_objectNotCreated;
	public static String indexedStore_objectNotFound;
	public static String indexedStore_objectNotReleased;
	public static String indexedStore_objectNotRemoved;
	public static String indexedStore_objectNotStored;
	public static String indexedStore_objectNotUpdated;

	public static String indexedStore_objectTypeError;
	public static String indexedStore_storeEmpty;
	public static String indexedStore_storeFormatError;
	public static String indexedStore_storeIsOpen;
	public static String indexedStore_storeNotClosed;
	public static String indexedStore_storeNotCommitted;
	public static String indexedStore_storeNotConverted;
	public static String indexedStore_storeNotCreated;
	public static String indexedStore_storeNotFlushed;
	public static String indexedStore_storeNotOpen;
	public static String indexedStore_storeNotOpened;
	public static String indexedStore_storeNotReadWrite;
	public static String indexedStore_storeNotRolledBack;

	public static String links_copyNotProject;
	public static String links_creating;
	public static String links_errorLinkReconcile;
	public static String links_invalidLocation;
	public static String links_localDoesNotExist;
	public static String links_locationOverlapsLink;
	public static String links_locationOverlapsProject;
	public static String links_moveNotProject;

	public static String links_natureVeto;
	public static String links_noPath;
	public static String links_overlappingResource;
	public static String links_parentNotAccessible;
	public static String links_parentNotProject;
	public static String links_updatingDuplicate;
	public static String links_vetoNature;
	public static String links_workspaceVeto;
	public static String links_wrongLocalType;

	// localstore
	public static String localstore_copying;
	public static String localstore_copyProblem;
	public static String localstore_couldNotCreateFolder;
	public static String localstore_couldnotDelete;
	public static String localstore_couldnotDeleteReadOnly;
	public static String localstore_couldNotLoadLibrary;
	public static String localstore_couldNotMove;
	public static String localstore_couldNotRead;
	public static String localstore_couldNotWrite;
	public static String localstore_couldNotWriteReadOnly;
	public static String localstore_deleteProblem;
	public static String localstore_deleteProblemDuringMove;
	public static String localstore_deleting;
	public static String localstore_failedMove;
	public static String localstore_failedReadDuringWrite;
	public static String localstore_fileExists;
	public static String localstore_fileNotFound;
	public static String localstore_locationUndefined;
	public static String localstore_moving;
	public static String localstore_notAFile;
	public static String localstore_readOnlyParent;
	public static String localstore_refreshing;
	public static String localstore_refreshingRoot;
	public static String localstore_resourceExists;
	public static String localstore_resourceIsOutOfSync;

	// internal.resources
	public static String natures_duplicateNature;
	public static String natures_hasCycle;
	public static String natures_invalidDefinition;
	public static String natures_invalidRemoval;
	public static String natures_invalidSet;
	public static String natures_missingIdentifier;
	public static String natures_missingNature;
	public static String natures_missingPrerequisite;
	public static String natures_multipleSetMembers;

	public static String objectStore_genericFailure;
	public static String objectStore_internalFailure;
	public static String objectStore_metadataRequestFailure;
	public static String objectStore_objectExistenceFailure;
	public static String objectStore_objectHeaderFailure;
	public static String objectStore_objectInsertFailure;
	public static String objectStore_objectIsLocked;
	public static String objectStore_objectRemoveFailure;
	public static String objectStore_objectSizeFailure;
	public static String objectStore_objectTypeFailure;
	public static String objectStore_objectUpdateFailure;
	public static String objectStore_pageReadFailure;
	public static String objectStore_pageVacancyFailure;
	public static String objectStore_pageWriteFailure;
	public static String objectStore_storeCloseFailure;
	public static String objectStore_storeConversionFailure;
	public static String objectStore_storeCreateFailure;
	public static String objectStore_storeOpenFailure;

	public static String pageStore_commitFailure;
	public static String pageStore_conversionFailure;
	public static String pageStore_createFailure;
	public static String pageStore_genericFailure;
	public static String pageStore_integrityFailure;
	public static String pageStore_lengthFailure;
	public static String pageStore_logCreateFailure;
	public static String pageStore_logOpenFailure;
	public static String pageStore_logReadFailure;
	public static String pageStore_logWriteFailure;
	public static String pageStore_metadataRequestFailure;
	public static String pageStore_openFailure;
	public static String pageStore_readFailure;
	public static String pageStore_writeFailure;

	public static String pathvar_beginLetter;
	public static String pathvar_invalidChar;
	public static String pathvar_invalidValue;
	public static String pathvar_length;
	public static String pathvar_undefined;

	public static String preferences_deleteException;
	public static String preferences_loadException;
	public static String preferences_removeNodeException;
	public static String preferences_saveProblems;
	public static String preferences_syncException;

	public static String projRead_badLinkLocation;
	public static String projRead_badLinkName;
	public static String projRead_badLinkType;
	public static String projRead_badLinkType2;
	public static String projRead_badLocation;
	public static String projRead_emptyLinkName;
	public static String projRead_failureReadingProjectDesc;
	public static String projRead_notProjectDescription;
	public static String projRead_whichKey;
	public static String projRead_whichValue;

	public static String properties_conversionFailed;
	public static String properties_conversionSucceeded;	
	public static String properties_couldNotClose;
	public static String properties_couldNotDeleteProp;
	public static String properties_couldNotReadProp;
	public static String properties_couldNotWriteProp;
	public static String properties_invalidPropName;
	public static String properties_qualifierIsNull;	
	public static String properties_readProperties;	
	public static String properties_storeNotAvailable;
	public static String properties_storeProblem;
	public static String properties_valueTooLong;	

	// auto-refresh
	public static String refresh_installError;
	public static String refresh_jobName;
	public static String refresh_pollJob;
	public static String refresh_refreshErr;
	public static String refresh_task;

	public static String resources_cannotModify;
	public static String resources_changeInAdd;
	public static String resources_charsetBroadcasting;
	public static String resources_charsetUpdating;
	public static String resources_closing_0;
	public static String resources_closing_1;
	public static String resources_copyDestNotSub;
	public static String resources_copying;
	public static String resources_copying_0;
	public static String resources_copyNotMet;
	public static String resources_copyProblem;
	public static String resources_couldnotDelete;
	public static String resources_create;
	public static String resources_creating;
	public static String resources_deleteMeta;
	public static String resources_deleteProblem;
	public static String resources_deleting;
	public static String resources_deleting_0;
	public static String resources_destNotNull;
	public static String resources_errorContentDescription;
	public static String resources_errorDeleting;
	public static String resources_errorMarkersDelete;
	public static String resources_errorMarkersMove;
	public static String resources_errorMembers;
	public static String resources_errorMoving;
	public static String resources_errorMultiRefresh;
	public static String resources_errorNature;
	public static String resources_errorPropertiesMove;
	public static String resources_errorReadProject;
	public static String resources_errorRefresh;
	public static String resources_errorValidator;
	public static String resources_errorVisiting;
	public static String resources_existsDifferentCase;
	public static String resources_existsLocalDifferentCase;
	public static String resources_exMasterTable;
	public static String resources_exReadProjectLocation;
	public static String resources_exSafeRead;
	public static String resources_exSafeSave;
	public static String resources_exSaveMaster;
	public static String resources_exSaveProjectLocation;
	public static String resources_fileExists;
	public static String resources_fileToProj;
	public static String resources_folderOverFile;
	public static String resources_format;
	public static String resources_getResourceAttributesFailed;
	public static String resources_ignored;
	public static String resources_initHook;
	public static String resources_initTeamHook;
	public static String resources_initValidator;
	public static String resources_invalidCharInName;
	public static String resources_invalidCharInPath;
	public static String resources_invalidName;
	public static String resources_invalidPath;
	public static String resources_invalidProjDesc;
	public static String resources_invalidResourceName;
	public static String resources_invalidRoot;
	public static String resources_markerNotFound;
	public static String resources_missingProjectMeta;
	public static String resources_missingProjectMetaRepaired;
	public static String resources_moveDestNotSub;
	public static String resources_moveMeta;
	public static String resources_moveNotMet;
	public static String resources_moveNotProject;
	public static String resources_moveProblem;
	public static String resources_moveRoot;
	public static String resources_moving;
	public static String resources_moving_0;
	public static String resources_mustBeAbsolute;
	public static String resources_mustBeLocal;
	public static String resources_mustBeOpen;
	public static String resources_mustExist;
	public static String resources_mustNotExist;
	public static String resources_nameEmpty;
	public static String resources_nameNull;
	public static String resources_natureClass;
	public static String resources_natureDeconfig;
	public static String resources_natureExtension;
	public static String resources_natureFormat;
	public static String resources_natureImplement;
	public static String resources_notChild;
	public static String resources_oneHook;
	public static String resources_oneTeamHook;
	public static String resources_oneValidator;
	public static String resources_opening_1;
	public static String resources_overlapLocal;
	public static String resources_pathNull;
	public static String resources_projectDesc;
	public static String resources_projectDescSync;
	public static String resources_projectPath;
	public static String resources_reading;
	public static String resources_readingEncoding;
	public static String resources_readingSnap;
	public static String resources_readMarkers;
	public static String resources_readMeta;
	public static String resources_readMetaWrongVersion;
	public static String resources_readOnly;
	public static String resources_readOnly2;
	public static String resources_readProjectMeta;
	public static String resources_readProjectTree;
	public static String resources_readSync;
	public static String resources_readWorkspaceMeta;
	public static String resources_readWorkspaceMetaValue;
	public static String resources_readWorkspaceSnap;
	public static String resources_readWorkspaceTree;
	public static String resources_refreshing;
	public static String resources_refreshingRoot;
	public static String resources_resetMarkers;
	public static String resources_resetSync;
	public static String resources_resourcePath;
	public static String resources_restoring;
	public static String resources_running;
	public static String resources_saveOp;
	public static String resources_saveProblem;
	public static String resources_saveWarnings;
	public static String resources_saving_0;
	public static String resources_savingEncoding;
	public static String resources_setDesc;
	public static String resources_setLocal;
	public static String resources_setResourceAttributesFailed;
	public static String resources_settingCharset;
	public static String resources_settingContents;
	public static String resources_settingDefaultCharsetContainer;
	public static String resources_shutdown;
	public static String resources_shutdownProblems;
	public static String resources_snapInit;
	public static String resources_snapRead;
	public static String resources_snapRequest;
	public static String resources_snapshot;
	public static String resources_startupProblems;
	public static String resources_touch;
	public static String resources_updating;
	public static String resources_updatingEncoding;
	public static String resources_workspaceClosed;
	public static String resources_workspaceOpen;
	public static String resources_writeMeta;
	public static String resources_writeWorkspaceMeta;

	public static String synchronizer_partnerNotRegistered;

	// URL
	public static String url_badVariant;
	public static String url_couldNotResolve;

	// utils
	public static String utils_clone;
	public static String utils_failed;
	public static String utils_noElements;
	public static String utils_null;
	public static String utils_print;
	public static String utils_stringJobName;
	public static String utils_wrongLength;

	// watson
	public static String watson_elementNotFound;
	public static String watson_illegalSubtree;
	public static String watson_immutable;
	public static String watson_noModify;
	public static String watson_nullArg;
	public static String watson_unknown;

	// auto-refresh win32 native
	public static String WM_beginTask;
	public static String WM_errCloseHandle;
	public static String WM_errCreateHandle;
	public static String WM_errFindChange;
	public static String WM_errors;
	public static String WM_jobName;
	public static String WM_nativeErr;

	static {
		// initialize resource bundles
		NLS.initializeMessages(BUNDLE_NAME, Messages.class); //$NON-NLS-1$
	}
}