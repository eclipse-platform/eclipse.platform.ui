/*******************************************************************************
* Copyright (c) 2021 SAP SE and others.
*
* This program and the accompanying materials
* are made available under the terms of the Eclipse Public License 2.0
* which accompanies this distribution, and is available at
* https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     SAP SE - initial version
******************************************************************************/
package org.eclipse.jface.widgets;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

/**
 * This class provides a convenient shorthand for creating and initializing
 * {@link Group}. This offers several benefits over creating Group normal way:
 *
 * <ul>
 * <li>The same factory can be used many times to create several Group
 * instances</li>
 * <li>The setters on GroupFactory all return "this", allowing them to be
 * chained</li>
 * </ul>
 *
 * Example usage:
 *
 * <pre>
 * Label label = GroupFactory.newGroup(SWT.SHADOW_NONE)//
 * 		.text("My Group") //
 * 		.layoutData(gridData) //
 * 		.create(parent);
 * </pre>
 * <p>
 * The above example creates a Group with a text and style: SWT.SHADOW_NONE.
 * Finally the group is created in "parent".
 * </p>
 *
 * <pre>
 * GroupFactory groupFactory = GroupFactory.newGroup(SWT.SHADOW_NONE);
 * GroupFactory.text("Group 1").create(parent);
 * GroupFactory.text("Group 2").create(parent);
 * GroupFactory.text("Group 3").create(parent);
 * </pre>
 * <p>
 * The above example creates three labels using the same instance of
 * LabelFactory.
 * </p>
 *
 * @since 3.24
 */
public class GroupFactory extends AbstractCompositeFactory<GroupFactory, Group> {

	private GroupFactory(int style) {
		super(GroupFactory.class, (Composite parent) -> new Group(parent, style));
	}

	/**
	 * Creates a new GroupFactory with the given style. Refer to
	 * {@link Group#Group(Composite, int)} for possible styles.
	 *
	 * @return a new GroupFactory instance
	 */
	public static GroupFactory newGroup(int style) {
		return new GroupFactory(style);
	}

	/**
	 * Sets the receiver's text, which is the string that will be displayed as the
	 * receiver's <em>title</em>, to the argument, which may not be null. The string
	 * may include the mnemonic character.
	 * <p>
	 * Mnemonics are indicated by an '&amp;' that causes the next character to be
	 * the mnemonic. When the user presses a key sequence that matches the mnemonic,
	 * focus is assigned to the first child of the group. On most platforms, the
	 * mnemonic appears underlined but may be emphasised in a platform specific
	 * manner. The mnemonic indicator character '&amp;' can be escaped by doubling
	 * it in the string, causing a single '&amp;' to be displayed.
	 * </p>
	 * <p>
	 * Note: If control characters like '\n', '\t' etc. are used in the string, then
	 * the behavior is platform dependent.
	 * </p>
	 *
	 * @param text the text
	 * @return this
	 *
	 * @see Group#setText(String)
	 */
	public GroupFactory text(String text) {
		addProperty(g -> g.setText(text));
		return this;
	}
}