/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.core.boot.BootLoader;
import org.eclipse.core.boot.IPlatformConfiguration;
import org.eclipse.core.boot.IPlatformConfiguration.IFeatureEntry;
import org.eclipse.core.internal.plugins.PluginDescriptor;
import org.eclipse.core.internal.runtime.InternalPlatform;
import org.eclipse.core.internal.runtime.PreferenceExporter;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IPluginRegistry;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.model.PluginFragmentModel;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IHelpContextIds;
import org.eclipse.update.configuration.IActivity;
import org.eclipse.update.configuration.IInstallConfiguration;
import org.eclipse.update.configuration.ILocalSite;
import org.eclipse.update.core.SiteManager;

/**
 * Displays system information about the eclipse application.
 */
public final class SystemSummaryDialog extends Dialog {
	
	/* package */ SystemSummaryDialog(Shell parentShell) {
		super(parentShell);
		setShellStyle(SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.APPLICATION_MODAL);
	}

	/* (non-Javadoc)
	 * Method declared on Window.
	 */
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(IDEWorkbenchMessages.getString("SystemSummary.title")); //$NON-NLS-1$
		WorkbenchHelp.setHelp(newShell, IHelpContextIds.SYSTEM_SUMMARY_DIALOG);
	} 

	/* (non-Javadoc)
	 * Method declared on Dialog.
	 */
	protected void createButtonsForButtonBar(Composite parent) {
		parent.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	
		createButton(parent, IDialogConstants.CLOSE_ID, IDialogConstants.CLOSE_LABEL, true);
	}

	/* (non-Javadoc)
	 * Method declared on Dialog.
	 */
	protected Control createDialogArea(Composite parent) {
		Composite outer = (Composite) super.createDialogArea(parent);

		Text text = new Text(outer, SWT.MULTI | SWT.READ_ONLY);
		GridData gridData =
			new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL);
		gridData.grabExcessVerticalSpace = true;
		gridData.grabExcessHorizontalSpace = true;
		gridData.heightHint = convertVerticalDLUsToPixels(300);
		gridData.widthHint = convertHorizontalDLUsToPixels(400);
		text.setLayoutData(gridData);
		text.setText(getSystemSummary());
		return outer;
	}

	private String getSystemSummary() {
		StringWriter out = new StringWriter();
		PrintWriter writer = new PrintWriter(out);
		appendTimestamp(writer);
		appendProperties(writer);
		appendFeatures(writer);
		appendRegistry(writer);
		appendUserPreferences(writer);
		appendUpdateManagerLog(writer);
		appendLog(writer);
		writer.close();
		return out.toString();
	}

	/*
	 * Appends a timestamp.
	 */
	private void appendTimestamp(PrintWriter writer) {
		writer.println(IDEWorkbenchMessages.format("SystemSummary.timeStamp", new Object[] {new Date()})); //$NON-NLS-1$
	}
	
	/*
	 * Appends the <code>System</code> properties.
	 */
	private void appendProperties(PrintWriter writer) {
		writer.println();
		writer.println(IDEWorkbenchMessages.getString("SystemSummary.systemProperties")); //$NON-NLS-1$
		Properties properties= System.getProperties();
		SortedSet set= new TreeSet(new Comparator() {
			public int compare(Object o1, Object o2) {
				String s1= (String)o1;
				String s2= (String)o2;
				return s1.compareTo(s2);
			}
		});
		set.addAll(properties.keySet());
		Iterator i= set.iterator();
		while(i.hasNext()) {
			Object key= i.next();
			writer.print(key);
			writer.print('=');
			writer.println(properties.get(key));
		}
	}
	
	/*
	 * Appends the installed, configured features.
	 */
	private void appendFeatures(PrintWriter writer) {
		writer.println();
		writer.println(IDEWorkbenchMessages.getString("SystemSummary.features")); //$NON-NLS-1$

		IPlatformConfiguration platformConfiguration = BootLoader.getCurrentPlatformConfiguration();
		IPlatformConfiguration.IFeatureEntry[] featuresArray = platformConfiguration.getConfiguredFeatureEntries();

		Arrays.sort(featuresArray, (new Comparator() {
			public int compare(Object o1, Object o2) {
				String s1= ((IFeatureEntry)o1).getFeatureIdentifier();
				String s2= ((IFeatureEntry)o2).getFeatureIdentifier();
				return s1.compareTo(s2);
			}
			}));

		IPluginRegistry pluginRegistry = Platform.getPluginRegistry();
		for (int i = 0; i < featuresArray.length; i++) {
			IFeatureEntry info = featuresArray[i];
			String pluginID = info.getFeaturePluginIdentifier();
			if (pluginID != null) {
				IPluginDescriptor descriptor= pluginRegistry.getPluginDescriptor(pluginID);
				pluginID = descriptor.getLabel();
				if ("".equals(pluginID)) {   //$NON-NLS-1$
					pluginID = IDEWorkbenchMessages.getString("SystemSummary.notSpecified"); //$NON-NLS-1$
				}
			} else {
				pluginID = IDEWorkbenchMessages.getString("SystemSummary.notSpecified"); //$NON-NLS-1$
			}

			String[] args= new String[] {info.getFeatureIdentifier(), info.getFeatureVersion(), pluginID};
			writer.println(IDEWorkbenchMessages.format("SystemSummary.featureVersion", args)); //$NON-NLS-1$
		}
	}	

	/*
	 * Appends the contents of the Plugin Registry.
	 */
	private void appendRegistry(PrintWriter writer) {
		writer.println();
		writer.println(IDEWorkbenchMessages.getString("SystemSummary.pluginRegistry")); //$NON-NLS-1$
		IPluginDescriptor[] descriptors= Platform.getPluginRegistry().getPluginDescriptors();
		SortedSet set= new TreeSet(new Comparator() {
			public int compare(Object o1, Object o2) {
				String s1= ((IPluginDescriptor)o1).getUniqueIdentifier();
				String s2= ((IPluginDescriptor)o2).getUniqueIdentifier();
				return s1.compareTo(s2);
			}
		});
		for(int i= 0, length= descriptors.length; i < length; i++) {
			set.add(descriptors[i]);
		}
		Iterator i= set.iterator();
		while(i.hasNext()) {
			PluginDescriptor descriptor= (PluginDescriptor)i.next();
			String[] args= new String[] {descriptor.getUniqueIdentifier(), descriptor.getVersionIdentifier().toString(), descriptor.getLabel()};
			writer.println(IDEWorkbenchMessages.format("SystemSummary.descriptorIdVersion", args)); //$NON-NLS-1$
			PluginFragmentModel[] fragments= descriptor.getFragments();
			if (fragments != null) {
				for(int j= 0, length= fragments.length; j < length; j++) {
					PluginFragmentModel fragment= fragments[j];
					writer.print('\t');
					args= new String[] {fragment.getId(), fragment.getVersion(), fragment.getName()};
					writer.println(IDEWorkbenchMessages.format("SystemSummary.fragmentIdVersion", args)); //$NON-NLS-1$
				}
			}
		}
	}	
	
	/*
	 * Appends the preferences
	 */
	private void appendUserPreferences(PrintWriter writer) {
		String tmpFile = ".tmpPrefFile"; //$NON-NLS-1$
		IPath path = new Path(InternalPlatform.getMetaArea().getLocation().append(tmpFile).toOSString());
		File file = path.toFile();
		file.delete();
		
		try {
			PreferenceExporter.exportPreferences(path);
		} catch (CoreException e) {
			writer.println("Error exporting user preferences " + e.toString()); //$NON-NLS-1$
		}				
		writer.println();
		writer.println(IDEWorkbenchMessages.getString("SystemSummary.userPreferences")); //$NON-NLS-1$

		BufferedReader reader = null;
		try {
			FileInputStream in = new FileInputStream(file);
			reader = new BufferedReader(new InputStreamReader(in, "8859_1")); //$NON-NLS-1$
			char[] chars= new char[8192];
			int read= reader.read(chars);
			while (read > 0) {
				writer.write(chars, 0, read);
				read= reader.read(chars);
			}
			reader.close();
			reader= null;			
		} catch (IOException e) {
			writer.println("Error reading user preference file " + e.toString()) ;//$NON-NLS-1$		
		}
					
		if (reader != null) {
			try {
				reader.close();
			} catch (IOException e) {
				writer.println("Error closing user preference file " + e.toString()); //$NON-NLS-1$
			}
		}	
		file.delete();
	}	
		
	/*
	 * Appends the contents of the Plugin Registry.
	 */
	private void appendUpdateManagerLog(PrintWriter writer) {
		writer.println();
		writer.println(IDEWorkbenchMessages.getString("SystemSummary.updateManagerLog")); //$NON-NLS-1$
		ILocalSite site;
		try {
			site = SiteManager.getLocalSite();
		} catch (CoreException e) {
			e.printStackTrace(writer);
			return;
		}
		IInstallConfiguration[] configurations = site.getConfigurationHistory();
		for (int i = 0; i < configurations.length; i++) {
			writer.println();
			if (i>0)
				writer.println("----------------------------------------------------"); //$NON-NLS-1$

			writer.println(IDEWorkbenchMessages.format("SystemSummary.configuration", new Object[] {configurations[i].getLabel()})); //$NON-NLS-1$
			writer.println(IDEWorkbenchMessages.format("SystemSummary.isCurrentConfiguration", new Object[] {new Boolean(configurations[i].isCurrent())})); //$NON-NLS-1$ 
			IActivity[] activities = configurations[i].getActivities();
			for (int j = 0; j < activities.length; j++) {
				writer.println();
				writer.println(IDEWorkbenchMessages.format("SystemSummary.date", new Object[] {activities[j].getDate()})); //$NON-NLS-1$
				writer.println(IDEWorkbenchMessages.format("SystemSummary.target", new Object[] {activities[j].getLabel()})); //$NON-NLS-1$
				writer.println(IDEWorkbenchMessages.format("SystemSummary.action", new Object[] {getActionLabel(activities[j])})); //$NON-NLS-1$
				writer.println(IDEWorkbenchMessages.format("SystemSummary.status", new Object[] {getStatusLabel(activities[j])})); //$NON-NLS-1$
			}
		}
	}

	/*
	 * Appends the contents of the .log file
	 */
	private void appendLog(PrintWriter writer) {
		File log= new File(InternalPlatform.getMetaArea().getLogLocation().toOSString());
		if (log.exists()) {
			writer.println();
			writer.println(IDEWorkbenchMessages.getString("SystemSummary.errorLog")); //$NON-NLS-1$
	
			BufferedReader reader = null;
			try {
				FileInputStream in = new FileInputStream(log);
				reader = new BufferedReader(new InputStreamReader(in, "UTF-8")); //$NON-NLS-1$
				char[] chars= new char[8192];
				int read= reader.read(chars);
				while (read > 0) {
					writer.write(chars, 0, read);
					read= reader.read(chars);
				}
				reader.close();
				reader= null;			
			} catch (IOException e) {
				writer.println("Error reading .log file"); //$NON-NLS-1$
			}			
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					writer.println("Error reading .log file"); //$NON-NLS-1$
				}
			}
		}
	}		
	
	private String getActionLabel(IActivity activity) {
		int action = activity.getAction();
		switch (action) {
			case IActivity.ACTION_CONFIGURE:
				return IDEWorkbenchMessages.getString("SystemSummary.activity.enabled"); //$NON-NLS-1$
			case IActivity.ACTION_FEATURE_INSTALL:
				return IDEWorkbenchMessages.getString("SystemSummary.activity.featureInstalled"); //$NON-NLS-1$
			case IActivity.ACTION_FEATURE_REMOVE:
				return IDEWorkbenchMessages.getString("SystemSummary.activity.featureRemoved"); //$NON-NLS-1$
			case IActivity.ACTION_SITE_INSTALL:
				return IDEWorkbenchMessages.getString("SystemSummary.activity.siteInstalled"); //$NON-NLS-1$
			case IActivity.ACTION_SITE_REMOVE:
				return IDEWorkbenchMessages.getString("SystemSummary.activity.siteRemoved"); //$NON-NLS-1$
			case IActivity.ACTION_UNCONFIGURE:
				return IDEWorkbenchMessages.getString("SystemSummary.activity.disabled"); //$NON-NLS-1$
			case IActivity.ACTION_REVERT:
				return IDEWorkbenchMessages.getString("SystemSummary.activity.revert"); //$NON-NLS-1$
			case IActivity.ACTION_RECONCILIATION:
				return IDEWorkbenchMessages.getString("SystemSummary.activity.reconcile"); //$NON-NLS-1$
			case IActivity.ACTION_ADD_PRESERVED:
				return IDEWorkbenchMessages.getString("SystemSummary.activity.preserved"); //$NON-NLS-1$
			default:
				return IDEWorkbenchMessages.getString("SystemSummary.activity.unknown"); //$NON-NLS-1$
		}
	}

	private String getStatusLabel(IActivity activity) {
		switch (activity.getStatus()) {
			case IActivity.STATUS_OK:
				return IDEWorkbenchMessages.getString("SystemSummary.activity.status.success"); //$NON-NLS-1$
			case IActivity.STATUS_NOK:
				return IDEWorkbenchMessages.getString("SystemSummary.activity.status.failure"); //$NON-NLS-1$
		}
		return IDEWorkbenchMessages.getString("SystemSummary.activity.status.unknown"); //$NON-NLS-1$
	}
}
