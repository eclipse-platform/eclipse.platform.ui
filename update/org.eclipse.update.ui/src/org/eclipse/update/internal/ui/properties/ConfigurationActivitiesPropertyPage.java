package org.eclipse.update.internal.ui.properties;

import org.eclipse.jface.viewers.*;
import org.eclipse.swt.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.dialogs.*;
import org.eclipse.update.configuration.*;
import org.eclipse.update.internal.ui.wizards.*;
import org.eclipse.update.internal.ui.UpdateUI;

/**
 * @see PropertyPage
 */
public class ConfigurationActivitiesPropertyPage extends PropertyPage implements IWorkbenchPropertyPage {
	/**
	 *
	 */
	public ConfigurationActivitiesPropertyPage() {
		noDefaultAndApplyButton();
	}

	/**
	 * @see PropertyPage#createContents
	 */
	protected Control createContents(Composite parent)  {
		Composite composite = new Composite(parent,SWT.NONE);
		composite.setLayout(new GridLayout());
		
		Label label = new Label(composite, SWT.NONE);
		label.setText(UpdateUI.getString("ConfigurationActivitiesPropertyPage.label")); //$NON-NLS-1$
		
		TableViewer viewer = ActivitiesTableViewer.createViewer(composite);
		viewer.setInput(((ILocalSite)getElement()).getCurrentConfiguration());
		
		viewer.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
		TableLayout layout = new TableLayout();
		layout.addColumnData(new ColumnWeightData(8, 20, false));
		layout.addColumnData(new ColumnWeightData(50, 80, false));
		layout.addColumnData(new ColumnWeightData(50, 80, false));
		layout.addColumnData(new ColumnWeightData(50, 80, false));

		viewer.getTable().setLayout(layout);
		
		return composite;
	}
}
