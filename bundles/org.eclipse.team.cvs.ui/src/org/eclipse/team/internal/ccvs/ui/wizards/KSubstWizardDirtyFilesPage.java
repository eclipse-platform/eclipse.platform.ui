package org.eclipse.team.internal.ccvs.ui.wizards;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.util.Collection;

import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.team.internal.ccvs.ui.Policy;

/**
 * Page to warn user about uncommitted outgoing changes.
 */
public class KSubstWizardDirtyFilesPage extends CVSWizardPage {
	private boolean includeDirtyFiles;
	private Button includeDirtyFilesButton;
	private ListViewer listViewer;

	public KSubstWizardDirtyFilesPage(String pageName, boolean includeDirtyFiles) {
		super(pageName);
		this.includeDirtyFiles = includeDirtyFiles;
	}
	
	public void createControl(Composite parent) {
		Composite top = new Composite(parent, SWT.NONE);
		top.setLayout(new GridLayout());
		setControl(top);
		createWrappingLabel(top, Policy.bind("KSubstWizardDirtyFilesPage.contents"), 0, LABEL_WIDTH_HINT);
		
		includeDirtyFilesButton = new Button(top, SWT.CHECK);
		includeDirtyFilesButton.setText(Policy.bind("KSubstWizardDirtyFilesPage.includeDirtyFiles"));
		includeDirtyFilesButton.setSelection(includeDirtyFiles);
		includeDirtyFilesButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				includeDirtyFiles = includeDirtyFilesButton.getSelection();
			}
		});
		
		createSeparator(top, SPACER_HEIGHT);
		listViewer = createFileListViewer(top,
			Policy.bind("KSubstWizardDirtyFilesPage.dirtyFilesViewer.title"), LIST_HEIGHT_HINT);
	}
	
	public boolean includeDirtyFiles() {
		return includeDirtyFiles;
	}
	
	public void setDirtyFilesList(Collection files) {
		listViewer.setInput(files);
	}
}
