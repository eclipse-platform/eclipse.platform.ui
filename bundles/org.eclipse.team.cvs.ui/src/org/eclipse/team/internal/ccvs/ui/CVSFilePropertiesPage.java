package org.eclipse.team.internal.ccvs.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.util.Date;

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
					createLabel(composite, Policy.bind("CVSFilePropertiesPage.ignored")); //$NON-NLS-1$
				} else {
					createLabel(composite, Policy.bind("CVSFilePropertiesPage.notManaged")); //$NON-NLS-1$
				}
				createLabel(composite, ""); //$NON-NLS-1$
				return composite;
			}
			ResourceSyncInfo syncInfo = cvsResource.getSyncInfo();
			

			
			if (syncInfo.isAdded()) {
				createLabel(composite, Policy.bind("CVSFilePropertiesPage.isAdded"), 2); //$NON-NLS-1$
			} else {
				// Base
				createLabel(composite, Policy.bind("CVSFilePropertiesPage.baseRevision")); //$NON-NLS-1$
				createLabel(composite, syncInfo.getRevision());
				Date baseTime = syncInfo.getTimeStamp();
				if (baseTime != null) {
					createLabel(composite, Policy.bind("CVSFilePropertiesPage.baseTimestamp")); //$NON-NLS-1$
					createLabel(composite, CVSDateFormatter.dateToEntryLine(syncInfo.getTimeStamp()));
				}
				
				// Modified
				createLabel(composite, Policy.bind("CVSFilePropertiesPage.modified")); //$NON-NLS-1$
				createLabel(composite, cvsResource.isModified() ? Policy.bind("yes") : Policy.bind("no")); //$NON-NLS-1$ //$NON-NLS-2$
			}
			
			// Keyword Mode
			createLabel(composite, Policy.bind("CVSFilePropertiesPage.keywordMode")); //$NON-NLS-1$
			createLabel(composite, syncInfo.getKeywordMode().getLongDisplayText());
			
			// Tag
			createLabel(composite, Policy.bind("CVSFilePropertiesPage.tag")); //$NON-NLS-1$
			CVSTag tag = syncInfo.getTag();
			if (tag == null) {
				createLabel(composite, Policy.bind("CVSFilePropertiesPage.none")); //$NON-NLS-1$
			} else {
				switch (tag.getType()) {
					case CVSTag.HEAD:
						createLabel(composite, tag.getName());
						break;
					case CVSTag.VERSION:
						createLabel(composite, Policy.bind("CVSFilePropertiesPage.version", tag.getName())); //$NON-NLS-1$
						break;
					case CVSTag.BRANCH:
						createLabel(composite, Policy.bind("CVSFilePropertiesPage.branch", tag.getName())); //$NON-NLS-1$
						break;
					case CVSTag.DATE:
						createLabel(composite, Policy.bind("CVSFilePropertiesPage.date", tag.getName())); //$NON-NLS-1$
						break;
				}
			}
			
			// Permissions
			createLabel(composite, Policy.bind("CVSFilePropertiesPage.permissions")); //$NON-NLS-1$
			String permissions = syncInfo.getPermissions();
			if (permissions == null) {
				createLabel(composite, Policy.bind("CVSFilePropertiesPage.notAvailable")); //$NON-NLS-1$
			} else {
				createLabel(composite, syncInfo.getPermissions());
			}
		} catch (TeamException e) {
			// Display error text
			createLabel(composite, Policy.bind("CVSFilePropertiesPage.error")); //$NON-NLS-1$
			createLabel(composite, ""); //$NON-NLS-1$
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
	protected Label createLabel(Composite parent, String text, int span) {
		Label label = new Label(parent, SWT.LEFT);
		label.setText(text);
		GridData data = new GridData();
		data.horizontalSpan = span;
		data.horizontalAlignment = GridData.FILL;
		label.setLayoutData(data);
		return label;
	}
	protected Label createLabel(Composite parent, String text) {
		return createLabel(parent, text, 1);
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

