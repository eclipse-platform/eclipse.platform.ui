package org.eclipse.team.internal.ccvs.core.response.custom;
import org.eclipse.team.ccvs.core.CVSTag;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
public interface ILogListener {			
	/**
	 * file status
	 */
	public void log(String revision, String author, String date, String comment, String state, CVSTag[] tags);

}
