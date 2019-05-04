/*******************************************************************************
 * Copyright (c) 2008, 2015 Matthew Hall and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 194734)
 *     Matthew Hall - bugs 256543, 213893, 262320, 262946, 264286, 266563, 169876, 306203
 *     Eugen Neufeld - bug 461560
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 482486
 ******************************************************************************/

package org.eclipse.jface.databinding.swt.typed;

import java.util.Date;

import org.eclipse.jface.databinding.swt.IWidgetListProperty;
import org.eclipse.jface.databinding.swt.IWidgetValueProperty;
import org.eclipse.jface.internal.databinding.swt.ButtonSelectionProperty;
import org.eclipse.jface.internal.databinding.swt.CComboSelectionProperty;
import org.eclipse.jface.internal.databinding.swt.ComboSelectionProperty;
import org.eclipse.jface.internal.databinding.swt.ControlBackgroundProperty;
import org.eclipse.jface.internal.databinding.swt.ControlBoundsProperty;
import org.eclipse.jface.internal.databinding.swt.ControlFocusedProperty;
import org.eclipse.jface.internal.databinding.swt.ControlFontProperty;
import org.eclipse.jface.internal.databinding.swt.ControlForegroundProperty;
import org.eclipse.jface.internal.databinding.swt.ControlLocationProperty;
import org.eclipse.jface.internal.databinding.swt.ControlSizeProperty;
import org.eclipse.jface.internal.databinding.swt.ControlVisibleProperty;
import org.eclipse.jface.internal.databinding.swt.DateTimeSelectionProperty;
import org.eclipse.jface.internal.databinding.swt.ListSelectionProperty;
import org.eclipse.jface.internal.databinding.swt.MenuItemSelectionProperty;
import org.eclipse.jface.internal.databinding.swt.ScaleSelectionProperty;
import org.eclipse.jface.internal.databinding.swt.SliderSelectionProperty;
import org.eclipse.jface.internal.databinding.swt.SpinnerSelectionProperty;
import org.eclipse.jface.internal.databinding.swt.WidgetEditableProperty;
import org.eclipse.jface.internal.databinding.swt.WidgetEnabledProperty;
import org.eclipse.jface.internal.databinding.swt.WidgetImageProperty;
import org.eclipse.jface.internal.databinding.swt.WidgetItemsProperty;
import org.eclipse.jface.internal.databinding.swt.WidgetMaximumProperty;
import org.eclipse.jface.internal.databinding.swt.WidgetMessageProperty;
import org.eclipse.jface.internal.databinding.swt.WidgetMinimumProperty;
import org.eclipse.jface.internal.databinding.swt.WidgetSelectionProperty;
import org.eclipse.jface.internal.databinding.swt.WidgetSingleSelectionIndexProperty;
import org.eclipse.jface.internal.databinding.swt.WidgetTextProperty;
import org.eclipse.jface.internal.databinding.swt.WidgetTextWithEventsProperty;
import org.eclipse.jface.internal.databinding.swt.WidgetTooltipTextProperty;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.ToolTip;
import org.eclipse.swt.widgets.TrayItem;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.Widget;

/**
 * A factory for creating properties of SWT {@link Widget widgets}.
 * <p>
 * This class is a new version of the deprecated class with the same name in the
 * parent package. The difference is that this class returns typed property
 * objects. This class is located in its own package to be able to coexist with
 * the old version while having the same name.
 *
 * @since 1.9
 */
public class WidgetProperties {
	/**
	 * Returns a value property for observing the background color of a
	 * {@link Control}.
	 *
	 * @return a value property for observing the background color of a
	 *         {@link Control}.
	 */
	public static <S extends Control> IWidgetValueProperty<S, Color> background() {
		return new ControlBackgroundProperty<>();
	}

	/**
	 * Returns a value property for observing the bounds of a {@link Control}.
	 *
	 * @return a value property for observing the bounds of a {@link Control}.
	 */
	public static <S extends Control> IWidgetValueProperty<S, Rectangle> bounds() {
		return new ControlBoundsProperty<>();
	}

	/**
	 * Returns a value property for observing the editable state of a
	 * {@link CCombo} (since 1.6), {@link StyledText} (since 1.6), or
	 * {@link Text}.
	 *
	 * @return a value property for observing the editable state of a
	 *         {@link CCombo}, {@link StyledText}, or {@link Text}.
	 */
	public static <S extends Control> IWidgetValueProperty<S, Boolean> editable() {
		return new WidgetEditableProperty<>();
	}

	/**
	 * Returns a value property for observing the enablement state of a
	 * {@link Control}, {@link Menu} (since 1.5), {@link MenuItem} (since 1.5),
	 * {@link ScrollBar} (since 1.5) or {@link ToolItem} (since 1.5).
	 *
	 * @return a value property for observing the enablement state of a
	 *         {@link Control}, {@link Menu}, {@link MenuItem},
	 *         {@link ScrollBar} or {@link ToolItem}.
	 */
	public static <S extends Widget> IWidgetValueProperty<S, Boolean> enabled() {
		return new WidgetEnabledProperty<>();
	}

