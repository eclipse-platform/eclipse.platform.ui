package org.eclipse.update.internal.ui.properties;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.update.core.IFeature;
import org.eclipse.update.internal.ui.model.IFeatureAdapter;

public class FeatureEnvironmentPropertyPage extends PropertyPage implements IWorkbenchPropertyPage {
	public FeatureEnvironmentPropertyPage() {
		noDefaultAndApplyButton();
	}

	protected Control createContents(Composite parent)  {
		try {
			IFeatureAdapter adapter = (IFeatureAdapter)getElement();
			IFeature feature = adapter.getFeature(null);
			
			Composite composite = new Composite(parent, SWT.NULL);
			GridLayout layout = new GridLayout();
			layout.numColumns = 2;
			composite.setLayout(layout);
			
			Label label = new Label(composite, SWT.NONE);
			label.setText("Operating System:");
			
			label = new Label(composite, SWT.NONE);
			label.setText(extractValue(feature.getOS()));
			
			label = new Label(composite, SWT.NONE);
			label.setText("Windowing System:");
			
			label = new Label(composite, SWT.NONE);
			label.setText(extractValue(feature.getWS()));
			
			label = new Label(composite, SWT.NONE);
			label.setText("CPU Architecture:");
			
			label = new Label(composite, SWT.NONE);
			label.setText(extractValue(feature.getOSArch()));

			label = new Label(composite, SWT.NONE);
			label.setText("Languages:");
			
			label = new Label(composite, SWT.NONE);
			label.setText(extractValue(feature.getNL()));
			
			return composite;
			
		} catch (CoreException e) {
		}
		return null;
	}
	
	private String extractValue(String value) {
		if (value == null || value.equals("*"))
			return "all";
		return value;
	}
}
