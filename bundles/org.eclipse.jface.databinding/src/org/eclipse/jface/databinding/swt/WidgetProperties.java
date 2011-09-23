/*******************************************************************************
 * Copyright (c) 2008, 2009, 2011 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 194734)
 *     Matthew Hall - bugs 256543, 213893, 262320, 262946, 264286, 266563,
 *                    169876, 306203
 ******************************************************************************/

package org.eclipse.jface.databinding.swt;

import org.eclipse.jface.internal.databinding.swt.ControlBackgroundProperty;
import org.eclipse.jface.internal.databinding.swt.ControlBoundsProperty;
import org.eclipse.jface.internal.databinding.swt.ControlFocusedProperty;
import org.eclipse.jface.internal.databinding.swt.ControlFontProperty;
import org.eclipse.jface.internal.databinding.swt.ControlForegroundProperty;
import org.eclipse.jface.internal.databinding.swt.ControlLocationProperty;
import org.eclipse.jface.internal.databinding.swt.ControlSizeProperty;
import org.eclipse.jface.internal.databinding.swt.ControlVisibleProperty;
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
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DateTime;
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
 * 
 * @since 1.3
 */
public class WidgetProperties {
	/**
	 * Returns a value property for observing the background color of a
	 * {@link Control}.
	 * 
	 * @return a value property for observing the background color of a
	 *         {@link Control}.
	 */
	public static IWidgetValueProperty background() {
		return new ControlBackgroundProperty();
	}

	/**
	 * Returns a value property for observing the bounds of a {@link Control}.
	 * 
	 * @return a value property for observing the bounds of a {@link Control}.
	 */
	public static IWidgetValueProperty bounds() {
		return new ControlBoundsProperty();
	}

	/**
	 * Returns a value property for observing the editable state of a
	 * {@link CCombo} (since 1.6), {@link StyledText} (since 1.6), or
	 * {@link Text}.
	 * 
	 * @return a value property for observing the editable state of a
	 *         {@link CCombo}, {@link StyledText}, or {@link Text}.
	 */
	public static IWidgetValueProperty editable() {
		return new WidgetEditableProperty();
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
	public static IWidgetValueProperty enabled() {
		return new WidgetEnabledProperty();
	}

	/**
	 * Returns a value property for observing the focus state of a
	 * {@link Control}.
	 * 
	 * @return a value property for observing the focus state of a
	 *         {@link Control}.
	 */
	public static IWidgetValueProperty focused() {
		return new ControlFocusedProperty();
	}

	/**
	 * Returns a value property for observing the font of a {@link Control}.
	 * 
	 * @return a value property for observing the font of a {@link Control}.
	 */
	public static IWidgetValueProperty font() {
		return new ControlFontProperty();
	}

	/**
	 * Returns a value property for observing the foreground color of a
	 * {@link Control}.
	 * 
	 * @return a value property for observing the foreground color of a
	 *         {@link Control}.
	 */
	public static IWidgetValueProperty foreground() {
		return new ControlForegroundProperty();
	}

	/**
	 * Returns a value property for observing the image of a {@link Button},
	 * {@link CLabel}, {@link Item} or {@link Label}.
	 * 
	 * @return a value property for observing the image of a {@link Button},
	 *         {@link CLabel}, {@link Item} or {@link Label}.
	 */
	public static IWidgetValueProperty image() {
		return new WidgetImageProperty();
	}

	/**
	 * Returns a list property for observing the items of a {@link CCombo},
	 * {@link Combo} or {@link List}.
	 * 
	 * @return a list property for observing the items of a {@link CCombo},
	 *         {@link Combo} or {@link List}.
	 */
	public static IWidgetListProperty items() {
		return new WidgetItemsProperty();
	}

	/**
	 * Returns a value property for observing the location of a {@link Control}.
	 * 
	 * @return a value property for observing the location of a {@link Control}.
	 */
	public static IWidgetValueProperty location() {
		return new ControlLocationProperty();
	}

	/**
	 * Returns a value property for observing the maximum value of a
	 * {@link Scale}, {@link Slider} (since 1.5) or {@link Spinner}.
	 * 
	 * @return a value property for observing the maximum value of a
	 *         {@link Scale}, {@link Slider} (since 1.5) or {@link Spinner}.
	 */
	public static IWidgetValueProperty maximum() {
		return new WidgetMaximumProperty();
	}

	/**
	 * Returns a value property for observing the message of a {@link Text} or
	 * {@link ToolTip}.
	 * 
	 * @return a value property for observing the message of a {@link Text} or
	 *         {@link ToolTip}.
	 */
	public static IWidgetValueProperty message() {
		return new WidgetMessageProperty();
	}

	/**
	 * Returns a value property for observing the minimum value of a
	 * {@link Scale}, {@link Slider} (since 1.5) or {@link Spinner}.
	 * 
	 * @return a value property for observing the minimum value of a
	 *         {@link Scale}, {@link Slider} (since 1.5) or {@link Spinner}.
	 */
	public static IWidgetValueProperty minimum() {
		return new WidgetMinimumProperty();
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
	public static IWidgetValueProperty selection() {
		return new WidgetSelectionProperty();
	}

	/**
	 * Returns a value property for observing the single selection index of a
	 * {@link CCombo}, {@link Combo}, {@link List} or {@link Table}.
	 * 
	 * @return a value property for the single selection index of a SWT Combo.
	 */
	public static IWidgetValueProperty singleSelectionIndex() {
		return new WidgetSingleSelectionIndexProperty();
	}

	/**
	 * Returns a value property for observing the size of a {@link Control}.
	 * 
	 * @return a value property for observing the size of a {@link Control}.
	 */
	public static IWidgetValueProperty size() {
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
	public static IWidgetValueProperty text() {
		return new WidgetTextProperty();
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
	public static IWidgetValueProperty text(final int event) {
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
	public static IWidgetValueProperty text(int[] events) {
		return new WidgetTextWithEventsProperty((int[]) events.clone());
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
	public static IWidgetValueProperty tooltipText() {
		return new WidgetTooltipTextProperty();
	}

	/**
	 * Returns a value property for observing the visibility state of a
	 * {@link Control}.
	 * 
	 * @return a value property for observing the visibility state of a
	 *         {@link Control}.
	 */
	public static IWidgetValueProperty visible() {
		return new ControlVisibleProperty();
	}
}
