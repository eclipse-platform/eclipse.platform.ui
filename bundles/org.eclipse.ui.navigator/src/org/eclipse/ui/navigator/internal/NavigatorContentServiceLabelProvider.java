/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.navigator.internal;


import org.eclipse.core.commands.common.EventManager;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.navigator.CommonViewer;
import org.eclipse.ui.navigator.INavigatorContentService;

/**
 * <p>
 * Provides relevant labels based on the associated
 * {@link INavigatorContentService}for the contents of a
 * TreeViewer .
 * <p>
 * 
 * <p>
 * Except for the dependency on
 * {@link INavigatorContentService}, this class has no
 * dependencies on the rest of the Common Navigator framework. Tree viewers that would like to use
 * the extensions defined by the Common Navigator, without using the actual view part or other
 * pieces of functionality (filters, sorting, etc) may choose to use this class, in effect using an
 * extensible label provider.
 * </p>
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as part of a work in
 * progress. There is a guarantee neither that this API will work nor that it will remain the same.
 * Please do not use this API without consulting with the Platform/UI team.
 * </p>
 * 
 * @since 3.2
 * 
 * @see org.eclipse.ui.navigator.internal.NavigatorContentService
 * @see org.eclipse.ui.navigator.internal.NavigatorContentServiceContentProvider
 */
public class NavigatorContentServiceLabelProvider extends EventManager
		implements ILabelProvider {

	private final ILabelDecorator decorator;
	private final INavigatorContentService contentService;
	private final boolean isContentServiceSelfManaged;

  
	/**
	 * <p>
	 * Uses the supplied content service to acquire the available extensions.
	 * </p>
	 * 
	 * @param aContentService
	 *            The associated NavigatorContentService that should be used to acquire information.
	 */
	public NavigatorContentServiceLabelProvider(INavigatorContentService aContentService) {
		contentService = aContentService;
		decorator = PlatformUI.getWorkbench().getDecoratorManager().getLabelDecorator();
		isContentServiceSelfManaged = false;
	}

	/**
	 * <p>
	 * Return the appropriate image for anElement. The image will be used as the icon for anElement
	 * when displayed in the tree viewer. This method uses information from its contentService to
	 * know what extensions to use to supply the correct label.
	 * </p>
	 * {@inheritDoc}
	 * 
	 * @param anElement 
	 *            An element from the Tree Viewer 
	 * @return The Image that will be used as the icon when anElement is displayed in the viewer.
	 * @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
	 */
	public Image getImage(Object anElement) {
		ILabelProvider[] labelProviders = contentService.findRelevantLabelProviders(anElement);
		Image image = null;
		for (int i = 0; i < labelProviders.length && image == null; i++)
			image = labelProviders[i].getImage(anElement);
		return image == null ? null : decorator.decorateImage(image, anElement);  
	}

	/**
	 * <p>
	 * Return a String representation of anElement to be used as the display name in the tree
	 * viewer.
	 * </p>
	 * {@inheritDoc}
	 * 
	 * @param anElement
	 *            An element from the Tree Viewer 
	 * @return The String label to display for the object when represented in the viewer.
	 * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
	 */
	public String getText(Object anElement) {
		ILabelProvider[] labelProviders = contentService.findRelevantLabelProviders(anElement);
		String text = null;
		for (int i = 0; i < labelProviders.length && text == null; i++)
			text = labelProviders[i].getText(anElement);
		// decorate the element
		return text == null ? "" : text; //$NON-NLS-1$
	}

	/**
	 * <p>
	 * Indicates whether anElelement has aProperty that affects the display of the label.
	 * </p>
	 * {@inheritDoc}
	 * 
	 * @param anElement
	 *            An element from the Tree Viewer
	 * @param aProperty
	 *            A property of the given element that could be a label provider
	 * @return True if any of the extensions enabled on anElement consider aProperty a
	 *         label-changing property.
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(java.lang.Object,
	 *      java.lang.String)
	 */
	public boolean isLabelProperty(Object anElement, String aProperty) {
		boolean result = false;
		ILabelProvider[] labelProviders = contentService.findRelevantLabelProviders(anElement);
		for (int i = 0; i < labelProviders.length && !result; i++)
			result = labelProviders[i].isLabelProperty(anElement, aProperty);
		return result;
	}


	/**
	 * <p>
	 * Label provider listeners are currently supported.
	 * </p>
	 * 
	 * {@inheritDoc}
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(org.eclipse.jface.viewers.ILabelProviderListener)
	 */
	public void addListener(ILabelProviderListener aListener) {
		addListenerObject(aListener);
	}

	/**
	 * <p>
	 * Label provider listeners are currently supported.
	 * </p>
	 * 
	 * {@inheritDoc}
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(org.eclipse.jface.viewers.ILabelProviderListener)
	 */
	public void removeListener(ILabelProviderListener aListener) {
		removeListenerObject(aListener);
	}

	/**
	 * <p>
	 * Dispose of the content service, if it was created and not supplied.
	 * </p>
	 * <p>
	 * <b>If a client uses this class outside of the framework of {@link CommonViewer}, this method
	 * must be called when finished. </b>
	 * </p>
	 * 
	 * {@inheritDoc}
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
	 */
	public void dispose() {
		if (isContentServiceSelfManaged)
			contentService.dispose();

	}
	
	/**
     * Fires a label provider changed event to all registered listeners
     * Only listeners registered at the time this method is called are notified.
     *
     * @param event a label provider changed event
     *
     * @see ILabelProviderListener#labelProviderChanged
     */
    protected void fireLabelProviderChanged(
            final LabelProviderChangedEvent event) {
        Object[] theListeners = getListeners();
        for (int i = 0; i < theListeners.length; ++i) {
            final ILabelProviderListener l = (ILabelProviderListener) theListeners[i];
            Platform.run(new SafeRunnable() {
                public void run() {
                    l.labelProviderChanged(event);
                }
            });

        }
    }

}
