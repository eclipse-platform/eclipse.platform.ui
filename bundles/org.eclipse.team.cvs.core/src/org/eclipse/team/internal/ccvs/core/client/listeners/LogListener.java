package org.eclipse.team.internal.ccvs.core.client.listeners;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.team.ccvs.core.*;
import org.eclipse.team.ccvs.core.CVSTag;
import org.eclipse.team.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.DateUtil;
import org.eclipse.team.internal.ccvs.core.resources.RemoteFile;

public class LogListener implements ICommandOutputListener {
	private List entries;
	private RemoteFile file;
	
	// state
	private final int BEGIN = 0, SYMBOLIC_NAMES = 1, REVISION = 2, COMMENT = 3, DONE = 4;
	private List tagNames = new ArrayList(5);
	private List tagRevisions = new ArrayList(5);
	private int state = BEGIN;  // current state
	private String creationDate;
	private String author;
	private String revision;    // revision number
	private String fileState;   //
	private StringBuffer comment; // comment

	public LogListener(RemoteFile file, List entries) {
		this.file = file;
		this.entries = entries;
	}

	public IStatus messageLine(String line, ICVSFolder commandRoot,
		IProgressMonitor monitor) {
		// Fields we will find in the log for a file
		// keys = String (tag name), values = String (tag revision number) */
		switch (state) {
			case BEGIN:
				if (line.startsWith("symbolic names:")) { //$NON-NLS-1$
					state = SYMBOLIC_NAMES;
				} else if (line.startsWith("revision ")) { //$NON-NLS-1$
					revision = line.substring(9);
					state = REVISION;
				}
				break;
			case SYMBOLIC_NAMES:
				if (line.startsWith("keyword substitution:")) { //$NON-NLS-1$
					state = BEGIN;
				} else {
					int firstColon = line.indexOf(':');
					String tagName = line.substring(1, firstColon);
					String tagRevision = line.substring(firstColon + 2);
					tagNames.add(tagName);
					tagRevisions.add(tagRevision);
				}
				break;
			case REVISION:
				// date: 2000/06/19 04:56:21;  author: somebody;  state: Exp;  lines: +114 -45
				// get the creation date
				int endOfDateIndex = line.indexOf(';', 6);
				creationDate = line.substring(6, endOfDateIndex) + " GMT"; //$NON-NLS-1$
	
				// get the author name
				int endOfAuthorIndex = line.indexOf(';', endOfDateIndex + 1);
				author = line.substring(endOfDateIndex + 11, endOfAuthorIndex);
	
				// get the file state (because this revision might be "dead")
				fileState = line.substring(endOfAuthorIndex + 10, line.indexOf(';', endOfAuthorIndex + 1));
				comment = new StringBuffer();
				state = COMMENT;
				break;
			case COMMENT:
				// skip next line (info about branches) if it exists, if not then it is a comment line.
				if (line.startsWith("branches:")) break; //$NON-NLS-1$
				if (line.equals("=============================================================================") //$NON-NLS-1$
					|| line.equals("----------------------------")) { //$NON-NLS-1$
					state = DONE;
					break;
				}
				if (comment.length() != 0) comment.append('\n');
				comment.append(line);
				break;
		}
		if (state == DONE) {
			// we are only interested in tag names for this revision, remove all others.
			List thisRevisionTags = new ArrayList(3);
			for (int i = 0; i < tagNames.size(); i++) {
				String tagName = (String) tagNames.get(i);
				String tagRevision = (String) tagRevisions.get(i);
				// If this is a branch tag then only include this tag with the revision
				// that is the root of this branch (e.g. 1.1 is root of branch 1.1.2).
				boolean isBranch = isBranchTag(tagRevision);
				if (isBranch) {
					int lastDot = tagRevision.lastIndexOf('.');
					if (tagRevision.charAt(lastDot - 1) == '0' && tagRevision.charAt(lastDot - 2) == '.') {
						lastDot = lastDot - 2;
					}
					tagRevision = tagRevision.substring(0, lastDot);
				}
				if (tagRevision.equals(revision)) {
					int type = isBranch ? CVSTag.BRANCH : CVSTag.VERSION;
					thisRevisionTags.add(new CVSTag(tagName, type));
				}
			}
			Date date = DateUtil.convertFromLogTime(creationDate);
			LogEntry entry = new LogEntry(file, revision, author, date,
				comment.toString(), fileState, (CVSTag[]) thisRevisionTags.toArray(new CVSTag[0]));
			entries.add(entry);
			state = BEGIN;
			// XXX should we reset the tagNames and tagRevisions stuff?
		}
		return OK;
	}

	public IStatus errorLine(String line, ICVSFolder commandRoot,
		IProgressMonitor monitor) {
		return OK;
	}
	
	/** branch tags have odd number of segments or have
	 *  an even number with a zero as the second last segment
	 *  e.g: 1.1.1, 1.26.0.2 are branch revision numbers */
	protected boolean isBranchTag(String tagName) {
		// First check if we have an odd number of segments (i.e. even number of dots)
		int numberOfDots = 0;
		int lastDot = 0;
		for (int i = 0; i < tagName.length(); i++) {
			if (tagName.charAt(i) == '.') {
				numberOfDots++;
				lastDot = i;
			}
		}
		if ((numberOfDots % 2) == 0) return true;
		if (numberOfDots == 1) return false;
		
		// If not, check if the second lat segment is a zero
		if (tagName.charAt(lastDot - 1) == '0' && tagName.charAt(lastDot - 2) == '.') return true;
		return false;
	}
}
