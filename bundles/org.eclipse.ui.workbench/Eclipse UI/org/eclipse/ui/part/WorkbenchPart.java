/*******************************************************************************
 * Copyright (c) 2000, 2022 IBM Corporation and others.
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
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 472654
 *     Alexander Fedorov <alexander.fedorov@arsysop.ru> - Bug 548799
 *******************************************************************************/
package org.eclipse.ui.part;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.eclipse.core.commands.common.EventManager;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.ui.workbench.renderers.swt.ContributedPartRenderer;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.ResourceLocator;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPart3;
import org.eclipse.ui.IWorkbenchPartConstants;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.PartSite;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.util.Util;

/**
 * Abstract base implementation of all workbench parts.
 * <p>
 * This class is not intended to be subclassed by clients outside this package;
 * clients should instead subclass <code>ViewPart</code> or
 * <code>EditorPart</code>.
 * </p>
 *
 * @see org.eclipse.ui.part.ViewPart
 * @see org.eclipse.ui.part.EditorPart
 * @noextend This class is not intended to be subclassed by clients.
 */
public abstract class WorkbenchPart extends EventManager
		implements IWorkbenchPart3, IExecutableExtension, IWorkbenchPartOrientation {
	private String title = ""; //$NON-NLS-1$

	private Optional<ImageDescriptor> imageDescriptor = Optional.empty();

	private Image titleImage;

	private String toolTip = ""; //$NON-NLS-1$

	private IConfigurationElement configElement;

	private IWorkbenchPartSite partSite;

	private String partName = ""; //$NON-NLS-1$

	private String contentDescription = ""; //$NON-NLS-1$

	private ListenerList<IPropertyChangeListener> partChangeListeners = new ListenerList<>();

	/**
	 * Creates a new workbench part.
	 */
	protected WorkbenchPart() {
		super();
	}

	@Override
	public void addPropertyListener(IPropertyListener l) {
		addListenerObject(l);
	}

	@Override
	public abstract void createPartControl(Composite parent);

	/**
	 * The <code>WorkbenchPart</code> implementation of this
	 * <code>IWorkbenchPart</code> method disposes the title image loaded by
	 * <code>setInitializationData</code>. Subclasses may extend.
	 */
	@Override
	public void dispose() {
		imageDescriptor.ifPresent(d -> {
			if (Display.getCurrent() != null) {
				JFaceResources.getResources().destroy(d);
			} // otherwise Device already destroyed => ignore
		});
		titleImage = null;

		// Clear out the property change listeners as we
		// should not be notifying anyone after the part
		// has been disposed.
		clearListeners();
		partChangeListeners.clear();
	}

	/**
	 * Fires a property changed event.
	 *
	 * @param propertyId the id of the property that changed
	 */
	protected void firePropertyChange(final int propertyId) {
		for (Object listener : getListeners()) {
			final IPropertyListener propertyListener = (IPropertyListener) listener;
			try {
				propertyListener.propertyChanged(WorkbenchPart.this, propertyId);
			} catch (RuntimeException e) {
				WorkbenchPlugin.log(e);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * Subclasses may override this method (however, if they do so, they should
	 * invoke the method on their superclass to ensure that the Platform's adapter
	 * manager is consulted).
	 */
	@Override
	public <T> T getAdapter(Class<T> adapter) {

		/**
		 * This implementation of the method declared by <code>IAdaptable</code> passes
		 * the request along to the platform's adapter manager; roughly
		 * <code>Platform.getAdapterManager().getAdapter(this, adapter)</code>.
		 */

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
		return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_DEF_VIEW);
	}

	@Override
	public IWorkbenchPartSite getSite() {
		return partSite;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * It is considered bad practise to overload or extend this method. Parts should
	 * set their title by calling setPartName and/or setContentDescription.
	 * </p>
	 */
	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public Image getTitleImage() {
		if (titleImage != null) {
			return titleImage;
		}
		return getDefaultImage();
	}

	@Override
	public String getTitleToolTip() {
		return toolTip;
	}

	@Override
	public void removePropertyListener(IPropertyListener l) {
		removeListenerObject(l);
	}

	@Override
	public abstract void setFocus();

	/**
	 * {@inheritDoc} The <code>WorkbenchPart</code> implementation of this
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
		// Part name and title.
		partName = Util.safeString(cfig.getAttribute("name"));//$NON-NLS-1$ ;
		title = partName;
		// Icon.
		String strIcon = cfig.getAttribute("icon");//$NON-NLS-1$
		if (strIcon == null) {
			return;
		}
		imageDescriptor = ResourceLocator.imageDescriptorFromBundle(configElement.getContributor().getName(), strIcon);
		if (!imageDescriptor.isPresent()) {
			ImageDescriptor shared = PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(strIcon);
			imageDescriptor = Optional.ofNullable(shared);
		}
		imageDescriptor.ifPresent(d -> titleImage = JFaceResources.getResources().createImageWithDefault(d));
	}

	/**
	 * Sets the part site.
	 * <p>
	 * Subclasses must invoke this method from <code>IEditorPart.init</code> and
	 * <code>IViewPart.init</code>.
	 *
	 * @param site the workbench part site
	 */
	protected void setSite(IWorkbenchPartSite site) {
		checkSite(site);
		this.partSite = site;
	}

	/**
	 * Checks that the given site is valid for this type of part. The default
	 * implementation does nothing.
	 *
	 * @param site the site to check
	 * @since 3.1
	 */
	protected void checkSite(IWorkbenchPartSite site) {
		// do nothing
	}

	/**
	 * Sets or clears the title of this part. Clients should call this method
	 * instead of overriding getTitle.
	 * <p>
	 * This may change a title that was previously set using setPartName or
	 * setContentDescription.
	 * </p>
	 *
	 * @deprecated new code should use setPartName and setContentDescription
	 *
	 * @param title the title, or <code>null</code> to clear
	 */
	@Deprecated
	protected void setTitle(String title) {
		title = Util.safeString(title);

		// Do not send changes if they are the same
		if (Objects.equals(this.title, title)) {
			return;
		}
		this.title = title;
		firePropertyChange(IWorkbenchPart.PROP_TITLE);
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
		firePropertyChange(IWorkbenchPart.PROP_TITLE);
		imageDescriptor.ifPresent(JFaceResources.getResources()::destroy);
		imageDescriptor = Optional.empty();
	}

	/**
	 * Sets or clears the title tool tip text of this part. Clients should call this
	 * method instead of overriding <code>getTitleToolTip</code>
	 *
	 * @param toolTip the new tool tip text, or <code>null</code> to clear
	 */
	protected void setTitleToolTip(String toolTip) {
		toolTip = Util.safeString(toolTip);
		// Do not send changes if they are the same
		if (Objects.equals(this.toolTip, toolTip)) {
			return;
		}
		this.toolTip = toolTip;
		firePropertyChange(IWorkbenchPart.PROP_TITLE);
	}

	/**
	 * Show that this part is busy due to a Job running that it is listening to.
	 *
	 * @param busy boolean to indicate that the busy state has started or ended.
	 * @see org.eclipse.ui.progress.IWorkbenchSiteProgressService#showBusyForFamily(Object)
	 * @since 3.0
	 */
	public void showBusy(boolean busy) {
		// By default do nothing
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * It is considered bad practise to overload or extend this method. Parts should
	 * call setPartName to change their part name.
	 * </p>
	 */
	@Override
	public String getPartName() {
		return partName;
	}

	/**
	 * Sets the name of this part. The name will be shown in the tab area for the
	 * part. Clients should call this method instead of overriding getPartName.
	 * Setting this to the empty string will cause a default part name to be used.
	 *
	 * <p>
	 * setPartName and setContentDescription are intended to replace setTitle. This
	 * may change a value that was previously set using setTitle.
	 * </p>
	 *
	 * @param partName the part name, as it should be displayed in tabs.
	 *
	 * @since 3.0
	 */
	protected void setPartName(String partName) {

		internalSetPartName(partName);

		setDefaultTitle();
	}

	void setDefaultTitle() {
		String description = getContentDescription();
		String name = getPartName();
		String newTitle = name;

		if (!Objects.equals(description, "")) { //$NON-NLS-1$
			newTitle = MessageFormat.format(WorkbenchMessages.WorkbenchPart_AutoTitleFormat, name, description);
		}

		setTitle(newTitle);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * It is considered bad practise to overload or extend this method. Parts should
	 * call setContentDescription to change their content description.
	 * </p>
	 */
	@Override
	public String getContentDescription() {
		return contentDescription;
	}

	/**
	 * Sets the content description for this part. The content description is
	 * typically a short string describing the current contents of the part. Setting
	 * this to the empty string will cause a default content description to be used.
	 * Clients should call this method instead of overriding
	 * getContentDescription(). For views, the content description is shown (by
	 * default) in a line near the top of the view. For editors, the content
	 * description is shown beside the part name when showing a list of editors. If
	 * the editor is open on a file, this typically contains the path to the input
	 * file, without the filename or trailing slash.
	 *
	 * <p>
	 * This may overwrite a value that was previously set in setTitle
	 * </p>
	 *
	 * @param description the content description
	 *
	 * @since 3.0
	 */
	protected void setContentDescription(String description) {
		internalSetContentDescription(description);

		setDefaultTitle();
	}

	void internalSetContentDescription(String description) {
		Assert.isNotNull(description);

		// Do not send changes if they are the same
		if (Objects.equals(contentDescription, description)) {
			return;
		}
		this.contentDescription = description;

		if (partSite instanceof PartSite) {
			PartSite site = (PartSite) partSite;
			ContributedPartRenderer.setDescription(site.getModel(), description);
		}
		firePropertyChange(IWorkbenchPartConstants.PROP_CONTENT_DESCRIPTION);
	}

	void internalSetPartName(String partName) {
		partName = Util.safeString(partName);

		Assert.isNotNull(partName);

		// Do not send changes if they are the same
		if (Objects.equals(this.partName, partName)) {
			return;
		}
		this.partName = partName;

		firePropertyChange(IWorkbenchPartConstants.PROP_PART_NAME);

	}

	@Override
	public int getOrientation() {
		// By default use the orientation in Window
		return Window.getDefaultOrientation();
	}

	@Override
	public void addPartPropertyListener(IPropertyChangeListener listener) {
		partChangeListeners.add(listener);
	}

	@Override
	public void removePartPropertyListener(IPropertyChangeListener listener) {
		partChangeListeners.remove(listener);
	}

	/**
	 * @since 3.3
	 */
	protected void firePartPropertyChanged(String key, String oldValue, String newValue) {
		final PropertyChangeEvent event = new PropertyChangeEvent(this, key, oldValue, newValue);
		for (IPropertyChangeListener l : partChangeListeners) {
			try {
				l.propertyChange(event);
			} catch (RuntimeException e) {
				WorkbenchPlugin.log(e);
			}
		}
	}

	private Map<String, String> partProperties = new HashMap<>();

	@Override
	public void setPartProperty(String key, String value) {
		String oldValue = partProperties.get(key);
		if (value == null) {
			partProperties.remove(key);
		} else {
			partProperties.put(key, value);
		}
		firePartPropertyChanged(key, oldValue, value);
	}

	@Override
	public String getPartProperty(String key) {
		return partProperties.get(key);
	}

	@Override
	public Map<String, String> getPartProperties() {
		return Collections.unmodifiableMap(partProperties);
	}
}
