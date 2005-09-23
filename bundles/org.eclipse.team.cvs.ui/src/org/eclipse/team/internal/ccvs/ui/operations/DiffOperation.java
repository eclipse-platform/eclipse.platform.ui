/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui.operations;

import java.io.*;
import java.util.HashSet;
import java.util.Iterator;

import org.eclipse.core.internal.resources.mapping.ResourceMapping;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.internal.ccvs.core.*;
import org.eclipse.team.internal.ccvs.core.client.*;
import org.eclipse.team.internal.ccvs.core.client.Command.LocalOption;
import org.eclipse.team.internal.ccvs.core.client.listeners.DiffListener;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.ui.*;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.ui.IWorkbenchPart;

public abstract class DiffOperation extends SingleCommandOperation {

	private static final int UNIFIED_FORMAT = 0;
	private static final int CONTEXT_FORMAT = 1;
	private static final int STANDARD_FORMAT = 2;
	
	protected boolean isMultiPatch;
	protected boolean includeFullPathInformation;
	protected PrintStream stream;
	protected IPath patchRoot;
	protected boolean patchHasContents;
	protected boolean patchHasNewFiles;
	
	public DiffOperation(IWorkbenchPart part, ResourceMapping[] mappings, LocalOption[] options, boolean isMultiPatch, boolean includeFullPathInformation, IPath patchRoot) {
		super(part, mappings, options);
		this.isMultiPatch = isMultiPatch;
		this.includeFullPathInformation=includeFullPathInformation;
		this.patchRoot=patchRoot;
		this.patchHasContents=false;
		this.patchHasNewFiles=false;
	}
	
	public void execute(IProgressMonitor monitor) throws CVSException, InterruptedException {
		try {
			stream = openStream();
			if (isMultiPatch){
				//stream.println(CompareUI.getWorkspacePatchHeader());
				stream.println("### Eclipse Workspace Patch 1.0");
			}
			super.execute(monitor);
		} finally {
			if (stream != null) {
				stream.close();
			}
		}
	}
 
	/**
	 * Open and return a stream for the diff output.
	 * @return a stream for the diff output
	 */
	protected abstract PrintStream openStream() throws CVSException;

