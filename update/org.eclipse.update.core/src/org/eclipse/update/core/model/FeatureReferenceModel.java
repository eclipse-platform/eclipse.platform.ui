package org.eclipse.update.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */ 

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import org.eclipse.update.core.ISite;

/**
 * An object which represents a feature reference.
 * <p>
 * This class may be instantiated, or further subclassed.
 * </p>
 * @since 2.0
 */

public class FeatureReferenceModel extends ModelObject {
	
	private String type;
	private String urlString;
	private URL url;
	private SiteMapModel site;
	private List /* of String*/ categoryNames;
	
	/**
	 * Creates an uninitialized model object.
	 * 
	 * @since 2.0
	 */
	public FeatureReferenceModel() {
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
	public SiteMapModel getSiteModel() {
		return site;
	}
	
	/**
	 * @since 2.0
	 */	
	public String getURLString() {
		return urlString;
	}
	
	/**
	 * Returns the resolved URL for the feature reference.
	 * 
	 * @return url, or <code>null</code>
	 * @since 2.0
	 */
	public URL getURL() {
		return url;
	}

	/**
	 * @since 2.0
	 */	
	public String[] getCategoryNames() {		
		if (categoryNames == null)
			return new String[0];
			
		return (String[]) categoryNames.toArray(new String[0]);
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
	public void setSiteModel(SiteMapModel site) {
		assertIsWriteable();
		this.site = site;
	}
	
	/**
	 * @since 2.0
	 */
	public void setURLString(String urlString) {
		assertIsWriteable();
		this.urlString = urlString;
		this.url = null;
	}

	/**
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
	 * @since 2.0
	 */
	public void removeCategoryName(String categoryName) {
		assertIsWriteable();
		if (this.categoryNames != null)
			this.categoryNames.remove(categoryName);
	}
	
	/**
	 * @since 2.0
	 */
	public void resolve(URL base, ResourceBundle bundle) throws MalformedURLException {
		// resolve local elements
		url = resolveURL(base, bundle,urlString);
	}
}
