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

import org.eclipse.core.internal.runtime.InternalPlatform;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

/**
 * The DecorationScheduler is the class that handles the
 * decoration of elements using a background thread.
 */
public class DecorationScheduler implements IResourceChangeListener {

	// When decorations are computed they are added to this cache via decorated() method
	private Map resultCache = Collections.synchronizedMap(new HashMap());

	// Resources that need an icon and text computed for display to the user
	private List awaitingDecoration = new ArrayList();

	private Map awaitingDecorationValues = new HashMap();

	private DecoratorManager decoratorManager;

	private boolean shutdown = false;

	private Thread decoratorUpdateThread;

	//The number of results to batch before the label changed is sent
	private final int NUM_TO_BATCH = 50;

	/**
	 * Return a new instance of the receiver configured for
	 * the supplied DecoratorManager.
	 * @param manager
	 */
	DecorationScheduler(DecoratorManager manager) {
		decoratorManager = manager;

		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
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
			queueForDecoration(element, adaptedElement);
			return text;
		} else{
			return decoration.decorateWithText(text);
		}
	}
	/**
	 * Queue the element and its adapted value if it has not been
	 * already.
	 * @param element
	 * @param adaptedElement. The adapted value of element. May be null.
	 */

	private synchronized void queueForDecoration(
		Object element,
		Object adaptedElement) {

		//Lazily create the thread that calculates the decoration for a resource
		if (decoratorUpdateThread == null) {
			createDecoratorThread();
			decoratorUpdateThread.start();
		}

		if (!awaitingDecorationValues.containsKey(element)) {
			DecorationReference reference =
				new DecorationReference(element, adaptedElement);
			awaitingDecorationValues.put(element, reference);
			awaitingDecoration.add(element);
			//Notify the receiver as the next method is
			//synchronized on the receiver.
			notify();
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
			queueForDecoration(element, adaptedElement);
			return image;
		} else
			return decoration.decorateWithOverlays(
				image,
				decoratorManager.getLightweightManager().getOverlayCache());
	}

	/**
	 * Add the list of decorations for the resource supplied
	 * @param resources
	 * @param decorationResults
	 */
	public void decorated(final Object[] elements) {
		
		//No need to send a message for nothing
		if(elements.length == 0)
			return;

		//Don't bother if we are shutdown now
		if (!shutdown) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					decoratorManager.labelProviderChanged(
						new LabelProviderChangedEvent(
							decoratorManager,
							elements));					
					resultCache.clear();
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
			// Wait for the decorator thread to finish before returning.
			decoratorUpdateThread.join();
		} catch (InterruptedException e) {
		}
	}

	/**
	 * @see org.eclipse.core.resources.IResourceChangeListener#resourceChanged(org.eclipse.core.resources.IResourceChangeEvent)
	 */
	public void resourceChanged(IResourceChangeEvent event) {
		IResourceDelta delta = event.getDelta();
		if (delta != null) {
			try {
				final List changedObjects = new ArrayList();
				delta.accept(new IResourceDeltaVisitor() {
					public boolean visit(IResourceDelta delta)
						throws CoreException {
						IResource resource = delta.getResource();

						if (resource.getType() == IResource.ROOT) {
							// continue with the delta
							return true;
						}

						switch (delta.getKind()) {
							case IResourceDelta.REMOVED :
								// remove the cached decoration for any removed resource
								resultCache.remove(resource);
								break;
							case IResourceDelta.CHANGED :
								// for changed resources remove the result as it will need to 
								//be recalculated.
								resultCache.remove(resource);
						}

						return true;
					}
				});

				changedObjects.clear();
			} catch (CoreException exception) {
				InternalPlatform.getRuntimePlugin().getLog().log(
					exception.getStatus());
			}
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

			if (awaitingDecoration.isEmpty()) {
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

					// if next() returned null, we are done and should shut down.
					if (reference == null) {
						return;
					}

					//Don't decorate if there is already a pending result
					if (!resultCache.containsKey(reference.getElement())) {

						LinkedList prefixes = new LinkedList();
						LinkedList suffixes = new LinkedList();

						decoratorManager
							.getLightweightManager()
							.getPrefixAndSuffix(
							reference.getElement(),
							reference.getAdaptedElement(),
							prefixes,
							suffixes);

						ImageDescriptor[] descriptors =
							decoratorManager
								.getLightweightManager()
								.findOverlays(
								reference.getElement(),
								reference.getAdaptedElement());

						if (prefixes.size() > 0 || descriptors != null) {
							DecorationResult result =
								new DecorationResult(
									prefixes,
									suffixes,
									descriptors);
							resultCache.put(reference.getElement(), result);
						}
					}

					// notify that decoration is ready
					if (awaitingDecoration.isEmpty()) {
						decorated(resultCache.keySet().toArray());
					}
				}
			};
		};

		decoratorUpdateThread = new Thread(decorationRunnable, "Decoration");
		decoratorUpdateThread.setPriority(Thread.MIN_PRIORITY);
	}
}
