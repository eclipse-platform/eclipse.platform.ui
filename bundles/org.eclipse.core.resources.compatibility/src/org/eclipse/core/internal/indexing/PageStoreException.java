/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.indexing;

import org.eclipse.core.internal.resources.CompatibilityMessages;


public class PageStoreException extends StoreException {
	public final static int GenericFailure = 0;
	public final static int CreateFailure = 1;
	public final static int OpenFailure = 2;
	public final static int LengthFailure = 3;
	public final static int WriteFailure = 4;
	public final static int ReadFailure = 5;
	public final static int CommitFailure = 6;
	public final static int IntegrityFailure = 7;
	public final static int MetadataRequestFailure = 8;
	public final static int ConversionFailure = 9;

	public final static int LogCreateFailure = 20;
	public final static int LogOpenFailure = 21;
	public final static int LogReadFailure = 23;
	public final static int LogWriteFailure = 24;

	public final static String[] message = new String[30];

	/**
	 * All serializable objects should have a stable serialVersionUID
	 */
	private static final long serialVersionUID = 1L;
	
	static {
		initializeMessages();
	}

	public int id = 0; // exception id

	public PageStoreException(int id) {
		this(id, null);
	}

	public PageStoreException(int id, Throwable exception) {
		super(message[id], exception);
		this.id = id;
	}

	/**
	 * Initialize the messages at class load time.
	 */
	private static void initializeMessages() {
		message[GenericFailure] = CompatibilityMessages.pageStore_genericFailure;
		message[CreateFailure] = CompatibilityMessages.pageStore_createFailure;
		message[OpenFailure] = CompatibilityMessages.pageStore_openFailure;
		message[LengthFailure] = CompatibilityMessages.pageStore_lengthFailure;
		message[WriteFailure] = CompatibilityMessages.pageStore_writeFailure;
		message[ReadFailure] = CompatibilityMessages.pageStore_readFailure;
		message[CommitFailure] = CompatibilityMessages.pageStore_commitFailure;
		message[IntegrityFailure] = CompatibilityMessages.pageStore_integrityFailure;
		message[MetadataRequestFailure] = CompatibilityMessages.pageStore_metadataRequestFailure;
		message[ConversionFailure] = CompatibilityMessages.pageStore_conversionFailure;
		message[LogCreateFailure] = CompatibilityMessages.pageStore_logCreateFailure;
		message[LogOpenFailure] = CompatibilityMessages.pageStore_logOpenFailure;
		message[LogReadFailure] = CompatibilityMessages.pageStore_logReadFailure;
		message[LogWriteFailure] = CompatibilityMessages.pageStore_logWriteFailure;
	}
}
