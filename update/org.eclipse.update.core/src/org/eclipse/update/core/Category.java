package org.eclipse.update.core;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import org.eclipse.update.core.model.CategoryModel;
import org.eclipse.update.core.model.URLEntryModel;

/**
 * Default Implementation of ICategory
 */
public class Category extends CategoryModel implements ICategory {
	
	/**
	 * Default Constructor
	 */
	public Category(){}


	/**
	 * Constructor
	 */
	public Category(String name, String label){
		setName(name);
		setLabel(label);
	}

	/*
	 * @see ICategory#getDescription()
	 */
	public IURLEntry getDescription() {
		return (IURLEntry)getDescriptionModel();
	}

	}