	protected void execute(CVSTeamProvider provider, IResource[] resources, boolean recurse, IProgressMonitor monitor) throws CVSException, InterruptedException {
		
		//add this project to the total projects encountered
		final HashSet newFiles = new HashSet(); //array of ICVSResource - need HashSet to guard for duplicate entries
		final HashSet existingFiles = new HashSet(); //array of IResource - need HashSet to guard for duplicate entries
		
		monitor.beginTask(null,100);
		final IProgressMonitor subMonitor = Policy.subMonitorFor(monitor,10);
		for (int i = 0; i < resources.length; i++) {
			IResource resource = resources[i];
			ICVSResource cvsResource = CVSWorkspaceRoot.getCVSResourceFor(resource);
			cvsResource.accept(new ICVSResourceVisitor() {
				public void visitFile(ICVSFile file) throws CVSException {
					if (!(file.isIgnored()))  {
						if (!file.isManaged() || file.getSyncInfo().isAdded() ){
							//this is a new file
							if (file.exists())
								newFiles.add(file);
						}else if (file.isModified(subMonitor)){
							existingFiles.add(file.getIResource());
						}
					}
				}
				
				public void visitFolder(ICVSFolder folder) throws CVSException {
					// Even if we are not supposed to recurse we still need to go into
					// the root directory.
					if (!folder.exists() || folder.isIgnored() )  {
						return;
					} 
					
					folder.acceptChildren(this);
					
				}
			}, recurse);
		}
		subMonitor.done();
		
		//Check options 
		//Append our diff output to the server diff output.
		// Our diff output includes new files and new files in new directories.
		int format = STANDARD_FORMAT;
	
		LocalOption[] localoptions = getLocalOptions(recurse);
		for (int i = 0; i < localoptions.length; i++)  {
			LocalOption option = localoptions[i];
			if (option.equals(Diff.UNIFIED_FORMAT) ||
				isMultiPatch)  {
				format = UNIFIED_FORMAT;
			} else if (option.equals(Diff.CONTEXT_FORMAT))  {
				format = CONTEXT_FORMAT;
			} 
		}
		
		boolean haveAddedProjectHeader=false;
		
		if (!existingFiles.isEmpty()){
			if (isMultiPatch && !haveAddedProjectHeader){
				haveAddedProjectHeader=true;
				IProject project=resources[0].getProject();
				//stream.println(CompareUI.getWorkspacePatchProjectHeader(project));
				stream.println("#P " + project.getName());
			}
			try{
				super.execute(provider, (IResource[]) existingFiles.toArray(new IResource[existingFiles.size()]), recurse, Policy.subMonitorFor(monitor, 90));
			} catch (CVSException ex){}
		}
		
		
		
		
		if (!newFiles.isEmpty() && Diff.INCLUDE_NEWFILES.isElementOf(localoptions)){
			//Set new file to flag to let us know that we have added something to the current patch
			patchHasNewFiles=true;
			
			if (isMultiPatch &&!haveAddedProjectHeader){
				haveAddedProjectHeader=true;
				IProject project=resources[0].getProject();
				//stream.println(CompareUI.getWorkspacePatchProjectHeader(project));
				stream.println("#P " + project.getName());
			}
			
			for (Iterator iter = newFiles.iterator(); iter.hasNext();) {
				ICVSFile cvsFile = (ICVSFile) iter.next();
				addFileToDiff(CVSWorkspaceRoot.getCVSFolderFor(cvsFile.getIResource().getProject()), cvsFile,stream,format);				
			}
		}
		
		monitor.done();
	}
	
	protected IStatus executeCommand(Session session, CVSTeamProvider provider, ICVSResource[] resources, boolean recurse, IProgressMonitor monitor) throws CVSException, InterruptedException {
		
		DiffListener diffListener = new DiffListener(stream);
		
		Command.DIFF.execute(session,
				Command.NO_GLOBAL_OPTIONS,
				getLocalOptions(recurse),
				resources,
				diffListener,
				monitor);
		
		//Once any run of the Diff commands reports that it has written something to the stream, the patch 
		//in its entirety is considered non-empty - until then keep trying to set the flag.
		if (!patchHasContents)
			patchHasContents = diffListener.wroteToStream();
		//ignore Command.DIFF.execute return value, just return OK
		return OK;
	}

	protected String getTaskName(CVSTeamProvider provider) {
		return NLS.bind(CVSUIMessages.DiffOperation_0, new String[]{provider.getProject().getName()});
	}

	protected String getTaskName() {
		return CVSUIMessages.DiffOperation_1;
	}
	
