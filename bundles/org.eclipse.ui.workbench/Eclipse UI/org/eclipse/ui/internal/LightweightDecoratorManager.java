package org.eclipse.ui.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import org.eclipse.swt.graphics.Image;

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
	 */
	Image decorateWithOverlays(Image image, Object element, Object adapted) {
		
		//Do not try to do anything if there is no source
		if(image == null)
			return image;

		LightweightDecoratorDefinition[] decorators = getDecoratorsFor(element);
		LightweightDecoratorDefinition[] adaptedDecorators;
		if (adapted == null)
			return overlayCache.getImageFor(image, element, decorators);
		else {
			adaptedDecorators = getDecoratorsFor(adapted);
			return overlayCache.getImageFor(
				image,
				element,
				decorators,
				adapted,
				adaptedDecorators);
		}
	}
	/**
		 * Decorate the String supplied with the prefixes and suffixes
		 * for the enabled lightweight decorators.
		 *  
		 */
	String decorateWithText(String text, Object element, Object adapted) {

		LinkedList appliedDecorators = new LinkedList();
		LinkedList appliedAdaptedDecorators = new LinkedList();
		StringBuffer result = new StringBuffer();

		LightweightDecoratorDefinition[] decorators = getDecoratorsFor(element);

		for (int i = 0; i < decorators.length; i++) {
			if (decorators[i].getEnablement().isEnabledFor(element)) {
				//Add in reverse order for symmetry of suffixes
				appliedDecorators.addFirst(decorators[i]);
				String prefix = decorators[i].getPrefix(element);
				if (prefix != null)
					result.append(prefix);
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
						result.append(prefix);
				}
			}
		}

		//Nothing happened so just return the text
		if (appliedDecorators.isEmpty() && appliedAdaptedDecorators.isEmpty())
			return text;

		result.append(text);

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
					result.append(suffix);
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
				result.append(suffix);
		}
		return result.toString();

	}
}
