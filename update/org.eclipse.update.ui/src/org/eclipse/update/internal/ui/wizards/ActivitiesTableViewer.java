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
import org.eclipse.update.internal.ui.UpdateUI;


/**
 * @author wassimm
 */
public class ActivitiesTableViewer{

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
			return ""; //$NON-NLS-1$
		}

		private String getActionLabel(IActivity activity) {
			int action = activity.getAction();
			switch (action) {
				case IActivity.ACTION_CONFIGURE :
					return UpdateUI.getString("ActivitiesTableViewer.enabled"); //$NON-NLS-1$
				case IActivity.ACTION_FEATURE_INSTALL :
					return UpdateUI.getString("ActivitiesTableViewer.featureInstalled"); //$NON-NLS-1$
				case IActivity.ACTION_FEATURE_REMOVE :
					return UpdateUI.getString("ActivitiesTableViewer.featureRemoved"); //$NON-NLS-1$
				case IActivity.ACTION_SITE_INSTALL :
					return UpdateUI.getString("ActivitiesTableViewer.siteInstalled"); //$NON-NLS-1$
				case IActivity.ACTION_SITE_REMOVE :
					return UpdateUI.getString("ActivitiesTableViewer.siteRemoved"); //$NON-NLS-1$
				case IActivity.ACTION_UNCONFIGURE :
					return UpdateUI.getString("ActivitiesTableViewer.disabled"); //$NON-NLS-1$
				case IActivity.ACTION_REVERT :
					return UpdateUI.getString("ActivitiesTableViewer.revert"); //$NON-NLS-1$
				case IActivity.ACTION_RECONCILIATION :
					return UpdateUI.getString("ActivitiesTableViewer.reconcile"); //$NON-NLS-1$
				default :
					return UpdateUI.getString("ActivitiesTableViewer.unknown"); //$NON-NLS-1$
			}
		}
	}

	public static TableViewer createViewer(Composite parent) {
		Table table = new Table(parent, SWT.BORDER);
		table.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		table.setHeaderVisible(true);

		TableColumn column = new TableColumn(table, SWT.NONE);

		column = new TableColumn(table, SWT.NONE);
		column.setText(UpdateUI.getString("ActivitiesTableViewer.date")); //$NON-NLS-1$

		column = new TableColumn(table, SWT.NONE);
		column.setText(UpdateUI.getString("ActivitiesTableViewer.target")); //$NON-NLS-1$

		column = new TableColumn(table, SWT.NONE);
		column.setText(UpdateUI.getString("ActivitiesTableViewer.action")); //$NON-NLS-1$

		TableViewer activitiesViewer = new TableViewer(table);
		activitiesViewer.setLabelProvider(new ActivitiesLabelProvider());
		activitiesViewer.setContentProvider(new ActivitiesContentProvider());
		return activitiesViewer;
	}

}
