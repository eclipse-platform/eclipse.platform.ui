/*******************************************************************************
 * Copyright (c) 2013, 2018 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.ui.internal;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.dynamichelpers.IExtensionTracker;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ResourceLocator;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IViewLayout;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.e4.compatibility.ModeledPageLayout;
import org.eclipse.ui.internal.menus.MenuHelper;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.internal.registry.RegistryReader;

/**
 * A strategy to read perspective extension from the registry. A pespective
 * extension is one of a view, viewAction, perspAction, newWizardAction, or
 * actionSet.
 */
public class PerspectiveExtensionReader extends RegistryReader {
	private String targetID;

	private ModeledPageLayout pageLayout;

	private Set<String> includeOnlyTags = null;

	private static final String VAL_LEFT = "left";//$NON-NLS-1$

	private static final String VAL_RIGHT = "right";//$NON-NLS-1$

	private static final String VAL_TOP = "top";//$NON-NLS-1$

	private static final String VAL_BOTTOM = "bottom";//$NON-NLS-1$

	private static final String VAL_STACK = "stack";//$NON-NLS-1$

	private static final String VAL_FAST = "fast";//$NON-NLS-1$

	private static final String VAL_TRUE = "true";//$NON-NLS-1$

	// VAL_FALSE added by dan_rubel@instantiations.com
	// TODO: this logic is backwards... we should be checking for true, but
	// technically this is API now...
	private static final String VAL_FALSE = "false";//$NON-NLS-1$

	private IExtensionTracker tracker;

	/**
	 * PerspectiveExtensionReader constructor..
	 */
	public PerspectiveExtensionReader() {
		// do nothing
	}

	/**
	 * Read the view extensions within a registry.
	 *
	 * @param extensionTracker the tracker
	 * @param id               the id
	 * @param out              the layout
	 */
	public void extendLayout(IExtensionTracker extensionTracker, String id, ModeledPageLayout out) {
		tracker = extensionTracker;
		targetID = id;
		pageLayout = out;
		readRegistry(Platform.getExtensionRegistry(), PlatformUI.PLUGIN_ID,
				IWorkbenchRegistryConstants.PL_PERSPECTIVE_EXTENSIONS);
	}

	/**
	 * Returns whether the given tag should be included.
	 */
	private boolean includeTag(String tag) {
		return includeOnlyTags == null || includeOnlyTags.contains(tag);
	}

	/**
	 * Process an action set.
	 */
	private boolean processActionSet(IConfigurationElement element) {
		String id = element.getAttribute(IWorkbenchRegistryConstants.ATT_ID);
		if (id != null) {
			pageLayout.addActionSet(id);
		}
		return true;
	}

	/**
	 * Process an extension. Assumption: Extension is for current perspective.
	 */
	private boolean processExtension(IConfigurationElement element) {
		String[] attributes = element.getAttributeNames();
		for (String attribute : attributes) {
			if (!attribute.equals(IWorkbenchRegistryConstants.ATT_TARGET_ID) && includeTag(attribute)) {
				boolean result = false;
				switch (attribute) {
				case IWorkbenchRegistryConstants.ATT_EDITOR_ONBOARDING_TEXT:
					result = processEditorOnboardingText(element.getAttribute(attribute));
					break;
				case IWorkbenchRegistryConstants.ATT_EDITOR_ONBOARDING_IMAGE:
					result = processEditorOnboardingImage(element, element.getAttribute(attribute));
					break;
				case IWorkbenchRegistryConstants.ATT_TARGET_ID:
					result = true;
					break;

				default:
					break;
				}
				if (!result) {
					ILog.of(getClass()).error("Unable to process attribute: " + //$NON-NLS-1$
							attribute + " in perspective extension: " + //$NON-NLS-1$
							element.getDeclaringExtension().getUniqueIdentifier());
				}
			}
		}
		IConfigurationElement[] children = element.getChildren();
		for (IConfigurationElement child : children) {
			String type = child.getName();
			if (includeTag(type)) {
				boolean result = false;
				switch (type) {
				case IWorkbenchRegistryConstants.TAG_ACTION_SET:
					result = processActionSet(child);
					break;
				case IWorkbenchRegistryConstants.TAG_VIEW:
					result = processView(child);
					break;
				case IWorkbenchRegistryConstants.TAG_VIEW_SHORTCUT:
					result = processViewShortcut(child);
					break;
				case IWorkbenchRegistryConstants.TAG_NEW_WIZARD_SHORTCUT:
					result = processWizardShortcut(child);
					break;
				case IWorkbenchRegistryConstants.TAG_PERSP_SHORTCUT:
					result = processPerspectiveShortcut(child);
					break;
				case IWorkbenchRegistryConstants.TAG_SHOW_IN_PART:
					result = processShowInPart(child);
					break;
				case IWorkbenchRegistryConstants.TAG_HIDDEN_MENU_ITEM:
					result = processHiddenMenuItem(child);
					break;
				case IWorkbenchRegistryConstants.TAG_HIDDEN_TOOLBAR_ITEM:
					result = processHiddenToolBarItem(child);
					break;
				case IWorkbenchRegistryConstants.TAG_EDITOR_ONBOARDING_COMMAND:
					result = processEditorOnboardingCommand(child);
					break;
				default:
					break;
				}
				if (!result) {
					ILog.of(getClass()).error("Unable to process element: " + //$NON-NLS-1$
							type + " in perspective extension: " + //$NON-NLS-1$
							element.getDeclaringExtension().getUniqueIdentifier());
				}
			}
		}
		return true;
	}

