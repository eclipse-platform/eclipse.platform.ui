/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs.cpd;

import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.internal.ActionSetContributionItem;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.dialogs.cpd.CustomizePerspectiveDialog.ActionSet;
import org.eclipse.ui.internal.dialogs.cpd.CustomizePerspectiveDialog.DisplayItem;
import org.eclipse.ui.internal.dialogs.cpd.CustomizePerspectiveDialog.DynamicContributionItem;
import org.eclipse.ui.internal.dialogs.cpd.CustomizePerspectiveDialog.ShortcutItem;
import org.eclipse.ui.views.IViewDescriptor;
import org.eclipse.ui.wizards.IWizardDescriptor;

/**
 * A tooltip which, given a model element, will display its icon (if there
 * is one), name, and a description (if there is one).
 *
 * @since 3.5
 */
abstract class NameAndDescriptionToolTip extends ToolTip {
	public NameAndDescriptionToolTip(Control control, int style) {
		super(control, style, false);
	}

	protected abstract Object getModelElement(Event event);

	/**
	 * Adds logic to only show a tooltip if a meaningful item is under the
	 * cursor.
	 */
	@Override
	protected boolean shouldCreateToolTip(Event event) {
		return super.shouldCreateToolTip(event)
				&& getModelElement(event) != null;
	}

	@Override
	protected Composite createToolTipContentArea(Event event,
			Composite parent) {
		Object modelElement = getModelElement(event);

		Image iconImage = null;
		String nameString = null;

		if (modelElement instanceof DisplayItem) {
			iconImage = ((DisplayItem) modelElement).getImage();
			nameString = ((DisplayItem) modelElement).getLabel();
		} else if (modelElement instanceof ActionSet) {
			nameString = ((ActionSet) modelElement).descriptor.getLabel();
		}

		// Create the content area
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setBackground(parent.getDisplay().getSystemColor(
				SWT.COLOR_INFO_BACKGROUND));
		composite.setLayout(new GridLayout(2, false));

		// The title area with the icon (if there is one) and label.
		Label title = createEntry(composite, iconImage, nameString);
		title.setFont(getTitleFont());
		GridDataFactory.createFrom((GridData)title.getLayoutData())
			.hint(SWT.DEFAULT, SWT.DEFAULT)
			.minSize(CustomizePerspectiveDialog.MIN_TOOLTIP_WIDTH, 1)
			.applyTo(title);

		// The description (if there is one)
		String descriptionString = NameAndDescriptionToolTip.getDescription(modelElement);
		if (descriptionString != null) {
			createEntry(composite, null, descriptionString);
		}

		// Other Content to add
		addContent(composite, modelElement);

		return composite;
	}

	/**
	 * @return a font for titles in the tooltips
	 */
	Font getTitleFont() {
		return JFaceResources.getFontRegistry().getBold(NameAndDescriptionToolTip.class.getName());
	}

	/**
	 * Adds a line of information to <code>parent</code>. If
	 * <code>icon</code> is not <code>null</code>, an icon is placed on the
	 * left, and then a label with <code>text</code>.
	 *
	 * @param parent
	 *            the composite to add the entry to
	 * @param icon
	 *            the icon to place next to the text. <code>null</code> for
	 *            none.
	 * @param text
	 *            the text to display
	 * @return the created label
	 */
	protected Label createEntry(Composite parent, Image icon, String text) {
		Color fg = parent.getDisplay().getSystemColor(SWT.COLOR_INFO_FOREGROUND);
		Color bg = parent.getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND);
		if (icon != null) {
			Label iconLabel = new Label(parent, SWT.NONE);
			iconLabel.setImage(icon);
			iconLabel.setForeground(fg);
			iconLabel.setBackground(bg);
			iconLabel.setData(new GridData());
		}

		Label textLabel = new Label(parent, SWT.WRAP);

		if(icon == null) {
			GridDataFactory.generate(textLabel, 2, 1);
		} else {
			GridDataFactory.generate(textLabel, 1, 1);
		}

		if (text != null) {
			textLabel.setText(text);
		}
		textLabel.setForeground(fg);
		textLabel.setBackground(bg);
		return textLabel;
	}

	/**
	 * Adds a line of information to <code>parent</code>. If
	 * <code>icon</code> is not <code>null</code>, an icon is placed on the
	 * left, and then a label with <code>text</code>, which supports using
	 * anchor tags to creates links
	 *
	 * @param parent
	 *            the composite to add the entry to
	 * @param icon
	 *            the icon to place next to the text. <code>null</code> for
	 *            none.
	 * @param text
	 *            the text to display
	 * @return the created link
	 */
	protected Link createEntryWithLink(Composite parent, Image icon,
			String text) {
		Color fg = parent.getDisplay().getSystemColor(SWT.COLOR_INFO_FOREGROUND);
		Color bg = parent.getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND);
		if (icon != null) {
			Label iconLabel = new Label(parent, SWT.NONE);
			iconLabel.setImage(icon);
			iconLabel.setForeground(fg);
			iconLabel.setBackground(bg);
			iconLabel.setData(new GridData());
		}

		Link textLink = new Link(parent, SWT.WRAP);

		if(icon == null) {
			GridDataFactory.generate(textLink, 2, 1);
		}

		textLink.setText(text);
		textLink.setForeground(fg);
		textLink.setBackground(bg);
		return textLink;
	}

	/**
	 * @param destination
	 * @param modelElement
	 */
	protected void addContent(Composite destination, Object modelElement) {
	}

	static String getDescription(IContributionItem item) {
		if (item instanceof ActionContributionItem) {
			ActionContributionItem aci = (ActionContributionItem) item;
			IAction action = aci.getAction();
			if (action == null) {
				return null;
			}
			return action.getDescription();
		}
		if (item instanceof ActionSetContributionItem) {
			ActionSetContributionItem asci = (ActionSetContributionItem) item;
			IContributionItem subitem = asci.getInnerItem();
			return getDescription(subitem);
		}
		return null;
	}

	static String getDescription(Object object) {
		if (object instanceof DisplayItem) {
			DisplayItem item = (DisplayItem) object;

			if (CustomizePerspectiveDialog.isNewWizard(item)) {
				ShortcutItem shortcut = (ShortcutItem) item;
				IWizardDescriptor descriptor = (IWizardDescriptor) shortcut
						.getDescriptor();
				return descriptor.getDescription();
			}

			if (CustomizePerspectiveDialog.isShowPerspective(item)) {
				ShortcutItem shortcut = (ShortcutItem) item;
				IPerspectiveDescriptor descriptor = (IPerspectiveDescriptor) shortcut
						.getDescriptor();
				return descriptor.getDescription();
			}

			if (CustomizePerspectiveDialog.isShowView(item)) {
				ShortcutItem shortcut = (ShortcutItem) item;
				IViewDescriptor descriptor = (IViewDescriptor) shortcut
						.getDescriptor();
				return descriptor.getDescription();
			}

			if (item instanceof DynamicContributionItem) {
				return WorkbenchMessages.HideItems_dynamicItemDescription;
			}

			IContributionItem contrib = item.getIContributionItem();
			return NameAndDescriptionToolTip.getDescription(contrib);
		}

		if (object instanceof ActionSet) {
			ActionSet actionSet = (ActionSet) object;
			return actionSet.descriptor.getDescription();
		}

		return null;
	}
}