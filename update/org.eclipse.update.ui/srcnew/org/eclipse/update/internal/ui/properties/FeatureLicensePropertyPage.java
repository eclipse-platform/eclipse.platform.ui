package org.eclipse.update.internal.ui.properties;

import java.net.URL;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.update.core.*;
import org.eclipse.update.internal.ui.*;
import org.eclipse.update.internal.ui.model.IFeatureAdapter;
import org.eclipse.update.internal.ui.parts.SWTUtil;


public class FeatureLicensePropertyPage extends PropertyPage implements IWorkbenchPropertyPage {
	public FeatureLicensePropertyPage() {
		noDefaultAndApplyButton();
	}

	protected Control createContents(Composite parent)  {
		try {
			Composite composite = new Composite(parent, SWT.NULL);
			composite.setLayout(new GridLayout());

			IFeatureAdapter adapter = (IFeatureAdapter)getElement();
			IFeature feature = adapter.getFeature(null);			
			IURLEntry license = feature.getLicense();
			String annotation = (license != null) ? license.getAnnotation() : null;
			
			if (annotation != null && annotation.length() > 0) {
				Text text = new Text(composite, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
				GridData gd = new GridData(GridData.FILL_BOTH);
				gd.heightHint = 200;
				gd.widthHint = 350;
				text.setLayoutData(gd);
				text.setText(annotation);
				text.setEditable(false);
				final URL url = license.getURL();
				String filename = (url != null) ? url.getFile() : null;
				if (filename != null && (filename.endsWith(".htm") || url.getFile().endsWith(".html"))) {
					Button button = new Button(composite, SWT.PUSH);
					button.setText("Show in Browser");
					button.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
					SWTUtil.setButtonDimensionHint(button);
					button.addSelectionListener(new SelectionAdapter() {
						public void widgetSelected(SelectionEvent e) {
							String urlName = url.getProtocol() + ":" + url.getFile();
							UpdateUI.showURL(urlName, false);
						}
					});
				}
			} else {
				Label label = new Label(composite, SWT.NULL);
				label.setText("Feature does not contain a license.");
			}
			
			Dialog.applyDialogFont(parent);
			
			return composite;
			
		} catch (CoreException e) {
		}
		return null;
	}
}
