package org.eclipse.update.internal.ui.properties;

import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.update.configuration.ILocalSite;
import org.eclipse.update.internal.ui.wizards.ActivitiesTableViewer;

/**
 * @see PropertyPage
 */
public class ConfigurationActivitiesPreferencePage extends PropertyPage implements IWorkbenchPropertyPage {
	/**
	 *
	 */
	public ConfigurationActivitiesPreferencePage() {
		noDefaultAndApplyButton();
	}

	/**
	 * @see PropertyPage#createContents
	 */
	protected Control createContents(Composite parent)  {
		Composite composite = new Composite(parent,SWT.NONE);
		composite.setLayout(new GridLayout());
		
		Label label = new Label(composite, SWT.NONE);
		label.setText("Activities that caused the creation of this configuration:");
		
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
