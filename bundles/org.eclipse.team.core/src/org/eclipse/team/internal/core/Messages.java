/**********************************************************************
 * Copyright (c) 2005 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.team.internal.core;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.team.internal.core.messages";//$NON-NLS-1$

	public static String ok;
	public static String concatStrings;

	public static String Assert_assertionFailed;

	public static String manager_providerAlreadyMapped;
	public static String manager_errorFlushSync;
	public static String manager_errorDeconfigure;
	public static String manager_providerTypeInvalid;
	public static String manager_providerExtensionNotFound;
	public static String manager_providerNoConfigElems;
	public static String manager_cannotInstantiateExt;
	public static String manager_errorSerialize;
	public static String manager_errorUnserializeProvider;
	public static String manager_errorUnserialize;
	public static String manager_notTeamNature;
	public static String manager_errorSettingNature;
	public static String manager_errorRemovingNature;

	public static String manager_badClassType;
	public static String manager_coreException;

	public static String FileModificationValidator_someReadOnly;
	public static String FileModificationValidator_fileIsReadOnly;
	public static String FileModificationValidator_editFailed;

	public static String RepositoryProvider_Error_removing_nature_from_project___1;
	public static String RepositoryProvider_Too_many_providers_associated_with_project___2;
	public static String RepositoryProviderTypeduplicate_provider_found_in_plugin_xml___1;
	public static String RepositoryProviderTypeRepositoryProvider_assigned_to_the_project_must_be_a_subclass_of_RepositoryProvider___2;
	public static String RepositoryProviderTypeRepositoryProvider_not_registered_as_a_nature_id___3;
	public static String RepositoryProvider_providerTypeIdNotRegistered;
	public static String RepositoryProvider_couldNotInstantiateProvider;
	public static String RepositoryProvider_No_Provider_Registered;
	public static String RepositoryProvider_propertyMismatch;
	public static String RepositoryProvider_linkedResourcesExist;
	public static String RepositoryProvider_linkedResourcesNotSupported;
	public static String RepositoryProvider_couldNotClearAfterError;
	public static String RepositoryProvider_invalidClass;
    public static String RepositoryProvider_toString;

	public static String Team_couldNotDelete;
	public static String Team_couldNotRename;
	public static String Team_writeError;
	public static String Team_readError;
	public static String Team_Could_not_delete_state_file_1;
	public static String Team_Could_not_rename_state_file_2;

	public static String PollingInputStream_readTimeout;
	public static String PollingInputStream_closeTimeout;
	public static String PollingOutputStream_writeTimeout;
	public static String PollingOutputStream_closeTimeout;
	public static String TimeoutOutputStream_cannotWriteToStream;

	public static String Config_error;

	public static String multiStatus_errorsOccurred;
	public static String filetransfer_monitor;

	public static String SynchronizedTargetProvider_invalidURLCombination;

	public static String RemoteSyncElement_delimit;
	public static String RemoteSyncElement_insync;
	public static String RemoteSyncElement_conflicting;
	public static String RemoteSyncElement_outgoing;
	public static String RemoteSyncElement_incoming;
	public static String RemoteSyncElement_change;
	public static String RemoteSyncElement_addition;
	public static String RemoteSyncElement_deletion;
	public static String RemoteSyncElement_manual;
	public static String RemoteSyncElement_auto;

	public static String Team_Error_loading_ignore_state_from_disk_1;

	public static String RemoteContentsCache_cacheNotEnabled;
	public static String RemoteContentsCache_cacheDisposed;
	public static String RemoteContentsCache_fileError;
	public static String TeamProvider_10;
	public static String TeamProvider_11;
	public static String ContentComparisonCriteria_2;
	public static String ContentComparisonCriteria_3;

	public static String SubscriberEventHandler_2;
	public static String SubscriberEventHandler_jobName;
	public static String SubscriberChangeSetCollector_0;
	public static String SubscriberChangeSetCollector_1;
	public static String SubscriberChangeSetCollector_2;
	public static String SubscriberChangeSetCollector_3;
	public static String SubscriberChangeSetCollector_4;
	public static String SubscriberEventHandler_errors;
	public static String RemoteContentsCacheEntry_3;
	public static String SynchronizationCacheRefreshOperation_0;
	public static String SubscriberEventHandler_8;
	public static String SubscriberEventHandler_9;
	public static String SubscriberEventHandler_10;
	public static String SubscriberEventHandler_11;
	public static String CachedResourceVariant_0;
	public static String CachedResourceVariant_1;
	public static String SyncInfoTree_0;
	public static String ResourceVariantTreeSubscriber_1;
	public static String ResourceVariantTreeSubscriber_2;
	public static String SyncByteConverter_1;
	public static String BatchingLock_11;
	public static String SubscriberEventHandler_12;
	public static String ProjectSetCapability_0;
	public static String ProjectSetCapability_1;

	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}
}