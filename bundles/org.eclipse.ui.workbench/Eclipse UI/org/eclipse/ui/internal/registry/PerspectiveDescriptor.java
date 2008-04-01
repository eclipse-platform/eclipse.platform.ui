/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Brock Janiczak (brockj_eclipse@ihug.com.au) - handler registration
 *******************************************************************************/
package org.eclipse.ui.internal.registry;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.IPluginContribution;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.IWorkbenchConstants;
import org.eclipse.ui.internal.WorkbenchImages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * PerspectiveDescriptor.
 * <p>
 * A PerspectiveDesciptor has 3 states:
 * </p>
 * <ol>
 * <li>It <code>isPredefined()</code>, in which case it was defined from an
 * extension point.</li>
 * <li>It <code>isPredefined()</code> and <code>hasCustomFile</code>, in
 * which case the user has customized a predefined perspective.</li>
 * <li>It <code>hasCustomFile</code>, in which case the user created a new
 * perspective.</li>
 * </ol>
 * 
 */
public class PerspectiveDescriptor implements IPerspectiveDescriptor,
		IPluginContribution {
	private String id;

	private String pluginId;

	private String originalId;

	private String label;

	private String className;

	private String description;

	private boolean singleton;

	private boolean fixed;

	private ImageDescriptor image;

	private IConfigurationElement configElement;

	/**
	 * Create a new empty descriptor.
	 * 
	 * @param id
	 *            the id of the new descriptor
	 * @param label
	 *            the label of the new descriptor
	 * @param originalDescriptor
	 *            the descriptor that this descriptor is based on
	 */
	public PerspectiveDescriptor(String id, String label,
			PerspectiveDescriptor originalDescriptor) {
		this.id = id;
		this.label = label;
		if (originalDescriptor != null) {
			this.originalId = originalDescriptor.getOriginalId();
			this.image = originalDescriptor.image;

			// This perspective is based on a perspective in some bundle -- if
			// that
			// bundle goes away then I think it makes sense to treat this
			// perspective
			// the same as any other -- so store it with the original
			// descriptor's
			// bundle's list.
			//
			// It might also make sense the other way...removing the following
			// line
			// will allow the perspective to stay around when the originating
			// bundle
			// is unloaded.
			//
			// This might also have an impact on upgrade cases -- should we
			// really be
			// destroying all user customized perspectives when the older
			// version is
			// removed?
			//
			// I'm leaving this here for now since its a good example, but
			// wouldn't be
			// surprised if we ultimately decide on the opposite.
			//
			// The reason this line is important is that this is the value used
			// to
			// put the object into the UI level registry. When that bundle goes
			// away,
			// the registry will remove the entire list of objects. So if this
			// desc
			// has been put into that list -- it will go away.
			this.pluginId = originalDescriptor.getPluginId();
		}
	}

	/**
	 * Create a descriptor from a config element.
	 * 
	 * @param id
	 *            the id of the element to create
	 * @param configElement
	 *            the element to base this perspective on
	 * @throws CoreException
	 *             thrown if there are any missing attributes
	 */
	public PerspectiveDescriptor(String id, IConfigurationElement configElement)
			throws CoreException {
		this.configElement = configElement;
		this.id = id;
		// Sanity check.
		if ((getId() == null) || (getLabel() == null)
				|| (getClassName() == null)) {
			throw new CoreException(
					new Status(
							IStatus.ERROR,
							WorkbenchPlugin.PI_WORKBENCH,
							0,
							"Invalid extension (missing label, id or class name): " + getId(),//$NON-NLS-1$
							null));
		}
	}

	/**
	 * Creates a factory for a predefined perspective. If the perspective is not
	 * predefined return <code>null</code>.
	 * 
	 * @return the IPerspectiveFactory or <code>null</code>
	 * @throws CoreException
	 *             if the object could not be instantiated.
	 */
	public IPerspectiveFactory createFactory() throws CoreException {
		// if there is an originalId, then use that descriptor instead
		if (originalId != null) {
			// Get the original descriptor to create the factory. If the
			// original is gone then nothing can be done.
			IPerspectiveDescriptor target = ((PerspectiveRegistry) WorkbenchPlugin
					.getDefault().getPerspectiveRegistry())
					.findPerspectiveWithId(originalId);

			return target == null ? null : ((PerspectiveDescriptor) target)
					.createFactory();
		}

		// otherwise try to create the executable extension
		if (configElement != null) {
			try {
				return (IPerspectiveFactory) configElement
						.createExecutableExtension(IWorkbenchRegistryConstants.ATT_CLASS);
			} catch (CoreException e) {
				// do nothing
			}
		}

		return null;
	}

	/**
	 * Deletes the custom definition for a perspective..
	 */
	public void deleteCustomDefinition() {
		((PerspectiveRegistry) WorkbenchPlugin.getDefault()
				.getPerspectiveRegistry()).deleteCustomDefinition(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IPerspectiveDescriptor#getDescription()
	 */
	public String getDescription() {
		return configElement == null ? description : RegistryReader
				.getDescription(configElement);
	}

	/**
	 * Returns whether or not this perspective is fixed.
	 * 
	 * @return whether or not this perspective is fixed
	 */
	public boolean getFixed() {
		return configElement == null ? fixed : Boolean.valueOf(
				configElement
						.getAttribute(IWorkbenchRegistryConstants.ATT_FIXED))
				.booleanValue();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IPerspectiveDescriptor#getId()
	 */
	public String getId() {
		return id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IPerspectiveDescriptor#getImageDescriptor()
	 */
	public ImageDescriptor getImageDescriptor() {
		if (image == null) {
			if (configElement != null) {
				String icon = configElement
						.getAttribute(IWorkbenchRegistryConstants.ATT_ICON);
				if (icon != null) {
					image = AbstractUIPlugin.imageDescriptorFromPlugin(
							configElement.getNamespace(), icon);
				}
				if (image == null) {
					image = WorkbenchImages
							.getImageDescriptor(ISharedImages.IMG_ETOOL_DEF_PERSPECTIVE);
				}
			}
		}
		return image;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IPerspectiveDescriptor#getLabel()
	 */
	public String getLabel() {
		return configElement == null ? label : configElement
				.getAttribute(IWorkbenchRegistryConstants.ATT_NAME);
	}

	/**
	 * Return the original id of this descriptor.
	 * 
	 * @return the original id of this descriptor
	 */
	public String getOriginalId() {
		if (originalId == null) {
			return getId();
		}
		return originalId;
	}

	/**
	 * Returns <code>true</code> if this perspective has a custom definition.
	 * 
	 * @return whether this perspective has a custom definition
	 */
	public boolean hasCustomDefinition() {
		return ((PerspectiveRegistry) WorkbenchPlugin.getDefault()
				.getPerspectiveRegistry()).hasCustomDefinition(this);
	}

	/**
	 * Returns <code>true</code> if this perspective wants to be default.
	 * 
	 * @return whether this perspective wants to be default
	 */
	public boolean hasDefaultFlag() {
		if (configElement == null) {
			return false;
		}

		return Boolean.valueOf(
				configElement
						.getAttribute(IWorkbenchRegistryConstants.ATT_DEFAULT))
				.booleanValue();
	}

	/**
	 * Returns <code>true</code> if this perspective is predefined by an
	 * extension.
	 * 
	 * @return boolean whether this perspective is predefined by an extension
	 */
	public boolean isPredefined() {
		return getClassName() != null && configElement != null;
	}

	/**
	 * Returns <code>true</code> if this perspective is a singleton.
	 * 
	 * @return whether this perspective is a singleton
	 */
	public boolean isSingleton() {
		return configElement == null ? singleton
				: configElement
						.getAttributeAsIs(IWorkbenchRegistryConstants.ATT_SINGLETON) != null;
	}

	/**
	 * Restore the state of a perspective from a memento.
	 * 
	 * @param memento
	 *            the memento to restore from
	 * @return the <code>IStatus</code> of the operation
	 * @see org.eclipse.ui.IPersistableElement
	 */
	public IStatus restoreState(IMemento memento) {
		IMemento childMem = memento
				.getChild(IWorkbenchConstants.TAG_DESCRIPTOR);
		if (childMem != null) {
			id = childMem.getString(IWorkbenchConstants.TAG_ID);
			originalId = childMem.getString(IWorkbenchConstants.TAG_DESCRIPTOR);
			label = childMem.getString(IWorkbenchConstants.TAG_LABEL);
			className = childMem.getString(IWorkbenchConstants.TAG_CLASS);
			singleton = (childMem.getInteger(IWorkbenchConstants.TAG_SINGLETON) != null);

			// Find a descriptor in the registry.
			IPerspectiveDescriptor descriptor = WorkbenchPlugin.getDefault()
					.getPerspectiveRegistry().findPerspectiveWithId(
							getOriginalId());

			if (descriptor != null) {
				// Copy the state from the registred descriptor.
				image = descriptor.getImageDescriptor();
			}
		}
		return new Status(IStatus.OK, PlatformUI.PLUGIN_ID, 0, "", null); //$NON-NLS-1$
	}

	/**
	 * Revert to the predefined extension template. Does nothing if this
	 * descriptor is user defined.
	 */
	public void revertToPredefined() {
		if (isPredefined()) {
			deleteCustomDefinition();
		}
	}

	/**
	 * Save the state of a perspective to a memento.
	 * 
	 * @param memento
	 *            the memento to restore from
	 * @return the <code>IStatus</code> of the operation
	 * @see org.eclipse.ui.IPersistableElement
	 */
	public IStatus saveState(IMemento memento) {
		IMemento childMem = memento
				.createChild(IWorkbenchConstants.TAG_DESCRIPTOR);
		childMem.putString(IWorkbenchConstants.TAG_ID, getId());
		if (originalId != null) {
			childMem.putString(IWorkbenchConstants.TAG_DESCRIPTOR, originalId);
		}
		childMem.putString(IWorkbenchConstants.TAG_LABEL, getLabel());
		childMem.putString(IWorkbenchConstants.TAG_CLASS, getClassName());
		if (singleton) {
			childMem.putInteger(IWorkbenchConstants.TAG_SINGLETON, 1);
		}
		return new Status(IStatus.OK, PlatformUI.PLUGIN_ID, 0, "", null); //$NON-NLS-1$
	}

	/**
	 * Return the configuration element used to create this perspective, if one
	 * was used.
	 * 
	 * @return the configuration element used to create this perspective
	 * @since 3.0
	 */
	public IConfigurationElement getConfigElement() {
		return configElement;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.activities.support.IPluginContribution#getLocalId()
	 */
	public String getLocalId() {
		return getId();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.activities.support.IPluginContribution#getPluginId()
	 */
	public String getPluginId() {
		return configElement == null ? pluginId : configElement.getNamespace();
	}

	/**
	 * Returns the factory class name for this descriptor.
	 * 
	 * @return the factory class name for this descriptor
	 * @since 3.1
	 */
	public String getClassName() {
		return configElement == null ? className : RegistryReader
				.getClassValue(configElement,
						IWorkbenchRegistryConstants.ATT_CLASS);
	}
}
