package org.eclipse.update.internal.ui.properties;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.dialogs.*;
import org.eclipse.update.core.*;
import org.eclipse.update.core.model.*;
import org.eclipse.update.internal.ui.model.*;
import org.eclipse.update.internal.ui.UpdateUI;

public class FeatureGeneralPropertyPage
	extends PropertyPage
	implements IWorkbenchPropertyPage {
		
	public FeatureGeneralPropertyPage() {
		noDefaultAndApplyButton();
	}

	protected Control createContents(Composite parent) {
		try {
			IFeatureAdapter adapter = (IFeatureAdapter) getElement();
			IFeature feature = adapter.getFeature(null);

			Composite composite = new Composite(parent, SWT.NONE);
			GridLayout layout = new GridLayout();
			layout.marginWidth = 0;
			layout.marginHeight = 0;
			layout.verticalSpacing = 15;
			composite.setLayout(layout);
			
			addGeneralSection(feature, composite);	
			addSupportedPlatformsSection(feature, composite);
			addDescription(feature, composite);
			
			Dialog.applyDialogFont(parent);
			
			return composite;

		} catch (CoreException e) {
		}

		return null;
	}

	private void addGeneralSection(IFeature feature, Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		composite.setLayout(layout);

		addField(composite, UpdateUI.getString("FeatureGeneralPropertyPage.name"), feature.getLabel()); //$NON-NLS-1$
		addField(
			composite,
			UpdateUI.getString("FeatureGeneralPropertyPage.id"), //$NON-NLS-1$
			feature.getVersionedIdentifier().getIdentifier());
		addField(
			composite,
			UpdateUI.getString("FeatureGeneralPropertyPage.version"), //$NON-NLS-1$
			feature.getVersionedIdentifier().getVersion().toString());
		addField(composite, UpdateUI.getString("FeatureGeneralPropertyPage.provider"), feature.getProvider()); //$NON-NLS-1$
		long size = feature.getInstallSize();
		if (size != ContentEntryModel.UNKNOWN_SIZE)
			addField(composite, UpdateUI.getString("FeatureGeneralPropertyPage.size"), new Long(size).toString() + " " + UpdateUI.getString("FeatureGeneralPropertyPage.Kilobytes")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	}
	
	private void addSupportedPlatformsSection(IFeature feature, Composite parent) {
		Group group = new Group(parent, SWT.NONE);
		group.setText(UpdateUI.getString("FeatureGeneralPropertyPage.platforms")); //$NON-NLS-1$

		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.makeColumnsEqualWidth = true;
		group.setLayout(layout);
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label label = new Label(group, SWT.NONE);
		label.setText(UpdateUI.getString("FeatureGeneralPropertyPage.os") + extractValue(feature.getOS())); //$NON-NLS-1$

		label = new Label(group, SWT.NONE);
		label.setText(UpdateUI.getString("FeatureGeneralPropertyPage.ws") + extractValue(feature.getWS())); //$NON-NLS-1$

		label = new Label(group, SWT.NONE);
		label.setText(UpdateUI.getString("FeatureGeneralPropertyPage.arch") + extractValue(feature.getOSArch())); //$NON-NLS-1$

		label = new Label(group, SWT.NONE);
		label.setText(UpdateUI.getString("FeatureGeneralPropertyPage.nl") + extractValue(feature.getNL())); //$NON-NLS-1$
	}

	private void addField(Composite parent, String property, String value) {

		if (value != null && value.length() > 0) {
			Label label = new Label(parent, SWT.NONE);
			label.setText(property);

			label = new Label(parent, SWT.NONE);
			label.setText(value);
		}

	}
	private String extractValue(String value) {
		if (value == null || value.equals("*")) //$NON-NLS-1$
			return UpdateUI.getString("FeatureGeneralPropertyPage.all"); //$NON-NLS-1$
		return value;
	}

	private void addDescription(IFeature feature, Composite parent) {
		IURLEntry description = feature.getDescription();
		if (description != null) {
			String annotation = description.getAnnotation();
			if (annotation != null && annotation.length() > 0) {
				Group group = new Group(parent, SWT.NONE);
				group.setText(UpdateUI.getString("FeatureGeneralPropertyPage.desc")); //$NON-NLS-1$
				group.setLayout(new GridLayout());
				group.setLayoutData(new GridData(GridData.FILL_BOTH));

				Text text = new Text(group, SWT.MULTI | SWT.WRAP);
				GridData gd = new GridData(GridData.FILL_BOTH);
				gd.heightHint = 200;
				gd.widthHint = 350;
				text.setEditable(false);
				text.setText(annotation);
				text.setLayoutData(gd);
			}
		}
	}
}