	/**
	 * Process a perspective shortcut
	 */
	private boolean processPerspectiveShortcut(IConfigurationElement element) {
		String id = element.getAttribute(IWorkbenchRegistryConstants.ATT_ID);
		if (id != null) {
			pageLayout.addPerspectiveShortcut(id);
		}
		return true;
	}

	/**
	 * Process a show in element.
	 */
	private boolean processShowInPart(IConfigurationElement element) {
		String id = element.getAttribute(IWorkbenchRegistryConstants.ATT_ID);
		if (id != null) {
			pageLayout.addShowInPart(id);
		}
		return true;
	}

	/**
	 * Process a hidden menu item
	 */
	private boolean processHiddenMenuItem(IConfigurationElement element) {
		String id = element.getAttribute(IWorkbenchRegistryConstants.ATT_ID);
		if (id != null) {
			pageLayout.addHiddenMenuItemId(id);
		}
		return true;
	}

	/**
	 * Process a hidden toolbar item
	 */
	private boolean processHiddenToolBarItem(IConfigurationElement element) {
		String id = element.getAttribute(IWorkbenchRegistryConstants.ATT_ID);
		if (id != null) {
			pageLayout.addHiddenToolBarItemId(id);
		}
		return true;
	}

	// processView(IConfigurationElement) modified by dan_rubel@instantiations.com
	/**
	 * Process a view
	 */
	private boolean processView(IConfigurationElement element) {
		// Get id, relative, and relationship.
		String id = element.getAttribute(IWorkbenchRegistryConstants.ATT_ID);
		String relative = element.getAttribute(IWorkbenchRegistryConstants.ATT_RELATIVE);
		String relationship = element.getAttribute(IWorkbenchRegistryConstants.ATT_RELATIONSHIP);
		String ratioString = element.getAttribute(IWorkbenchRegistryConstants.ATT_RATIO);
		boolean visible = !VAL_FALSE.equals(element.getAttribute(IWorkbenchRegistryConstants.ATT_VISIBLE));
		String closeable = element.getAttribute(IWorkbenchRegistryConstants.ATT_CLOSEABLE);
		String moveable = element.getAttribute(IWorkbenchRegistryConstants.ATT_MOVEABLE);
		String standalone = element.getAttribute(IWorkbenchRegistryConstants.ATT_STANDALONE);
		String showTitle = element.getAttribute(IWorkbenchRegistryConstants.ATT_SHOW_TITLE);

		// Default to 'false'
		String minVal = element.getAttribute(IWorkbenchRegistryConstants.ATT_MINIMIZED);
		boolean minimized = minVal != null && VAL_TRUE.equals(minVal);

		float ratio;

		if (id == null) {
			logMissingAttribute(element, IWorkbenchRegistryConstants.ATT_ID);
			return false;
		}
		if (relationship == null) {
			logMissingAttribute(element, IWorkbenchRegistryConstants.ATT_RELATIONSHIP);
			return false;
		}
		if (!VAL_FAST.equals(relationship) && relative == null) {
			logError(element, "Attribute '" + IWorkbenchRegistryConstants.ATT_RELATIVE //$NON-NLS-1$
					+ "' not defined.  This attribute is required when " //$NON-NLS-1$
					+ IWorkbenchRegistryConstants.ATT_RELATIONSHIP + "=\"" + relationship + "\"."); //$NON-NLS-1$ //$NON-NLS-2$
			return false;
		}
		// Get the ratio.
		if (ratioString == null) {
			// The ratio has not been specified.
			ratio = IPageLayout.NULL_RATIO;
		} else {
			try {
				ratio = Float.parseFloat(ratioString);
			} catch (NumberFormatException e) {
				return false;
			}
			// If the ratio is outside the allowable range, mark it as invalid.
			if (ratio < IPageLayout.RATIO_MIN || ratio > IPageLayout.RATIO_MAX) {
				ratio = IPageLayout.INVALID_RATIO;
			}
		}

		// Get relationship details.
		boolean stack = false;
		int intRelation = 0;
		boolean fast = false;
		switch (relationship) {
		case VAL_LEFT:
			intRelation = IPageLayout.LEFT;
			break;
		case VAL_RIGHT:
			intRelation = IPageLayout.RIGHT;
			break;
		case VAL_TOP:
			intRelation = IPageLayout.TOP;
			break;
		case VAL_BOTTOM:
			intRelation = IPageLayout.BOTTOM;
			break;
		case VAL_STACK:
			stack = true;
			break;
		case VAL_FAST:
			fast = true;
			break;
		default:
			return false;
		}

		if (visible) {
			// If adding a view (not just a placeholder), remove any existing placeholder.
			// See bug 85948 [Perspectives] Adding register & expressions view by default to
			// debug perspective fails
			pageLayout.removePlaceholder(id);
		}

		// If stack ..
		if (stack) {
			if (visible) {
				pageLayout.stackView(id, relative, true);
			} else {
				pageLayout.stackView(id, relative, false);
			}
		}
		// If the view is a fast view...
		else if (fast) {
			if (ratio == IPageLayout.NULL_RATIO) {
				// The ratio has not been specified.
				pageLayout.addFastView(id);
			} else {
				pageLayout.addFastView(id, ratio);
			}
		} else {

			// The view is a regular view.
			// If the ratio is not specified or is invalid, use the default ratio.
			if (ratio == IPageLayout.NULL_RATIO || ratio == IPageLayout.INVALID_RATIO) {
				ratio = IPageLayout.DEFAULT_VIEW_RATIO;
			}

			if (visible) {
				if (VAL_TRUE.equals(standalone)) {
					pageLayout.addStandaloneView(id, !VAL_FALSE.equals(showTitle), intRelation, ratio, relative);
				} else {
					pageLayout.addView(id, intRelation, ratio, relative, minimized);
				}
			} else // Fix for 99155, CGross (schtoo@schtoo.com)
			// Adding standalone placeholder for standalone views
			if (VAL_TRUE.equals(standalone)) {
				pageLayout.addStandaloneViewPlaceholder(id, intRelation, ratio, relative, !VAL_FALSE.equals(showTitle));
			} else {
				pageLayout.addPlaceholder(id, intRelation, ratio, relative);
			}
		}
		IViewLayout viewLayout = pageLayout.getViewLayout(id);
		// may be null if it's been filtered by activity
		if (viewLayout != null) {
			if (closeable != null) {
				viewLayout.setCloseable(!VAL_FALSE.equals(closeable));
			}
			if (moveable != null) {
				viewLayout.setMoveable(!VAL_FALSE.equals(moveable));
			}
		}

		return true;
	}

