package org.eclipse.update.core.model;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * Feature reference model object.
 * <p>
 * This class may be instantiated or subclassed by clients. However, in most 
 * cases clients should instead instantiate or subclass the provided 
 * concrete implementation of this model.
 * </p>
 * @see org.eclipse.update.core.FeatureReference
 * @since 2.0
 */
public class FeatureReferenceModel extends ModelObject {

	private String type;
	private URL url;
	private String urlString;
	private SiteModel site;
	private List /* of String*/
	categoryNames;

	/**
	 * Creates an uninitialized feature reference model object.
	 * 
	 * @since 2.0
	 */
	public FeatureReferenceModel() {
		super();
	}

	/**
	 * Compares 2 feature reference models for equality
	 *  
	 * @param object feature reference model to compare with
	 * @return <code>true</code> if the two models are equal, 
	 * <code>false</code> otherwise
	 * @since 2.0 
	 */
	public boolean equals(Object object) {

		if (object == null)
			return false;
		if (getURL() == null)
			return false;

		if (!(object instanceof FeatureReferenceModel))
			return false;

		FeatureReferenceModel f = (FeatureReferenceModel) object;
		if (getURL().equals(f.getURL())) return true;
		
		// check if URL are file: URL as we may
		// have 2 URL pointing to the same featureReference
		// but with different representation
		// (i.e. file:/C;/ and file:C:/)
		if (!"file".equalsIgnoreCase(getURL().getProtocol())) return false;
		if (!"file".equalsIgnoreCase(f.getURL().getProtocol())) return false;		
		
		File file1 = new File(getURL().getFile());
		File file2 = new File(f.getURL().getFile());
		
		if (file1==null) return false;
		return (file1.equals(file2));
		
	}

	/**
	 * Returns the referenced feature type.
	 * 
	 * @return feature type, or <code>null</code> representing the default
	 * feature type for the site
	 * @since 2.0
	 */
	public String getType() {
		return type;
	}

	/**
	 * Returns the site model for the reference.
	 * 
	 * @return site model
	 * @since 2.0
	 */
	public SiteModel getSiteModel() {
		return site;
	}

	/**
	 * Returns the unresolved URL string for the reference.
	 *
	 * @return url string
	 * @since 2.0
	 */
	public String getURLString() {
		return urlString;
	}

	/**
	 * Returns the resolved URL for the feature reference.
	 * 
	 * @return url string
	 * @since 2.0
	 */
	public URL getURL() {
		return url;
	}

	/**
	 * Returns the names of categories the referenced feature belongs to.
	 * 
	 * @return an array of names, or an empty array.
	 * @since 2.0
	 */
	public String[] getCategoryNames() {
		if (categoryNames == null)
			return new String[0];

		return (String[]) categoryNames.toArray(new String[0]);
	}

	/**
	 * Sets the referenced feature type.
	 * Throws a runtime exception if this object is marked read-only.
	 * 
	 * @param type referenced feature type
	 * @since 2.0
	 */
	public void setType(String type) {
		assertIsWriteable();
		this.type = type;
	}

	/**
	 * Sets the site for the referenced.
	 * Throws a runtime exception if this object is marked read-only.
	 * 
	 * @param site site for the reference
	 * @since 2.0
	 */
	public void setSiteModel(SiteModel site) {
		assertIsWriteable();
		this.site = site;
	}

	/**
	 * Sets the unresolved URL for the feature reference.
	 * Throws a runtime exception if this object is marked read-only.
	 * 
	 * @param urlString unresolved URL string
	 * @since 2.0
	 */
	public void setURLString(String urlString) {
		assertIsWriteable();
		this.urlString = urlString;
		this.url = null;
	}

	/**
	 * Sets the names of categories this feature belongs to.
	 * Throws a runtime exception if this object is marked read-only.
	 * 
	 * @param categoryNames an array of category names
	 * @since 2.0
	 */
	public void setCategoryNames(String[] categoryNames) {
		assertIsWriteable();
		if (categoryNames == null)
			this.categoryNames = null;
		else
			this.categoryNames = new ArrayList(Arrays.asList(categoryNames));
	}

	/**
	 * Adds the name of a category this feature belongs to.
	 * Throws a runtime exception if this object is marked read-only.
	 * 
	 * @param categoryName category name
	 * @since 2.0
	 */
	public void addCategoryName(String categoryName) {
		assertIsWriteable();
		if (this.categoryNames == null)
			this.categoryNames = new ArrayList();
		if (!this.categoryNames.contains(categoryName))
			this.categoryNames.add(categoryName);
	}
	/**
	 * Removes the name of a categorys this feature belongs to.
	 * Throws a runtime exception if this object is marked read-only.
	 * 
	 * @param categoryName category name
	 * @since 2.0
	 */
	public void removeCategoryName(String categoryName) {
		assertIsWriteable();
		if (this.categoryNames != null)
			this.categoryNames.remove(categoryName);
	}

	/**
	 * Resolve the model object.
	 * Any URL strings in the model are resolved relative to the 
	 * base URL argument. Any translatable strings in the model that are
	 * specified as translation keys are localized using the supplied 
	 * resource bundle.
	 * 
	 * @param base URL
	 * @param bundle resource bundle
	 * @exception MalformedURLException
	 * @since 2.0
	 */
	public void resolve(URL base, ResourceBundle bundle)
		throws MalformedURLException {
		// resolve local elements
		url = resolveURL(base, bundle, urlString);
	}

}