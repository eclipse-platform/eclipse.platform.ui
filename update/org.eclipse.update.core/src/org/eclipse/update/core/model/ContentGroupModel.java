package org.eclipse.update.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

/**
 * An object which represents a group of related
 * plug-in or non-plug-in entries in the
 * packaging manifest.
 * <p>
 * This class may be instantiated, or further subclassed.
 * </p>
 * @since 2.0
 */

public class ContentGroupModel extends ModelObject {

	private String id;
	private String label;
	private String localizedLabel;
	private boolean optional;
	private URLEntryModel description;
	private String[] includes;
	private String[] excludes;
	private List /*of ImportModel*/ imports;
	private List /*of PluginEntryModel*/ pluginEntries;
	private List /*of NonPluginEntryModel*/ nonPluginEntries;
	private List /*of ContentGroupModel*/ nestedGroupEntries;

	/**
	 * Creates a uninitialized model object.
	 * 
	 * @since 2.0
	 */
	public ContentGroupModel() {
		super();
	}

	/**
	 * @since 2.0
	 */	
	public String getIdentifier() {
		return id;
	}

	/**
	 * @since 2.0
	 */	
	public String getLabel() {
		if (localizedLabel != null)
			return localizedLabel;
		else
			return label;
	}
	/**
	 * @since 2.0
	 */	
	public String getLabelNonLocalized() {
		return label;
	}

	/**
	 * @since 2.0
	 */	
	public boolean isOptional() {
		return optional;
	}

	/**
	 * @since 2.0
	 */	
	public URLEntryModel getDescriptionModel() {
		return description;
	}

	/**
	 * @since 2.0
	 */	
	public String[] getIncludedGroupEntries() {
		if (includes == null)
			return new String[0];

		String[] list = new String[includes.length];
		System.arraycopy(includes, 0, list, 0, includes.length);
		return list;
	}

	/**
	 * @since 2.0
	 */	
	public String[] getExcludedGroupEntries() {
		if (excludes == null)
			return new String[0];

		String[] list = new String[excludes.length];
		System.arraycopy(excludes, 0, list, 0, excludes.length);
		return list;
	}

	/**
	 * @since 2.0
	 */	
	public ImportModel[] getImportModels() {
		if (imports == null)
			return new ImportModel[0];

		return (ImportModel[]) imports.toArray(arrayTypeFor(imports));
	}

	/**
	 * @since 2.0
	 */	
	public PluginEntryModel[] getPluginEntryModels() {
		if (pluginEntries == null)
			return new PluginEntryModel[0];

		return (PluginEntryModel[]) pluginEntries.toArray(arrayTypeFor(pluginEntries));
	}

	/**
	 * @since 2.0
	 */	
	public NonPluginEntryModel[] getNonPluginEntryModels() {
		if (nonPluginEntries == null)
			return new NonPluginEntryModel[0];

		return (NonPluginEntryModel[]) nonPluginEntries.toArray(
			arrayTypeFor(nonPluginEntries));
	}

	/**
	 * @since 2.0
	 */	
	public ContentGroupModel[] getNestedGroupEntryModels() {
		if (nestedGroupEntries == null)
			return new ContentGroupModel[0];

		return (ContentGroupModel[]) nestedGroupEntries.toArray(
			arrayTypeFor(nestedGroupEntries));
	}

	/**
	 * @since 2.0
	 */	
	public void setIdentifier(String id) {
		assertIsWriteable();
		this.id = id;
	}

	/**
	 * @since 2.0
	 */	
	public void setLabel(String label) {
		assertIsWriteable();
		this.label = label;
		this.localizedLabel = null;
	}

	/**
	 * @since 2.0
	 */	
	public void isOptional(boolean optional) {
		assertIsWriteable();
		this.optional = optional;
	}

	/**
	 * @since 2.0
	 */	
	public void setDescriptionModel(URLEntryModel description) {
		assertIsWriteable();
		this.description = description;
	}

	/**
	 * @since 2.0
	 */	
	public void setIncludedGroupEntries(String[] includes) {
		assertIsWriteable();
		this.includes = includes;
	}

	/**
	 * @since 2.0
	 */	
	public void setExcludedGroupEntries(String[] excludes) {
		assertIsWriteable();
		this.excludes = excludes;
	}

	public void setImportModels(ImportModel[] imports) {
		assertIsWriteable();
		if (imports == null)
			this.imports = null;
		else
			this.imports = new ArrayList(Arrays.asList(imports));
	}

