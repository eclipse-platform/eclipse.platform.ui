/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.indexing;

import org.eclipse.core.internal.utils.Policy;

public class IndexedStoreException extends StoreException {

	public static final int GenericError = 0;
	public static final int EntryKeyLengthError = 1;
	public static final int EntryNotRemoved = 2;
	public static final int EntryValueLengthError = 3;
	public static final int EntryValueNotUpdated = 4;
	public static final int IndexNodeNotRetrieved = 5;
	public static final int IndexNodeNotStored = 6;
	public static final int IndexNodeNotSplit = 7;
	public static final int IndexNodeNotCreated = 8;
	public static final int IndexExists = 9;
	public static final int IndexNotCreated = 10;
	public static final int IndexNotFound = 11;
	public static final int IndexNotRemoved = 12;
	public static final int ObjectExists = 13;
	public static final int ObjectNotAcquired = 14;
	public static final int ObjectNotCreated = 15;
	public static final int ObjectNotFound = 16;
	public static final int ObjectNotReleased = 17;
	public static final int ObjectNotRemoved = 18;
	public static final int ObjectNotUpdated = 19;
	public static final int ObjectNotStored = 20;
	public static final int ObjectTypeError = 21;
	public static final int StoreEmpty = 22;
	public static final int StoreFormatError = 23;
	public static final int StoreNotCreated = 24;
	public static final int StoreNotOpen = 25;
	public static final int StoreNotClosed = 26;
	public static final int StoreNotFlushed = 27;
	public static final int StoreNotOpened = 28;
	public static final int StoreNotReadWrite = 29;
	public static final int ContextNotAvailable = 30;
	public static final int ObjectIDInvalid = 31;
	public static final int MetadataRequestError = 32;
	public static final int EntryRemoved = 33;
	public static final int StoreNotConverted = 34;
	public static final int StoreIsOpen = 35;
	public static final int StoreNotCommitted = 36;
	public static final int StoreNotRolledBack = 37;

	public static String[] messages = new String[40];

	/**
	 * All serializable objects should have a stable serialVersionUID
	 */
	private static final long serialVersionUID = 1L;

	static {
		initializeMessages();
	}

	public int id = GenericError;

	/**
	 * IndexedStoreException constructor comment.
	 */
	public IndexedStoreException(int id) {
		super(messages[id]);
		this.id = id;
	}

	/**
	 * IndexedStoreException constructor comment.
	 */
	public IndexedStoreException(int id, Throwable e) {
		super(messages[id], e);
		this.id = id;
	}

	/**
	 * IndexedStoreException constructor comment.
	 */
	public IndexedStoreException(String s) {
		super(s);
		id = GenericError;
	}

	/**
	 * Initializes the messages at class load time.
	 */
	private static void initializeMessages() {
		messages[GenericError] = bind("indexedStore.genericError"); //$NON-NLS-1$
		messages[EntryKeyLengthError] = bind("indexedStore.entryKeyLengthError"); //$NON-NLS-1$
		messages[EntryNotRemoved] = bind("indexedStore.entryNotRemoved"); //$NON-NLS-1$
		messages[EntryValueLengthError] = bind("indexedStore.entryValueLengthError"); //$NON-NLS-1$
		messages[EntryValueNotUpdated] = bind("indexedStore.entryValueNotUpdated"); //$NON-NLS-1$
		messages[IndexNodeNotRetrieved] = bind("indexedStore.indexNodeNotRetrieved"); //$NON-NLS-1$
		messages[IndexNodeNotStored] = bind("indexedStore.indexNodeNotStored"); //$NON-NLS-1$
		messages[IndexNodeNotSplit] = bind("indexedStore.indexNodeNotSplit"); //$NON-NLS-1$
		messages[IndexNodeNotCreated] = bind("indexedStore.indexNodeNotCreated"); //$NON-NLS-1$
		messages[IndexExists] = bind("indexedStore.indexExists"); //$NON-NLS-1$
		messages[IndexNotCreated] = bind("indexedStore.indexNotCreated"); //$NON-NLS-1$
		messages[IndexNotFound] = bind("indexedStore.indexNotFound"); //$NON-NLS-1$
		messages[IndexNotRemoved] = bind("indexedStore.indexNotRemoved"); //$NON-NLS-1$
		messages[ObjectExists] = bind("indexedStore.objectExists"); //$NON-NLS-1$
		messages[ObjectNotAcquired] = bind("indexedStore.objectNotAcquired"); //$NON-NLS-1$
		messages[ObjectNotCreated] = bind("indexedStore.objectNotCreated"); //$NON-NLS-1$
		messages[ObjectNotFound] = bind("indexedStore.objectNotFound"); //$NON-NLS-1$
		messages[ObjectNotReleased] = bind("indexedStore.objectNotReleased"); //$NON-NLS-1$
		messages[ObjectNotRemoved] = bind("indexedStore.objectNotRemoved"); //$NON-NLS-1$
		messages[ObjectNotUpdated] = bind("indexedStore.objectNotUpdated"); //$NON-NLS-1$
		messages[ObjectNotStored] = bind("indexedStore.objectNotStored"); //$NON-NLS-1$
		messages[ObjectTypeError] = bind("indexedStore.objectTypeError"); //$NON-NLS-1$
		messages[StoreEmpty] = bind("indexedStore.storeEmpty"); //$NON-NLS-1$
		messages[StoreFormatError] = bind("indexedStore.storeFormatError"); //$NON-NLS-1$
		messages[StoreNotCreated] = bind("indexedStore.storeNotCreated"); //$NON-NLS-1$
		messages[StoreNotOpen] = bind("indexedStore.storeNotOpen"); //$NON-NLS-1$
		messages[StoreNotClosed] = bind("indexedStore.storeNotClosed"); //$NON-NLS-1$
		messages[StoreNotFlushed] = bind("indexedStore.storeNotFlushed"); //$NON-NLS-1$
		messages[StoreNotOpened] = bind("indexedStore.storeNotOpened"); //$NON-NLS-1$
		messages[StoreNotReadWrite] = bind("indexedStore.storeNotReadWrite"); //$NON-NLS-1$
		messages[ContextNotAvailable] = bind("indexedStore.contextNotAvailable"); //$NON-NLS-1$
		messages[ObjectIDInvalid] = bind("indexedStore.objectIDInvalid"); //$NON-NLS-1$
		messages[MetadataRequestError] = bind("indexedStore.metadataRequestError"); //$NON-NLS-1$
		messages[EntryRemoved] = bind("indexedStore.entryRemoved"); //$NON-NLS-1$
		messages[StoreNotConverted] = bind("indexedStore.storeNotConverted"); //$NON-NLS-1$
		messages[StoreIsOpen] = bind("indexedStore.storeIsOpen"); //$NON-NLS-1$
		messages[StoreNotCommitted] = bind("indexedStore.storeNotCommitted"); //$NON-NLS-1$
		messages[StoreNotRolledBack] = bind("indexedStore.storeNotRolledBack"); //$NON-NLS-1$
	}

	private static String bind(String name) {
		return Policy.bind(name);
	}

	/**
	 * Creates a printable representation of this exception.
	 */
	public String toString() {
		StringBuffer buffer = new StringBuffer(50);
		buffer.append("IndexedStoreException:"); //$NON-NLS-1$
		buffer.append(getMessage());
		if (wrappedException != null) {
			buffer.append("\n"); //$NON-NLS-1$
			buffer.append(wrappedException.toString());
		}
		return buffer.toString();
	}
}