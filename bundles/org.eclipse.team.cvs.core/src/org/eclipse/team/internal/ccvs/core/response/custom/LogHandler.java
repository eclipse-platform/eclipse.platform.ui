package org.eclipse.team.internal.ccvs.core.response.custom;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.connection.Connection;
import org.eclipse.team.internal.ccvs.core.resources.CVSEntryLineTag;
import org.eclipse.team.internal.ccvs.core.resources.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.resources.RemoteFile;
import org.eclipse.team.internal.ccvs.core.response.ResponseHandler;
import org.eclipse.team.internal.ccvs.core.util.Assert;

/**
 * Handles a valid request from the server.
 */
public class LogHandler extends ResponseHandler {
	
	public static final String NAME = "M";

	private ILogListener logListener;
	private List tagNames = new ArrayList(5);
	private List tagRevisions = new ArrayList(5);

	public LogHandler(ILogListener logListener) {
		this.logListener = logListener;
		Assert.isNotNull(logListener);
	}
	
	public LogHandler(RemoteFile file, List entries) {
		this(new LogListener(file, entries));
	}
	
	public String getLine(Connection context) throws CVSException {
		String line = context.readLine();
		if (line.startsWith(getName() + " "))
			return line.substring(2);
		else
			return line;
	}
	
	public String getName() {
		return NAME;
	}

	/** branch tags have odd number of segments or have
	 *  an even number with a zero as the second last segment
	 *  e.g: 1.1.1, 1.26.0.2 are branch revision numbers */
	protected boolean isBranchTag(String tagName) {
		int numberOfSegments = 0;
		boolean isBranch = false;
		for (int i = 0; i < tagName.length(); i++) {
			if (tagName.charAt(i) == '.')
				numberOfSegments++;
		}
		isBranch = (numberOfSegments % 2) == 0;
		if (!isBranch && tagName.lastIndexOf('0') != -1)
			isBranch = true;
		return isBranch;
	}

	public void handle(
		Connection context,
		PrintStream messageOutput,
		ICVSFolder mRoot,
		IProgressMonitor monitor)
			throws CVSException {
				
		String line = null;

		// Fields we will find in the log for a file
		// keys = String (tag name), values = String (tag revision number) */
		String creationDate;
		String author;
		String comment;
		String revision;
		String state;

		// get the line
		line = context.readLine();

		if (line.startsWith("symbolic names:")) {
			line = getLine(context);
			while (!line.startsWith("keyword substitution:")) {
				int firstColon = line.indexOf(':');
				String tagName = line.substring(1, firstColon);
				String tagRevision = line.substring(firstColon + 2);
				tagNames.add(tagName);
				tagRevisions.add(tagRevision);
				line = getLine(context);
			};
		}

		// next lines will have "M " prefixed to each line, this is because the
		// response context does not strip these. We loop so we must remove them.
		if (line.startsWith("revision ")) {

			boolean done = false;

			// get the revision number 
			revision = line.substring(9);

			// read next line, which looks like this:
			// date: 2000/06/19 04:56:21;  author: somebody;  state: Exp;  lines: +114 -45
			line = getLine(context);

			// get the creation date
			int endOfDateIndex = line.indexOf(';', 6);
			creationDate = line.substring(6, endOfDateIndex) + " GMT";

			// get the author name
			int endOfAuthorIndex = line.indexOf(';', endOfDateIndex + 1);
			author = line.substring(endOfDateIndex + 11, endOfAuthorIndex);

			// get the file state (because this revision might be "dead")
			state =
				line.substring(endOfAuthorIndex + 10, line.indexOf(';', endOfAuthorIndex + 1));

			// read comment
			// skip next line (info about branches) if it exists, if not then it is a comment line.
			line = getLine(context);
			if (line.startsWith("branches:"))
				line = getLine(context);
			comment = "";
			while (line != null) {
				if (line
					.equals("=============================================================================")
					|| line.equals("----------------------------")) {
					done = true;
					break;
				}
				if (!comment.equals(""))
					comment += "\n";
				comment += line;
				line = getLine(context);
			}

			// we are only interested in tag names for this revision, remove all others.
			List thisRevisionTags = new ArrayList(3);
			for (int i = 0; i < tagNames.size(); i++) {
				String tagName = (String) tagNames.get(i);
				String tagRev = (String) tagRevisions.get(i);
				// If this is a branch tag then only include this tag with the revision
				// that is the root of this branch (e.g. 1.1 is root of branch 1.1.2).
				boolean isBranch = isBranchTag(tagRev);
				if (isBranch) {
					int zeroIndex = tagRev.lastIndexOf('0');
					int lastDot = -1;
					if (zeroIndex != -1)
						lastDot = zeroIndex - 1;
					else
						lastDot = tagRev.lastIndexOf('.');
					tagRev = tagRev.substring(0, lastDot);
				}
				if (tagRev.equals(revision)) {
					int type = isBranch ? CVSTag.BRANCH : CVSTag.VERSION;
					thisRevisionTags.add(new CVSTag(tagName, type));
				}
			}
			logListener.log(
				revision,
				author,
				creationDate,
				comment,
				state,
				(CVSTag[]) thisRevisionTags.toArray(new CVSTag[0]));
		}

		return;
	}
}