	/**
	 * @since 2.0
	 */	
	public void setPluginEntryModels(PluginEntryModel[] pluginEntries) {
		assertIsWriteable();
		if (pluginEntries == null)
			this.pluginEntries = null;
		else
			this.pluginEntries = new ArrayList(Arrays.asList(pluginEntries));
	}

	/**
	 * @since 2.0
	 */	
	public void setNonPluginEntryModels(NonPluginEntryModel[] nonPluginEntries) {
		assertIsWriteable();
		if (nonPluginEntries == null)
			this.nonPluginEntries = null;
		else
			this.nonPluginEntries = new ArrayList(Arrays.asList(nonPluginEntries));
	}

	/**
	 * @since 2.0
	 */	
	public void setNestedGroupEntryModels(ContentGroupModel[] nestedGroupEntries) {
		assertIsWriteable();
		if (nestedGroupEntries == null)
			this.nestedGroupEntries = null;
		else
			this.nestedGroupEntries = new ArrayList(Arrays.asList(nestedGroupEntries));
	}

	/**
	 * @since 2.0
	 */	
	public void addImportModel(ImportModel importEntry) {
		assertIsWriteable();
		if (imports == null)
			this.imports = new ArrayList();
		if (!this.imports.contains(importEntry))
			this.imports.add(importEntry);
	}

	/**
	 * @since 2.0
	 */	
	public void addPluginEntryModel(PluginEntryModel pluginEntry) {
		assertIsWriteable();
		if (this.pluginEntries == null)
			this.pluginEntries = new ArrayList();
		if (!this.pluginEntries.contains(pluginEntry))
			this.pluginEntries.add(pluginEntry);
	}

	/**
	 * @since 2.0
	 */	
	public void addNonPluginEntryModel(NonPluginEntryModel nonPluginEntry) {
		assertIsWriteable();
		if (this.nonPluginEntries == null)
			this.nonPluginEntries = new ArrayList();
		if (!this.nonPluginEntries.contains(nonPluginEntry))
			this.nonPluginEntries.add(nonPluginEntry);
	}

	/**
	 * @since 2.0
	 */	
	public void addNestedGroupEntryModel(ContentGroupModel nestedGroupEntry) {
		assertIsWriteable();
		if (this.nestedGroupEntries == null)
			this.nestedGroupEntries = new ArrayList();
		if (!this.nestedGroupEntries.contains(nestedGroupEntry))
			this.nestedGroupEntries.add(nestedGroupEntry);
	}

	/**
	 * @since 2.0
	 */	
	public void removeImportModel(ImportModel importEntry) {
		assertIsWriteable();
		if (this.imports != null)
			this.imports.remove(importEntry);
	}

	/**
	 * @since 2.0
	 */	
	public void removePluginEntryModel(PluginEntryModel pluginEntry) {
		assertIsWriteable();
		if (this.pluginEntries != null)
			this.pluginEntries.remove(pluginEntry);
	}

	/**
	 * @since 2.0
	 */	
	public void removeNonPluginEntryModel(NonPluginEntryModel nonPluginEntry) {
		assertIsWriteable();
		if (this.nonPluginEntries != null)
			this.nonPluginEntries.remove(nonPluginEntry);
	}

	/**
	 * @since 2.0
	 */	
	public void removeNestedGroupEntryModel(ContentGroupModel nestedGroupEntry) {
		assertIsWriteable();
		if (this.nestedGroupEntries != null)
			this.nestedGroupEntries.remove(nestedGroupEntry);
	}
	
	/**
	 * @since 2.0
	 */
	public void markReadOnly() {		
		markReferenceReadOnly(getDescriptionModel());
		markListReferenceReadOnly(getImportModels());
		markListReferenceReadOnly(getPluginEntryModels());
		markListReferenceReadOnly(getNonPluginEntryModels());
		markListReferenceReadOnly(getNestedGroupEntryModels());
	}
	
	/**
	 * @since 2.0
	 */
	public void resolve(URL base, ResourceBundle bundle) throws MalformedURLException {
		// resolve local elements
		localizedLabel = resolveNLString(bundle, label);
		
		// delegate to references
		resolveReference(getDescriptionModel(), base, bundle);
		resolveListReference(getImportModels(), base, bundle);
		resolveListReference(getPluginEntryModels(), base, bundle);
		resolveListReference(getNonPluginEntryModels(), base, bundle);
		resolveListReference(getNestedGroupEntryModels(), base, bundle);
	}
}