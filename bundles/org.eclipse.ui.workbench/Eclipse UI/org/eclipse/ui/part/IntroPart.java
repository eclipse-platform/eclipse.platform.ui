/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.part;

import org.eclipse.core.commands.common.EventManager;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.intro.IntroMessages;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.internal.util.Util;
import org.eclipse.ui.intro.IIntroPart;
import org.eclipse.ui.intro.IIntroSite;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * Abstract base implementation of an intro part.
 * <p>
 * Subclasses must implement the following methods:
 * <ul>
 * <li><code>createPartControl</code>- to create the intro part's controls
 * </li>
 * <li><code>setFocus</code>- to accept focus</li>
 * <li><code>standbyStateChanged</code>- to change the standby mode</li>
 * </ul>
 * </p>
 * <p>
 * Subclasses may extend or reimplement the following methods as required:
 * <ul>
 * <li><code>setInitializationData</code>- extend to provide additional
 * initialization when the intro extension is instantiated</li>
 * <li><code>init(IIntroSite, IMemento)</code>- extend to provide additional
 * initialization when intro is assigned its site</li>
 * <li><code>dispose</code>- extend to provide additional cleanup</li>
 * <li><code>getAdapter</code>- reimplement to make their intro adaptable
 * </li>
 * </ul>
 * </p>
 * @since 3.0
 */
