package org.eclipse.team.internal.ccvs.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.ICVSFile;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.core.syncinfo.ResourceSyncInfo;
import org.eclipse.team.internal.ccvs.core.util.CVSDateFormatter;
import org.eclipse.ui.dialogs.PropertyPage;

public class CVSFilePropertiesPage extends PropertyPage {
	IFile file;

	/*
	 * @see PreferencesPage#createContents
	 */
	protected Control createContents(Composite parent) {
		initialize();
		noDefaultAndApplyButton();
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		composite.setLayout(layout);
		
		try {
			ICVSFile cvsResource = CVSWorkspaceRoot.getCVSFileFor(file);
			if (!cvsResource.isManaged()) {
				if (cvsResource.isIgnored()) {
					createLabel(composite, Policy.bind("CVSFilePropertiesPage.ignored"));
				} else {
					createLabel(composite, Policy.bind("CVSFilePropertiesPage.notManaged"));
				}
				createLabel(composite, "");
				return composite;
			}
			ResourceSyncInfo syncInfo = cvsResource.getSyncInfo();
			
			// Base
			createLabel(composite, Policy.bind("CVSFilePropertiesPage.baseRevision"));
			createLabel(composite, syncInfo.getRevision());
			createLabel(composite, Policy.bind("CVSFilePropertiesPage.baseTimestamp"));
			createLabel(composite, CVSDateFormatter.dateToEntryLine(syncInfo.getTimeStamp()));
			
			// Dirty and Modified
			createLabel(composite, Policy.bind("CVSFilePropertiesPage.dirty"));
			createLabel(composite, cvsResource.isDirty() ? Policy.bind("yes") : Policy.bind("no"));
			createLabel(composite, Policy.bind("CVSFilePropertiesPage.modified"));
			createLabel(composite, cvsResource.isModified() ? Policy.bind("yes") : Policy.bind("no"));
			
			// Keyword Mode
			createLabel(composite, Policy.bind("CVSFilePropertiesPage.keywordMode"));
			createLabel(composite, syncInfo.getKeywordMode().getLongDisplayText());
			
			// Tag
			createLabel(composite, Policy.bind("CVSFilePropertiesPage.tag"));
			CVSTag tag = syncInfo.getTag();
			if (tag == null) {
				createLabel(composite, Policy.bind("CVSFilePropertiesPage.none"));
			} else {
				switch (tag.getType()) {
					case CVSTag.HEAD:
						createLabel(composite, tag.getName());
						break;
					case CVSTag.VERSION:
						createLabel(composite, Policy.bind("CVSFilePropertiesPage.version", tag.getName()));
						break;
					case CVSTag.BRANCH:
						createLabel(composite, Policy.bind("CVSFilePropertiesPage.branch", tag.getName()));
						break;
					case CVSTag.DATE:
						createLabel(composite, Policy.bind("CVSFilePropertiesPage.date", tag.getName()));
						break;
				}
			}
			
			// Permissions
			createLabel(composite, Policy.bind("CVSFilePropertiesPage.permissions"));
			String permissions = syncInfo.getPermissions();
			if (permissions == null) {
				createLabel(composite, Policy.bind("CVSFilePropertiesPage.notAvailable"));
			} else {
				createLabel(composite, syncInfo.getPermissions());
			}
		} catch (TeamException e) {
			// Display error text
			createLabel(composite, Policy.bind("CVSFilePropertiesPage.error"));
			createLabel(composite, "");
		}
		return composite;
	}
	/**
	 * Utility method that creates a label instance
	 * and sets the default layout data.
	 *
	 * @param parent  the parent for the new label
	 * @param text  the text for the new label
	 * @return the new label
	 */
	protected Label createLabel(Composite parent, String text) {
		Label label = new Label(parent, SWT.LEFT);
		label.setText(text);
		GridData data = new GridData();
		data.horizontalSpan = 1;
		data.horizontalAlignment = GridData.FILL;
		label.setLayoutData(data);
		return label;
	}
	/**
	 * Initializes the page
	 */
	private void initialize() {
		// Get the file that is the source of this property page
		file = null;
		IAdaptable element = getElement();
		if (element instanceof IFile) {
			file = (IFile)element;
		} else {
			Object adapter = element.getAdapter(IFile.class);
			if (adapter instanceof IFile) {
				file = (IFile)adapter;
			}
		}
	}
}

