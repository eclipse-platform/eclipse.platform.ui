package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
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
import org.eclipse.ui.texteditor.AbstractDocumentProvider;

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
		ByteArrayOutputStream out= new ByteArrayOutputStream();
		PrintWriter writer= new PrintWriter(out);
		appendTimestamp(writer);
		appendProperties(writer);
		appendRegistry(writer);
		appendLog(writer);
		writer.close();
		return new String(out.toByteArray());
	}

	/*
	 * Appends a timestamp.
	 */
	private void appendTimestamp(PrintWriter writer) {
		writer.print("Date: "); //$NON-NLS-1$
		writer.println(new Date());
	}
	
	/*
	 * Appends the <code>System</code> properties.
	 */
	private void appendProperties(PrintWriter writer) {
		writer.println();
		writer.println("System properties:"); //$NON-NLS-1$
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
	 * Appends the contents of the Plugin Registry.
	 */
	private void appendRegistry(PrintWriter writer) {
		writer.println();
		writer.println("Plugin Registry:"); //$NON-NLS-1$
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
			writer.print(")\t");
			writer.println(descriptor.getLabel());
			PluginFragmentModel[] fragments= descriptor.getFragments();
			if (fragments != null) {
				for(int j= 0, length= fragments.length; j < length; j++) {
					PluginFragmentModel fragment= fragments[j];
					writer.print('\t');
					writer.print(fragment.getId());
					writer.print(" (");
					writer.print(fragment.getVersion());
					writer.print(")\t");
					writer.println(fragment.getName());
				}
				writer.println();
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
			writer.println("Error Log:"); //$NON-NLS-1$
			
			FileReader reader= null;
			try {
				reader= new FileReader(log);
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
}

