/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.decorators;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

/**
 * The DecorationScheduler is the class that handles the
 * decoration of elements using a background thread.
 */
public class DecorationScheduler {

	// When decorations are computed they are added to this cache via decorated() method
	private Map resultCache = new HashMap();

	// Objects that need an icon and text computed for display to the user
	private List awaitingDecoration = new ArrayList();

	// Objects that are awaiting a label update.
	private Set pendingUpdate = new HashSet();

	private Object resultLock = new Object();

	private Map awaitingDecorationValues = new HashMap();

	private DecoratorManager decoratorManager;

	private boolean shutdown = false;

	private Thread decoratorUpdateThread;

	/**
	 * Return a new instance of the receiver configured for
	 * the supplied DecoratorManager.
	 * @param manager
	 */
	DecorationScheduler(DecoratorManager manager) {
		decoratorManager = manager;
	}

	/**
	* Decorate the text for the receiver. If it has already
	* been done then return the result, otherwise queue
	* it for decoration.
	* 
	* @return String
	* @param text
	* @param element
	* @param adaptedElement. The adapted value of element. May be null.
	*/

	public String decorateWithText(
		String text,
		Object element,
		Object adaptedElement) {

		//We do not support decoration of null
		if (element == null)
			return text;

		DecorationResult decoration =
			(DecorationResult) resultCache.get(element);

		if (decoration == null) {
			queueForDecoration(element, adaptedElement, false);
			return text;
		} else
			return decoration.decorateWithText(text);

	}
	/**
	 * Queue the element and its adapted value if it has not been
	 * already.
	 * @param element
	 * @param adaptedElement. The adapted value of element. May be null.
	 * @param forceUpdate. If true then a labelProviderChanged is fired
	 * 	whether decoration occured or not.
	 */

	synchronized void queueForDecoration(
		Object element,
		Object adaptedElement,
		boolean forceUpdate) {

		//Lazily create the thread that calculates the decoration for a resource
		if (decoratorUpdateThread == null) {
			createDecoratorThread();
			decoratorUpdateThread.start();
		}

		if (!awaitingDecorationValues.containsKey(element)) {
			DecorationReference reference =
				new DecorationReference(element, adaptedElement);
			reference.setForceUpdate(forceUpdate);
			awaitingDecorationValues.put(element, reference);
			awaitingDecoration.add(element);
			//Notify the receiver as the next method is
			//synchronized on the receiver.
			notifyAll();
		}

	}

	/**
	 * Decorate the supplied image, element and its adapted value.
	 * 
	 * @return Image
	 * @param image
	 * @param element
	 * @param adaptedElement. The adapted value of element. May be null.
	 * 
	 */
	public Image decorateWithOverlays(
		Image image,
		Object element,
		Object adaptedElement) {

		//We do not support decoration of null
		if (element == null)
			return image;

		DecorationResult decoration =
			(DecorationResult) resultCache.get(element);

		if (decoration == null) {
			queueForDecoration(element, adaptedElement, false);
			return image;
		} else
			return decoration.decorateWithOverlays(
				image,
				decoratorManager.getLightweightManager().getOverlayCache());
	}

