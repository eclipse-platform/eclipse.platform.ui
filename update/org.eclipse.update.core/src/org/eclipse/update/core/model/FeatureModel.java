package org.eclipse.update.core.model;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * Feature model object.
 * <p>
 * This class may be instantiated or subclassed by clients. However, in most 
 * cases clients should instead instantiate or subclass the provided 
 * concrete implementation of this model.
 * </p>
 * @see org.eclipse.update.core.Feature
 * @since 2.0
 */
public class FeatureModel extends ModelObject {

	private String featureId;
	private String featureVersion;
	private String label;
	private String localizedLabel;
	private String provider;
	private String localizedProvider;
	private String imageURLString;
	private URL imageURL;
	private String os;
	private String ws;
	private String nl;
	private String arch;
	private boolean isPrimary = false;
	private String application;
	private InstallHandlerEntryModel installHandler;
	private URLEntryModel description;
	private URLEntryModel copyright;
	private URLEntryModel license;
	private URLEntryModel updateSiteInfo;
	private List /*of InfoModel*/
	discoverySiteInfo;
	private List /*of ImportModel*/
	imports;
	private List /*of PluginEntryModel*/
	pluginEntries;
	private List /*of NonPluginEntryModel*/
	nonPluginEntries;
	private List /*of ContentGroupModel*/
	groupEntries;

	/**
	 * Creates an uninitialized feature object.
	 * 
	 * @since 2.0
	 */
	public FeatureModel() {
		super();
	}

	/**
	 * Compares 2 feature models for equality
	 *  
	 * @param obj feature model to compare with
	 * @return <code>true</code> if the two models are equal, 
	 * <code>false</code> otherwise
	 * @since 2.0
	 */
	public boolean equals(Object obj) {
		if (!(obj instanceof FeatureModel))
			return false;
		FeatureModel model = (FeatureModel) obj;

		return (
			featureId.toLowerCase().equals(model.getFeatureIdentifier())
				&& featureVersion.toLowerCase().equals(model.getFeatureVersion()));
	}

	/**
	 * Returns the feature identifier as a string
	 * 
	 * @see org.eclipse.update.core.IFeature#getVersionedIdentifier()
	 * @return feature identifier
	 * @since 2.0
	 */
	public String getFeatureIdentifier() {
		return featureId;
	}

	/**
	 * Returns the feature version as a string
	 * 
	 * @see org.eclipse.update.core.IFeature#getVersionedIdentifier()
	 * @return feature version 
	 * @since 2.0
	 */
	public String getFeatureVersion() {
		return featureVersion;
	}

	/**
	 * Retrieve the displayable label for the feature. If the model
	 * object has been resolved, the label is localized.
	 * 
	 * @return displayable label, or <code>null</code>.
	 * @since 2.0
	 */
	public String getLabel() {
		if (localizedLabel != null)
			return localizedLabel;
		else
			return label;
	}

	/**
	 * Retrieve the non-localized displayable label for the feature.
	 * 
	 * @return non-localized displayable label, or <code>null</code>.
	 * @since 2.0
	 */
	public String getLabelNonLocalized() {
		return label;
	}

	/**
	 * Retrieve the displayable label for the feature provider. If the model
	 * object has been resolved, the label is localized.
	 * 
	 * @return displayable label, or <code>null</code>.
	 * @since 2.0
	 */
	public String getProvider() {
		if (localizedProvider != null)
			return localizedProvider;
		else
			return provider;
	}

	/**
	 * Retrieve the non-localized displayable label for the feature provider.
	 * 
	 * @return non-localized displayable label, or <code>null</code>.
	 * @since 2.0
	 */
	public String getProviderNonLocalized() {
		return provider;
	}

	/**
	 * Returns the unresolved URL string for the feature image.
	 *
	 * @return url string, or <code>null</code>
	 * @since 2.0
	 */
	public String getImageURLString() {
		return imageURLString;
	}

	/**
	 * Returns the resolved URL for the image.
	 * 
	 * @return url, or <code>null</code>
	 * @since 2.0
	 */
	public URL getImageURL() {
		return imageURL;
	}

	/**
	 * Get optional operating system specification as a comma-separated string.
	 * 
	 * @see org.eclipse.core.boot.BootLoader 
	 * @return the operating system specification string, or <code>null</code>.
	 * @since 2.0
	 */
	public String getOS() {
		return os;
	}

