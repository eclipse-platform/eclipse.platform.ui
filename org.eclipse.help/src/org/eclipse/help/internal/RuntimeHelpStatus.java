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
package org.eclipse.help.internal;

import java.util.*;

import org.eclipse.core.runtime.*;

/**
 * This class is intended to capture all runtime exception happening during the
 * execution of the Help System.
 */

public class RuntimeHelpStatus {
	private static RuntimeHelpStatus inst = null;

	// contains Status objects of errors occurred
	private ArrayList errorList = new ArrayList();

	// contains File names (Strings) of invalid contribution files.
	private ArrayList badFilesList = new ArrayList();

	// contains the error messages (Strings) from the parser
	private ArrayList parserErrorMessagesList = new ArrayList();

	/**
	 * RuntimeHelpStatus constructor comment.
	 */
	public RuntimeHelpStatus() {
		super();
	}
	public synchronized void addParseError(String message,
			String invalidFileName) {
		// add the Exception to the files list only once. These exceptions will
		// be used to
		// produce the list of files with errors.
		if (!badFilesList.contains(invalidFileName))
			badFilesList.add(invalidFileName);

		// now add the message. All parser messages are added
		parserErrorMessagesList.add(message);

	}
	public boolean errorsExist() {
		if (errorList.isEmpty() && parserErrorMessagesList.isEmpty()
				&& badFilesList.isEmpty())
			return false;
		else
			return true;
	}
	public static synchronized RuntimeHelpStatus getInstance() {
		if (inst == null) // create instance
			inst = new RuntimeHelpStatus();
		return inst;
	}
	/**
	 * clears RuntimeHelpStatus object.
	 */
	public void reset() {
		errorList.clear();
		badFilesList.clear();
		parserErrorMessagesList.clear();
	}
	public synchronized String toString() {
		StringBuffer fullText = new StringBuffer();
		if (!errorList.isEmpty()) {
			fullText.append(HelpResources.getString("E006")); //$NON-NLS-1$
			fullText.append("******************** \n"); //$NON-NLS-1$
			for (int i = 0; i < errorList.size(); i++) {
				fullText.append(((Status) (errorList.get(i))).getMessage());
				fullText.append("\n"); //$NON-NLS-1$
			}
		}

		if (fullText.length() > 0)
			fullText.append("\n"); //$NON-NLS-1$

		if (!parserErrorMessagesList.isEmpty()) {
			// display the files that failed to parse
			fullText.append(HelpResources.getString("E007")); //$NON-NLS-1$
			fullText.append("********************  \n"); //$NON-NLS-1$
			for (int i = 0; i < badFilesList.size(); i++) {
				fullText.append(((String) (badFilesList.get(i))));
				fullText.append("\n"); //$NON-NLS-1$
			}

			fullText.append("\n"); //$NON-NLS-1$

			// and the parse error message
			fullText.append(HelpResources.getString("E008")); //$NON-NLS-1$
			fullText.append("********************  \n"); //$NON-NLS-1$
			for (int i = 0; i < parserErrorMessagesList.size(); i++) {
				fullText.append(((String) (parserErrorMessagesList.get(i))));
				fullText.append("\n"); //$NON-NLS-1$
			}
		}

		if (fullText.length() > 0)
			return fullText.toString();
		else
			return ""; //$NON-NLS-1$

	}
}
