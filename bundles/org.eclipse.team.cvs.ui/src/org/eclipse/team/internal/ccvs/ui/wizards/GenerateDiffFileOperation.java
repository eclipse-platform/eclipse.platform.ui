package org.eclipse.team.internal.ccvs.ui.wizards;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.ccvs.core.CVSTeamProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.TeamPlugin;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.Policy;

/**
 * An operation to run the CVS diff operation on a set of resources. The result
 * of the diff is written to a file. If there are no differences found, the
 * user is notified and the output file is not created.
 */
public class GenerateDiffFileOperation implements IRunnableWithProgress {

	private IFile outputFile;
	private IResource[] resources;
	private Shell shell;

	GenerateDiffFileOperation(IResource[] resources, IFile file, Shell shell) {
		this.resources = resources;
		this.outputFile = file;
		this.shell = shell;
	}

	/**
	 * @see IRunnableWithProgress#run(IProgressMonitor)
	 */
	public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		MultiStatus result = new MultiStatus(CVSUIPlugin.ID, 1, Policy.bind("GenerateCVSDiff.error"), null);
		try {
			if (resources == null || outputFile == null)
				return;

			monitor.beginTask("", resources.length * 500);
			monitor.setTaskName(
				Policy.bind("GenerateCVSDiff.working"));

			ByteArrayOutputStream os = new ByteArrayOutputStream();

			for (int i = 0; i < resources.length; i++) {
				// NOTE: We should group resources by provider!
				IResource resource = resources[i];
				CVSTeamProvider provider = (CVSTeamProvider)TeamPlugin.getManager().getProvider(resource);
				provider.diff(new IResource[] {resource}, IResource.DEPTH_INFINITE, new PrintStream(os), new SubProgressMonitor(monitor, 500));
			}

			if (os.size() == 0) {
				//check for empty diff and report
				MessageDialog.openInformation(
					shell,
					Policy.bind("GenerateCVSDiff.noDiffsFoundTitle"),
					Policy.bind("GenerateCVSDiff.noDiffsFoundMsg"));
			} else {
				// only create the file if necessary
				if(outputFile.exists()) {
					outputFile.delete(true, monitor);
				}
				outputFile.create(new ByteArrayInputStream(os.toByteArray()), true, monitor);
			}
		} catch (CoreException e) {
			throw new InvocationTargetException(e);
		} catch (TeamException e) {
			throw new InvocationTargetException(e);
		} finally {
			monitor.done();
		}
	}
}