	/**
	 * Get optional windowing system specification as a comma-separated string.
	 * 
	 * @see org.eclipse.core.boot.BootLoader 
	 * @return the windowing system specification string, or <code>null</code>.
	 * @since 2.0
	 */
	public String getWS() {
		return ws;
	}

	/**
	 * Get optional system architecture specification as a comma-separated string.
	 * 
	 * @see org.eclipse.core.boot.BootLoader 
	 * @return the system architecture specification string, or <code>null</code>.
	 * @since 2.0
	 */
	public String getArch() {
		return arch;
	}

	/**
	 * Get optional locale specification as a comma-separated string.
	 * 
	 * @return the locale specification string, or <code>null</code>.
	 * @since 2.0
	 */
	public String getNL() {
		return nl;
	}

	/**
	 * Indicates whether the feature can be used as a primary feature.
	 * 
	 * @return <code>true</code> if this is a primary feature, 
	 * otherwise <code>false</code>
	 * @since 2.0
	 */
	public boolean isPrimary() {
		return isPrimary;
	}

	/**
	 * Returns an optional identifier for the feature application
	 * 
	 * @return application identifier, or <code>null</code>.
	 * @since 2.0
	 */
	public String getApplication() {
		return application;
	}

	/**
	 * Returns and optional custom install handler entry.
	 * 
	 * @return install handler entry, or <code>null</code> if
	 * none was specified
	 * @since 2.0
	 */
	public InstallHandlerEntryModel getInstallHandlerModel() {
		return installHandler;
	}

	/**
	 * Returns the feature description.
	 * 
	 * @return feature rescription, or <code>null</code>.
	 * @since 2.0
	 */
	public URLEntryModel getDescriptionModel() {
		return description;
	}

	/**
	 * Returns the copyright information for the feature.
	 * 
	 * @return copyright information, or <code>null</code>.
	 * @since 2.0
	 */
	public URLEntryModel getCopyrightModel() {
		return copyright;
	}

	/**
	 * Returns the license information for the feature.
	 * 
	 * @return feature license, or <code>null</code>.
	 * @since 2.0
	 */
	public URLEntryModel getLicenseModel() {
		return license;
	}

	/**
	 * Returns an information entry referencing the location of the
	 * feature update site.
	 * 
	 * @return update site entry, or <code>null</code>.
	 * @since 2.0
	 */
	public URLEntryModel getUpdateSiteEntryModel() {
		return updateSiteInfo;
	}

	/**
	 * Return an array of information entries referencing locations of other
	 * update sites. 
	 * 
	 * @return an array of site entries, or an empty array.
	 * @since 2.0 
	 * @since 2.0
	 */
	public URLEntryModel[] getDiscoverySiteEntryModels() {
		if (discoverySiteInfo == null)
			return new URLEntryModel[0];

		return (URLEntryModel[]) discoverySiteInfo.toArray(
			arrayTypeFor(discoverySiteInfo));
	}

	/**
	 * Return a list of plug-in dependencies for this feature.
	 * 
	 * @return the list of required plug-in dependencies, or an empty array.
	 * @since 2.0
	 */
	public ImportModel[] getImportModels() {
		if (imports == null)
			return new ImportModel[0];

		return (ImportModel[]) imports.toArray(arrayTypeFor(imports));
	}

	/**
	 * Returns an array of plug-in entries referenced by this feature
	 * 
	 * @return an erray of plug-in entries, or an empty array.
	 * @since 2.0
	 */
	public PluginEntryModel[] getPluginEntryModels() {
		if (pluginEntries == null)
			return new PluginEntryModel[0];

		return (PluginEntryModel[]) pluginEntries.toArray(arrayTypeFor(pluginEntries));
	}

