/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
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
package org.eclipse.ui.dialogs;

import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;

import java.text.MessageFormat;
import java.util.Iterator;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;

/**
 * The PreferenceLinkArea is the link area used to open a specific preference
 * page.
 *
 * @since 3.1
 */
public class PreferenceLinkArea extends Object {

	private Link pageLink;

	/**
	 * Create a new instance of the receiver
	 *
	 * @param parent        the parent Composite
	 * @param style         the SWT style
	 * @param pageId        the page id
	 * @param message       the message to use as text. If this message has {0} in
	 *                      its value it will be bound with the displayed name of
	 *                      the preference page. This message must be well formed
	 *                      html if you wish to link to another page.
	 * @param pageContainer - The container another page will be opened in.
	 * @param pageData      - The data to apply to the page.
	 */
	public PreferenceLinkArea(Composite parent, int style, final String pageId, String message,
			final IWorkbenchPreferenceContainer pageContainer, final Object pageData) {
		pageLink = new Link(parent, style);

		IPreferenceNode node = getPreferenceNode(pageId);
		String result;
		if (node == null) {
			result = NLS.bind(WorkbenchMessages.PreferenceNode_NotFound, pageId);
		} else {
			result = MessageFormat.format(message, node.getLabelText());

			// Only add the selection listener if the node is found
			pageLink.addSelectionListener(widgetSelectedAdapter(e -> pageContainer.openPage(pageId, pageData)));
		}
		pageLink.setText(result);

	}

	/**
	 * Get the preference node with pageId.
	 *
	 * @return IPreferenceNode
	 */
	private IPreferenceNode getPreferenceNode(String pageId) {
		Iterator<IPreferenceNode> iterator = PlatformUI.getWorkbench().getPreferenceManager().getElements(
				PreferenceManager.PRE_ORDER)
				.iterator();
		while (iterator.hasNext()) {
			IPreferenceNode next = iterator.next();
			if (next.getId().equals(pageId)) {
				return next;
			}
		}
		return null;
	}

	/**
	 * Return the control for the receiver.
	 *
	 * @return Control
	 */
	public Control getControl() {
		return pageLink;
	}
}
