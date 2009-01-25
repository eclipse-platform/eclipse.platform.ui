/*******************************************************************************
 * Copyright (c) 2008 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 194734)
 *     Matthew Hall - bugs 256543, 213893
 ******************************************************************************/

package org.eclipse.jface.databinding.swt;

import org.eclipse.core.databinding.property.list.DelegatingListProperty;
import org.eclipse.core.databinding.property.list.IListProperty;
import org.eclipse.core.databinding.property.value.DelegatingValueProperty;
import org.eclipse.core.databinding.property.value.IValueProperty;
import org.eclipse.jface.internal.databinding.swt.ButtonImageProperty;
import org.eclipse.jface.internal.databinding.swt.ButtonSelectionProperty;
import org.eclipse.jface.internal.databinding.swt.ButtonTextProperty;
import org.eclipse.jface.internal.databinding.swt.CComboItemsProperty;
import org.eclipse.jface.internal.databinding.swt.CComboSelectionProperty;
import org.eclipse.jface.internal.databinding.swt.CComboSingleSelectionIndexProperty;
import org.eclipse.jface.internal.databinding.swt.CComboTextProperty;
import org.eclipse.jface.internal.databinding.swt.CLabelImageProperty;
import org.eclipse.jface.internal.databinding.swt.CLabelTextProperty;
import org.eclipse.jface.internal.databinding.swt.CTabItemTooltipTextProperty;
import org.eclipse.jface.internal.databinding.swt.ComboItemsProperty;
import org.eclipse.jface.internal.databinding.swt.ComboSelectionProperty;
import org.eclipse.jface.internal.databinding.swt.ComboSingleSelectionIndexProperty;
import org.eclipse.jface.internal.databinding.swt.ComboTextProperty;
import org.eclipse.jface.internal.databinding.swt.ControlBackgroundProperty;
import org.eclipse.jface.internal.databinding.swt.ControlBoundsProperty;
import org.eclipse.jface.internal.databinding.swt.ControlEnabledProperty;
import org.eclipse.jface.internal.databinding.swt.ControlFocusedProperty;
import org.eclipse.jface.internal.databinding.swt.ControlFontProperty;
import org.eclipse.jface.internal.databinding.swt.ControlForegroundProperty;
import org.eclipse.jface.internal.databinding.swt.ControlLocationProperty;
import org.eclipse.jface.internal.databinding.swt.ControlSizeProperty;
import org.eclipse.jface.internal.databinding.swt.ControlTooltipTextProperty;
import org.eclipse.jface.internal.databinding.swt.ControlVisibleProperty;
import org.eclipse.jface.internal.databinding.swt.ItemTextProperty;
import org.eclipse.jface.internal.databinding.swt.LabelImageProperty;
import org.eclipse.jface.internal.databinding.swt.LabelTextProperty;
import org.eclipse.jface.internal.databinding.swt.LinkTextProperty;
import org.eclipse.jface.internal.databinding.swt.ListItemsProperty;
import org.eclipse.jface.internal.databinding.swt.ListSelectionProperty;
import org.eclipse.jface.internal.databinding.swt.ListSingleSelectionIndexProperty;
import org.eclipse.jface.internal.databinding.swt.ScaleMaximumProperty;
import org.eclipse.jface.internal.databinding.swt.ScaleMinimumProperty;
import org.eclipse.jface.internal.databinding.swt.ScaleSelectionProperty;
import org.eclipse.jface.internal.databinding.swt.ShellTextProperty;
import org.eclipse.jface.internal.databinding.swt.SpinnerMaximumProperty;
import org.eclipse.jface.internal.databinding.swt.SpinnerMinimumProperty;
import org.eclipse.jface.internal.databinding.swt.SpinnerSelectionProperty;
import org.eclipse.jface.internal.databinding.swt.StyledTextTextProperty;
import org.eclipse.jface.internal.databinding.swt.TabItemTooltipTextProperty;
import org.eclipse.jface.internal.databinding.swt.TableColumnTooltipTextProperty;
import org.eclipse.jface.internal.databinding.swt.TableSingleSelectionIndexProperty;
import org.eclipse.jface.internal.databinding.swt.TextEditableProperty;
import org.eclipse.jface.internal.databinding.swt.TextTextProperty;
import org.eclipse.jface.internal.databinding.swt.ToolItemTooltipTextProperty;
import org.eclipse.jface.internal.databinding.swt.TrayItemTooltipTextProperty;
import org.eclipse.jface.internal.databinding.swt.TreeItemTooltipTextProperty;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.TrayItem;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;

