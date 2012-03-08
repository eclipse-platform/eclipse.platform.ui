/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.runtime.model;

/**
 * An object which represents the user-defined extension point in a plug-in
 * manifest.
 * <p>
 * This class may be instantiated, or further subclassed.
 * </p>
 * @deprecated In Eclipse 3.0 the runtime was refactored and all 
 * non-essential elements removed.  This class provides facilities primarily intended
 * for tooling.  As such it has been removed and no directly substitutable API provided.
 * This API will be deleted in a future release. See bug 370248 for details.
 */
public class ExtensionPointModel extends PluginModelObject {

	// DTD properties (included in plug-in manifest)
	private String id = null;
	private String schema = null;

	// transient properties (not included in plug-in manifest)
	private PluginModel plugin = null; // declaring plugin
	private ExtensionModel[] extensions = null; // configured extensions

	/**
	 * Creates a new extension point model in which all fields are <code>null</code>.
	 */
	public ExtensionPointModel() {
		super();
	}

	/**
	 * Returns this extensions added to this extension point.
	 * 
	 * @return the extensions in this extension point or <code>null</code>
	 */
	public ExtensionModel[] getDeclaredExtensions() {
		return extensions;
	}

	/**
	 * Returns the simple identifier of this extension point, or <code>null</code>
	 * if this extension point does not have an identifier. This identifier is
	 * specified in the plug-in manifest as a non-empty string containing no
	 * period characters (<code>'.'</code>) and must be unique within the
	 * defining plug-in.
	 * 
	 * @return the simple identifier of the extension point (e.g. <code>"main"</code>)
	 *               or <code>null</code>
	 */
	public String getId() {
		return id;
	}

	/**
	 * Returns the plug-in model (descriptor or fragment) in which this
	 * extension is declared.
	 * 
	 * @return the plug-in model in which this extension is declared or <code>null</code>
	 */
	public PluginModel getParent() {
		return plugin;
	}

	/**
	 * Returns the plug-in descriptor in which this extension point is
	 * declared.
	 * 
	 * @return the plug-in descriptor in which this extension point is declared
	 *               or <code>null</code>
	 */
	public PluginDescriptorModel getParentPluginDescriptor() {
		return (PluginDescriptorModel) plugin;
	}

	/**
	 * Returns the schema specification for this extension point.
	 * 
	 * @return the schema specification for this extension point or <code>null</code>
	 */
	public String getSchema() {
		return schema;
	}

	/**
	 * Sets this extensions added to this extension point. This object must not
	 * be read-only.
	 * 
	 * @param value the extensions in this extension point. May be <code>null</code>.
	 */
	public void setDeclaredExtensions(ExtensionModel[] value) {
		assertIsWriteable();
		extensions = value;
	}

	/**
	 * Sets the simple identifier of this extension point, or <code>null</code>
	 * if this extension point does not have an identifier. This identifier is
	 * specified in the plug-in manifest as a non-empty string containing no
	 * period characters (<code>'.'</code>) and must be unique within the
	 * defining plug-in. This object must not be read-only.
	 * 
	 * @param value the simple identifier of the extension point (e.g. <code>"main"</code>).
	 *              May be <code>null</code>.
	 */
	public void setId(String value) {
		assertIsWriteable();
		id = value;
	}

	/**
	 * Sets the plug-in model in which this extension is declared. This object
	 * must not be read-only.
	 * 
	 * @param value the plug-in model in which this extension is declared. May be <code>null</code>.
	 */
	public void setParent(PluginModel value) {
		assertIsWriteable();
		plugin = value;
	}

	/**
	 * Sets the plug-in descriptor in which this extension point is declared.
	 * This object must not be read-only.
	 * 
	 * @param value the plug-in descriptor in which this extension point is declared.
	 *             May be <code>null</code>.
	 */
	public void setParentPluginDescriptor(PluginDescriptorModel value) {
		assertIsWriteable();
		plugin = value;
	}

	/**
	 * Sets the schema specification for this extension point. This object must
	 * not be read-only.
	 * 
	 * @param value the schema specification for this extension point. May be
	 *          <code>null</code>.
	 */
	public void setSchema(String value) {
		assertIsWriteable();
		schema = value;
	}
}
