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

	public PageStoreException(String s) {
		this(s, null);
	}

	public PageStoreException(String s, Throwable exception) {
		super(s, exception);
		this.id = GenericFailure;
	}

	/**
	 * Initialize the messages at class load time.
	 */
	private static void initializeMessages() {
		message[GenericFailure] = bind("pageStore.genericFailure"); //$NON-NLS-1$
		message[CreateFailure] = bind("pageStore.createFailure"); //$NON-NLS-1$
		message[OpenFailure] = bind("pageStore.openFailure"); //$NON-NLS-1$
		message[LengthFailure] = bind("pageStore.lengthFailure"); //$NON-NLS-1$
		message[WriteFailure] = bind("pageStore.writeFailure"); //$NON-NLS-1$
		message[ReadFailure] = bind("pageStore.readFailure"); //$NON-NLS-1$
		message[CommitFailure] = bind("pageStore.commitFailure"); //$NON-NLS-1$
		message[IntegrityFailure] = bind("pageStore.integrityFailure"); //$NON-NLS-1$
		message[MetadataRequestFailure] = bind("pageStore.metadataRequestFailure"); //$NON-NLS-1$
		message[ConversionFailure] = bind("pageStore.conversionFailure"); //$NON-NLS-1$
		message[LogCreateFailure] = bind("pageStore.logCreateFailure"); //$NON-NLS-1$
		message[LogOpenFailure] = bind("pageStore.logOpenFailure"); //$NON-NLS-1$
		message[LogReadFailure] = bind("pageStore.logReadFailure"); //$NON-NLS-1$
		message[LogWriteFailure] = bind("pageStore.logWriteFailure"); //$NON-NLS-1$
	}

	private static String bind(String name) {
		return Policy.bind(name);
	}

}