package org.eclipse.update.internal.ui.properties;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.update.core.IFeature;
import org.eclipse.update.core.IURLEntry;
import org.eclipse.update.core.model.ContentEntryModel;
import org.eclipse.update.internal.ui.model.IFeatureAdapter;

public class FeatureGeneralPropertyPage extends PropertyPage implements IWorkbenchPropertyPage {
	public FeatureGeneralPropertyPage() {
		noDefaultAndApplyButton();
	}

	protected Control createContents(Composite parent) {
		try {
			IFeatureAdapter adapter = (IFeatureAdapter) getElement();
			IFeature feature = adapter.getFeature(null);
			
			Composite composite = new Composite(parent, SWT.NULL);
			GridLayout layout = new GridLayout();
			layout.numColumns = 2;			
			composite.setLayout(layout);

			addField(composite, "Name:", feature.getLabel());
			addField(composite, "Identifier:", feature.getVersionedIdentifier().getIdentifier());
			addField(composite, "Version:", feature.getVersionedIdentifier().getVersion().toString());
			addField(composite, "Provider:", feature.getProvider());
			long size = feature.getInstallSize();
			if (size != ContentEntryModel.UNKNOWN_SIZE)
				addField(composite, "Size:", new Long(size).toString());
			
			addDescription(feature, composite);
			
			
				return composite;
		} catch (CoreException e) {
		}

		return null;
	}
	
	private void addField(Composite parent, String property, String value) {
			
		if (value != null && value.length() > 0) {
			FontData data = new FontData();
			data.setStyle(SWT.BOLD);
			data.setHeight(8);
			
			Label label = new Label(parent, SWT.NONE);
			label.setText(property);
			label.setFont(new Font(null, data));
			
			label = new Label(parent, SWT.NONE);
			label.setText(value);
		}
		
	}
	
	private void addDescription(IFeature feature, Composite parent) {
		IURLEntry description = feature.getDescription();
		if (description != null) {
			String annotation = description.getAnnotation();
			if (annotation != null && annotation.length() > 0) {
				GridData gd = new GridData();
				gd.horizontalSpan = 2;
				
				FontData data = new FontData();
				data.setStyle(SWT.BOLD);
				data.setHeight(8);
			
				Label label = new Label(parent, SWT.NONE);
				label.setText("Description:");
				label.setFont(new Font(null, data));
				label.setLayoutData(gd);
				
				gd = new GridData();
				gd.horizontalSpan = 2;
				
				if (annotation.length() < 80) {
					label = new Label(parent, SWT.NONE);
					label.setText(annotation);
					label.setLayoutData(gd);
				} else {
					Text text =
						new Text(parent, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
					text.setEditable(false);
					text.setText(annotation);
					text.setLayoutData(gd);				
				}
			}
			
		}
	}
}
