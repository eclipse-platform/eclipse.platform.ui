package org.eclipse.update.internal.ui.properties;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.update.core.IFeature;
import org.eclipse.update.core.IURLEntry;
import org.eclipse.update.core.model.ContentEntryModel;
import org.eclipse.update.internal.ui.model.IFeatureAdapter;

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

		addField(composite, "Name:", feature.getLabel());
		addField(
			composite,
			"Identifier:",
			feature.getVersionedIdentifier().getIdentifier());
		addField(
			composite,
			"Version:",
			feature.getVersionedIdentifier().getVersion().toString());
		addField(composite, "Provider:", feature.getProvider());
		long size = feature.getInstallSize();
		if (size != ContentEntryModel.UNKNOWN_SIZE)
			addField(composite, "Size:", new Long(size).toString() + " KB");

	}
	
	private void addSupportedPlatformsSection(IFeature feature, Composite parent) {
		Group group = new Group(parent, SWT.NONE);
		group.setText("Supported Platforms");

		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.makeColumnsEqualWidth = true;
		group.setLayout(layout);
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label label = new Label(group, SWT.NONE);
		label.setText("Operating System: " + extractValue(feature.getOS()));

		label = new Label(group, SWT.NONE);
		label.setText("Windowing System: " + extractValue(feature.getWS()));

		label = new Label(group, SWT.NONE);
		label.setText("CPU Architecture: " + extractValue(feature.getOSArch()));

		label = new Label(group, SWT.NONE);
		label.setText("Languages: " + extractValue(feature.getNL()));
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
		if (value == null || value.equals("*"))
			return "all";
		return value;
	}

	private void addDescription(IFeature feature, Composite parent) {
		IURLEntry description = feature.getDescription();
		if (description != null) {
			String annotation = description.getAnnotation();
			if (annotation != null && annotation.length() > 0) {
				Group group = new Group(parent, SWT.NONE);
				group.setText("Description");
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