	/**
	 * Returns a value property for observing the focus state of a
	 * {@link Control}.
	 *
	 * @return a value property for observing the focus state of a
	 *         {@link Control}.
	 */
	public static <S extends Control> IWidgetValueProperty<S, Boolean> focused() {
		return new ControlFocusedProperty<>();
	}

	/**
	 * Returns a value property for observing the font of a {@link Control}.
	 *
	 * @return a value property for observing the font of a {@link Control}.
	 */
	public static <S extends Control> IWidgetValueProperty<S, Font> font() {
		return new ControlFontProperty<>();
	}

	/**
	 * Returns a value property for observing the foreground color of a
	 * {@link Control}.
	 *
	 * @return a value property for observing the foreground color of a
	 *         {@link Control}.
	 */
	public static <S extends Control> IWidgetValueProperty<S, Color> foreground() {
		return new ControlForegroundProperty<>();
	}

	/**
	 * Returns a value property for observing the image of a {@link Button},
	 * {@link CLabel}, {@link Item} or {@link Label}.
	 *
	 * @return a value property for observing the image of a {@link Button},
	 *         {@link CLabel}, {@link Item} or {@link Label}.
	 */
	public static <S extends Widget> IWidgetValueProperty<S, Image> image() {
		return new WidgetImageProperty<>();
	}

	/**
	 * Returns a list property for observing the items of a {@link CCombo},
	 * {@link Combo} or {@link List}.
	 *
	 * @return a list property for observing the items of a {@link CCombo},
	 *         {@link Combo} or {@link List}.
	 */
	public static <S extends Control> IWidgetListProperty<S, String> items() {
		return new WidgetItemsProperty<>();
	}

	/**
	 * Returns a value property for observing the location of a {@link Control}.
	 *
	 * @return a value property for observing the location of a {@link Control}.
	 */
	public static <S extends Control> IWidgetValueProperty<S, Point> location() {
		return new ControlLocationProperty<>();
	}

	/**
	 * Returns a value property for observing the maximum value of a
	 * {@link Scale}, {@link Slider} (since 1.5) or {@link Spinner}.
	 *
	 * @return a value property for observing the maximum value of a
	 *         {@link Scale}, {@link Slider} (since 1.5) or {@link Spinner}.
	 */
	public static <S extends Control> IWidgetValueProperty<S, Integer> maximum() {
		return new WidgetMaximumProperty<>();
	}

	/**
	 * Returns a value property for observing the message of a {@link Text} or
	 * {@link ToolTip}.
	 *
	 * @return a value property for observing the message of a {@link Text} or
	 *         {@link ToolTip}.
	 */
	public static <S extends Widget> IWidgetValueProperty<S, String> message() {
		return new WidgetMessageProperty<>();
	}

	/**
	 * Returns a value property for observing the minimum value of a
	 * {@link Scale}, {@link Slider} (since 1.5) or {@link Spinner}.
	 *
	 * @return a value property for observing the minimum value of a
	 *         {@link Scale}, {@link Slider} (since 1.5) or {@link Spinner}.
	 */
	public static <S extends Control> IWidgetValueProperty<S, Integer> minimum() {
		return new WidgetMinimumProperty<>();
	}

	/**
	 * Returns a value property for observing the selection state of a
	 * {@link Button}, {@link CCombo}, {@link Combo}, {@link DateTime},
	 * {@link List}, {@link MenuItem} (since 1.5), {@link Scale}, {@link Slider}
	 * (since 1.5) or {@link Spinner}.
	 *
	 * @return a value property for observing the selection state of a
	 *         {@link Button}, {@link CCombo}, {@link Combo}, {@link DateTime},
	 *         {@link List}, {@link MenuItem}, {@link Scale}, {@link Slider} or
	 *         {@link Spinner}.
	 */
	public static <S extends Widget, T> IWidgetValueProperty<S, T> widgetSelection() {
		return new WidgetSelectionProperty<>();
	}

	/**
	 * Returns a value property for observing the selection state of a
	 * {@link DateTime}.
	 *
	 * @return a value property for observing the selection state of a
	 *         {@link DateTime}.
	 * @since 1.9
	 */
	public static IWidgetValueProperty<DateTime, Date> dateTimeSelection() {
		return new DateTimeSelectionProperty();
	}

	/**
	 * Returns a value property for observing the selection state of a
	 * {@link Button}.
	 *
	 * @return a value property for observing the selection state of a
	 *         {@link Button}.
	 * @since 1.9
	 */
	public static IWidgetValueProperty<Button, Boolean> buttonSelection() {
		return new ButtonSelectionProperty();
	}

	/**
	 * Returns a value property for observing the selection state of a
	 * {@link Combo}.
	 *
	 * @return a value property for observing the selection state of a
	 *         {@link Combo}.
	 * @since 1.9
	 */
	public static IWidgetValueProperty<Combo, String> comboSelection() {
		return new ComboSelectionProperty();
	}

