package org.eclipse.team.internal.ccvs.ui.model;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteFile;
import org.eclipse.team.internal.ccvs.core.ILogEntry;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.ICVSUIConstants;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;

public class CVSRemoteFilePropertySource implements IPropertySource {
	ICVSRemoteFile file;
	ILogEntry entry;
	boolean initialized;
	
	// Property Descriptors
	static protected IPropertyDescriptor[] propertyDescriptors = new IPropertyDescriptor[5];
	{
		PropertyDescriptor descriptor;
		String category = Policy.bind("cvs");
		
		// resource name
		descriptor = new PropertyDescriptor(ICVSUIConstants.PROP_NAME, Policy.bind("CVSRemoteFilePropertySource.name"));
		descriptor.setAlwaysIncompatible(true);
		descriptor.setCategory(category);
		propertyDescriptors[0] = descriptor;
		// revision
		descriptor = new PropertyDescriptor(ICVSUIConstants.PROP_REVISION, Policy.bind("CVSRemoteFilePropertySource.revision"));
		descriptor.setAlwaysIncompatible(true);
		descriptor.setCategory(category);
		propertyDescriptors[1] = descriptor;
		// date
		descriptor = new PropertyDescriptor(ICVSUIConstants.PROP_DATE, Policy.bind("CVSRemoteFilePropertySource.date"));
		descriptor.setAlwaysIncompatible(true);
		descriptor.setCategory(category);
		propertyDescriptors[2] = descriptor;
		// author
		descriptor = new PropertyDescriptor(ICVSUIConstants.PROP_AUTHOR, Policy.bind("CVSRemoteFilePropertySource.author"));
		descriptor.setAlwaysIncompatible(true);
		descriptor.setCategory(category);
		propertyDescriptors[3] = descriptor;
		// comment
		descriptor = new PropertyDescriptor(ICVSUIConstants.PROP_COMMENT, Policy.bind("CVSRemoteFilePropertySource.comment"));
		descriptor.setAlwaysIncompatible(true);
		descriptor.setCategory(category);
		propertyDescriptors[4] = descriptor;
	}

	/**
	 * Create a PropertySource and store its file
	 */
	public CVSRemoteFilePropertySource(ICVSRemoteFile file) {
		this.file = file;
	}
	
	/**
	 * Do nothing because properties are read only.
	 */
	public Object getEditableValue() {
		return this;
	}

	/**
	 * Return the Property Descriptors for the receiver.
	 */
	public IPropertyDescriptor[] getPropertyDescriptors() {
		return propertyDescriptors;
	}

	/*
	 * @see IPropertySource#getPropertyValue(Object)
	 */
	public Object getPropertyValue(Object id) {
		if (!initialized) {
			initialize();
			initialized = true;
		}
		if (id.equals(ICVSUIConstants.PROP_NAME)) {
			return file.getName();
		}
		if (id.equals(ICVSUIConstants.PROP_REVISION)) {
			return entry.getRevision();
		}
		if (id.equals(ICVSUIConstants.PROP_DATE)) {
			return entry.getDate();
		}
		if (id.equals(ICVSUIConstants.PROP_AUTHOR)) {
			return entry.getAuthor();
		}
		if (id.equals(ICVSUIConstants.PROP_COMMENT)) {
			return entry.getComment();
		}
		return "";
	}

	/**
	 * Answer true if the value of the specified property 
	 * for this object has been changed from the default.
	 */
	public boolean isPropertySet(Object property) {
		return false;
	}
	/**
	 * Reset the specified property's value to its default value.
	 * Do nothing because properties are read only.
	 * 
	 * @param   property    The property to reset.
	 */
	public void resetPropertyValue(Object property) {
	}
	/**
	 * Do nothing because properties are read only.
	 */
	public void setPropertyValue(Object name, Object value) {
	}
	
	private void initialize() {
		BusyIndicator.showWhile(Display.getDefault(), new Runnable() {
			public void run() {
				try {
					ILogEntry[] entries = file.getLogEntries(new NullProgressMonitor());
					String revision = file.getRevision();
					for (int i = 0; i < entries.length; i++) {
						if (entries[i].getRevision().equals(revision)) {
							entry = entries[i];
							return;
						}
					}
				} catch (TeamException e) {
					CVSUIPlugin.log(e.getStatus());
				}
			}
		});
	}
}
