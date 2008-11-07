/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.internal.ui.wizards;

import java.util.ArrayList;

import org.eclipse.jface.viewers.*;
import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.update.configuration.*;
import org.eclipse.update.core.*;
import org.eclipse.update.internal.core.*;
import org.eclipse.update.internal.ui.*;
import org.eclipse.update.internal.ui.parts.*;


/**
 * Viewer for configuration activities
 */
public class ActivitiesTableViewer{

	private static InstallLogParser parser; 
	
	static class ActivitiesContentProvider
		extends DefaultContentProvider
		implements IStructuredContentProvider {
		
		private boolean showCurrentOnly;
		
		public ActivitiesContentProvider(boolean showCurrentOnly){
			this.showCurrentOnly = showCurrentOnly;
		}
		
		public Object[] getElements(Object element) {
			InstallConfiguration currentConfig = (InstallConfiguration)element;
			InstallConfiguration[] configs = parser.getConfigurations();
			boolean hitCurrentConfig = false;
			ArrayList activitiesList = new ArrayList();
			for (int i = 0; i<configs.length; i++){
				if (configs[i].equals(currentConfig) && !hitCurrentConfig)
					hitCurrentConfig = true;
				if (hitCurrentConfig && showCurrentOnly)
					return configs[i].getActivities();
				else if (hitCurrentConfig){
					IActivity[] activities = configs[i].getActivities();
					for (int j = 0; j<activities.length; j++)
						activitiesList.add(activities[j]);	
				}
				
			}
			return (IActivity[])activitiesList.toArray(new IActivity[activitiesList.size()]);
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
					return UpdateUIMessages.ActivitiesTableViewer_enabled; 
				case IActivity.ACTION_FEATURE_INSTALL :
					return UpdateUIMessages.ActivitiesTableViewer_featureInstalled; 
				case IActivity.ACTION_FEATURE_REMOVE :
					return UpdateUIMessages.ActivitiesTableViewer_featureRemoved; 
				case IActivity.ACTION_SITE_INSTALL :
					return UpdateUIMessages.ActivitiesTableViewer_siteInstalled; 
				case IActivity.ACTION_SITE_REMOVE :
					return UpdateUIMessages.ActivitiesTableViewer_siteRemoved; 
				case IActivity.ACTION_UNCONFIGURE :
					return UpdateUIMessages.ActivitiesTableViewer_disabled; 
				case IActivity.ACTION_REVERT :
					return UpdateUIMessages.ActivitiesTableViewer_revert; 
				case IActivity.ACTION_RECONCILIATION :
					return UpdateUIMessages.ActivitiesTableViewer_reconcile; 
				default :
					return UpdateUIMessages.ActivitiesTableViewer_unknown; 
			}
		}
	}

	public static TableViewer createViewer(Composite parent, boolean showCurrentOnly) {
		parser = new InstallLogParser();
		parser.parseInstallationLog();
		Table table = new Table(parent, SWT.BORDER);
		table.setLayoutData(new GridData(GridData.FILL_BOTH));
		table.setHeaderVisible(true);

		TableColumn column = new TableColumn(table, SWT.NONE);

		column = new TableColumn(table, SWT.NONE);
		column.setText(UpdateUIMessages.ActivitiesTableViewer_date); 

		column = new TableColumn(table, SWT.NONE);
		column.setText(UpdateUIMessages.ActivitiesTableViewer_target); 

		column = new TableColumn(table, SWT.NONE);
		column.setText(UpdateUIMessages.ActivitiesTableViewer_action); 

		TableViewer activitiesViewer = new TableViewer(table);
		activitiesViewer.setLabelProvider(new ActivitiesLabelProvider());
		activitiesViewer.setContentProvider(new ActivitiesContentProvider(showCurrentOnly));
		return activitiesViewer;
	}

}
