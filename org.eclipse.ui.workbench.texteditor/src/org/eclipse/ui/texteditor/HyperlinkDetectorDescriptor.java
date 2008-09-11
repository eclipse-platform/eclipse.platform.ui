/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.texteditor;

import java.util.ArrayList;
import java.util.List;

import org.osgi.framework.Bundle;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;

import org.eclipse.jface.util.SafeRunnable;

import org.eclipse.jface.text.hyperlink.AbstractHyperlinkDetector;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;

import org.eclipse.ui.internal.texteditor.NLSUtility;
import org.eclipse.ui.internal.texteditor.TextEditorPlugin;


/**
 * Describes a contribution to the 'org.eclipse.ui.workbench.texteditor.hyperlinkDetectors'
 * extension point.
 *
 * @since 3.3
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public final class HyperlinkDetectorDescriptor {

	public static final String STATE_MASK_POSTFIX= "_stateMask"; //$NON-NLS-1$

	private static final String HYPERLINK_DETECTORS_EXTENSION_POINT= "org.eclipse.ui.workbench.texteditor.hyperlinkDetectors"; //$NON-NLS-1$
	private static final String HYPERLINK_DETECTOR_ELEMENT= "hyperlinkDetector"; //$NON-NLS-1$
	private static final String ID_ATTRIBUTE= "id"; //$NON-NLS-1$
	private static final String NAME_ATTRIBUTE= "name"; //$NON-NLS-1$
	private static final String DESCRIPTION_ATTRIBUTE= "description"; //$NON-NLS-1$
	private static final String TARGET_ID_ATTRIBUTE= "targetId"; //$NON-NLS-1$
	private static final String CLASS_ATTRIBUTE= "class"; //$NON-NLS-1$
	private static final String ACTIVATE_PLUG_IN_ATTRIBUTE= "activate"; //$NON-NLS-1$
	private static final String MODIFIER_KEYS= "modifierKeys"; //$NON-NLS-1$

	private IConfigurationElement fElement;
	private HyperlinkDetectorTargetDescriptor fTarget;


	/**
	 * Returns descriptors for all hyperlink detector extensions.
	 *
	 * @return an array with the contributed hyperlink detectors
	 */
	public static HyperlinkDetectorDescriptor[] getContributedHyperlinkDetectors() {
		IExtensionRegistry registry= Platform.getExtensionRegistry();
		IConfigurationElement[] elements= registry.getConfigurationElementsFor(HYPERLINK_DETECTORS_EXTENSION_POINT);
		HyperlinkDetectorDescriptor[] hyperlinkDetectorDescs= createDescriptors(elements);
		return hyperlinkDetectorDescs;
	}

	/**
	 * Creates a new descriptor from the given configuration element.
	 *
	 * @param element the configuration element
	 */
	private HyperlinkDetectorDescriptor(IConfigurationElement element) {
		Assert.isNotNull(element);
		fElement= element;
	}

	/**
	 * Creates a new {@link IHyperlinkDetector}.
	 *
	 * @return the hyperlink detector or <code>null</code> if the plug-in isn't loaded yet
	 * @throws CoreException if a failure occurred during creation
	 */
	public AbstractHyperlinkDetector createHyperlinkDetector() throws CoreException {
		final Throwable[] exception= new Throwable[1];
		final AbstractHyperlinkDetector[] result= new AbstractHyperlinkDetector[1];
		String message= NLSUtility.format(EditorMessages.Editor_error_HyperlinkDetector_couldNotCreate_message, new String[] { getId(), fElement.getContributor().getName() });
		ISafeRunnable code= new SafeRunnable(message) {
			/*
			 * @see org.eclipse.core.runtime.ISafeRunnable#run()
			 */
			public void run() throws Exception {
		 		String pluginId = fElement.getContributor().getName();
				boolean isPlugInActivated= Platform.getBundle(pluginId).getState() == Bundle.ACTIVE;
				if (isPlugInActivated || canActivatePlugIn())
					result[0]= (AbstractHyperlinkDetector)fElement.createExecutableExtension(CLASS_ATTRIBUTE);
			}
			/*
			 * @see org.eclipse.jface.util.SafeRunnable#handleException(java.lang.Throwable)
			 */
			public void handleException(Throwable ex) {
				super.handleException(ex);
				exception[0]= ex;
			}

		};

		SafeRunner.run(code);

		if (exception[0] == null)
			return result[0];
		throw new CoreException(new Status(IStatus.ERROR, TextEditorPlugin.PLUGIN_ID, IStatus.OK, message, exception[0]));

	}

	private boolean isValid(HyperlinkDetectorTargetDescriptor[] targets) {
		if (getId() == null || getName() == null || getTargetId() == null)
			return false;

		String targetId= getTargetId();
		for (int i= 0; i < targets.length; i++) {
			if (targetId.equals(targets[i].getId())) {
				fTarget= targets[i];
				return true;
			}
		}
		return false;

	}

	//---- XML Attribute accessors ---------------------------------------------

	/**
	 * Returns the hyperlink detector's id.
	 *
	 * @return the hyperlink detector's id
	 */
	public String getId() {
		return fElement.getAttribute(ID_ATTRIBUTE);
	}

	/**
	 * Returns the hyperlink detector's name.
	 *
	 * @return the hyperlink detector's name
	 */
	public String getName() {
		return fElement.getAttribute(NAME_ATTRIBUTE);
	}

	/**
	 * Returns the hyperlink detector's target descriptor.
	 *
	 * @return the hyperlink detector's target descriptor
	 */
	public HyperlinkDetectorTargetDescriptor getTarget() {
		return fTarget;
	}

	/**
	 * Returns the hyperlink detector's target id.
	 *
	 * @return the hyperlink detector's target id
	 */
	public String getTargetId() {
		return fElement.getAttribute(TARGET_ID_ATTRIBUTE);
	}

	/**
	 * Returns the hyperlink detector's description.
	 *
	 * @return the hyperlink detector's description or <code>null</code> if not provided
	 */
	public String getDescription() {
		return fElement.getAttribute(DESCRIPTION_ATTRIBUTE);
	}

	/**
	 * Returns the hyperlink detector's modifier keys that
	 * need to be pressed for this hyperlink detector.
	 *
	 * @return the hyperlink detector's description or <code>null</code> if not provided
	 */
	public String getModifierKeys() {
		return fElement.getAttribute(MODIFIER_KEYS);
	}

	public boolean canActivatePlugIn() {
		String value= fElement.getAttribute(ACTIVATE_PLUG_IN_ATTRIBUTE);
		if (value == null)
			return true;
		return Boolean.valueOf(value).booleanValue();
	}

	public boolean equals(Object obj) {
		if (obj == null || !obj.getClass().equals(this.getClass()) || getId() == null)
			return false;
		return getId().equals(((HyperlinkDetectorDescriptor)obj).getId());
	}

	public int hashCode() {
		return getId().hashCode();
	}

	private static HyperlinkDetectorDescriptor[] createDescriptors(IConfigurationElement[] elements) {
		HyperlinkDetectorTargetDescriptor[] targets= HyperlinkDetectorTargetDescriptor.getContributedHyperlinkDetectorTargets();
		List result= new ArrayList(elements.length);
		for (int i= 0; i < elements.length; i++) {
			IConfigurationElement element= elements[i];
			if (HYPERLINK_DETECTOR_ELEMENT.equals(element.getName())) {
				HyperlinkDetectorDescriptor desc= new HyperlinkDetectorDescriptor(element);
				if (desc.isValid(targets))
					result.add(desc);
				else {
					String message= NLSUtility.format(EditorMessages.Editor_error_HyperlinkDetector_invalidExtension_message, new String[] {desc.getId(), element.getContributor().getName()});
					TextEditorPlugin.getDefault().getLog().log(new Status(IStatus.ERROR, TextEditorPlugin.PLUGIN_ID, IStatus.OK, message, null));
				}
			} else {
				String message= NLSUtility.format(EditorMessages.Editor_error_HyperlinkDetector_invalidElementName_message, new String[] { element.getContributor().getName(), element.getName() });
				TextEditorPlugin.getDefault().getLog().log(new Status(IStatus.ERROR, TextEditorPlugin.PLUGIN_ID, IStatus.OK, message, null));
			}
		}
		return (HyperlinkDetectorDescriptor[])result.toArray(new HyperlinkDetectorDescriptor[result.size()]);
	}

}
