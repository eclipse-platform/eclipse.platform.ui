/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal;

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
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.model.PluginFragmentModel;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.ui.texteditor.AbstractDocumentProvider;
import org.eclipse.update.configuration.IActivity;
import org.eclipse.update.configuration.IInstallConfiguration;
import org.eclipse.update.configuration.ILocalSite;
import org.eclipse.update.core.SiteManager;

/**
 * The <code>SystemSummaryDocumentProvider</code> creates diagnostic 
 * information about the Eclipse instance in which it is running.
 */
class SystemSummaryDocumentProvider extends AbstractDocumentProvider {
	/**
	 * @see AbstractDocumentProvider#isDeleted(Object)
	 */
	public boolean isDeleted(Object element) {
		return false;
	}

	/**
	 * @see AbstractDocumentProvider#getSynchronizationStamp(Object)
	 */
	public long getSynchronizationStamp(Object element) {
		return 0;
	}

	/**
	 * @see AbstractDocumentProvider#getModificationStamp(Object)
	 */
	public long getModificationStamp(Object element) {
		return 0;
	}

	/**
	 * @see AbstractDocumentProvider#doSaveDocument(IProgressMonitor, Object, IDocument, boolean)
	 */
	protected void doSaveDocument(
		IProgressMonitor arg0,
		Object arg1,
		IDocument arg2,
		boolean arg3)
		throws CoreException {
	}

	/**
	 * @see AbstractDocumentProvider#createDocument(Object)
	 */
	protected IDocument createDocument(Object element) throws CoreException {
		Document doc= new Document();
		doc.set(createDiagnostics());
		return doc;
	}

	/**
	 * @see AbstractDocumentProvider#createAnnotationModel(Object)
	 */
	protected IAnnotationModel createAnnotationModel(Object element)
		throws CoreException {
		return null;
	}

	/*
	 * Returns a <code>String</code> of diagnostics information.
	 */ 
	private String createDiagnostics() {
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
		writer.println(SystemSummaryMessages.getFormattedString("SystemSummary.timeStamp", new Date())); //$NON-NLS-1$
	}
	
	/*
	 * Appends the <code>System</code> properties.
	 */
	private void appendProperties(PrintWriter writer) {
		writer.println();
		writer.println(SystemSummaryMessages.getString("SystemSummary.systemProperties")); //$NON-NLS-1$
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
		writer.println(SystemSummaryMessages.getString("SystemSummary.features")); //$NON-NLS-1$

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
					pluginID = SystemSummaryMessages.getString("SystemSummary.notSpecified"); //$NON-NLS-1$
				}
			} else {
				pluginID = SystemSummaryMessages.getString("SystemSummary.notSpecified"); //$NON-NLS-1$
			}

			String[] args= new String[] {info.getFeatureIdentifier(), info.getFeatureVersion(), pluginID};
			writer.println(SystemSummaryMessages.getFormattedString("SystemSummary.featureVersion", args)); //$NON-NLS-1$
		}
	}	

	/*
	 * Appends the contents of the Plugin Registry.
	 */
	private void appendRegistry(PrintWriter writer) {
		writer.println();
		writer.println(SystemSummaryMessages.getString("SystemSummary.pluginRegistry")); //$NON-NLS-1$
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
			writer.println(SystemSummaryMessages.getFormattedString("SystemSummary.descriptorIdVersion", args)); //$NON-NLS-1$
			PluginFragmentModel[] fragments= descriptor.getFragments();
			if (fragments != null) {
				for(int j= 0, length= fragments.length; j < length; j++) {
					PluginFragmentModel fragment= fragments[j];
					writer.print('\t');
					args= new String[] {fragment.getId(), fragment.getVersion(), fragment.getName()};
					writer.println(SystemSummaryMessages.getFormattedString("SystemSummary.fragmentIdVersion", args)); //$NON-NLS-1$
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
		writer.println(SystemSummaryMessages.getString("SystemSummary.userPreferences")); //$NON-NLS-1$

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
		writer.println(SystemSummaryMessages.getString("SystemSummary.updateManagerLog")); //$NON-NLS-1$
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

			writer.println(SystemSummaryMessages.getFormattedString("SystemSummary.configuration", configurations[i].getLabel())); //$NON-NLS-1$
			writer.println(SystemSummaryMessages.getFormattedString("SystemSummary.isCurrentConfiguration", "" + configurations[i].isCurrent())); //$NON-NLS-1$ //$NON-NLS-2$
			IActivity[] activities = configurations[i].getActivities();
			for (int j = 0; j < activities.length; j++) {
				writer.println();
				writer.println(SystemSummaryMessages.getFormattedString("SystemSummary.date", activities[j].getDate())); //$NON-NLS-1$
				writer.println(SystemSummaryMessages.getFormattedString("SystemSummary.target", activities[j].getLabel())); //$NON-NLS-1$
				writer.println(SystemSummaryMessages.getFormattedString("SystemSummary.action", getActionLabel(activities[j]))); //$NON-NLS-1$
				writer.println(SystemSummaryMessages.getFormattedString("SystemSummary.status", getStatusLabel(activities[j]))); //$NON-NLS-1$
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
			writer.println(SystemSummaryMessages.getString("SystemSummary.errorLog")); //$NON-NLS-1$
	
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
				return SystemSummaryMessages.getString("SystemSummary.activity.enabled"); //$NON-NLS-1$
			case IActivity.ACTION_FEATURE_INSTALL:
				return SystemSummaryMessages.getString("SystemSummary.activity.featureInstalled"); //$NON-NLS-1$
			case IActivity.ACTION_FEATURE_REMOVE:
				return SystemSummaryMessages.getString("SystemSummary.activity.featureRemoved"); //$NON-NLS-1$
			case IActivity.ACTION_SITE_INSTALL:
				return SystemSummaryMessages.getString("SystemSummary.activity.siteInstalled"); //$NON-NLS-1$
			case IActivity.ACTION_SITE_REMOVE:
				return SystemSummaryMessages.getString("SystemSummary.activity.siteRemoved"); //$NON-NLS-1$
			case IActivity.ACTION_UNCONFIGURE:
				return SystemSummaryMessages.getString("SystemSummary.activity.disabled"); //$NON-NLS-1$
			case IActivity.ACTION_REVERT:
				return SystemSummaryMessages.getString("SystemSummary.activity.revert"); //$NON-NLS-1$
			case IActivity.ACTION_RECONCILIATION:
				return SystemSummaryMessages.getString("SystemSummary.activity.reconcile"); //$NON-NLS-1$
			case IActivity.ACTION_ADD_PRESERVED:
				return SystemSummaryMessages.getString("SystemSummary.activity.preserved"); //$NON-NLS-1$
			default:
				return SystemSummaryMessages.getString("SystemSummary.activity.unknown"); //$NON-NLS-1$
		}
	}

	private String getStatusLabel(IActivity activity) {
		switch (activity.getStatus()) {
			case IActivity.STATUS_OK:
				return SystemSummaryMessages.getString("SystemSummary.activity.status.success"); //$NON-NLS-1$
			case IActivity.STATUS_NOK:
				return SystemSummaryMessages.getString("SystemSummary.activity.status.failure"); //$NON-NLS-1$
		}
		return SystemSummaryMessages.getString("SystemSummary.activity.status.unknown"); //$NON-NLS-1$
	}
}

