/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.resources;

import org.eclipse.osgi.util.NLS;

public class CompatibilityMessages extends NLS {

	private static final String BUNDLE_NAME = "org.eclipse.core.internal.resources.messages"; //$NON-NLS-1$

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
	public static String history_problemsAccessing;
	public static String history_tooManySimUpdates;
	public static String history_problemsCleaning;
	public static String history_notValid;
	public static String history_specificProblemsCleaning;
	public static String history_corrupt;
	public static String history_problemsRemoving;
	public static String history_couldNotAdd;
	public static String history_interalPathErrors;
	public static String history_copyToNull;
	public static String history_copyToSelf;
	public static String history_problemsPurging;
	public static String history_problemCopying;
	public static String history_conversionSucceeded;
	// history store
	public static String history_conversionFailed;

	public static String properties_storeProblem;
	public static String properties_invalidPropName;
	public static String properties_conversionFailed;
	public static String properties_conversionSucceeded;	
	public static String properties_couldNotWriteProp;
	public static String properties_couldNotDeleteProp;
	public static String properties_couldNotReadProp;
	public static String properties_storeNotAvailable;

	public static String resources_mustExist;

	static {
		// initialize resource bundles
		NLS.initializeMessages(BUNDLE_NAME, CompatibilityMessages.class);
	}

}
