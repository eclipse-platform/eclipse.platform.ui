package org.eclipse.update.internal.ui;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.net.MalformedURLException;
import org.eclipse.core.internal.boot.update.*;
import java.net.URL;
import java.util.Date;
import org.eclipse.core.internal.boot.LaunchInfo;
/**
 */
public class Installation {
	private static final String STRING_ID        = "-id";
	private static final String STRING_LIST      = "-list";
	private static final String STRING_LOCATION  = "-location";
	private static final String STRING_SET       = "-set";

	private boolean _bList       = false;
	private boolean _bSet        = false;
	private String  _strLocation = null;
	private String  _strId       = null;
	private int     _iId         = -1;
	private LaunchInfo _launchInfo = null;
/**
 * Installation constructor comment.
 */
public Installation() {
	super();
}
/**
 * 
 */
private String checkCommandLineOptions() {

	// Command
	//--------
	if (_bList == true) {
		if (_bSet == true) {
			return "Invalid option syntax";
		}
	}

	else if (_bSet == true) {
		if (_bList == true) {
			return "Invalid option syntax";
		}
	}

	else {
		return "Command missing";
	}

	// Location
	//---------
	if (_strLocation == null || _strLocation.length() == 0) {
		return "Location missing";
	}

	URL urlLocation = null;

	try {
		urlLocation = new URL(_strLocation);
	}
	catch (MalformedURLException ex) {
		return "Invalid location syntax";
	}

	// Set the location of the installation directory
	//-----------------------------------------------
//	LaunchInfo.startup(urlLocation);
	_launchInfo = LaunchInfo.getCurrent();

	// Set
	//----
	if (_bSet == true) {
		if (_strId == null || _strId.length() == 0) {
			return "Installation identifier number missing";
		}

		try {
			_iId = new Integer(_strId).intValue();
		}
		catch (NumberFormatException ex) {
			return "Invalid identifier syntax";
		}
	}

	return null;
}
/**
 */
public String doList() {

	LaunchInfo launchInfoFormer = null;

	LaunchInfo.History[] histories = _launchInfo.getLaunchInfoHistory();

	if( histories.length == 0 )
		return "No histories found";

	if( histories.length == 1 && histories[0].getLaunchInfoDate() == null )
		return "No histories found";
		
	// Same order as doSet()
	// Most recent to oldest order
	//----------------------------
	int iIndex = 1;

	for (int i = histories.length - 1; i >= 0; --i) {

		Date date = histories[i].getLaunchInfoDate();

		// Older profile
		//--------------
		if (date != null) {
			System.out.println(Integer.toString(iIndex++) + " " + histories[i].getLaunchInfoDate().toString());
		}
	}

	return null;
}
/**
 * 
 */
public String doSet() {

	LaunchInfo.History[] histories = _launchInfo.getLaunchInfoHistory();

	if (_iId < histories.length) {

		int iIndex = 1;

		// Same algorithm as doList()
		// Most recent to oldest order
		//----------------------------
		for (int i = histories.length - 1; i >= 0; --i) {

			Date date = histories[i].getLaunchInfoDate();

			// Older profile
			//--------------
			if (date != null) {
				if (iIndex == _iId) {
					_launchInfo.revertTo(histories[i]);
					System.out.println(date.toString());
					return null;
				}
				else {
					iIndex++;
				}
			}
		}
	}

	return "Identifier not found";
}
/**
 */
public static void main(String[] args) {

	Installation installation = new Installation();

	installation.parseCommandLineOptions(args);
	
	String strError = installation.checkCommandLineOptions();

	if (strError == null) {
		if (installation._bList == true)
			strError = installation.doList();

		else if (installation._bSet == true)
			strError = installation.doSet();
	}

	if (strError != null) {
		System.out.println(strError);
		printUsage();
		System.exit(-1);
	}
}
/**
 */
public void parseCommandLineOptions(String[] straArgs) {

	for (int i = 0; i < straArgs.length; ++i) {

		// Command: List
		//--------------
		if (straArgs[i].equalsIgnoreCase(STRING_LIST) == true) {
			_bList = true;
		}
		
		// Command: Set
		//-------------
		else if (straArgs[i].equalsIgnoreCase(STRING_SET) == true) {
			_bSet = true;
		}

		// Location to install from
		//-------------------------
		else if (straArgs[i].equalsIgnoreCase(STRING_LOCATION) == true && i < straArgs.length - 1) {
			_strLocation = straArgs[++i];
		}

		// Identifier
		//-----------
		else if (straArgs[i].equalsIgnoreCase(STRING_ID) == true && i < straArgs.length - 1) {
			_strId = straArgs[++i];
		}
	}
}
/**
 */
public static void printUsage() {
	System.out.println("");
	System.out.println("-list -location urlPath");
	System.out.println("-set  -location urlPath -id number");
}
}
