package org.eclipse.core.internal.boot.update;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

public interface UpdateManagerConstants {
	
	public static final int TYPE_UNKNOWN            = 0;
	public static final int TYPE_COMPONENT          = 1;
	public static final int TYPE_COMPONENT_CATEGORY = 2;
	public static final int TYPE_COMPONENT_ENTRY    = 3;
	public static final int TYPE_PRODUCT            = 4;
	public static final int TYPE_PRODUCT_CATEGORY   = 5;
	public static final int TYPE_URL                = 6;

	public static final int MODE_UNDEFINED = -1;	
	public static final int MODE_LOCAL     =  0;
	public static final int MODE_REMOTE    =  1;
	public static final int MODE_CACHED    =  2;

	public static final String OPERATION_ACTION_INSTALL = "install";
	public static final String OPERATION_UNZIP_INSTALL = "unzip-install";	// the install/ dir structure
	public static final String OPERATION_UNZIP_PLUGINS = "unzip-plugins";
	public static final String OPERATION_UNZIP_BINDIR = "unzip-bin-dir";
	public static final String OPERATION_COPY           = "copy";
	public static final String OPERATION_VERIFY_JAR = "verify-jar";

	public static final String STATUS_FAILED                   = "failed";
	public static final String STATUS_FAILED_UNDO_SUCCEEDED    = "failed, undo succeeded";
	public static final String STATUS_FAILED_UNDO_FAILED       = "failed, undo failed";
	public static final String STATUS_PENDING                  = "pending";
	public static final String STATUS_SUCCEEDED                = "succeeded";
	public static final String STATUS_SUCCEEDED_UNDO_SUCCEEDED = "succeeded, undo succeeded";
	public static final String STATUS_SUCCEEDED_UNDO_FAILED    = "succeeded, undo failed";

	public static final String STRING_EMPTY    = "";

	public static final String STRING_0                = "0";
	public static final String STRING_ACTION           = "action";
	public static final String STRING_ATTEMPTS         = "attempts";
	public static final String STRING_CONNECTION       = "connection";
	public static final String STRING_ID               = "id";    
	public static final String STRING_ITEM             = "item";
	public static final String STRING_MESSAGE          = "message";
	public static final String STRING_OPERATION        = "operation";
	public static final String STRING_PARCEL           = "parcel";
	public static final String STRING_SESSION          = "session";
	public static final String STRING_SOURCE           = "source";
	public static final String STRING_STATUS           = "status";
	public static final String STRING_SUCCESSFUL       = "successful";
	public static final String STRING_TARGET           = "target";
	public static final String STRING_TEXT             = "text";
	public static final String STRING_TYPE             = "type";
	public static final String STRING_TIMESTAMP        = "timestamp";
	public static final String STRING_UNDO_SUCCESSFUL  = "undo successful";

	public static final int OK              =  0;
	public static final int OK_TO_INSTALL   =  0;
	public static final int COMMAND_INVALID =  1;
	public static final int COMMAND_MISSING =  2;
	public static final int ID_MISSING      =  3;
	public static final int ID_NOT_FOUND    =  4;
	public static final int NOT_COMPATIBLE  =  5;
	public static final int NOT_NEWER       =  6;
	public static final int NOT_UPDATABLE   =  7;
	public static final int OTHER_ERROR     =  8;
	public static final int URL_INVALID     =  9;
	public static final int URL_MISSING     = 10;
	public static final int URL_NOT_FOUND   = 11;

	public static final int CURRENT_REGISTRY = 0;
	public static final int LOCAL_REGISTRY = 1;
	public static final int REMOTE_REGISTRY = 2;
}
