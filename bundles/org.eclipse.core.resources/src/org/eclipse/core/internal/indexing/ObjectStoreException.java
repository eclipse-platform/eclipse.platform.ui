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

import org.eclipse.core.internal.utils.Messages;

public class ObjectStoreException extends StoreException {

	public final static int GenericFailure = 0;
	public final static int InternalFailure = 1;
	public final static int StoreCreateFailure = 10;
	public final static int StoreConversionFailure = 11;
	public final static int StoreOpenFailure = 12;
	public final static int StoreCloseFailure = 13;
	public final static int PageReadFailure = 20;
	public final static int PageWriteFailure = 21;
	public final static int PageVacancyFailure = 22;
	public final static int ObjectTypeFailure = 23;
	public final static int ObjectSizeFailure = 24;
	public final static int ObjectExistenceFailure = 25;
	public final static int ObjectHeaderFailure = 26;
	public final static int ObjectInsertFailure = 27;
	public final static int ObjectRemoveFailure = 28;
	public final static int ObjectUpdateFailure = 29;
	public final static int ObjectIsLocked = 30;
	public final static int MetadataRequestFailure = 40;

	public final static String[] message = new String[50];

	/**
	 * All serializable objects should have a stable serialVersionUID
	 */
	private static final long serialVersionUID = 1L;

	static {
		initializeMessages();
	}

	public int id = 0; // exception id

	public ObjectStoreException(int id) {
		this(id, null);
	}

	public ObjectStoreException(int id, Throwable exception) {
		super(message[id], exception);
		this.id = id;
	}

	public ObjectStoreException(String s) {
		this(s, null);
	}

	public ObjectStoreException(String s, Throwable exception) {
		super(s, exception);
		this.id = GenericFailure;
	}

	/**
	 * Initializes the messages at class load time.
	 */
	private static void initializeMessages() {
		message[GenericFailure] = Messages.bind(Messages.objectStore_genericFailure);
		message[InternalFailure] = Messages.bind(Messages.objectStore_internalFailure);
		message[StoreCreateFailure] = Messages.bind(Messages.objectStore_storeCreateFailure);
		message[StoreConversionFailure] = Messages.bind(Messages.objectStore_storeConversionFailure);
		message[StoreOpenFailure] = Messages.bind(Messages.objectStore_storeOpenFailure);
		message[StoreCloseFailure] = Messages.bind(Messages.objectStore_storeCloseFailure);
		message[PageReadFailure] = Messages.bind(Messages.objectStore_pageReadFailure);
		message[PageWriteFailure] = Messages.bind(Messages.objectStore_pageWriteFailure);
		message[PageVacancyFailure] = Messages.bind(Messages.objectStore_pageVacancyFailure);
		message[ObjectTypeFailure] = Messages.bind(Messages.objectStore_objectTypeFailure);
		message[ObjectSizeFailure] = Messages.bind(Messages.objectStore_objectSizeFailure);
		message[ObjectExistenceFailure] = Messages.bind(Messages.objectStore_objectExistenceFailure);
		message[ObjectHeaderFailure] = Messages.bind(Messages.objectStore_objectHeaderFailure);
		message[ObjectInsertFailure] = Messages.bind(Messages.objectStore_objectInsertFailure);
		message[ObjectRemoveFailure] = Messages.bind(Messages.objectStore_objectRemoveFailure);
		message[ObjectUpdateFailure] = Messages.bind(Messages.objectStore_objectUpdateFailure);
		message[ObjectIsLocked] = Messages.bind(Messages.objectStore_objectIsLocked);
		message[MetadataRequestFailure] = Messages.bind(Messages.objectStore_metadataRequestFailure);
	}

}