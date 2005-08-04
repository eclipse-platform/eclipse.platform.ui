/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui.wizards;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.core.CVSTeamProvider;
import org.eclipse.team.internal.ccvs.core.client.Command.LocalOption;
import org.eclipse.team.internal.ccvs.ui.CVSUIMessages;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;

/**
 * An operation to run the CVS diff operation on a set of resources. The result
 * of the diff is written to a file. If there are no differences found, the
 * user is notified and the output file is not created.
 */
public class GenerateDiffFileOperation implements IRunnableWithProgress {

	private File outputFile;
	private IResource resource;
	private Shell shell;
	private LocalOption[] options;

	GenerateDiffFileOperation(IResource resource, File file, LocalOption[] options, Shell shell) {
		this.resource = resource;
		this.outputFile = file;
		this.shell = shell;
		this.options = options;
	}

	/**
	 * @see IRunnableWithProgress#run(IProgressMonitor)
	 */
	public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
	    
	    final CVSTeamProvider provider = (CVSTeamProvider)RepositoryProvider.getProvider(resource.getProject(), CVSProviderPlugin.getTypeId());
	    monitor.beginTask("", 500); //$NON-NLS-1$
	    monitor.setTaskName(CVSUIMessages.GenerateCVSDiff_working); 
		try {
			if (outputFile != null) {
			    generateDiffToFile(monitor, provider, outputFile);
			} else {
			    generateDiffToClipboard(monitor, provider);
			}
		} catch (TeamException e) {
		    throw new InvocationTargetException(e); 
		} finally {
			monitor.done();
		}
	}
	
    private void generateDiffToFile(IProgressMonitor monitor, CVSTeamProvider provider, File file) throws TeamException {
        
        final FileOutputStream os;
        try {
            os= new FileOutputStream(file);
            try {
                provider.diff(resource, options, new PrintStream(os), new SubProgressMonitor(monitor, 500));
            } finally {
                os.close();
            }
        } catch (FileNotFoundException e) {
            throw new TeamException(CVSUIMessages.GenerateDiffFileOperation_0, e); 
        } catch (IOException e) {
            throw new TeamException(CVSUIMessages.GenerateDiffFileOperation_1, e); 
        }
        
		if (file.length() == 0) {
			outputFile.delete();
			reportEmptyDiff();
		}	
	}

    private void generateDiffToClipboard(IProgressMonitor monitor, CVSTeamProvider provider) throws TeamException {
        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            try {
                provider.diff(resource, options, new PrintStream(os), new SubProgressMonitor(monitor, 500));
            } finally {
                os.close();
            }
        } catch (IOException e) {
            throw new TeamException(CVSUIMessages.GenerateDiffFileOperation_2, e); 
        }
        if (os.size() == 0) {
            reportEmptyDiff();
        } else {
            copyToClipboard(os);
        }
    }
 
    private void copyToClipboard(final ByteArrayOutputStream baos) {
        shell.getDisplay().syncExec(new Runnable() {
        	public void run() {
        		TextTransfer plainTextTransfer = TextTransfer.getInstance();
        		Clipboard clipboard = new Clipboard(shell.getDisplay());		
        		clipboard.setContents(
        			new String[]{baos.toString()}, 
        			new Transfer[]{plainTextTransfer});	
        		clipboard.dispose();
        	}
        });
    }

    private void reportEmptyDiff() {
        CVSUIPlugin.openDialog(shell, new CVSUIPlugin.IOpenableInShell() {
        	public void open(Shell shell) {
        		MessageDialog.openInformation(
        			shell,
        			CVSUIMessages.GenerateCVSDiff_noDiffsFoundTitle, 
        			CVSUIMessages.GenerateCVSDiff_noDiffsFoundMsg); 
        	}
        }, CVSUIPlugin.PERFORM_SYNC_EXEC);
    }
}