	private void addFileToDiff(ICVSFolder cmdRoot, ICVSFile file, PrintStream printStream, int format) throws CVSException {
		
		String nullFilePrefix = ""; //$NON-NLS-1$
		String newFilePrefix = ""; //$NON-NLS-1$
		String positionInfo = ""; //$NON-NLS-1$
		String linePrefix = ""; //$NON-NLS-1$
		
		String pathString=""; //$NON-NLS-1$

		
		//get the path string for this file
	    pathString= file.getRelativePath(cmdRoot); //$NON-NLS-1$
	
		int lines = 0;
		BufferedReader fileReader = new BufferedReader(new InputStreamReader(file.getContents()));
		try {
			while (fileReader.readLine() != null)  {
				lines++;
			}
		} catch (IOException e) {
			throw CVSException.wrapException(file.getIResource(), NLS.bind(CVSMessages.CVSTeamProvider_errorAddingFileToDiff, new String[] { pathString }), e); 
		} finally {
			try {
				fileReader.close();
			} catch (IOException e1) {
				//ignore
			}
		}
			
		// Ignore empty files
		if (lines == 0)
			return;
		
		switch (format) {
		case UNIFIED_FORMAT:
			nullFilePrefix = "--- ";	//$NON-NLS-1$
			newFilePrefix = "+++ "; 	//$NON-NLS-1$
			positionInfo = "@@ -0,0 +1," + lines + " @@" ;	//$NON-NLS-1$ //$NON-NLS-2$
			linePrefix = "+"; //$NON-NLS-1$
			break;
			
		case CONTEXT_FORMAT :
			nullFilePrefix = "*** ";	//$NON-NLS-1$
			newFilePrefix = "--- ";		//$NON-NLS-1$
			positionInfo = "--- 1," + lines + " ----";	//$NON-NLS-1$ //$NON-NLS-2$
			linePrefix = "+ ";	//$NON-NLS-1$
			break;
			
		default :
			positionInfo = "0a1," + lines;	//$NON-NLS-1$
		linePrefix = "> ";	//$NON-NLS-1$
					break;
		}
		
		fileReader = new BufferedReader(new InputStreamReader(file.getContents()));
		try {
				
			printStream.println("Index: " + pathString);		//$NON-NLS-1$
			printStream.println("===================================================================");	//$NON-NLS-1$
			printStream.println("RCS file: " + pathString);	//$NON-NLS-1$
			printStream.println("diff -N " + pathString);	//$NON-NLS-1$
			
			
			if (format != STANDARD_FORMAT)  {
				printStream.println(nullFilePrefix + "/dev/null	1 Jan 1970 00:00:00 -0000");	//$NON-NLS-1$
				// Technically this date should be the local file date but nobody really cares.
				printStream.println(newFilePrefix + pathString + "	1 Jan 1970 00:00:00 -0000");	//$NON-NLS-1$
			}
			
			if (format == CONTEXT_FORMAT)  {
				printStream.println("***************");	//$NON-NLS-1$
				printStream.println("*** 0 ****");		//$NON-NLS-1$
			}
			
			printStream.println(positionInfo);
			
			for (int i = 0; i < lines; i++)  {
				printStream.print(linePrefix);
				printStream.println(fileReader.readLine());
			}
		} catch (IOException e) {
			throw CVSException.wrapException(file.getIResource(), NLS.bind(CVSMessages.CVSTeamProvider_errorAddingFileToDiff, new String[] { pathString }), e); 
		} finally  {
			try {
				fileReader.close();
			} catch (IOException e1) {
			}
		}
	}

	public void setStream(PrintStream stream) {
		this.stream = stream;
	}

	protected void reportEmptyDiff() {
        CVSUIPlugin.openDialog(getShell(), new CVSUIPlugin.IOpenableInShell() {
        	public void open(Shell shell) {
        		MessageDialog.openInformation(
        			shell,
        			CVSUIMessages.GenerateCVSDiff_noDiffsFoundTitle, 
        			CVSUIMessages.GenerateCVSDiff_noDiffsFoundMsg); 
        	}
        }, CVSUIPlugin.PERFORM_SYNC_EXEC);
	 }
	
	protected ICVSFolder getLocalRoot(CVSTeamProvider provider) throws CVSException {
		if (!isMultiPatch &&
			!includeFullPathInformation){
			//Check to see if the selected patchRoot has enough segments to consider it a folder/resource
			//if not just get the project

			IResource patchFolder = null;
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			if (patchRoot.segmentCount() > 1){
				patchFolder = root.getFolder(patchRoot);
			} else {
				patchFolder = root.getProject(patchRoot.toString());
			}
		
			ICVSResource cvsResource = CVSWorkspaceRoot.getCVSResourceFor(patchFolder);
			if (!cvsResource.isFolder()) {
				cvsResource = cvsResource.getParent();
			}
			return (ICVSFolder) cvsResource;
		}
		
		return super.getLocalRoot(provider);
	}

}
