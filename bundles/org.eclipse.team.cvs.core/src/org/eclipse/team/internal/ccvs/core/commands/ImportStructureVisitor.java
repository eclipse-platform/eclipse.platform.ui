package org.eclipse.team.internal.ccvs.core.commands;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.team.internal.ccvs.core.util.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.requests.RequestSender;
import org.eclipse.team.internal.ccvs.core.resources.api.IManagedFile;
import org.eclipse.team.internal.ccvs.core.resources.api.IManagedFolder;
import org.eclipse.team.internal.ccvs.core.util.StringMatcher;

/**
 * The ImportStructureVisitor sends the content of the folder it is
 * used on to the server. It constructs the locations of the resources
 * because the resources do not yet have a remote-location.<br>
 * Up to that it can ignore certain files and decides wether to send
 * a file in binary or text mode due to a specification that is passed 
 * as a "wrapper" argument.
 */
class ImportStructureVisitor extends AbstractStructureVisitor {
	
	private static final String KEYWORD_OPTION = "-k";
	private static final String QUOTE = "'";
	
	private final String mode;
	private final String[] ignores;
	private final String[] wrappers;
	
	private final FileNameMatcher ignoreMatcher;
	private final FileNameMatcher wrapMatcher;
	
	/**
	 * Constructor for ImportStructureVisitor.
	 * @param requestSender
	 * @param mRoot
	 * @param monitor
	 */
	public ImportStructureVisitor(
		RequestSender requestSender,
		IManagedFolder mRoot,
		IProgressMonitor monitor,
		String mode,
		String[] ignores,
		String[] wrappers) {
		super(requestSender, mRoot, monitor);
		
		this.mode = mode;
		this.ignores = ignores;
		ignoreMatcher = new FileNameMatcher(ignores);
		
		this.wrappers = wrappers;
		wrapMatcher = initWrapMatcher(wrappers);
	}
	

	/**
	 * Inits the wrapMatcher, that is responsible to find out
	 * whether a file is to be send as a binary (on an import)
	 * or not.
	 * 
	 * Takes wrappers of this format:
	 *   *.class -k 'o'
	 * 
	 * and inits the FileNameMatcher to give
	 *   -ko back if you call it with match("somename.class")
	 * 
	 * ignores all wrappers, that do not contain -k
	 */
	private FileNameMatcher initWrapMatcher(String[] wrappers) {
		
		FileNameMatcher wrapMatcher;
		
		if (wrappers == null) {
			return null;
		}
		
		wrapMatcher = new FileNameMatcher();
		
		for (int i = 0; i < wrappers.length; i++) {
			
			if (wrappers[i].indexOf(KEYWORD_OPTION) == -1) {
				continue;
			}
			
			StringTokenizer st = new StringTokenizer(wrappers[i]);
			String pattern = st.nextToken();
			String option = st.nextToken();
			// get rid of the quotes
			StringTokenizer quoteSt = 
				new StringTokenizer(st.nextToken(),QUOTE);
			option += quoteSt.nextToken();
			
			wrapMatcher.register(pattern,option);
		}
		
		return wrapMatcher;
	}	
	
	/**
	 * @see IManagedVisitor#visitFile(IManagedFile)
	 */
	public void visitFile(IManagedFile mFile) throws CVSException {
		
		String mode = this.mode;
		
		if (ignoreMatcher != null && ignoreMatcher.match(mFile.getName())) {
			return;
		}
		
		if (mode == null && wrapMatcher != null) {
			mode = wrapMatcher.getMatch(mFile.getName());
		}		
		
		sendFile(mFile,false,mode);
		
	}

	/**
	 * @see IManagedVisitor#visitFolder(IManagedFolder)
	 */
	public void visitFolder(IManagedFolder mFolder) throws CVSException {
		
		if (ignoreMatcher != null && ignoreMatcher.match(mFolder.getName())) {
			return;
		}
		
		sendFolder(mFolder,true,false);
		mFolder.acceptChildren(this);
		
	}

}