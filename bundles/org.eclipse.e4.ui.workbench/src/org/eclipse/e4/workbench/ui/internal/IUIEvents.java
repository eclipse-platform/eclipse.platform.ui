/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.workbench.ui.internal;

import org.eclipse.e4.ui.model.application.MApplicationPackage;

/**
 * Transforms E4 MPart events into 3.x legacy events.
 */
public interface IUIEvents {
	public static interface EventTypes {
		public static final String Create = "Create"; //$NON-NLS-1$
		public static final String Set = "Set"; //$NON-NLS-1$
		public static final String Add = "Add"; //$NON-NLS-1$
		public static final String Remove = "Remove"; //$NON-NLS-1$
	}

	// Event data Tags
	public static interface EventTags {
		public static final String Element = "Changed Element"; //$NON-NLS-1$
		public static final String Widget = "Widget"; //$NON-NLS-1$
		public static final String Type = "Event Type"; //$NON-NLS-1$
		public static final String AttName = "Att Name"; //$NON-NLS-1$
		public static final String OldValue = "Old Value"; //$NON-NLS-1$
		public static final String NewValue = "New Value"; //$NON-NLS-1$
	}

	/**
	 * This is a hand-coded scrape of the current UI model. This really should be generated from the
	 * model itself. It's used to break the model's events into different topics based on the
	 * abstract data element being modified. This allows a client to, for example, listen only to
	 * UIItem changes rather than getting spammed with all UI Model events.
	 * 
	 * <b>NOTE:</b> Due to the current implementation we are required to use *unique* names for each
	 * of the data properties. This is to avoid name clashing issues should both data elements be
	 * inherited by some concrete leaf. We should look into how to handle this more elegantly...
	 */
	// Event 'buckets' are based on the EMF class name of the data classes
	public static final String UITopicBase = "org/eclipse/e4/ui/model"; //$NON-NLS-1$

	public static interface AppElement {
		public static final String Topic = UITopicBase + '/'
				+ MApplicationPackage.Literals.APPLICATION_ELEMENT.getName();
		public static final String Id = MApplicationPackage.Literals.APPLICATION_ELEMENT__ID
				.getName();
	}

	public static interface Command {
		public static final String Topic = UITopicBase + '/'
				+ MApplicationPackage.Literals.COMMAND.getName();

		public static final String Name = MApplicationPackage.Literals.COMMAND__COMMAND_NAME
				.getName();
	}

	public static interface Context {
		public static final String Topic = UITopicBase + '/'
				+ MApplicationPackage.Literals.CONTEXT.getName();
		public static final String Context = MApplicationPackage.Literals.CONTEXT__CONTEXT
				.getName();
		public static final String Variables = MApplicationPackage.Literals.CONTEXT__VARIABLES
				.getName();
	}

	public static interface Contribution {
		public static final String Topic = UITopicBase + '/'
				+ MApplicationPackage.Literals.CONTRIBUTION.getName();

		public static final String URI = MApplicationPackage.Literals.CONTRIBUTION__URI.getName();
		public static final String State = MApplicationPackage.Literals.CONTRIBUTION__PERSISTED_STATE
				.getName();
		public static final String Object = MApplicationPackage.Literals.CONTRIBUTION__OBJECT
				.getName();
	}

	public static interface Input {
		public static final String Topic = UITopicBase + '/'
				+ MApplicationPackage.Literals.INPUT.getName();

		public static final String Dirty = MApplicationPackage.Literals.INPUT__DIRTY.getName();
		public static final String URI = MApplicationPackage.Literals.INPUT__INPUT_URI.getName();
	}

	public static interface Parameter {
		public static final String Topic = UITopicBase + '/'
				+ MApplicationPackage.Literals.PARAMETER.getName();

		public static final String Tag = MApplicationPackage.Literals.PARAMETER__TAG.getName();
		public static final String Value = MApplicationPackage.Literals.PARAMETER__VALUE.getName();
	}

	public static interface UIItem {
		public static final String Topic = UITopicBase + '/'
				+ MApplicationPackage.Literals.UI_ITEM.getName();

		public static final String Name = MApplicationPackage.Literals.UI_ITEM__NAME.getName();
		public static final String IconURI = MApplicationPackage.Literals.UI_ITEM__ICON_URI
				.getName();
		public static final String Tooltip = MApplicationPackage.Literals.UI_ITEM__TOOLTIP
				.getName();
	}

	public static interface UIElement {
		public static final String Topic = UITopicBase + '/'
				+ MApplicationPackage.Literals.UI_ELEMENT.getName();

		public static final String Factory = MApplicationPackage.Literals.UI_ELEMENT__FACTORY
				.getName();
		public static final String Parent = MApplicationPackage.Literals.UI_ELEMENT__PARENT
				.getName();
		public static final String Visible = MApplicationPackage.Literals.UI_ELEMENT__VISIBLE
				.getName();
		public static final String Widget = MApplicationPackage.Literals.UI_ELEMENT__WIDGET
				.getName();
	}

	public static interface ElementContainer {
		public static final String Topic = UITopicBase + '/'
				+ MApplicationPackage.Literals.ELEMENT_CONTAINER.getName();

		public static final String Children = MApplicationPackage.Literals.ELEMENT_CONTAINER__CHILDREN
				.getName();
		public static final String ActiveChild = MApplicationPackage.Literals.ELEMENT_CONTAINER__ACTIVE_CHILD
				.getName();
	}

	public static interface Window {
		public static final String Topic = UITopicBase + '/'
				+ MApplicationPackage.Literals.WINDOW.getName();

		public static final String X = MApplicationPackage.Literals.WINDOW__X.getName();
		public static final String Y = MApplicationPackage.Literals.WINDOW__Y.getName();
		public static final String Width = MApplicationPackage.Literals.WINDOW__WIDTH.getName();
		public static final String Height = MApplicationPackage.Literals.WINDOW__HEIGHT.getName();
		public static final String MainMenu = MApplicationPackage.Literals.WINDOW__MAIN_MENU
				.getName();
	}
}
