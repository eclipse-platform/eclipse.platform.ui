package org.eclipse.core.internal.indexing;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000 
 */

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
	message[GenericFailure] = "Error occurred during a page store file operation.";
	message[CreateFailure] = "Error occurred creating page store file.";
	message[LengthFailure] = "Error occurred determing page store file length.";
	message[OpenFailure] = "Error occurred opening page store file.";
	message[ReadFailure] = "Error occurred reading page store file.";
	message[WriteFailure] = "Error occurred writing page store file.";
	message[CommitFailure] = "Error occurred during commit processing.";
	message[MetadataRequestFailure] = "Error occurred during metadata request processing.";
	message[ConversionFailure] = "Error occurred during conversion of the page store file.";
	message[IntegrityFailure] = "Error occurred that compromises the integrity of the page store file.";
	message[LogOpenFailure] = "Error occurred opening transaction log file.";
	message[LogReadFailure] = "Error occurred reading transaction log file.";
	message[LogWriteFailure] = "Error occurred writing transaction log file.";
	message[LogCreateFailure] = "Error occurred creating transaction log file.";
}
}
