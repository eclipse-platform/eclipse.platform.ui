package org.eclipse.team.internal.ccvs.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.team.ccvs.core.CVSTeamProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.TeamPlugin;
import org.eclipse.ui.dialogs.PropertyPage;

/**
 * A property page which displays the CVS-specific properties for the
 * selected resource.
 */
public class ResourcePropertiesPage extends PropertyPage {
	// The resource to show properties for
	IResource resource;

	/*
	 * @see PreferencePage#createContents(Composite)
	 */
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = layout.marginWidth = 0;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		try {
			IResource resource = getSelectedElement();
			if (resource != null) {
				CVSTeamProvider provider = (CVSTeamProvider)TeamPlugin.getManager().getProvider(resource.getProject());;
				if (!provider.isManaged(resource)) {
					createPair(composite, Policy.bind("ResourcePropertiesPage.status"), Policy.bind("ResourcePropertiesPage.notManaged"));
				} else {
					createPair(composite, Policy.bind("ResourcePropertiesPage.status"), provider.hasRemote(resource) ? Policy.bind("ResourcePropertiesPage.versioned") : Policy.bind("ResourcePropertiesPage.notVersioned"));
					createPair(composite, Policy.bind("ResourcePropertiesPage.state"), provider.isCheckedOut(resource) ? Policy.bind("ResourcePropertiesPage.checkedOut") : Policy.bind("ResourcePropertiesPage.checkedIn"));

					//createPair(composite, Policy.bind("ResourcePropertiesPage.baseRevision"), common != null ? common.getVersionName() : Policy.bind("ResourcePropertiesPage.none"));
				}
			}
		} catch (TeamException e) {
			createPair(composite, Policy.bind("ResourcePropertiesPage.error"), e.getMessage());
		}
		return composite;
	}

	/**
	 * Creates a key-value property pair in the given parent.
	 * 
	 * @param parent  the parent for the labels
	 * @param left  the string for the left label
	 * @param right  the string for the right label
	 */
	protected void createPair(Composite parent, String left, String right) {
		Label label = new Label(parent, SWT.NONE);
		label.setText(left);
	
		label = new Label(parent, SWT.NONE);
		label.setText(right);
		label.setToolTipText(right);
		label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	}

	/**
	 * Returns the element selected when the properties was run
	 * 
	 * @return the selected element
	 */	
	protected IResource getSelectedElement() {
		// get the resource that is the source of this property page
		IResource resource = null;
		IAdaptable element = getElement();
		if (element instanceof IResource) {
			resource = (IResource)element;
		} else {
			Object adapter = element.getAdapter(IResource.class);
			if (adapter instanceof IResource) {
				resource = (IResource)adapter;
			}
		}
		return resource;
	}
}
