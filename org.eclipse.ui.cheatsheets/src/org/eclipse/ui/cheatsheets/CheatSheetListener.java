/*
 * Licensed Materials - Property of IBM
 * (c) Copyright IBM Corporation 2000, 2003.
 * All Rights Reserved. 
 * Note to U.S. Government Users Restricted Rights:  Use, duplication or disclosure restricted by GSA ADP  schedule Contract with IBM Corp. 
*/

package org.eclipse.ui.cheatsheets;

/**
 * <p>Listener classes must subclass this class and be registered in the plugin.xml file of the implementing plugin
 * using the cheatsheetContent extension.  Also see <code>ICheatSheetLifeCycleEvent</code>.</p>
 */
public class CheatSheetListener {

	 public void cheatSheetEvent(ICheatSheetEvent e) {}

}
