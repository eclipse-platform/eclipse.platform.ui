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
		setWindowTitle(Policy.bind("MergeWizard.title"));
		ImageDescriptor mergeImage = CVSUIPlugin.getPlugin().getImageDescriptor(ICVSUIConstants.IMG_WIZBAN_MERGE);
		startPage = new MergeWizardStartPage("startPage", Policy.bind("MergeWizard.start"), mergeImage);
		startPage.setProject(project);
		addPage(startPage);
		endPage = new MergeWizardEndPage("endPage", Policy.bind("MergeWizard.end"), mergeImage, startPage);
		endPage.setProject(project);
		addPage(endPage);
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