public abstract class IntroPart extends EventManager implements IIntroPart,
		IExecutableExtension {

    private IConfigurationElement configElement;

    private ImageDescriptor imageDescriptor;

    private IIntroSite partSite;

    private Image titleImage;

	private String titleLabel;

    /**
     * Creates a new intro part.
     */
    protected IntroPart() {
        super();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.intro.IIntroPart#addPropertyListener(org.eclipse.ui.IPropertyListener)
     */
    public void addPropertyListener(IPropertyListener l) {
        addListenerObject(l);
    }

    /*
     * (non-Javadoc) Creates the SWT controls for this intro part. <p>
     * Subclasses must implement this method. For a detailed description of the
     * requirements see <code> IIntroPart </code></p>
     * 
     * @param parent the parent control
     * 
     * @see IIntroPart
     */
    public abstract void createPartControl(Composite parent);

    /**
     * The <code>IntroPart</code> implementation of this
     * <code>IIntroPart</code> method disposes the title image loaded by
     * <code>setInitializationData</code>. Subclasses may extend.
     */
    public void dispose() {
        if (titleImage != null) {
            JFaceResources.getResources().destroyImage(imageDescriptor);
            titleImage = null;
        }

        // Clear out the property change listeners as we
        // should not be notifying anyone after the part
        // has been disposed.
        clearListeners();
    }

    /**
     * Fires a property changed event.
     * 
     * @param propertyId
     *            the id of the property that changed
     */
    protected void firePropertyChange(final int propertyId) {
        Object[] array = getListeners();
        for (int nX = 0; nX < array.length; nX++) {
            final IPropertyListener l = (IPropertyListener) array[nX];
            Platform.run(new SafeRunnable() {

                public void run() {
                    l.propertyChanged(this, propertyId);
                }
            });
        }
    }

    /**
     * This implementation of the method declared by <code>IAdaptable</code>
     * passes the request along to the platform's adapter manager; roughly
     * <code>Platform.getAdapterManager().getAdapter(this, adapter)</code>.
     * Subclasses may override this method (however, if they do so, they should
     * invoke the method on their superclass to ensure that the Platform's
     * adapter manager is consulted).
     */
    public Object getAdapter(Class adapter) {
        return Platform.getAdapterManager().getAdapter(this, adapter);
    }

    /**
     * Returns the configuration element for this part. The configuration
     * element comes from the plug-in registry entry for the extension defining
     * this part.
     * 
     * @return the configuration element for this part
     */
    protected IConfigurationElement getConfigurationElement() {
        return configElement;
    }

    /**
     * Returns the default title image.
     * 
     * @return the default image
     */
    protected Image getDefaultImage() {
        return PlatformUI.getWorkbench().getSharedImages().getImage(
                ISharedImages.IMG_DEF_VIEW);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.intro.IIntroPart#getIntroSite()
     */
    public final IIntroSite getIntroSite() {
        return partSite;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.intro.IIntroPart#getTitleImage()
     */
    public Image getTitleImage() {
        if (titleImage != null) {
            return titleImage;
        }
        return getDefaultImage();
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.intro.IIntroPart#getTitle()
     */
    public String getTitle() {
    	if (titleLabel != null) {
    		return titleLabel;
    	}
    	return getDefaultTitle();
    }

    /**
     * Return the default title string.
     * 
	 * @return the default title string
	 */
	private String getDefaultTitle() {
		return IntroMessages.Intro_default_title;
	}

	/**
     * The base implementation of this {@link org.eclipse.ui.intro.IIntroPart}method ignores the
     * memento and initializes the part in a fresh state. Subclasses may extend
     * to perform any state restoration, but must call the super method.
     * 
     * @param site
     *            the intro site
     * @param memento
     *            the intro part state or <code>null</code> if there is no
     *            previous saved state
     * @exception PartInitException
     *                if this part was not initialized successfully
     */
    public void init(IIntroSite site, IMemento memento)
            throws PartInitException {
        setSite(site);
    }

    /**
     * Sets the part site.
     * <p>
     * Subclasses must invoke this method from {@link org.eclipse.ui.intro.IIntroPart#init(IIntroSite, IMemento)}.
     * </p>
     *
     * @param site the intro part site
     */
    protected void setSite(IIntroSite site) {
        this.partSite = site;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.intro.IIntroPart#removePropertyListener(org.eclipse.ui.IPropertyListener)
     */
    public void removePropertyListener(IPropertyListener l) {
        removeListenerObject(l);
    }

    /**
     * The base implementation of this {@link org.eclipse.ui.intro.IIntroPart} method does nothing.
     * Subclasses may override.
     * 
     * @param memento
     *            a memento to receive the object state
     */
    public void saveState(IMemento memento) {
        //no-op
    }

    /*
     * (non-Javadoc) Asks this part to take focus within the workbench. 
     * <p>
     * Subclasses must implement this method. For a detailed description of the
     * requirements see <code>IIntroPart</code>
     * </p>
     * 
     * @see IIntroPart
     */
    public abstract void setFocus();

    /**
     * The <code>IntroPart</code> implementation of this
     * <code>IExecutableExtension</code> records the configuration element in
     * and internal state variable (accessible via <code>getConfigElement</code>).
     * It also loads the title image, if one is specified in the configuration
     * element. Subclasses may extend.
     * 
     * Should not be called by clients. It is called by the core plugin when
     * creating this executable extension.
     */
    public void setInitializationData(IConfigurationElement cfig,
            String propertyName, Object data) {

        // Save config element.
        configElement = cfig;

        titleLabel = cfig.getAttribute(IWorkbenchRegistryConstants.ATT_LABEL);
        
        // Icon.
        String strIcon = cfig.getAttribute(IWorkbenchRegistryConstants.ATT_ICON);
        if (strIcon == null) {
			return;
		}

        imageDescriptor = AbstractUIPlugin.imageDescriptorFromPlugin(
                configElement.getNamespace(), strIcon);

        if (imageDescriptor == null) {
			return;
		}

        Image image = JFaceResources.getResources().createImageWithDefault(imageDescriptor);
        titleImage = image;
    }

    /**
     * Sets or clears the title image of this part.
     * 
     * @param titleImage
     *            the title image, or <code>null</code> to clear
     */
    protected void setTitleImage(Image titleImage) {
        Assert.isTrue(titleImage == null || !titleImage.isDisposed());
        //Do not send changes if they are the same
        if (this.titleImage == titleImage) {
			return;
		}
        this.titleImage = titleImage;
        firePropertyChange(IIntroPart.PROP_TITLE);
    }
    
    /**
     * Set the title string for this part.
     * 
     * @param titleLabel the title string.  Must not be <code>null</code>.
     * @since 3.2
     */
    protected void setTitle(String titleLabel) {
    	Assert.isNotNull(titleLabel);
    	if (Util.equals(this.titleLabel, titleLabel))
    		return;
    	this.titleLabel = titleLabel;
    	firePropertyChange(IIntroPart.PROP_TITLE);
    }
}
