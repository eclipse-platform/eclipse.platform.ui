package org.eclipse.update.internal.ui.wizards;

import org.eclipse.jface.wizard.*;
import org.eclipse.update.ui.internal.model.*;
import org.eclipse.update.internal.ui.*;
import java.util.*;

public class InstallWizard extends Wizard {
	private ReviewPage reviewPage;
	private Vector jobs = new Vector();

	/**
	 * Constructor for InstallWizard
	 */
	public InstallWizard() {
		this(null);
	}
	
	public InstallWizard(ChecklistJob job) {
		setDialogSettings(UpdateUIPlugin.getDefault().getDialogSettings());
		setDefaultPageImageDescriptor(UpdateUIPluginImages.DESC_INSTALL_WIZ);
		setForcePreviousAndNextButtons(true);
		if (job!=null)
		   jobs.add(job);
		else
		   createJobs();
	}
	
	private void createJobs() {
		UpdateModel model = UpdateUIPlugin.getDefault().getUpdateModel();
		ChecklistJob [] jobArray = model.getJobs();
		addJobs(jobArray, ChecklistJob.UNINSTALL);
		addJobs(jobArray, ChecklistJob.INSTALL);
	}
	
	private void addJobs(ChecklistJob [] jobArray, int jobType) {
		for (int i=0; i<jobArray.length; i++) {
			ChecklistJob job = jobArray[i];
			if (job.getJobType()==jobType)
			   jobs.add(job);
		}
	}
		
	

	/**
	 * @see Wizard#performFinish()
	 */
	public boolean performFinish() {
		return true;
	}
	
	public void addPages() {
		reviewPage = new ReviewPage(jobs);
		addPage(reviewPage);
	}

}

