package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.core.internal.plugins.PluginDescriptor;
import org.eclipse.core.internal.runtime.InternalPlatform;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.model.PluginFragmentModel;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.ui.PlatformUI;
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
	 */ 	private String createDiagnostics() {
		StringWriter out = new StringWriter();
		PrintWriter writer = new PrintWriter(out);
		appendTimestamp(writer);
		appendProperties(writer);
		appendFeatures(writer);
		appendRegistry(writer);
		appendUpdateManagerLog(writer);
		appendLog(writer);
		writer.close();
		return out.toString();
	}

	/*
	 * Appends a timestamp.
	 */
	private void appendTimestamp(PrintWriter writer) {
		writer.print("*** Date: "); //$NON-NLS-1$
		writer.println(new Date());
	}
	
	/*
	 * Appends the <code>System</code> properties.
	 */
	private void appendProperties(PrintWriter writer) {
		writer.println();
		writer.println("*** System properties:"); //$NON-NLS-1$
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
		writer.println("*** Features:"); //$NON-NLS-1$

		AboutInfo[] featuresArray = ((Workbench)PlatformUI.getWorkbench()).getFeaturesInfo();
		SortedSet set= new TreeSet(new Comparator() {
			public int compare(Object o1, Object o2) {
				String s1= ((AboutInfo)o1).getFeatureId();
				String s2= ((AboutInfo)o2).getFeatureId();
				return s1.compareTo(s2);
			}
		});
		for(int i= 0, length= featuresArray.length; i < length; i++) {
			set.add(featuresArray[i]);
		}
		Iterator i= set.iterator();
		while(i.hasNext()) {
			AboutInfo info = (AboutInfo)i.next();
			writer.print(info.getFeatureId());
			writer.print(" (");
			writer.print(info.getVersion());
			writer.println(")");
		}
	}	

	/*
	 * Appends the contents of the Plugin Registry.
	 */
	private void appendRegistry(PrintWriter writer) {
		writer.println();
		writer.println("*** Plugin Registry:"); //$NON-NLS-1$
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
			writer.print(descriptor.getUniqueIdentifier());
			writer.print(" (");
			writer.print(descriptor.getVersionIdentifier().toString());
			writer.println(")");
			PluginFragmentModel[] fragments= descriptor.getFragments();
			if (fragments != null) {
				for(int j= 0, length= fragments.length; j < length; j++) {
					PluginFragmentModel fragment= fragments[j];
					writer.print('\t');
					writer.print(fragment.getId());
					writer.print(" (");
					writer.print(fragment.getVersion());
					writer.print(")");
				}
				writer.println();
			}
		}
	}	
	
	/*
	 * Appends the contents of the Plugin Registry.
	 */
	private void appendUpdateManagerLog(PrintWriter writer) {
		writer.println();
		writer.println("*** Update Manager Log:"); //$NON-NLS-1$
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
			writer.print("Configuration=");
			writer.println(configurations[i].getLabel());
			writer.print("Current configuration=");
			writer.println(configurations[i].isCurrent());
			IActivity[] activities = configurations[i].getActivities();
			for (int j = 0; j < activities.length; j++) {
				writer.println();
				writer.print("Date=");	
				writer.println(activities[j].getDate());
				writer.print("Target=");			
				writer.println(activities[j].getLabel());
				writer.print("Action=");			
				writer.println(getActionLabel(activities[j]));
				writer.print("Status=");			
				writer.println(getStatusLabel(activities[j]));
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
			writer.println("*** Error Log:"); //$NON-NLS-1$
	
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
				return "Enabled";
			case IActivity.ACTION_FEATURE_INSTALL:
				return "Feature installed";
			case IActivity.ACTION_FEATURE_REMOVE:
				return "Feature removed";
			case IActivity.ACTION_SITE_INSTALL:
				return "Site installed";
			case IActivity.ACTION_SITE_REMOVE:
				return "Site removed";
			case IActivity.ACTION_UNCONFIGURE:
				return "Disabled";
			case IActivity.ACTION_REVERT:
				return "Revert";
			case IActivity.ACTION_RECONCILIATION:
				return "Reconcile";				
			case IActivity.ACTION_ADD_PRESERVED:
				return "Preserved";					
			default:
				return "Unknown";		
		}
	}
	
	private String getStatusLabel(IActivity activity) {
		switch (activity.getStatus()) {
			case IActivity.STATUS_OK:
				return "Success";
			case IActivity.STATUS_NOK:
				return "Failure";
		}
		return "Unknown";
	}
	
}