/**
 * A factory for creating properties of SWT {@link Widget widgets}.
 * 
 * @since 1.3
 */
public class WidgetProperties {
	private static RuntimeException notSupported(Object source) {
		return new IllegalArgumentException(
				"Widget [" + source.getClass().getName() + "] is not supported."); //$NON-NLS-1$//$NON-NLS-2$
	}

	/**
	 * Returns a value property for observing the background color of a
	 * {@link Control}.
	 * 
	 * @return a value property for observing the background color of a
	 *         {@link Control}.
	 */
	public static IValueProperty background() {
		return new ControlBackgroundProperty();
	}

	/**
	 * Returns a value property for observing the bounds of a {@link Control}.
	 * 
	 * @return a value property for observing the bounds of a {@link Control}.
	 */
	public static IValueProperty bounds() {
		return new ControlBoundsProperty();
	}

	/**
	 * Returns a value property for observing the editable state of a
	 * {@link Text}.
	 * 
	 * @return a value property for observing the editable state of a
	 *         {@link Text}.
	 */
	public static IValueProperty editable() {
		return new DelegatingValueProperty(Boolean.TYPE) {
			IValueProperty text = new TextEditableProperty();

			protected IValueProperty doGetDelegate(Object source) {
				if (source instanceof Text)
					return text;
				throw notSupported(source);
			}
		};
	}

	/**
	 * Returns a value property for observing the enablement state of a
	 * {@link Control}.
	 * 
	 * @return a value property for observing the enablement state of a
	 *         {@link Control}.
	 */
	public static IValueProperty enabled() {
		return new ControlEnabledProperty();
	}

	/**
	 * Returns a value property for observing the focus state of a
	 * {@link Control}.
	 * 
	 * @return a value property for observing the focus state of a
	 *         {@link Control}.
	 */
	public static IValueProperty focused() {
		return new ControlFocusedProperty();
	}

	/**
	 * Returns a value property for observing the font of a {@link Control}.
	 * 
	 * @return a value property for observing the font of a {@link Control}.
	 */
	public static IValueProperty font() {
		return new ControlFontProperty();
	}

	/**
	 * Returns a value property for observing the foreground color of a
	 * {@link Control}.
	 * 
	 * @return a value property for observing the foreground color of a
	 *         {@link Control}.
	 */
	public static IValueProperty foreground() {
		return new ControlForegroundProperty();
	}

	/**
	 * Returns a value property for observing the image of a {@link Button},
	 * {@link CLabel} or {@link Label}.
	 * 
	 * @return a value property for observing the image of a {@link Button},
	 *         {@link CLabel} or {@link Label}.
	 */
	public static IValueProperty image() {
		return new DelegatingValueProperty(Image.class) {
			private IValueProperty button = new ButtonImageProperty();
			private IValueProperty cLabel = new CLabelImageProperty();
			private IValueProperty label = new LabelImageProperty();

			protected IValueProperty doGetDelegate(Object source) {
				if (source instanceof Button)
					return button;
				if (source instanceof Label)
					return label;
				if (source instanceof CLabel)
					return cLabel;
				throw notSupported(source);
			}
		};
	}

	/**
	 * Returns a list property for observing the items of a {@link CCombo},
	 * {@link Combo} or {@link List}.
	 * 
	 * @return a list property for observing the items of a {@link CCombo},
	 *         {@link Combo} or {@link List}.
	 */
	public static IListProperty items() {
		return new DelegatingListProperty(String.class) {
			private IListProperty cCombo = new CComboItemsProperty();
			private IListProperty combo = new ComboItemsProperty();
			private IListProperty list = new ListItemsProperty();

			protected IListProperty doGetDelegate(Object source) {
				if (source instanceof CCombo)
					return cCombo;
				if (source instanceof Combo)
					return combo;
				if (source instanceof List)
					return list;
				throw notSupported(source);
			}
		};
	}

	/**
	 * Returns a value property for observing the location of a {@link Control}.
	 * 
	 * @return a value property for observing the location of a {@link Control}.
	 */
	public static IValueProperty location() {
		return new ControlLocationProperty();
	}