	/**
	 * Returns an array of non-plug-in entries referenced by this feature
	 * 
	 * @return an erray of non-plug-in entries, or an empty array.
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
	public void setFeatureIdentifier(String featureId) {
		assertIsWriteable();
		this.featureId = featureId;
	}

	/**
	 * @since 2.0
	 */
	public void setFeatureVersion(String featureVersion) {
		assertIsWriteable();
		this.featureVersion = featureVersion;
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
	public void setProvider(String provider) {
		assertIsWriteable();
		this.provider = provider;
		this.localizedProvider = null;
	}

	/**
	 * @since 2.0
	 */
	public void setImageURLString(String imageURLString) {
		assertIsWriteable();
		this.imageURLString = imageURLString;
		this.imageURL = null;
	}

	/**
	 * @since 2.0
	 */
	public void setOS(String os) {
		assertIsWriteable();
		this.os = os;
	}

	/**
	 * @since 2.0
	 */
	public void setWS(String ws) {
		assertIsWriteable();
		this.ws = ws;
	}

	/**
	 * @since 2.0
	 */
	public void setNL(String nl) {
		assertIsWriteable();
		this.nl = nl;
	}

	/**
	 * Sets the arch.
	 * @param arch The arch to set
	 */
	public void setArch(String arch) {
		assertIsWriteable();
		this.arch = arch;
	}

	/**
	 * @since 2.0
	 */
	public void isPrimary(boolean isPrimary) {
		assertIsWriteable();
		this.isPrimary = isPrimary;
	}

	/**
	 * @since 2.0
	 */
	public void setApplication(String application) {
		assertIsWriteable();
		this.application = application;
	}

	/**
	 * @since 2.0
	 */
	public void setInstallHandlerModel(InstallHandlerEntryModel installHandler) {
		assertIsWriteable();
		this.installHandler = installHandler;
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
	public void setCopyrightModel(URLEntryModel copyright) {
		assertIsWriteable();
		this.copyright = copyright;
	}

	/**
	 * @since 2.0
	 */
	public void setLicenseModel(URLEntryModel license) {
		assertIsWriteable();
		this.license = license;
	}

	/**
	 * @since 2.0
	 */
	public void setUpdateSiteEntryModel(URLEntryModel updateSiteInfo) {
		assertIsWriteable();
		this.updateSiteInfo = updateSiteInfo;
	}

	/**
	 * @since 2.0
	 */
	public void setDiscoverySiteEntryModels(URLEntryModel[] discoverySiteInfo) {
		assertIsWriteable();
		if (discoverySiteInfo == null)
			this.discoverySiteInfo = null;
		else
			this.discoverySiteInfo = new ArrayList(Arrays.asList(discoverySiteInfo));
	}

	/**
	 * @since 2.0
	 */
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
	public void addDiscoverySiteEntryModel(URLEntryModel discoverySiteInfo) {
		assertIsWriteable();
		if (this.discoverySiteInfo == null)
			this.discoverySiteInfo = new ArrayList();
		if (!this.discoverySiteInfo.contains(discoverySiteInfo))
			this.discoverySiteInfo.add(discoverySiteInfo);
	}

	/**
	 * @since 2.0
	 */
	public void addImportModel(ImportModel importEntry) {
		assertIsWriteable();
		if (this.imports == null)
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
	public void removeDiscoverySiteEntryModel(URLEntryModel discoverySiteInfo) {
		assertIsWriteable();
		if (this.discoverySiteInfo != null)
			this.discoverySiteInfo.remove(discoverySiteInfo);
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
	public void markReadOnly() {
		super.markReadOnly();
		markReferenceReadOnly(getDescriptionModel());
		markReferenceReadOnly(getCopyrightModel());
		markReferenceReadOnly(getLicenseModel());
		markReferenceReadOnly(getUpdateSiteEntryModel());
		markListReferenceReadOnly(getDiscoverySiteEntryModels());
		markListReferenceReadOnly(getImportModels());
		markListReferenceReadOnly(getPluginEntryModels());
		markListReferenceReadOnly(getNonPluginEntryModels());
	}

	/**
	 * @since 2.0
	 */
	public void resolve(URL base, ResourceBundle bundle)
		throws MalformedURLException {
		// resolve local elements
		localizedLabel = resolveNLString(bundle, label);
		localizedProvider = resolveNLString(bundle, provider);
		imageURL = resolveURL(base, bundle, imageURLString);

		// delegate to references		
		resolveReference(getDescriptionModel(), base, bundle);
		resolveReference(getCopyrightModel(), base, bundle);
		resolveReference(getLicenseModel(), base, bundle);
		resolveReference(getUpdateSiteEntryModel(), null, bundle);
		resolveListReference(getDiscoverySiteEntryModels(), null, bundle);
		resolveListReference(getImportModels(), base, bundle);
		resolveListReference(getPluginEntryModels(), base, bundle);
		resolveListReference(getNonPluginEntryModels(), base, bundle);
	}

}