/*
 * Created on May 27, 2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package org.eclipse.update.internal.ui.wizards;

import org.eclipse.jface.viewers.*;
import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.update.configuration.*;
import org.eclipse.update.core.*;
import org.eclipse.update.internal.ui.*;
import org.eclipse.update.internal.ui.parts.*;


/**
 * @author wassimm
 */
public class ActivitiesTableViewer{
	private static final String KEY_CONFIGURE =
		"InstallConfigurationPage.ActivitySection.action.configure";
	private static final String KEY_FEATURE_INSTALL =
		"InstallConfigurationPage.ActivitySection.action.featureInstall";
	private static final String KEY_FEATURE_REMOVE =
		"InstallConfigurationPage.ActivitySection.action.featureRemove";
	private static final String KEY_SITE_INSTALL =
		"InstallConfigurationPage.ActivitySection.action.siteInstall";
	private static final String KEY_SITE_REMOVE =
		"InstallConfigurationPage.ActivitySection.action.siteRemove";
	private static final String KEY_UNCONFIGURE =
		"InstallConfigurationPage.ActivitySection.action.unconfigure";
	private static final String KEY_UNKNOWN =
		"InstallConfigurationPage.ActivitySection.action.unknown";
	private static final String KEY_REVERT =
		"InstallConfigurationPage.ActivitySection.action.revert";
	private static final String KEY_RECONCILIATION =
		"InstallConfigurationPage.ActivitySection.action.reconcile";
	private static final String KEY_ADD_PRESERVED =
		"InstallConfigurationPage.ActivitySection.action.addpreserved";


	static class ActivitiesContentProvider
		extends DefaultContentProvider
		implements IStructuredContentProvider {
		public Object[] getElements(Object element) {
			return ((IInstallConfiguration) element).getActivities();
		}
	}

	static class ActivitiesLabelProvider
		extends LabelProvider
		implements ITableLabelProvider {
		public Image getColumnImage(Object element, int columnIndex) {
			if (columnIndex == 0) {
				UpdateLabelProvider provider =
					UpdateUI.getDefault().getLabelProvider();
				switch (((IActivity) element).getStatus()) {
					case IActivity.STATUS_OK :
						return provider.get(UpdateUIImages.DESC_OK_ST_OBJ, 0);
					case IActivity.STATUS_NOK :
						return provider.get(UpdateUIImages.DESC_ERR_ST_OBJ, 0);
				}
			}
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			IActivity activity = (IActivity) element;
			switch (columnIndex) {
				case 1 :
					return Utilities.format(activity.getDate());
				case 2 :
					return activity.getLabel();
				case 3 :
					return getActionLabel(activity);
			}
			return "";
		}

		private String getActionLabel(IActivity activity) {
			int action = activity.getAction();
			switch (action) {
				case IActivity.ACTION_CONFIGURE :
					return UpdateUI.getString(KEY_CONFIGURE);
				case IActivity.ACTION_FEATURE_INSTALL :
					return UpdateUI.getString(KEY_FEATURE_INSTALL);
				case IActivity.ACTION_FEATURE_REMOVE :
					return UpdateUI.getString(KEY_FEATURE_REMOVE);
				case IActivity.ACTION_SITE_INSTALL :
					return UpdateUI.getString(KEY_SITE_INSTALL);
				case IActivity.ACTION_SITE_REMOVE :
					return UpdateUI.getString(KEY_SITE_REMOVE);
				case IActivity.ACTION_UNCONFIGURE :
					return UpdateUI.getString(KEY_UNCONFIGURE);
				case IActivity.ACTION_REVERT :
					return UpdateUI.getString(KEY_REVERT);
				case IActivity.ACTION_RECONCILIATION :
					return UpdateUI.getString(KEY_RECONCILIATION);
				case IActivity.ACTION_ADD_PRESERVED :
					return UpdateUI.getString(KEY_ADD_PRESERVED);
				default :
					return UpdateUI.getString(KEY_UNKNOWN);
			}
		}
	}

	public static TableViewer createViewer(Composite parent) {
		Table table = new Table(parent, SWT.BORDER);
		table.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		table.setHeaderVisible(true);

		TableColumn column = new TableColumn(table, SWT.NONE);

		column = new TableColumn(table, SWT.NONE);
		column.setText("Date");

		column = new TableColumn(table, SWT.NONE);
		column.setText("Target");

		column = new TableColumn(table, SWT.NONE);
		column.setText("Action");

		TableViewer activitiesViewer = new TableViewer(table);
		activitiesViewer.setLabelProvider(new ActivitiesLabelProvider());
		activitiesViewer.setContentProvider(new ActivitiesContentProvider());
		return activitiesViewer;
	}

}
