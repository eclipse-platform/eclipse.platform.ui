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
 * Page to warn user about the side-effects of changing keyword
 * substitution on already committed files.
 */
public class KSubstWizardSharedFilesPage extends CVSWizardPage {
	private boolean includeSharedFiles;
	private Button includeSharedFilesButton;
	private ListViewer listViewer;

	public KSubstWizardSharedFilesPage(String pageName, boolean includeSharedFiles) {
		super(pageName);
		this.includeSharedFiles = includeSharedFiles;
	}
	
	public void createControl(Composite parent) {
		Composite top = new Composite(parent, SWT.NONE);
		top.setLayout(new GridLayout());
		setControl(top);
		createWrappingLabel(top, Policy.bind("KSubstWizardSharedFilesPage.contents"), 0, LABEL_WIDTH_HINT);
		
		includeSharedFilesButton = new Button(top, SWT.CHECK);
		includeSharedFilesButton.setText(Policy.bind("KSubstWizardSharedFilesPage.includeSharedFiles"));
		includeSharedFilesButton.setSelection(includeSharedFiles);
		includeSharedFilesButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				includeSharedFiles = includeSharedFilesButton.getSelection();
			}
		});

		createSeparator(top, SPACER_HEIGHT);
		listViewer = createFileListViewer(top,
			Policy.bind("KSubstWizardSharedFilesPage.sharedFilesViewer.title"), LIST_HEIGHT_HINT);
	}
	
	public boolean includeSharedFiles() {
		return includeSharedFiles;
	}

	public void setSharedFilesList(Collection files) {
		listViewer.setInput(files);
	}
}
