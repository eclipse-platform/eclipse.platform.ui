package org.eclipse.update.internal.ui.properties;

import org.eclipse.swt.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.dialogs.*;
import org.eclipse.update.configuration.*;
import org.eclipse.update.internal.ui.model.*;

/**
 * @see PropertyPage
 */
public class ConfiguredSitePropertyPage extends PropertyPage implements IWorkbenchPropertyPage {
	/**
	 *
	 */
	public ConfiguredSitePropertyPage() {
		noDefaultAndApplyButton();
	}

	/**
	 * @see PropertyPage#createContents
	 */
	protected Control createContents(Composite parent)  {
		IConfiguredSiteAdapter adapter = (IConfiguredSiteAdapter)getElement();
		IConfiguredSite csite = adapter.getConfiguredSite();
		Composite composite = new Composite(parent,SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		composite.setLayout(layout);
		addProperty(composite, "Location path: ", csite.getSite().getURL().toString());
		addProperty(composite, "Location type: ", getLocationType(csite));
		addProperty(composite, "Enabled: ", csite.isEnabled()?"yes":"no");
		return composite;
	}
	
	private String getLocationType(IConfiguredSite csite) {
		if (csite.isPrivateSite())
			return "private";
		if (csite.isExtensionSite())
			return "product extension";
		if (csite.isProductSite())
			return "product";
		return "unknown";
	}
	
	private void addProperty(Composite parent, String key, String value) {
		Label label = new Label(parent, SWT.NULL);
		label.setText(key);
		
		label = new Label(parent, SWT.NULL);
		label.setText(value);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		label.setLayoutData(gd);
	}
}
