package org.eclipse.ui.internal.decorators;

import java.util.*;
import java.util.List;
import java.util.ListIterator;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.graphics.Image;

/** 
 * The Decoration Result is the result of a decoration.
 */
class DecorationResult implements IDecoration {
	
	private static int DECORATOR_ARRAY_SIZE = 5;

	private List prefixes = new ArrayList();
	private List suffixes = new ArrayList();
	private ImageDescriptor[] descriptors = new ImageDescriptor[DECORATOR_ARRAY_SIZE];
	LightweightDecoratorDefinition currentDefinition;
	
	//A flag set if a value has been added
	private boolean valueSet = false;

	DecorationResult() {}

	/**
	 * Returns the descriptors.
	 * @return ImageDescriptor[]
	 */
	public ImageDescriptor[] getDescriptors() {
		return descriptors;
	}

	/**
	 * Returns the prefixes.
	 * @return List
	 */
	public List getPrefixes() {
		return prefixes;
	}

	/**
	 * Returns the suffixes.
	 * @return List
	 */
	public List getSuffixes() {
		return suffixes;
	}

	/**
	 * Decorate the Image supplied with the overlays.
	 */
	Image decorateWithOverlays(Image image, OverlayCache overlayCache) {

		//Do not try to do anything if there is no source or overlays
		if (image == null || descriptors == null)
			return image;

		return overlayCache.applyDescriptors(image, descriptors);
	}

	/**
	 * Decorate the String supplied with the prefixes and suffixes.
	 */
	String decorateWithText(String text) {

		if (prefixes.isEmpty() && suffixes.isEmpty())
			return text;

		StringBuffer result = new StringBuffer();

		ListIterator prefixIterator = prefixes.listIterator();

		while (prefixIterator.hasNext()) {
			result.append(prefixIterator.next());
		}

		result.append(text);

		ListIterator suffixIterator = suffixes.listIterator();

		while (suffixIterator.hasNext()) {
			result.append(suffixIterator.next());
		}

		return result.toString();
	}
	
	/**
	 * Set the value of the definition we are currently 
	 * working on.
	 * @param definition
	 */
	void setCurrentDefinition(LightweightDecoratorDefinition definition){
		this.currentDefinition = definition;
	}

	/**
	 * @see org.eclipse.jface.viewers.IDecoration#addOverlay(org.eclipse.jface.resource.ImageDescriptor)
	 */
	public void addOverlay(ImageDescriptor overlay) {
		int quadrant = currentDefinition.getQuadrant();
		if(descriptors[quadrant] == null)
			descriptors[quadrant] = overlay;
		valueSet = true;
	}

	/**
	 * @see org.eclipse.jface.viewers.IDecoration#addPrefix(java.lang.String)
	 */
	public void addPrefix(String prefixString) {
		prefixes.add(prefixString);
		valueSet = true;
	}

	/**
	 * @see org.eclipse.jface.viewers.IDecoration#addSuffix(java.lang.String)
	 */
	public void addSuffix(String suffixString) {
		suffixes.add(suffixString);
		valueSet = true;
	}
	
	/**
	 * Clear the current values and return a copy.
	 */
	DecorationResult copyAndClear(){
		DecorationResult newResult = new DecorationResult();
		newResult.prefixes = new ArrayList(prefixes);
		newResult.suffixes = new ArrayList(suffixes);
		newResult.descriptors = descriptors;
		
		this.prefixes.clear();
		this.suffixes.clear();
		this.descriptors = new ImageDescriptor[DECORATOR_ARRAY_SIZE];
		valueSet = false;
		
		return newResult;
	}
	
	/**
	 * Return whether or not a value has been set.
	 * @return boolean
	 */
	boolean hasValue(){
		return valueSet;
	}

}
