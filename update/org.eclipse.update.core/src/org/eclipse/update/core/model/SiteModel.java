package org.eclipse.update.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */ 

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * An object which represents a site map.
 * <p>
 * This class may be instantiated, or further subclassed.
 * </p>
 * @since 2.0
 */

public class SiteModel extends ModelObject {
	
	private String type;
	private URLEntryModel description;
	private List /*of FeatureReferenceModel*/ featureReferences;
	private List /*of ArchiveReferenceModel*/ archiveReferences;
	private Set /*of CategoryModel*/ categories;
	private String locationURLString;
	private URL locationURL;
	
	/**
	 * Creates an uninitialized model object.
	 * 
	 * @since 2.0
	 */
	public SiteModel() {
		super();
	}

	
	/**
	 * @since 2.0
	 */
	public String getType() {
		return type;
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
	public FeatureReferenceModel[] getFeatureReferenceModels() {
		if (featureReferences == null)
			return new FeatureReferenceModel[0];
			
		return (FeatureReferenceModel[]) featureReferences.toArray(arrayTypeFor(featureReferences));
	}

	/**
	 * @since 2.0
	 */
	public ArchiveReferenceModel[] getArchiveReferenceModels() {
		if (archiveReferences == null)
			return new ArchiveReferenceModel[0];
			
		return (ArchiveReferenceModel[]) archiveReferences.toArray(arrayTypeFor(archiveReferences));
	}

	/**
	 * @since 2.0
	 */
	public CategoryModel[] getCategoryModels() {
		if (categories == null)
			return new CategoryModel[0];
			
		return (CategoryModel[]) categories.toArray(arrayTypeFor(categories));
	}

	/**
	 * Gets the locationURLString.
	 * @return Returns a String
	 */
	public String getLocationURLString() {
		return locationURLString;
	}

	
	
	/**
	 * Gets the locationURL.
	 * @return Returns a URL
	 */
	public URL getLocationURL() {
		return locationURL;
	}

	/**
	 * Sets the locationURLString.
	 * @param locationURLString The locationURLString to set
	 */
	public void setLocationURLString(String locationURLString) {
		assertIsWriteable();
		this.locationURLString = locationURLString;
	}

	
	/**
	 * @since 2.0
	 */
	public void setType(String type) {
		assertIsWriteable();
		this.type = type;
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
	public void setFeatureReferenceModels(FeatureReferenceModel[] featureReferences) {
		assertIsWriteable();
		if (featureReferences == null)
			this.featureReferences = null;
		else
			this.featureReferences = new ArrayList(Arrays.asList(featureReferences));
	}

	/**
	 * @since 2.0
	 */
	public void setArchiveReferenceModels(ArchiveReferenceModel[] archiveReferences) {
		assertIsWriteable();
		if (archiveReferences == null)
			this.archiveReferences = null;
		else
			this.archiveReferences = new ArrayList(Arrays.asList(archiveReferences));
	}

	/**
	 * @since 2.0
	 */
	public void setCategoryModels(CategoryModel[] categories) {
		assertIsWriteable();
		if (categories == null)
			this.categories = null;
		else{
			this.categories = new TreeSet(CategoryModel.getComparator());
			this.categories.addAll(Arrays.asList(categories));
		}
	}

	/**
	 * @since 2.0
	 */
	public void addFeatureReferenceModel(FeatureReferenceModel featureReference) {
		assertIsWriteable();
		if (this.featureReferences == null)
			this.featureReferences = new ArrayList();
		if (!this.featureReferences.contains(featureReference))
			this.featureReferences.add(featureReference);
	}

	/**
	 * @since 2.0
	 */
	public void addArchiveReferenceModel(ArchiveReferenceModel archiveReference) {
		assertIsWriteable();
		if (this.archiveReferences == null)
			this.archiveReferences = new ArrayList();
		if (!this.archiveReferences.contains(archiveReference))
			this.archiveReferences.add(archiveReference);
	}

	/**
	 * @since 2.0
	 */
	public void addCategoryModel(CategoryModel category) {
		assertIsWriteable();
		if (this.categories == null)
			this.categories = new TreeSet(CategoryModel.getComparator());
		if (!this.categories.contains(category))
			this.categories.add(category);
	}

	/**
	 * @since 2.0
	 */
	public void removeFeatureReferenceModel(FeatureReferenceModel featureReference) {
		assertIsWriteable();
		if (this.featureReferences != null)
			this.featureReferences.remove(featureReference);
	}

	/**
	 * @since 2.0
	 */
	public void removeArchiveReferenceModel(ArchiveReferenceModel archiveReference) {
		assertIsWriteable();
		if (this.archiveReferences != null)
			this.archiveReferences.remove(archiveReference);
	}

	/**
	 * @since 2.0
	 */
	public void removeCategoryModel(CategoryModel category) {
		assertIsWriteable();
		if (this.categories != null)
			this.categories.remove(category);
	}
	
	/**
	 * @since 2.0
	 */
	public void markReadOnly() {
		super.markReadOnly();		
		markReferenceReadOnly(getDescriptionModel());
		markListReferenceReadOnly(getFeatureReferenceModels());
		markListReferenceReadOnly(getArchiveReferenceModels());
		markListReferenceReadOnly(getCategoryModels());
	}
	
	/**
	 * @since 2.0
	 */
	public void resolve(URL base, ResourceBundle bundle) throws MalformedURLException {	
		
		// Archives and feature are relative to location URL is teh Site element had 
		// a URL tag: see spec	
		locationURL = resolveURL(base,bundle,getLocationURLString());		
		if (locationURL==null) locationURL= base;
		resolveListReference(getFeatureReferenceModels(), locationURL, bundle);
		resolveListReference(getArchiveReferenceModels(), locationURL, bundle);

		resolveReference(getDescriptionModel(), base, bundle);
		resolveListReference(getCategoryModels(), base, bundle);
	}

	
}