	/**
	 * Returns a value property for observing the selection state of a
	 * {@link CCombo}.
	 *
	 * @return a value property for observing the selection state of a
	 *         {@link CCombo}.
	 * @since 1.9
	 */
	public static IWidgetValueProperty<CCombo, String> ccomboSelection() {
		return new CComboSelectionProperty();
	}

	/**
	 * Returns a value property for observing the selection state of a {@link List}.
	 *
	 * @return a value property for observing the selection state of a {@link List}.
	 * @since 1.9
	 */
	public static IWidgetValueProperty<List, String> listSelection() {
		return new ListSelectionProperty();
	}

	/**
	 * Returns a value property for observing the selection state of a
	 * {@link MenuItem}.
	 *
	 * @return a value property for observing the selection state of a
	 *         {@link MenuItem}.
	 * @since 1.9
	 */
	public static IWidgetValueProperty<MenuItem, Boolean> menuItemSelection() {
		return new MenuItemSelectionProperty();
	}

	/**
	 * Returns a value property for observing the selection state of a
	 * {@link Scale}.
	 *
	 * @return a value property for observing the selection state of a
	 *         {@link Scale}.
	 * @since 1.9
	 */
	public static IWidgetValueProperty<Scale, Integer> scaleSelection() {
		return new ScaleSelectionProperty();
	}

	/**
	 * Returns a value property for observing the selection state of a
	 * {@link Slider}.
	 *
	 * @return a value property for observing the selection state of a
	 *         {@link Slider}.
	 * @since 1.9
	 */
	public static IWidgetValueProperty<Slider, Integer> sliderSelection() {
		return new SliderSelectionProperty();
	}

	/**
	 * Returns a value property for observing the selection state of a
	 * {@link Spinner}.
	 *
	 * @return a value property for observing the selection state of a
	 *         {@link Spinner}.
	 * @since 1.9
	 */
	public static IWidgetValueProperty<Spinner, Integer> spinnerSelection() {
		return new SpinnerSelectionProperty();
	}

	/**
	 * Returns a value property for observing the single selection index of a
	 * {@link CCombo}, {@link Combo}, {@link List} or {@link Table}.
	 *
	 * @return a value property for the single selection index of a SWT Combo.
	 */
	public static <S extends Control> IWidgetValueProperty<S, Integer> singleSelectionIndex() {
		return new WidgetSingleSelectionIndexProperty<>();
	}

	/**
	 * Returns a value property for observing the size of a {@link Control}.
	 *
	 * @return a value property for observing the size of a {@link Control}.
	 */
	public static <S extends Control> IWidgetValueProperty<S, Point> size() {
		return new ControlSizeProperty<>();
	}

	/**
	 * Returns a value property for observing the text of a {@link Button},
	 * {@link CCombo}, {@link CLabel}, {@link Combo}, {@link Item},
	 * {@link Label}, {@link Link}, {@link Shell}, {@link Group},
	 * {@link StyledText} or {@link Text}.
	 *
	 * @return a value property for observing the text of a {@link Button},
	 *         {@link CCombo}, {@link CLabel}, {@link Combo}, {@link Group},
	 *         {@link Item}, {@link Label}, {@link Link}, {@link Shell}, link
	 *         StyledText} or {@link Text}.
	 */
	public static <S extends Widget> IWidgetValueProperty<S, String> text() {
		return new WidgetTextProperty<>();
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
	public static <S extends Widget> IWidgetValueProperty<S, String> text(final int event) {
		return text(new int[] { event });
	}

	/**
	 * Returns a value property for observing the text of a {@link StyledText}
	 * or {@link Text}.
	 *
	 * @param events
	 *            varags of SWT event types to register for change events. May
	 *            include {@link SWT#None}, {@link SWT#Modify},
	 *            {@link SWT#FocusOut} or {@link SWT#DefaultSelection}.
	 *
	 * @return a value property for observing the text of a {@link StyledText}
	 *         or {@link Text}.
	 */
	public static <S extends Widget> IWidgetValueProperty<S, String> text(int... events) {
		return new WidgetTextWithEventsProperty<>(events.clone());
	}

	/**
	 * Returns a value property for observing the tooltip text of a
	 * {@link CTabItem}, {@link Control}, {@link TabItem}, {@link TableColumn},
	 * {@link ToolItem}, {@link TrayItem} or {@link TreeColumn}.
	 *
	 * @return a value property for observing the tooltip text of a
	 *         {@link CTabItem}, {@link Control}, {@link TabItem},
	 *         {@link TableColumn}, {@link ToolItem}, {@link TrayItem} or
	 *         {@link TreeColumn}.
	 */
	public static <S extends Widget> IWidgetValueProperty<S, String> tooltipText() {
		return new WidgetTooltipTextProperty<>();
	}

	/**
	 * Returns a value property for observing the visibility state of a
	 * {@link Control}.
	 *
	 * @return a value property for observing the visibility state of a
	 *         {@link Control}.
	 */
	public static <S extends Control> IWidgetValueProperty<S, Boolean> visible() {
		return new ControlVisibleProperty<>();
	}
}
