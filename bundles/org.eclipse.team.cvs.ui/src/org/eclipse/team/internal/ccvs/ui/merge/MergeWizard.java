package org.eclipse.team.internal.ccvs.ui.merge;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.lang.reflect.InvocationTargetException;

import org.eclipse.compare.CompareUI;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.team.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.ICVSUIConstants;
import org.eclipse.team.internal.ccvs.ui.Policy;

public class MergeWizard extends Wizard {
	MergeWizardStartPage startPage;
	MergeWizardEndPage endPage;
	IProject project;

	public void addPages() {
		// Provide a progress monitor to indicate what is going on
		try {
			new ProgressMonitorDialog(getShell()).run(false, false, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					monitor.beginTask(Policy.bind("MergeWizard.preparing"), 100);
					setWindowTitle(Policy.bind("MergeWizard.title"));
					ImageDescriptor mergeImage = CVSUIPlugin.getPlugin().getImageDescriptor(ICVSUIConstants.IMG_WIZBAN_MERGE);
					startPage = new MergeWizardStartPage("startPage", Policy.bind("MergeWizard.start"), mergeImage);
					monitor.subTask(Policy.bind("MergeWizard.preparingStart"));
					startPage.setProject(project);
					monitor.worked(50);
					addPage(startPage);
					endPage = new MergeWizardEndPage("endPage", Policy.bind("MergeWizard.end"), mergeImage);
					monitor.subTask(Policy.bind("MergeWizard.preparingEnd"));
					endPage.setProject(project);
					addPage(endPage);
					monitor.done();
				}
			});
		} catch (InvocationTargetException e) {
			CVSUIPlugin.log(new Status(IStatus.ERROR, CVSUIPlugin.ID, 0, Policy.bind("internal"), e.getTargetException()));
		} catch (InterruptedException e) {
			// Ignore
		}
	}

	/*
	 * @see IWizard#performFinish()
	 */
	public boolean performFinish() {
		CVSTag startTag = startPage.getTag();
		CVSTag endTag = endPage.getTag();
		CompareUI.openCompareEditor(new MergeEditorInput(project, startTag, endTag));
		return true;
	}
	public void setProject(IProject project) {
		this.project = project;
	}
}
