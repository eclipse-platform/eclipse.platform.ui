package org.eclipse.team.internal.ccvs.core.response.custom;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.util.List;

import org.eclipse.team.internal.ccvs.core.resources.RemoteFile;

public class LogListener implements ILogListener {

	private List entries;
	private RemoteFile file;
	/**
	 * Constructor for LogListener.
	 */
	public LogListener(RemoteFile file, List entries) {
		this.entries = entries;
		this.file = file;
	}

	/**
	 * @see ILogListener#log(String, String, String, String, String, CVSTag[])
	 */
	public void log(
		String revision,
		String author,
		String date,
		String comment,
		String state,
		CVSTag[] tags) {
			
		entries.add(new LogEntry(file, revision, author, date, comment, state, tags));
	}

}

