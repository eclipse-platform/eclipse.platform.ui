/*
 * Licensed Materials - Property of IBM
 * (c) Copyright IBM Corporation 2000, 2003.
 * All Rights Reserved. 
 * Note to U.S. Government Users Restricted Rights:  Use, duplication or disclosure restricted by GSA ADP  schedule Contract with IBM Corp. 
*/

package org.eclipse.ui.internal.cheatsheets.views;

import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.cheatsheets.ICheatSheetManager;

public class CheatSheetEvent extends Event {
	int fCheatsheetEventType;
	String fCheatsheetID;
	ICheatSheetManager csm;
}
