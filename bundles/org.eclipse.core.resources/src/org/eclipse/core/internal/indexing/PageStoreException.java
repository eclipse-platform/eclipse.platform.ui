package org.eclipse.core.internal.indexing;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.internal.utils.Policy;

public class PageStoreException extends Exception {
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
	static {
		initializeMessages();
	}

	public int id = 0; // exception id

	public PageStoreException(int id) {
		super(message[id]);
		this.id = id;
	}
	public PageStoreException(String s) {
		super(s);
		this.id = GenericFailure;
	}
	
	/**
	 * Initialize the messages at class load time.
	 */
	private static void initializeMessages() {
		message[GenericFailure] = bind("pageStore.genericFailure");
		message[CreateFailure] = bind("pageStore.createFailure");
		message[LengthFailure] = bind("pageStore.lengthFailure");
		message[OpenFailure] = bind("pageStore.openFailure");
		message[ReadFailure] = bind("pageStore.readFailure");
		message[WriteFailure] = bind("pageStore.writeFailure");
		message[CommitFailure] = bind("pageStore.commitFailure");
		message[MetadataRequestFailure] = bind("pageStore.metadataRequestFailure");
		message[ConversionFailure] = bind("pageStore.conversionFailure");
		message[IntegrityFailure] = bind("pageStore.integrityFailure");
		message[LogOpenFailure] = bind("pageStore.logOpenFailure");
		message[LogReadFailure] = bind("pageStore.logReadFailure");
		message[LogWriteFailure] = bind("pageStore.logWriteFailure");
		message[LogCreateFailure] = bind("pageStore.logCreateFailure");
	}

	private static String bind(String name) {
		String message = name;
		try {
			message = Policy.bind(name);
		} catch (NoClassDefFoundError e) {
		}
		return message;
	}

}