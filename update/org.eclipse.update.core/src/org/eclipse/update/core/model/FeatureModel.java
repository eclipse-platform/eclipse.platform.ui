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
 * An object which represents the feature in the
 * packaging manifest.
 * <p>
 * This class may be instantiated, or further subclassed.
 * </p>
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
	private String application;
	private InstallHandlerModel installHandler;
	private URLEntryModel description;
	private URLEntryModel copyright;
	private URLEntryModel license;
	private URLEntryModel updateSiteInfo;
	private List /*of InfoModel*/ discoverySiteInfo;
	private List /*of ImportModel*/ imports;
	private List /*of PluginEntryModel*/ pluginEntries;
	private List /*of NonPluginEntryModel*/ nonPluginEntries;
	private List /*of ContentGroupModel*/ groupEntries;

	/**
	 * Creates an uninitialized model object.
	 * 
	 * @since 2.0
	 */	
	public FeatureModel() {
		super();
	}

	/**
	 * @since 2.0
	 */	
	public String getFeatureIdentifier() {
		return featureId;
	}

	/**
	 * @since 2.0
	 */	
	public String getFeatureVersion() {
		return featureVersion;
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
	public String getProvider() {
		if (localizedProvider != null)
			return localizedProvider;
		else
			return provider;
	}

	/**
	 * @since 2.0
	 */
	public String getProviderNonLocalized() {
		return provider;
	}

	/**
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
	 * @since 2.0
	 */
	public String getOS() {
		return os;
	}

	/**
	 * @since 2.0
	 */
	public String getWS() {
		return ws;
	}

	/**
	 * @since 2.0
	 */
	public String getNL() {
		return nl;
	}

	/**
	 * @since 2.0
	 */
	public String getApplication() {
		return application;
	}

	/**
	 * @since 2.0
	 */	
	public InstallHandlerModel getInstallHandlerModel() {
		return installHandler;
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
	public URLEntryModel getCopyrightModel() {
		return copyright;
	}

	/**
	 * @since 2.0
	 */
	public URLEntryModel getLicenseModel() {
		return license;
	}

	/**
	 * @since 2.0
	 */
	public URLEntryModel getUpdateSiteEntryModel() {
		return updateSiteInfo;
	}

	/**
	 * @since 2.0
	 */
	public URLEntryModel[] getDiscoverySiteEntryModels() {
		if (discoverySiteInfo == null)
			return new URLEntryModel[0];
			
		return (URLEntryModel[]) discoverySiteInfo.toArray(arrayTypeFor(discoverySiteInfo));
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
			
		return (NonPluginEntryModel[]) nonPluginEntries.toArray(arrayTypeFor(nonPluginEntries));
	}

	/**
	 * @since 2.0
	 */	
	public ContentGroupModel[] getGroupEntryModels() {
		if (groupEntries == null)
			return new ContentGroupModel[0];
			
		return (ContentGroupModel[]) groupEntries.toArray(arrayTypeFor(groupEntries));
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
	 * @since 2.0
	 */
	public void setApplication(String application) {
		assertIsWriteable();
		this.application = application;
	}

	/**
	 * @since 2.0
	 */	
	public void setInstallHandlerModel(InstallHandlerModel installHandler) {
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
	public void setGroupEntryModels(ContentGroupModel[] groupEntries) {
		assertIsWriteable();
		if (groupEntries == null)
			this.groupEntries = null;
		else
			this.groupEntries = new ArrayList(Arrays.asList(groupEntries));
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
	public void addGroupEntryModel(ContentGroupModel groupEntry) {
		assertIsWriteable();
		if (this.groupEntries == null)
			this.groupEntries = new ArrayList();
		if (!this.groupEntries.contains(groupEntry))
			this.groupEntries.add(groupEntry);
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
	public void removeGroupEntryModel(ContentGroupModel groupEntry) {
		assertIsWriteable();
		if (this.groupEntries != null)
			this.groupEntries.remove(groupEntry);
	}
	
	/**
	 * @since 2.0
	 */
	public void markReadOnly() {		
		markReferenceReadOnly(getDescriptionModel());
		markReferenceReadOnly(getCopyrightModel());
		markReferenceReadOnly(getLicenseModel());
		markReferenceReadOnly(getUpdateSiteEntryModel());
		markListReferenceReadOnly(getDiscoverySiteEntryModels());
		markListReferenceReadOnly(getImportModels());
		markListReferenceReadOnly(getPluginEntryModels());
		markListReferenceReadOnly(getNonPluginEntryModels());
		markListReferenceReadOnly(getGroupEntryModels());
	}
	
	/**
	 * @since 2.0
	 */
	public void resolve(URL base, ResourceBundle bundle) throws MalformedURLException {
		// resolve local elements
		localizedLabel = resolveNLString(bundle, label);
		localizedProvider = resolveNLString(bundle,provider);
		imageURL = resolveURL(base, bundle,imageURLString);

		// delegate to references		
		resolveReference(getDescriptionModel(), base, bundle);
		resolveReference(getCopyrightModel(), base, bundle);
		resolveReference(getLicenseModel(), base, bundle);
		resolveReference(getUpdateSiteEntryModel(), null, bundle);
		resolveListReference(getDiscoverySiteEntryModels(), null, bundle);
		resolveListReference(getImportModels(), base, bundle);
		resolveListReference(getPluginEntryModels(), base, bundle);
		resolveListReference(getNonPluginEntryModels(), base, bundle);
		resolveListReference(getGroupEntryModels(), base, bundle);
	}
}
