package org.eclipse.ui.internal;

/*******************************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 *
 * Contributors:
 * IBM - Initial implementation
 ******************************************************************************/

import java.util.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import org.eclipse.jface.resource.ImageDescriptor;

/*
 * (c) Copyright IBM Corp. 2002.
 * All Rights Reserved.
 */

/**
 * The LightweightDecoratorManager is a decorator manager
 * that encapsulates the behavior for the lightweight decorators.
 */
class LightweightDecoratorManager {

	//The cachedDecorators are a 1-many mapping of type to full decorator.
	private HashMap cachedLightweightDecorators = new HashMap();

	//The lightweight definitionsread from the registry
	private LightweightDecoratorDefinition[] lightweightDefinitions;

	private static final LightweightDecoratorDefinition[] EMPTY_LIGHTWEIGHT_DEF =
		new LightweightDecoratorDefinition[0];

	private OverlayCache overlayCache = new OverlayCache();

	LightweightDecoratorManager(LightweightDecoratorDefinition[] definitions) {
		super();
		lightweightDefinitions = definitions;
	}

	/**
	 * Get the lightweight definitions for the receiver.
	 * @return LightweightDecoratorDefinition[]
	 */
	LightweightDecoratorDefinition[] getDefinitions() {
		return lightweightDefinitions;
	}

	/**
	 * Return the enabled lightweight decorator definitions.
	 * @return LightweightDecoratorDefinition[]
	 */
	LightweightDecoratorDefinition[] enabledDefinitions() {
		ArrayList result = new ArrayList();
		for (int i = 0; i < lightweightDefinitions.length; i++) {
			if (lightweightDefinitions[i].isEnabled())
				result.add(lightweightDefinitions[i]);
		}
		LightweightDecoratorDefinition[] returnArray =
			new LightweightDecoratorDefinition[result.size()];
		result.toArray(returnArray);
		return returnArray;
	}

	/**
	 * Reset any cached values.
	 */
	void reset() {
		cachedLightweightDecorators = new HashMap();
	}

	/**
	* Shutdown the decorator manager by disabling all
	* of the decorators so that dispose() will be called
	* on them.
	*/
	void shutdown() {
		//Disable all fo the enabled decorators 
		//so as to force a dispose of thier decorators
		for (int i = 0; i < lightweightDefinitions.length; i++) {
			if (lightweightDefinitions[i].isEnabled())
				lightweightDefinitions[i].setEnabledWithErrorHandling(false);
		}
		overlayCache.disposeAll();
	}

	/**
	 * Get the LightweightDecoratorDefinition with the supplied id
	 * @return LightweightDecoratorDefinition or <code>null</code> if it is not found
	 * @param decoratorId String
	 */
	LightweightDecoratorDefinition getDecoratorDefinition(String decoratorId) {
		for (int i = 0; i < lightweightDefinitions.length; i++) {
			if (lightweightDefinitions[i].getId().equals(decoratorId))
				return lightweightDefinitions[i];
		}
		return null;
	}

	/**
	* Get the lightweight  registered for elements of this type.
	*/
	LightweightDecoratorDefinition[] getDecoratorsFor(Object element) {

		if (element == null)
			return EMPTY_LIGHTWEIGHT_DEF;

		String className = element.getClass().getName();
		LightweightDecoratorDefinition[] decoratorArray =
			(LightweightDecoratorDefinition[]) cachedLightweightDecorators.get(
				className);
		if (decoratorArray != null) {
			return decoratorArray;
		}

		Collection decorators =
			DecoratorManager.getDecoratorsFor(element, enabledDefinitions());

		if (decorators.size() == 0)
			decoratorArray = EMPTY_LIGHTWEIGHT_DEF;
		else {
			decoratorArray =
				new LightweightDecoratorDefinition[decorators.size()];
			decorators.toArray(decoratorArray);
		}

		cachedLightweightDecorators.put(className, decoratorArray);
		return decoratorArray;
	}

	/**
	 * Decorate the Image supplied with the overlays for any of
	 * the enabled lightweight decorators. 
	 * @return ImageDescriptor[] is any work is done, otherwise null.
	 */
	ImageDescriptor[] findOverlays(Object element, Object adapted) {

		LightweightDecoratorDefinition[] decorators = getDecoratorsFor(element);
		ImageDescriptor[] descriptors = new ImageDescriptor[4];
		boolean decorated =
			overlayCache.findDescriptors(element, decorators, descriptors);
		if (adapted != null) {
			decorated =
				decorated
					|| overlayCache.findDescriptors(
						adapted,
						getDecoratorsFor(adapted),
						descriptors);
		}
		if (decorated)
			return descriptors;
		else
			return null;
	}
	/**
	* Fill the prefixResult and suffixResult with all of the applied prefixes
	* and suffixes.
	* 
	* @param element The source element
    * @param adapted The adapted value of element or null
    * @param prefixResult. All of the applied prefixes
    * @param suffixResult. All of the applied suffixes.
	*/
	
	void getPrefixAndSuffix(
		Object element,
		Object adapted,
		List prefixResult,
		List suffixResult) {

		LinkedList appliedDecorators = new LinkedList();
		LinkedList appliedAdaptedDecorators = new LinkedList();

		LightweightDecoratorDefinition[] decorators = getDecoratorsFor(element);

		for (int i = 0; i < decorators.length; i++) {
			if (decorators[i].getEnablement().isEnabledFor(element)) {
				//Add in reverse order for symmetry of suffixes
				appliedDecorators.addFirst(decorators[i]);
				String prefix = decorators[i].getPrefix(element);
				if (prefix != null)
					prefixResult.add(prefix);
			}
		}

		if (adapted != null) {
			LightweightDecoratorDefinition[] adaptedDecorators =
				getDecoratorsFor(adapted);
			for (int i = 0; i < adaptedDecorators.length; i++) {
				if (adaptedDecorators[i]
					.getEnablement()
					.isEnabledFor(adapted)) {
					//Add in reverse order for symmetry of suffixes
					appliedAdaptedDecorators.addFirst(adaptedDecorators[i]);
					String prefix = adaptedDecorators[i].getPrefix(adapted);
					if (prefix != null)
						prefixResult.add(prefix);
				}
			}
		}

		//Nothing happened so just return
		if (appliedDecorators.isEmpty() && appliedAdaptedDecorators.isEmpty())
			return;

		if (adapted != null) {
			Iterator appliedIterator = appliedAdaptedDecorators.iterator();
			while (appliedIterator.hasNext()) {
				String suffix =
					(
						(LightweightDecoratorDefinition) appliedIterator
							.next())
							.getSuffix(
						element);
				if (suffix != null)
					suffixResult.add(suffix);
			}
		}

		Iterator appliedIterator = appliedDecorators.iterator();
		while (appliedIterator.hasNext()) {
			String suffix =
				(
					(LightweightDecoratorDefinition) appliedIterator
						.next())
						.getSuffix(
					element);
			if (suffix != null)
				suffixResult.add(suffix);
		}

	}
	/**
	 * Returns the overlayCache.
	 * @return OverlayCache
	 */
	OverlayCache getOverlayCache() {
		return overlayCache;
	}

}
