/*******************************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial implementation
 ******************************************************************************/
package org.eclipse.team.internal.ccvs.core.syncinfo;

import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.Policy;
import org.eclipse.team.internal.ccvs.core.util.Util;

/**
 * This class represents the information in the CVS/Baserev file
 */
public class BaserevInfo {
	private static final String BASEREV_PREFIX = "B"; //$NON-NLS-1$
	
	private String name;
	private String revision;
	
	public BaserevInfo(String entryLine) throws CVSException {
		setEntryLine(entryLine);
	}

	public BaserevInfo(String name, String revision) {
		this.name = name;
		this.revision = revision;
	}
	/**
	 * Return the entry line as it appears in the CVS/Baserev file
	 * @return String
	 */
	public String getEntryLine() {
		StringBuffer result = new StringBuffer();
		result.append(BASEREV_PREFIX);
		result.append(name);
		result.append(ResourceSyncInfo.SEPARATOR);
		result.append(revision);
		return result.toString();
	}	
	private void setEntryLine(String entryLine) throws CVSException {
		if(entryLine.startsWith(BASEREV_PREFIX)) {
			entryLine = entryLine.substring(1);
		}
		String[] strings = Util.parseIntoSubstrings(entryLine, ResourceSyncInfo.SEPARATOR);
		if(strings.length != 2) {
			throw new CVSException(Policy.bind("BaseRevInfo.malformedEntryLine", entryLine)); //$NON-NLS-1$
		}

		name = strings[0];

		if(name.length()==0) {
			throw new CVSException(Policy.bind("BaseRevInfo.malformedEntryLine", entryLine)); //$NON-NLS-1$
		}

		revision = strings[1];

		if(revision.length()==0) {
			throw new CVSException(Policy.bind("BaseRevInfo.malformedEntryLine", entryLine)); //$NON-NLS-1$
		}
	}
	/**
	 * Returns the name.
	 * @return String
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the revision.
	 * @return String
	 */
	public String getRevision() {
		return revision;
	}

}
