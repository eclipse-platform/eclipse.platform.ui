/*******************************************************************************
 * Copyright (c) 2009 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 262946)
 ******************************************************************************/

package org.eclipse.jface.tests.databinding.swt;

import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.conformance.util.RealmTester;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.tests.databinding.AbstractSWTTestCase;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Tray;
import org.eclipse.swt.widgets.TrayItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;

/**
 * @since 3.2
 * 
 */
public class WidgetPropertiesTest extends AbstractSWTTestCase {
	private Shell shell;

	protected void setUp() throws Exception {
		super.setUp();

		shell = getShell();
		RealmTester.setDefault(SWTObservables.getRealm(shell.getDisplay()));
	}

	protected void tearDown() throws Exception {
		super.tearDown();

		RealmTester.setDefault(null);
	}

	public void testTooltipText_ObserveCTabItem() {
		CTabFolder tf = new CTabFolder(shell, SWT.NONE);
		CTabItem item = new CTabItem(tf, SWT.NONE);
		item.setToolTipText("1");

		IObservableValue observable = WidgetProperties.tooltipText().observe(
				item);
		assertEquals("1", observable.getValue());

		observable.setValue("2");
		assertEquals("2", item.getToolTipText());
	}

	public void testTooltipText_ObserveControl() {
		Control control = new Label(shell, SWT.NONE);
		control.setToolTipText("1");

		IObservableValue observable = WidgetProperties.tooltipText().observe(
				control);
		assertEquals("1", observable.getValue());

		observable.setValue("2");
		assertEquals("2", control.getToolTipText());
	}

	public void testTooltipText_ObserveTabItem() {
		TabFolder tf = new TabFolder(shell, SWT.NONE);
		TabItem item = new TabItem(tf, SWT.NONE);
		item.setToolTipText("1");

		IObservableValue observable = WidgetProperties.tooltipText().observe(
				item);
		assertEquals("1", observable.getValue());

		observable.setValue("2");
		assertEquals("2", item.getToolTipText());
	}

	public void testTooltipText_ObserveTableColumn() {
		Table table = new Table(shell, SWT.NONE);
		TableColumn column = new TableColumn(table, SWT.NONE);
		column.setToolTipText("1");

		IObservableValue observable = WidgetProperties.tooltipText().observe(
				column);
		assertEquals("1", observable.getValue());

		observable.setValue("2");
		assertEquals("2", column.getToolTipText());
	}

	public void testTooltipText_ObserveToolItem() {
		ToolBar bar = new ToolBar(shell, SWT.NONE);
		ToolItem item = new ToolItem(bar, SWT.NONE);
		item.setToolTipText("1");

		IObservableValue observable = WidgetProperties.tooltipText().observe(
				item);
		assertEquals("1", observable.getValue());

		observable.setValue("2");
		assertEquals("2", item.getToolTipText());
	}

	public void testTooltipText_ObserveTrayItem() {
		Tray tray = shell.getDisplay().getSystemTray();
		TrayItem item = new TrayItem(tray, SWT.NONE);
		item.setToolTipText("1");

		IObservableValue observable = WidgetProperties.tooltipText().observe(
				item);
		assertEquals("1", observable.getValue());

		observable.setValue("2");
		assertEquals("2", item.getToolTipText());
	}

	public void testTooltipText_ObserveTreeColumn() {
		Tree tree = new Tree(shell, SWT.NONE);
		TreeColumn column = new TreeColumn(tree, SWT.NONE);
		column.setToolTipText("1");

		IObservableValue observable = WidgetProperties.tooltipText().observe(
				column);
		assertEquals("1", observable.getValue());

		observable.setValue("2");
		assertEquals("2", column.getToolTipText());
	}
}
