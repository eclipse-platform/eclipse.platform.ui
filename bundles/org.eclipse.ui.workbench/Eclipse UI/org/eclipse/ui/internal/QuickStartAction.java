package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.text.Collator;
import java.util.*;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.*;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.*;
import org.eclipse.ui.actions.PartEventAction;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.dialogs.WelcomeEditorInput;

/**
 * Launch the quick start action.
 */
public class QuickStartAction extends PartEventAction {
	private static final String EDITOR_ID = "org.eclipse.ui.internal.dialogs.WelcomeEditor";  //$NON-NLS-1$
	
	private IWorkbench workbench;
	
/**
 *	Create an instance of this class.
 *  <p>
 * 	This consructor added to support calling the action from the welcome page
 *  </p>
 */
public QuickStartAction() {
	this(PlatformUI.getWorkbench());
}
/**
 *	Create an instance of this class
 */
public QuickStartAction(IWorkbench aWorkbench) {
	super(WorkbenchMessages.getString("QuickStart.text")); //$NON-NLS-1$
	setToolTipText(WorkbenchMessages.getString("QuickStart.toolTip")); //$NON-NLS-1$
	WorkbenchHelp.setHelp(this, IHelpContextIds.QUICK_START_ACTION);
	this.workbench = aWorkbench;
}
/**
 *	The user has invoked this action
 */
public void run() {
	// Ask the user to select a feature
	AboutInfo[] features = ((Workbench)workbench).getConfigurationInfo().getFeaturesInfo();
	ArrayList welcomeFeatures = new ArrayList();
	for (int i = 0; i < features.length; i++) {
		if (features[i].getWelcomePageURL() != null) 
			welcomeFeatures.add(features[i]);
	}
	
	IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
	if (window == null)
		return;
	
	Shell shell = window.getShell();
	
	if (welcomeFeatures.size() == 0) {
		MessageDialog.openInformation(
			shell, 
			WorkbenchMessages.getString("QuickStartMessageDialog.title"), //$NON-NLS-1$
			WorkbenchMessages.getString("QuickStartMessageDialog.message")); //$NON-NLS-1$
		return;
	}			
	
	features = new AboutInfo[welcomeFeatures.size()];
	welcomeFeatures.toArray(features);
	
	// Sort ascending
	Arrays.sort(features, new Comparator() {
		Collator coll = Collator.getInstance(Locale.getDefault());
			public int compare(Object a, Object b) {
				AboutInfo i1, i2;
				String name1, name2;
				i1 = (AboutInfo)a;
				name1 = i1.getFeatureLabel();
				i2 = (AboutInfo)b;
				name2 = i2.getFeatureLabel();
				if (name1 == null)
					name1 = ""; //$NON-NLS-1$
				if (name2 == null)
					name2 = ""; //$NON-NLS-1$
				return coll.compare(name1, name2);
			}
		});

	// Find primary feature
	AboutInfo primaryFeature = ((Workbench)workbench).getConfigurationInfo().getAboutInfo();
	int index = -1;
	if (primaryFeature != null) {
		for (int i = 0; i < features.length; i++) {
			if (features[i].getFeatureId().equals(primaryFeature.getFeatureId())) {
				index = i;
				break;
			}
		}
	}	

	WelcomePageSelectionDialog d = 
		new WelcomePageSelectionDialog(
			shell,
			features,
			index);
	if(d.open() != Dialog.OK || d.getResult().length != 1)
		return;
		
	AboutInfo feature = (AboutInfo)d.getResult()[0];
	
	IWorkbenchPage page = null;

	// See if the feature wants a specific perspective
	String perspectiveId = feature.getWelcomePerspective();

	if (perspectiveId == null) {
		// Just use the current perspective unless one is not open 
		// in which case use the default
		page = window.getActivePage();
	
		if (page == null || page.getPerspective() == null) {
			perspectiveId = WorkbenchPlugin.getDefault().getPerspectiveRegistry().getDefaultPerspective();		
		}
	}

	if (perspectiveId != null) { 			
		try {
			page =
				(WorkbenchPage) workbench.showPerspective(
					perspectiveId,
					window);
		} catch (WorkbenchException e) {
			return;
		}
	} 		
	
	page.setEditorAreaVisible(true);

	// create input
	WelcomeEditorInput input = new WelcomeEditorInput(feature);

	// see if we already have a welcome editor
	IEditorPart editor = page.findEditor(input);
	if(editor != null) {
		page.activate(editor);
		return;
	}

	try {
		page.openEditor(input, EDITOR_ID);
	} catch (PartInitException e) {
		IStatus status = new Status(IStatus.ERROR, WorkbenchPlugin.PI_WORKBENCH, 1, WorkbenchMessages.getString("QuickStartAction.openEditorException"), e); //$NON-NLS-1$
		ErrorDialog.openError(
			workbench.getActiveWorkbenchWindow().getShell(),
			WorkbenchMessages.getString("QuickStartAction.errorDialogTitle"),  //$NON-NLS-1$
			WorkbenchMessages.getString("QuickStartAction.errorDialogMessage"),  //$NON-NLS-1$
			status);
	}
}
}