	/**
	 * Returns a value property for observing the maximum value of a
	 * {@link Scale} or {@link Spinner}.
	 * 
	 * @return a value property for observing the maximum value of a
	 *         {@link Scale} or {@link Spinner}.
	 */
	public static IValueProperty maximum() {
		return new DelegatingValueProperty(Integer.TYPE) {
			private IValueProperty scale = new ScaleMaximumProperty();
			private IValueProperty spinner = new SpinnerMaximumProperty();

			protected IValueProperty doGetDelegate(Object source) {
				if (source instanceof Scale)
					return scale;
				if (source instanceof Spinner)
					return spinner;
				throw notSupported(source);
			}
		};
	}

	/**
	 * Returns a value property for observing the minimum value of a
	 * {@link Scale} or {@link Spinner}.
	 * 
	 * @return a value property for observing the minimum value of a
	 *         {@link Scale} or {@link Spinner}.
	 */
	public static IValueProperty minimum() {
		return new DelegatingValueProperty(Integer.TYPE) {
			private IValueProperty scale = new ScaleMinimumProperty();
			private IValueProperty spinner = new SpinnerMinimumProperty();

			protected IValueProperty doGetDelegate(Object source) {
				if (source instanceof Scale)
					return scale;
				if (source instanceof Spinner)
					return spinner;
				throw notSupported(source);
			}
		};
	}

	/**
	 * Returns a value property for observing the selection state of a
	 * {@link Button}, {@link CCombo}, {@link Combo}, {@link List},
	 * {@link Scale} or {@link Spinner}.
	 * 
	 * @return a value property for observing the selection state of a
	 *         {@link Button}, {@link CCombo}, {@link Combo}, {@link List},
	 *         {@link Scale} or {@link Spinner}.
	 */
	public static IValueProperty selection() {
		return new DelegatingValueProperty() {
			private IValueProperty button = new ButtonSelectionProperty();
			private IValueProperty cCombo = new CComboSelectionProperty();
			private IValueProperty combo = new ComboSelectionProperty();
			private IValueProperty list = new ListSelectionProperty();
			private IValueProperty scale = new ScaleSelectionProperty();
			private IValueProperty spinner = new SpinnerSelectionProperty();

			protected IValueProperty doGetDelegate(Object source) {
				if (source instanceof Button)
					return button;
				if (source instanceof CCombo)
					return cCombo;
				if (source instanceof Combo)
					return combo;
				if (source instanceof List)
					return list;
				if (source instanceof Scale)
					return scale;
				if (source instanceof Spinner)
					return spinner;
				throw notSupported(source);
			}
		};
	}

	/**
	 * Returns a value property for observing the single selection index of a
	 * {@link CCombo}, {@link Combo}, {@link List} or {@link Table}.
	 * 
	 * @return a value property for the single selection index of a SWT Combo.
	 */
	public static IValueProperty singleSelectionIndex() {
		return new DelegatingValueProperty(Integer.TYPE) {
			private IValueProperty cCombo = new CComboSingleSelectionIndexProperty();
			private IValueProperty combo = new ComboSingleSelectionIndexProperty();
			private IValueProperty list = new ListSingleSelectionIndexProperty();
			private IValueProperty table = new TableSingleSelectionIndexProperty();

			protected IValueProperty doGetDelegate(Object source) {
				if (source instanceof CCombo)
					return cCombo;
				if (source instanceof Combo)
					return combo;
				if (source instanceof List)
					return list;
				if (source instanceof Table)
					return table;
				throw notSupported(source);
			}
		};
	}

	/**
	 * Returns a value property for observing the size of a {@link Control}.
	 * 
	 * @return a value property for observing the size of a {@link Control}.
	 */
	public static IValueProperty size() {
		return new ControlSizeProperty();
	}

