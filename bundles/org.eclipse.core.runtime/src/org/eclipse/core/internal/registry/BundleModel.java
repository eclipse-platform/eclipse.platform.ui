/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.registry;

import java.util.*;
import org.eclipse.core.internal.runtime.ResourceTranslator;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.osgi.framework.Bundle;

/**
 * An object which represents the user-defined contents of a bundle model
 * in a extensions manifest.
 */
public class BundleModel extends NestedRegistryModelObject implements IRegistryElement {
	private String hostId;
	private IExtensionPoint[] extensionPoints;
	private IExtension[] extensions;
	private transient ResourceBundle resourceBundle;
	private String schemaVersion;
	private Bundle bundle; //Introduced to fix #46308

	private final static String NO_EXTENSION_MUNGING = "eclipse.noExtensionMunging"; //$NON-NLS-1$ //System property

	// Introduced for backward compatibility
	private static Map extensionPointMap;
	static {
		initializeExtensionPointMap();
	}

	/**
	 * Initialize the list of renamed extension point ids
	 */
	private static void initializeExtensionPointMap() {
		Map map = new HashMap(13);
		// TODO should this be hard coded? can we use a properties file?
		map.put("org.eclipse.ui.markerImageProvider", "org.eclipse.ui.ide.markerImageProvider"); //$NON-NLS-1$ //$NON-NLS-2$
		map.put("org.eclipse.ui.markerHelp", "org.eclipse.ui.ide.markerHelp"); //$NON-NLS-1$ //$NON-NLS-2$
		map.put("org.eclipse.ui.markerImageProviders", "org.eclipse.ui.ide.markerImageProviders"); //$NON-NLS-1$ //$NON-NLS-2$
		map.put("org.eclipse.ui.markerResolution", "org.eclipse.ui.ide.markerResolution"); //$NON-NLS-1$ //$NON-NLS-2$
		map.put("org.eclipse.ui.projectNatureImages", "org.eclipse.ui.ide.projectNatureImages"); //$NON-NLS-1$ //$NON-NLS-2$
		map.put("org.eclipse.ui.resourceFilters", "org.eclipse.ui.ide.resourceFilters"); //$NON-NLS-1$ //$NON-NLS-2$
		map.put("org.eclipse.ui.markerUpdaters", "org.eclipse.ui.editors.markerUpdaters"); //$NON-NLS-1$ //$NON-NLS-2$
		map.put("org.eclipse.ui.documentProviders", "org.eclipse.ui.editors.documentProviders"); //$NON-NLS-1$ //$NON-NLS-2$
		map.put("org.eclipse.ui.workbench.texteditor.markerAnnotationSpecification", "org.eclipse.ui.editors.markerAnnotationSpecification"); //$NON-NLS-1$ //$NON-NLS-2$
		map.put("org.eclipse.help.browser", "org.eclipse.help.base.browser"); //$NON-NLS-1$ //$NON-NLS-2$
		map.put("org.eclipse.help.luceneAnalyzer", "org.eclipse.help.base.luceneAnalyzer"); //$NON-NLS-1$ //$NON-NLS-2$
		map.put("org.eclipse.help.webapp", "org.eclipse.help.base.webapp"); //$NON-NLS-1$ //$NON-NLS-2$
		map.put("org.eclipse.help.support", "org.eclipse.ui.helpSupport"); //$NON-NLS-1$ //$NON-NLS-2$
		extensionPointMap = map;
	}

	public String getUniqueIdentifier() {
		return getName();
	}

	public void setUniqueIdentifier(String value) {
		setName(value);
	}

	public void setExtensions(IExtension[] value) {
		extensions = value;
		fixRenamedExtensionPoints();
	}

	public IExtension getExtension(String id) {
		if (id == null)
			return null;
		IExtension[] list = getExtensions();
		if (list == null)
			return null;
		for (int i = 0; i < list.length; i++) {
			if (id.equals(list[i].getUniqueIdentifier()))
				return list[i];
		}
		return null;
	}

	public IExtension[] getExtensions() {
		return extensions == null ? new IExtension[0] : extensions;
	}

	public void setExtensionPoints(IExtensionPoint[] value) {
		extensionPoints = value;
	}

	public IExtensionPoint getExtensionPoint(String xpt) {
		if (xpt == null)
			return null;
		IExtensionPoint[] list = getExtensionPoints();
		if (list == null)
			return null;
		for (int i = 0; i < list.length; i++) {
			if (xpt.equals(list[i].getSimpleIdentifier()))
				return list[i];
		}
		return null;
	}

	public IExtensionPoint[] getExtensionPoints() {
		return extensionPoints == null ? new IExtensionPoint[0] : extensionPoints;
	}

	public void setHostIdentifier(String value) {
		hostId = value;
	}

	public String getHostIdentifier() {
		return hostId;
	}

	public boolean isFragment() {
		return hostId != null;
	}

	public String toString() {
		return "BundleModel: " + getName(); //$NON-NLS-1$
	}

	/**
	 * Fixes up the extension declarations in the given pre-3.0 plug-in or fragment to compensate
	 * for extension points that were renamed between release 2.1 and 3.0.
	 */
	private void fixRenamedExtensionPoints() {
		if (extensions == null || (schemaVersion != null && schemaVersion.equals("3.0")) || System.getProperties().get(NO_EXTENSION_MUNGING) != null) //$NON-NLS-1$
			return;
		for (int i = 0; i < extensions.length; i++) {
			Extension extension = (Extension) extensions[i];
			String oldPointId = extension.getExtensionPointIdentifier();
			String newPointId = (String) extensionPointMap.get(oldPointId);
			if (newPointId != null) {
				extension.setExtensionPointIdentifier(newPointId);
			}
		}
	}

	protected void setSchemaVersion(String schemaVersion) {
		this.schemaVersion = schemaVersion;
	}

	public long getId() {
		// returns an invalid id, but avoids NPE
		return bundle == null ? -1 : bundle.getBundleId();
	}

	public Bundle getBundle() {
		return bundle;
	}

	public void setBundle(Bundle value) {
		bundle = value;
	}

	public String getResourceString(String value) {
		if (resourceBundle == null)
			resourceBundle = ResourceTranslator.getResourceBundle(bundle);
		if (resourceBundle == null)
			return value;
		return ResourceTranslator.getResourceString(null, value, resourceBundle);
	}
}