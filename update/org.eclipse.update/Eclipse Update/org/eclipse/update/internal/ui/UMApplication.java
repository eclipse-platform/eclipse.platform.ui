package org.eclipse.update.internal.ui;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/* 
 * Update Manager Install Application
 *
 * Command to run from command line:
 *
 *.\jre\bin\java -verify -cp startup.jar org.eclipse.core.launcher.UIMain -application org.eclipse.update.internal.ui.updateManager -install -url http://klicnik.torolab.ibm.com/eclipse/site/ -id org.eclipse.sdk %*
 *
 *
 * Arguments are:
 *   -install   Install command
 *   -url [url] Location from which to install from
 *   -id  [id]  Identifier of the manifest to install
 *
 * Sample:
 *   -install -url file:/c:/temp/updatesite/ -id org.eclipse.sdk
 */
import java.util.StringTokenizer;
import java.util.Vector;

public class UMApplication extends Main {
	private static final String DEFAULT_APPLICATION = "org.eclipse.update.internal.ui.updateManager";
public UMApplication() {
	super();
	application = DEFAULT_APPLICATION;
}
public static void main(String[] args) {

	int iReturnCode = 0;

	try {
		new UMApplication().run(args);
	}

	catch (Exception e) {
	}

	System.exit(0);
}
}