	/**
	 * Returns a value property for observing the text of a {@link Button},
	 * {@link CCombo}, {@link CLabel}, {@link Combo}, {@link Item},
	 * {@link Label}, {@link Link}, {@link Shell}, {@link StyledText} or
	 * {@link Text}.
	 * 
	 * @return a value property for observing the text of a {@link Button},
	 *         {@link CCombo}, {@link CLabel}, {@link Combo}, {@link Item},
	 *         {@link Label}, {@link Link}, {@link Shell}, {@link StyledText} or
	 *         {@link Text}.
	 */
	public static IValueProperty text() {
		return new DelegatingValueProperty(String.class) {
			private IValueProperty button = new ButtonTextProperty();
			private IValueProperty cCombo = new CComboTextProperty();
			private IValueProperty cLabel = new CLabelTextProperty();
			private IValueProperty combo = new ComboTextProperty();
			private IValueProperty item = new ItemTextProperty();
			private IValueProperty label = new LabelTextProperty();
			private IValueProperty link = new LinkTextProperty();
			private IValueProperty shell = new ShellTextProperty();
			private IValueProperty styledText = new StyledTextTextProperty();
			private IValueProperty text = new TextTextProperty();

			protected IValueProperty doGetDelegate(Object source) {
				if (source instanceof Button)
					return button;
				if (source instanceof CCombo)
					return cCombo;
				if (source instanceof CLabel)
					return cLabel;
				if (source instanceof Combo)
					return combo;
				if (source instanceof Item)
					return item;
				if (source instanceof Label)
					return label;
				if (source instanceof Link)
					return link;
				if (source instanceof Shell)
					return shell;
				if (source instanceof StyledText)
					return styledText;
				if (source instanceof Text)
					return text;
				throw notSupported(source);
			}
		};
	}

	/**
	 * Returns a value property for observing the text of a {@link StyledText}
	 * or {@link Text}.
	 * 
	 * @param event
	 *            the SWT event type to register for change events. May be
	 *            {@link SWT#None}, {@link SWT#Modify}, {@link SWT#FocusOut} or
	 *            {@link SWT#DefaultSelection}.
	 * 
	 * @return a value property for observing the text of a {@link StyledText}
	 *         or {@link Text}.
	 */
	public static IValueProperty text(final int event) {
		return text(new int[] { event });
	}

	/**
	 * Returns a value property for observing the text of a {@link StyledText}
	 * or {@link Text}.
	 * 
	 * @param events
	 *            array of SWT event types to register for change events. May
	 *            include {@link SWT#None}, {@link SWT#Modify},
	 *            {@link SWT#FocusOut} or {@link SWT#DefaultSelection}.
	 * 
	 * @return a value property for observing the text of a {@link StyledText}
	 *         or {@link Text}.
	 */
	public static IValueProperty text(int[] events) {
		final int[] events_ = (int[]) events.clone();
		return new DelegatingValueProperty(String.class) {
			private IValueProperty styledText = new StyledTextTextProperty(
					events_);
			private IValueProperty text = new TextTextProperty(events_);

			protected IValueProperty doGetDelegate(Object source) {
				if (source instanceof StyledText)
					return styledText;
				if (source instanceof Text)
					return text;
				throw notSupported(source);
			}
		};
	}

	/**
	 * Returns a value property for observing the tooltip text of a
	 * {@link CTabItem}, {@link Control}, {@link TabItem}, {@link TableColumn},
	 * {@link ToolItem}, {@link TrayItem}, {@link TreeColumn} or
	 * {@link TreeItem}.
	 * 
	 * @return a value property for observing the tooltip text of a
	 *         {@link CTabItem}, {@link Control}, {@link TabItem},
	 *         {@link TableColumn}, {@link ToolItem}, {@link TrayItem},
	 *         {@link TreeColumn} or {@link TreeItem}.
	 */
	public static IValueProperty tooltipText() {
		return new DelegatingValueProperty(String.class) {
			private IValueProperty cTabItem = new CTabItemTooltipTextProperty();
			private IValueProperty control = new ControlTooltipTextProperty();
			private IValueProperty tabItem = new TabItemTooltipTextProperty();
			private IValueProperty tableColumn = new TableColumnTooltipTextProperty();
			private IValueProperty toolItem = new ToolItemTooltipTextProperty();
			private IValueProperty trayItem = new TrayItemTooltipTextProperty();
			private IValueProperty treeItem = new TreeItemTooltipTextProperty();

			protected IValueProperty doGetDelegate(Object source) {
				if (source instanceof CTabItem)
					return cTabItem;
				if (source instanceof Control)
					return control;
				if (source instanceof TabItem)
					return tabItem;
				if (source instanceof TableColumn)
					return tableColumn;
				if (source instanceof ToolItem)
					return toolItem;
				if (source instanceof TrayItem)
					return trayItem;
				if (source instanceof TreeItem)
					return treeItem;
				throw notSupported(source);
			}
		};
	}

	/**
	 * Returns a value property for observing the visibility state of a
	 * {@link Control}.
	 * 
	 * @return a value property for observing the visibility state of a
	 *         {@link Control}.
	 */
	public static IValueProperty visible() {
		return new ControlVisibleProperty();
	}
}