	/**
	 * Execute a label update using the pending decorations.
	 * @param resources
	 * @param decorationResults
	 */
	public void decorated() {

		//Don't bother if we are shutdown now
		if (!shutdown) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {

					if (pendingUpdate.isEmpty())
						return;
					synchronized (resultLock) {
						//Get the elements awaiting update and then
						//clear the list
						Object[] elements =
							pendingUpdate.toArray(
								new Object[pendingUpdate.size()]);
						pendingUpdate.clear();
						decoratorManager.fireListeners(
							new LabelProviderChangedEvent(
								decoratorManager,
								elements));
						//Other decoration requests may have occured due to
						//updates. Only clear the results if there are none pending.
						if (awaitingDecoration.isEmpty())
							resultCache.clear();
					}
				}
			});

		}
	}

	/**
	 * Shutdown the decoration thread.
	 */
	void shutdown() {
		shutdown = true;
		// Wake the thread up if it is asleep.
		synchronized (this) {
			notifyAll();
		}
		try {
			if (decoratorUpdateThread != null)
				// Wait for the decorator thread to finish before returning.
				decoratorUpdateThread.join();
		} catch (InterruptedException e) {
		}
	}

	/**
	 * Get the next resource to be decorated.
	 * @return IResource
	 */
	synchronized DecorationReference next() {
		try {
			if (shutdown)
				return null;

			while (!shutdown && awaitingDecoration.isEmpty()) {
				wait();
			}
			// We were awakened.
			if (shutdown) {
				// The decorator was awakened by the plug-in as it was shutting down.
				return null;
			}
			Object element = awaitingDecoration.remove(0);

			return (DecorationReference) awaitingDecorationValues.remove(
				element);
		} catch (InterruptedException e) {
		}
		return null;
	}

	/**
	 * Create the Thread used for running decoration.
	 */
	private void createDecoratorThread() {
		Runnable decorationRunnable = new Runnable() {
			/* @see Runnable#run()
				*/
			public void run() {
				while (true) {
					// will block if there are no resources to be decorated
					DecorationReference reference = next();
					DecorationBuilder cacheResult = new DecorationBuilder();

					// if next() returned null, we are done and should shut down.
					if (reference == null) {
						return;
					}

					//Don't decorate if there is already a pending result
					Object element = reference.getElement();
					Object adapted = reference.getAdaptedElement();
					boolean elementIsCached = true;
					DecorationResult adaptedResult = null;

					//Synchronize on the result lock as we want to
					//be sure that we do not try and decorate during
					//label update servicing.
					synchronized (resultLock) {
						elementIsCached = resultCache.containsKey(element);
						if (elementIsCached) {
							pendingUpdate.add(element);
						}
						if (adapted != null) {
							adaptedResult =
								(DecorationResult) resultCache.get(adapted);
						}
					}
					if (!elementIsCached) {
						//Just build for the resource first
						if (adapted != null) {
							if (adaptedResult == null) {
								decoratorManager
									.getLightweightManager()
									.getDecorations(
									adapted,
									cacheResult);
								if (cacheResult.hasValue()) {
									adaptedResult = cacheResult.createResult();
								}
							} else {
								// If we already calculated the decoration 
								// for the adapted element, reuse the result.
								cacheResult.applyResult(adaptedResult);
								// Set adaptedResult to null to indicate that
								// we do not need to cache the result again.
								adaptedResult = null;
							}
						}

						//Now add in the results for the main object

						decoratorManager
							.getLightweightManager()
							.getDecorations(
							element,
							cacheResult);

						//If we should update regardless then put a result anyways
						if (cacheResult.hasValue()
							|| reference.shouldForceUpdate()) {

							//Synchronize on the result lock as we want to
							//be sure that we do not try and decorate during
							//label update servicing.
							//Note: resultCache and pendingUpdate modifications
							//must be done atomically.  
							synchronized (resultLock) {
								if (adaptedResult != null) {
									resultCache.put(adapted, adaptedResult);
								}
								//Only add something to look up if it is interesting
								if (cacheResult.hasValue()) {
									resultCache.put(
										element,
										cacheResult.createResult());
								}

								//Add an update for only the original element to 
								//prevent multiple updates and clear the cache.
								pendingUpdate.add(element);
							}
						};
					}

					// Only notify listeners when we have exhausted the
					// queue of decoration requests.
					if (awaitingDecoration.isEmpty()) {
						decorated();
					}

				}
			};
		};

		decoratorUpdateThread = new Thread(decorationRunnable, "Decoration"); //$NON-NLS-1$
		decoratorUpdateThread.setDaemon(true);
		decoratorUpdateThread.setPriority(Thread.MIN_PRIORITY);
	}

	/**
	 * An external update request has been made. Clear the results as
	 * they are likely obsolete now.
	 */
	void clearResults() {
		synchronized (resultLock) {
			resultCache.clear();
		}

	}
}
