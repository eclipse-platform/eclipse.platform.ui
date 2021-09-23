/*******************************************************************************
* Copyright (c) 2019 vogella GmbH and others.
*
* This program and the accompanying materials
* are made available under the terms of the Eclipse Public License 2.0
* which accompanies this distribution, and is available at
* https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Lars Vogel <Lars.Vogel@vogella.com> - initial version
******************************************************************************/
package org.eclipse.jface.widgets;


import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Sash;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;

/**
 * This class provides a convenient shorthand for creating and initializing
 * factories for SWT widgets. This offers several benefits over creating SWT
 * widgets with the low level SWT API
 *
 * <ul>
 * <li>The same factory can be used many times to create several widget
 * instances</li>
 * <li>The setters can be chained</li>
 * <li>Factories accept a lambda whenever applicable</li>
 * </ul>
 *
 * Example usage:
 *
 * <pre>
 * Button button = WidgetFactory.button(SWT.PUSH) //
 * 		.text("Click me!") //
 * 		.onSelect(event -&gt; buttonClicked(event)) //
 * 		.layoutData(gridData) //
 * 		.create(parent);
 * </pre>
 * <p>
 * The above example creates a push button with a text, registers a
 * SelectionListener and finally creates the button in "parent".
 * </p>
 *
 * <pre>
 * GridDataFactory gridDataFactory = GridDataFactory.swtDefaults();
 * ButtonFactory buttonFactory = WidgetFactory.button(SWT.PUSH).onSelect(event -&gt; buttonClicked(event))
 * 		.layout(gridDataFactory::create);
 * buttonFactory.text("Button 1").create(parent);
 * buttonFactory.text("Button 2").create(parent);
 * buttonFactory.text("Button 3").create(parent);
 * </pre>
 * <p>
 * The above example creates three buttons using the same instance of
 * ButtonFactory. Note the layout method. A Supplier is used to create unique
 * GridData for every single button.
 * </p>
 *
 * @since 3.18
 *
 */
public final class WidgetFactory {
	private WidgetFactory() {
	}

	/**
	 * @param style SWT style applicable for Button. Refer to
	 *              {@link Button#Button(Composite, int)} for supported styles.
	 * @return ButtonFactory
	 */
	public static ButtonFactory button(int style) {
		return ButtonFactory.newButton(style);
	}

	/**
	 * @param style SWT style applicable for Text. Refer to
	 *              {@link Text#Text(Composite, int)} for supported styles.
	 * @return TextFactory
	 */
	public static TextFactory text(int style) {
		return TextFactory.newText(style);
	}

	/**
	 * @param style SWT style applicable for Label. Refer to
	 *              {@link Label#Label(Composite, int)} for supported styles.
	 * @return LabelFactory
	 */
	public static LabelFactory label(int style) {
		return LabelFactory.newLabel(style);
	}

	/**
	 * @param style SWT style applicable for Composite. Refer to
	 *              {@link Composite#Composite(Composite, int)} for supported styles.
	 * @return CompositeFactory
	 */
	public static CompositeFactory composite(int style) {
		return CompositeFactory.newComposite(style);
	}

	/**
	 * @param style SWT style applicable for DateTime. Refer to
	 *              {@link DateTime#DateTime(Composite, int)} for supported styles.
	 * @return DateTimeFactory
	 *
	 * @since 3.22
	 */
	public static DateTimeFactory dateTime(int style) {
		return DateTimeFactory.newDateTime(style);
	}

	/**
	 * @param style SWT style applicable for Spinner. Refer to
	 *              {@link Spinner#Spinner(Composite, int)} for supported styles.
	 * @return SpinnerFactory
	 */
	public static SpinnerFactory spinner(int style) {
		return SpinnerFactory.newSpinner(style);
	}

	/**
	 * @param style SWT style applicable for Table. Refer to
	 *              {@link Table#Table(Composite, int)} for supported styles.
	 * @return TableFactory
	 */
	public static TableFactory table(int style) {
		return TableFactory.newTable(style);
	}

	/**
	 * @param style SWT style applicable for Tree. Refer to
	 *              {@link Tree#Tree(Composite, int)} for supported styles.
	 * @return TreeFactory
	 */
	public static TreeFactory tree(int style) {
		return TreeFactory.newTree(style);
	}

	/**
	 * @param style SWT style applicable for TableColumn. Refer to
	 *              {@link TableColumn#TableColumn(Table, int)} for supported
	 *              styles.
	 * @return TableColumnFactory
	 */
	public static TableColumnFactory tableColumn(int style) {
		return TableColumnFactory.newTableColumn(style);
	}

	/**
	 * @param style SWT style applicable for TreeColumn. Refer to
	 *              {@link TreeColumn#TreeColumn(Tree, int)} for supported styles.
	 * @return TreeColumnFactory
	 */
	public static TreeColumnFactory treeColumn(int style) {
		return TreeColumnFactory.newTreeColumn(style);
	}

	/**
	 * @param style SWT style applicable for Sash. Refer to
	 *              {@link Sash#Sash(Composite, int)} for supported styles.
	 * @return SashFactory
	 *
	 * @since 3.21
	 */
	public static SashFactory sash(int style) {
		return SashFactory.newSash(style);
	}

	/**
	 * @param style SWT style applicable for SashForm. Refer to
	 *              {@link SashForm#SashForm(Composite, int)} for supported styles.
	 * @return SashFormFactory
	 *
	 * @since 3.21
	 */
	public static SashFormFactory sashForm(int style) {
		return SashFormFactory.newSashForm(style);
	}

	/**
	 * @param style SWT style applicable for Shell. Refer to
	 *              {@link Shell#Shell(Shell, int)} for supported styles.
	 * @return ShellFactory
	 *
	 * @since 3.21
	 */
	public static ShellFactory shell(int style) {
		return ShellFactory.newShell(style);
	}

	/**
	 * @param style SWT style applicable for Group. Refer to
	 *              {@link Group#Group(Composite, int)} for supported styles.
	 * @return GroupFactory
	 *
	 * @since 3.24
	 */
	public static GroupFactory group(int style) {
		return GroupFactory.newGroup(style);
	}
}