	/**
	 * Process a view shortcut
	 */
	private boolean processViewShortcut(IConfigurationElement element) {
		String id = element.getAttribute(IWorkbenchRegistryConstants.ATT_ID);
		if (id != null) {
			pageLayout.addShowViewShortcut(id);
		}
		return true;
	}

	/**
	 * Process a wizard shortcut
	 */
	private boolean processWizardShortcut(IConfigurationElement element) {
		String id = element.getAttribute(IWorkbenchRegistryConstants.ATT_ID);
		if (id != null) {
			pageLayout.addNewWizardShortcut(id);
		}
		return true;
	}

	/**
	 * Process the onboarding text.
	 *
	 * @param text the text
	 */
	private boolean processEditorOnboardingText(String text) {
		if (text != null) {
			pageLayout.setEditorOnboardingText(text);
		}
		return true;
	}

	/**
	 * Process the onboarding image.
	 *
	 * @param element  a configuration element
	 * @param imageUri the image uri
	 */
	private boolean processEditorOnboardingImage(IConfigurationElement element, String imageUri) {
		if (imageUri != null) {
			String namespaceId = element.getNamespaceIdentifier();
			ImageDescriptor descriptor = ResourceLocator.imageDescriptorFromBundle(namespaceId, imageUri).orElse(null);
			if (descriptor != null) {
				pageLayout.setEditorOnboardingImageUri(MenuHelper.getImageUrl(descriptor));
			}
		}
		return true;
	}

	/**
	 * Process an onbarding command.
	 *
	 * @param element a configuration element
	 */
	private boolean processEditorOnboardingCommand(IConfigurationElement element) {
		String id = element.getAttribute(IWorkbenchRegistryConstants.ATT_ID);
		if (id != null) {
			pageLayout.addEditorOnboardingCommandId(id);
		}
		return true;
	}

	@Override
	protected boolean readElement(IConfigurationElement element) {
		String type = element.getName();
		if (type.equals(IWorkbenchRegistryConstants.TAG_PERSPECTIVE_EXTENSION)) {
			String id = element.getAttribute(IWorkbenchRegistryConstants.ATT_TARGET_ID);
			if (targetID.equals(id) || "*".equals(id)) { //$NON-NLS-1$
				if (tracker != null) {
					tracker.registerObject(element.getDeclaringExtension(), new DirtyPerspectiveMarker(id),
							IExtensionTracker.REF_STRONG);
				}
				return processExtension(element);
			}
			return true;
		}
		return false;
	}

	/**
	 * Sets the tags to include. All others are ignored.
	 *
	 * @param tags the tags to include
	 */
	public void setIncludeOnlyTags(String[] tags) {
		includeOnlyTags = new HashSet<>();
		includeOnlyTags.addAll(Arrays.asList(tags));
	}
}
