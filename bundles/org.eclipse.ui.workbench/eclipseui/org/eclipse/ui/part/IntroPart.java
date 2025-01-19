/*******************************************************************************
 * Copyright (c) 2004, 2019 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Alexander Fedorov <alexander.fedorov@arsysop.ru> - Bug 548799
 *******************************************************************************/
package org.eclipse.ui.part;

import java.util.Objects;
import java.util.Optional;
import org.eclipse.core.commands.common.EventManager;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.ResourceLocator;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.internal.intro.IntroMessages;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.intro.IIntroPart;
import org.eclipse.ui.intro.IIntroSite;

/**
 * Abstract base implementation of an intro part.
 * <p>
 * Subclasses must implement the following methods:
 * </p>
 * <ul>
 * <li><code>createPartControl</code>- to create the intro part's controls</li>
 * <li><code>setFocus</code>- to accept focus</li>
 * <li><code>standbyStateChanged</code>- to change the standby mode</li>
 * </ul>
 * <p>
 * Subclasses may extend or reimplement the following methods as required:
 * </p>
 * <ul>
 * <li><code>setInitializationData</code>- extend to provide additional
 * initialization when the intro extension is instantiated</li>
 * <li><code>init(IIntroSite, IMemento)</code>- extend to provide additional
 * initialization when intro is assigned its site</li>
 * <li><code>dispose</code>- extend to provide additional cleanup</li>
 * <li><code>getAdapter</code>- reimplement to make their intro adaptable</li>
 * </ul>
 *
 * @since 3.0
 */
public abstract class IntroPart extends EventManager implements IIntroPart, IExecutableExtension {

	private IConfigurationElement configElement;

	private Optional<ImageDescriptor> imageDescriptor = Optional.empty();

	private IIntroSite partSite;

	private Image titleImage;

	private String titleLabel;

	/**
	 * Creates a new intro part.
	 */
	protected IntroPart() {
		super();
	}

	@Override
	public void addPropertyListener(IPropertyListener l) {
		addListenerObject(l);
	}

	@Override
	public abstract void createPartControl(Composite parent);

	/**
	 * The <code>IntroPart</code> implementation of this <code>IIntroPart</code>
	 * method disposes the title image loaded by <code>setInitializationData</code>.
	 * Subclasses may extend.
	 */
	@Override
	public void dispose() {
		imageDescriptor.ifPresent(JFaceResources.getResources()::destroy);
		titleImage = null;
		// Clear out the property change listeners as we
		// should not be notifying anyone after the part
		// has been disposed.
		clearListeners();
	}

	/**
	 * Fires a property changed event.
	 *
	 * @param propertyId the id of the property that changed
	 */
	protected void firePropertyChange(final int propertyId) {
		for (Object listener : getListeners()) {
			final IPropertyListener propertyListener = (IPropertyListener) listener;
			SafeRunner.run(new SafeRunnable() {

				@Override
				public void run() {
					propertyListener.propertyChanged(this, propertyId);
				}
			});
		}
	}

	/**
	 * This implementation of the method declared by <code>IAdaptable</code> passes
	 * the request along to the platform's adapter manager; roughly
	 * <code>Platform.getAdapterManager().getAdapter(this, adapter)</code>.
	 * Subclasses may override this method (however, if they do so, they should
	 * invoke the method on their superclass to ensure that the Platform's adapter
	 * manager is consulted).
	 */
	@Override
	public <T> T getAdapter(Class<T> adapter) {
		return Platform.getAdapterManager().getAdapter(this, adapter);
	}

	/**
	 * Returns the configuration element for this part. The configuration element
	 * comes from the plug-in registry entry for the extension defining this part.
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
		return ISharedImages.get().getImage(ISharedImages.IMG_DEF_VIEW);
	}

	@Override
	public final IIntroSite getIntroSite() {
		return partSite;
	}

	@Override
	public Image getTitleImage() {
		if (titleImage != null) {
			return titleImage;
		}
		return getDefaultImage();
	}

	@Override
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
	 * The base implementation of this {@link org.eclipse.ui.intro.IIntroPart}method
	 * ignores the memento and initializes the part in a fresh state. Subclasses may
	 * extend to perform any state restoration, but must call the super method.
	 *
	 * @param site    the intro site
	 * @param memento the intro part state or <code>null</code> if there is no
	 *                previous saved state
	 * @exception PartInitException if this part was not initialized successfully
	 */
	@Override
	public void init(IIntroSite site, IMemento memento) throws PartInitException {
		setSite(site);
	}

	/**
	 * Sets the part site.
	 * <p>
	 * Subclasses must invoke this method from
	 * {@link org.eclipse.ui.intro.IIntroPart#init(IIntroSite, IMemento)}.
	 * </p>
	 *
	 * @param site the intro part site
	 */
	protected void setSite(IIntroSite site) {
		this.partSite = site;
	}

	@Override
	public void removePropertyListener(IPropertyListener l) {
		removeListenerObject(l);
	}

	/**
	 * The base implementation of this {@link org.eclipse.ui.intro.IIntroPart}
	 * method does nothing. Subclasses may override.
	 *
	 * @param memento a memento to receive the object state
	 */
	@Override
	public void saveState(IMemento memento) {
		// no-op
	}

	@Override
	public abstract void setFocus();

	/**
	 * The <code>IntroPart</code> implementation of this
	 * <code>IExecutableExtension</code> records the configuration element in and
	 * internal state variable (accessible via <code>getConfigElement</code>). It
	 * also loads the title image, if one is specified in the configuration element.
	 * Subclasses may extend.
	 *
	 * Should not be called by clients. It is called by the core plugin when
	 * creating this executable extension.
	 */
	@Override
	public void setInitializationData(IConfigurationElement cfig, String propertyName, Object data) {
		// Save config element.
		configElement = cfig;
		titleLabel = cfig.getAttribute(IWorkbenchRegistryConstants.ATT_LABEL);
		// Icon.
		String strIcon = cfig.getAttribute(IWorkbenchRegistryConstants.ATT_ICON);
		if (strIcon != null) {
			imageDescriptor = ResourceLocator.imageDescriptorFromBundle(configElement.getContributor().getName(),
					strIcon);
			imageDescriptor.ifPresent(d -> titleImage = JFaceResources.getResources().createImageWithDefault(d));
		}
	}

	/**
	 * Sets or clears the title image of this part.
	 *
	 * @param titleImage the title image, or <code>null</code> to clear
	 */
	protected void setTitleImage(Image titleImage) {
		Assert.isTrue(titleImage == null || !titleImage.isDisposed());
		// Do not send changes if they are the same
		if (this.titleImage == titleImage) {
			return;
		}
		this.titleImage = titleImage;
		firePropertyChange(IIntroPart.PROP_TITLE);
	}

	/**
	 * Set the title string for this part.
	 *
	 * @param titleLabel the title string. Must not be <code>null</code>.
	 * @since 3.2
	 */
	protected void setTitle(String titleLabel) {
		Assert.isNotNull(titleLabel);
		if (Objects.equals(this.titleLabel, titleLabel))
			return;
		this.titleLabel = titleLabel;
		firePropertyChange(IIntroPart.PROP_TITLE);
	}
}
