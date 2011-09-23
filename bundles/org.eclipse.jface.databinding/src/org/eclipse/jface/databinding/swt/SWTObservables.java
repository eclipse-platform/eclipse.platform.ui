/*******************************************************************************
 * Copyright (c) 2005, 2009, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matt Carter - bug 170668
 *     Brad Reynolds - bug 170848
 *     Matthew Hall - bugs 180746, 207844, 245647, 248621, 232917, 194734,
 *                    195222, 256543, 213893, 262320, 264286, 266563, 306203
 *     Michael Krauter - bug 180223
 *     Boris Bokowski - bug 245647
 *     Tom Schindl - bug 246462
 *******************************************************************************/
package org.eclipse.jface.databinding.swt;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.databinding.observable.Observables;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IVetoableValue;
import org.eclipse.core.databinding.observable.value.ValueChangingEvent;
import org.eclipse.jface.internal.databinding.swt.SWTDelayedObservableValueDecorator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Widget;

/**
 * A factory for creating observables for SWT widgets
 * 
 * @since 1.1
 */
public class SWTObservables {

	private static java.util.List realms = new ArrayList();

	/**
	 * Returns the realm representing the UI thread for the given display.
	 * 
	 * @param display
	 * @return the realm representing the UI thread for the given display
	 */
	public static Realm getRealm(final Display display) {
		synchronized (realms) {
			for (Iterator it = realms.iterator(); it.hasNext();) {
				DisplayRealm displayRealm = (DisplayRealm) it.next();
				if (displayRealm.display == display) {
					return displayRealm;
				}
			}
			DisplayRealm result = new DisplayRealm(display);
			realms.add(result);
			return result;
		}
	}

	/**
	 * Returns an observable which delays notification of value change events
	 * from <code>observable</code> until <code>delay</code> milliseconds have
	 * elapsed since the last change event, or until a FocusOut event is
	 * received from the underlying widget (whichever happens first). This
	 * observable helps to boost performance in situations where an observable
	 * has computationally expensive listeners (e.g. changing filters in a
	 * viewer) or many dependencies (master fields with multiple detail fields).
	 * A common use of this observable is to delay validation of user input
	 * until the user stops typing in a UI field.
	 * <p>
	 * To notify about pending changes, the returned observable fires a stale
	 * event when the wrapped observable value fires a change event, and remains
	 * stale until the delay has elapsed and the value change is fired. A call
	 * to {@link IObservableValue#getValue() getValue()} while a value change is
	 * pending will fire the value change immediately, short-circuiting the
	 * delay.
	 * <p>
	 * Note that this observable will not forward {@link ValueChangingEvent}
	 * events from a wrapped {@link IVetoableValue}.
	 * 
	 * @param delay
	 *            the delay in milliseconds
	 * @param observable
	 *            the observable being delayed
	 * @return an observable which delays notification of value change events
	 *         from <code>observable</code> until <code>delay</code>
	 *         milliseconds have elapsed since the last change event.
	 * 
	 * @since 1.2
	 */
	public static ISWTObservableValue observeDelayedValue(int delay,
			ISWTObservableValue observable) {
		return new SWTDelayedObservableValueDecorator(
				Observables.observeDelayedValue(delay, observable),
				observable.getWidget());
	}

	/**
	 * Returns an observable value tracking the enabled state of the given
	 * widget. The supported types are:
	 * <ul>
	 * <li>org.eclipse.swt.widgets.Control</li>
	 * <li>org.eclipse.swt.widgets.Menu</li>
	 * <li>org.eclipse.swt.widgets.MenuItem</li>
	 * <li>org.eclipse.swt.widgets.ScrollBar</li>
	 * <li>org.eclipse.swt.widgets.ToolItem</li>
	 * </ul>
	 * 
	 * @param widget
	 * @return an observable value tracking the enabled state of the given
	 *         widget.
	 * @since 1.5
	 */
	public static ISWTObservableValue observeEnabled(Widget widget) {
		return WidgetProperties.enabled().observe(widget);
	}

	/**
	 * Returns an observable value tracking the enabled state of the given
	 * control
	 * 
	 * @param control
	 *            the control to observe
	 * @return an observable value tracking the enabled state of the given
	 *         control
	 */
	public static ISWTObservableValue observeEnabled(Control control) {
		return observeEnabled((Widget) control);
	}

	/**
	 * Returns an observable value tracking the visible state of the given
	 * control
	 * 
	 * @param control
	 *            the control to observe
	 * @return an observable value tracking the visible state of the given
	 *         control
	 */
	public static ISWTObservableValue observeVisible(Control control) {
		return WidgetProperties.visible().observe(control);
	}

	/**
	 * Returns an observable tracking the tooltip text of the given item. The
	 * supported types are:
	 * <ul>
	 * <li>org.eclipse.swt.widgets.Control</li>
	 * <li>org.eclipse.swt.custom.CTabItem</li>
	 * <li>org.eclipse.swt.widgets.TabItem</li>
	 * <li>org.eclipse.swt.widgets.TableColumn</li>
	 * <li>org.eclipse.swt.widgets.ToolItem</li>
	 * <li>org.eclipse.swt.widgets.TrayItem</li>
	 * <li>org.eclipse.swt.widgets.TreeColumn</li>
	 * </ul>
	 * 
	 * @param widget
	 * @return an observable value tracking the tooltip text of the given item
	 * 
	 * @since 1.3
	 */
	public static ISWTObservableValue observeTooltipText(Widget widget) {
		return WidgetProperties.tooltipText().observe(widget);
	}

	/**
	 * Returns an observable value tracking the tooltip text of the given
	 * control
	 * 
	 * @param control
	 *            the control to observe
	 * @return an observable value tracking the tooltip text of the given
	 *         control
	 */
	public static ISWTObservableValue observeTooltipText(Control control) {
		return observeTooltipText((Widget) control);
	}

	/**
	 * Returns an observable observing the selection attribute of the provided
	 * <code>control</code>. The supported types are:
	 * <ul>
	 * <li>org.eclipse.swt.widgets.Spinner</li>
	 * <li>org.eclipse.swt.widgets.Button</li>
	 * <li>org.eclipse.swt.widgets.Combo</li>
	 * <li>org.eclipse.swt.custom.CCombo</li>
	 * <li>org.eclipse.swt.widgets.List</li>
	 * <li>org.eclipse.swt.widgets.MenuItem (since 1.5)</li>
	 * <li>org.eclipse.swt.widgets.Scale</li>
	 * </ul>
	 * 
	 * @param widget
	 * @return observable value
	 * @throws IllegalArgumentException
	 *             if <code>control</code> type is unsupported
	 * @since 1.5
	 */
	public static ISWTObservableValue observeSelection(Widget widget) {
		return WidgetProperties.selection().observe(widget);
	}

	/**
	 * Returns an observable observing the selection attribute of the provided
	 * <code>control</code>. The supported types are:
	 * <ul>
	 * <li>org.eclipse.swt.widgets.Button</li>
	 * <li>org.eclipse.swt.widgets.Combo</li>
	 * <li>org.eclipse.swt.custom.CCombo</li>
	 * <li>org.eclipse.swt.widgets.List</li>
	 * <li>org.eclipse.swt.widgets.Scale</li>
	 * <li>org.eclipse.swt.widgets.Slider (since 1.5)</li>
	 * <li>org.eclipse.swt.widgets.Spinner</li>
	 * </ul>
	 * 
	 * @param control
	 * @return observable value
	 * @throws IllegalArgumentException
	 *             if <code>control</code> type is unsupported
	 */
	public static ISWTObservableValue observeSelection(Control control) {
		return observeSelection((Widget) control);
	}

	/**
	 * Returns an observable observing the minimum attribute of the provided
	 * <code>control</code>. The supported types are:
	 * <ul>
	 * <li>org.eclipse.swt.widgets.Spinner</li>
	 * <li>org.eclipse.swt.widgets.Slider (since 1.5)</li>
	 * <li>org.eclipse.swt.widgets.Scale</li>
	 * </ul>
	 * 
	 * @param control
	 * @return observable value
	 * @throws IllegalArgumentException
	 *             if <code>control</code> type is unsupported
	 */
	public static ISWTObservableValue observeMin(Control control) {
		return WidgetProperties.minimum().observe(control);
	}

	/**
	 * Returns an observable observing the maximum attribute of the provided
	 * <code>control</code>. The supported types are:
	 * <ul>
	 * <li>org.eclipse.swt.widgets.Spinner</li>
	 * <li>org.eclipse.swt.widgets.Slider (since 1.5)</li>
	 * <li>org.eclipse.swt.widgets.Scale</li>
	 * </ul>
	 * 
	 * @param control
	 * @return observable value
	 * @throws IllegalArgumentException
	 *             if <code>control</code> type is unsupported
	 */
	public static ISWTObservableValue observeMax(Control control) {
		return WidgetProperties.maximum().observe(control);
	}

	/**
	 * Returns an observable observing the text attribute of the provided
	 * <code>control</code>. The supported types are:
	 * <ul>
	 * <li>org.eclipse.swt.widgets.Text</li>
	 * <li>org.eclipse.swt.custom.StyledText (as of 1.3)</li>
	 * </ul>
	 * 
	 * @param control
	 * @param events
	 *            array of SWT event types to register for change events. May
	 *            include {@link SWT#None}, {@link SWT#Modify},
	 *            {@link SWT#FocusOut} or {@link SWT#DefaultSelection}.
	 * @return observable value
	 * @throws IllegalArgumentException
	 *             if <code>control</code> type is unsupported
	 * @since 1.3
	 */
	public static ISWTObservableValue observeText(Control control, int[] events) {
		return WidgetProperties.text(events).observe(control);
	}

	/**
	 * Returns an observable observing the text attribute of the provided
	 * <code>control</code>. The supported types are:
	 * <ul>
	 * <li>org.eclipse.swt.widgets.Text</li>
	 * <li>org.eclipse.swt.custom.StyledText (as of 1.3)</li>
	 * </ul>
	 * 
	 * @param control
	 * @param event
	 *            event type to register for change events
	 * @return observable value
	 * @throws IllegalArgumentException
	 *             if <code>control</code> type is unsupported
	 */
	public static ISWTObservableValue observeText(Control control, int event) {
		return WidgetProperties.text(event).observe(control);
	}

	/**
	 * Returns an observable observing the text attribute of the provided
	 * <code>widget</code>. The supported types are:
	 * <ul>
	 * <li>org.eclipse.swt.widgets.Button (as of 1.3)</li>
	 * <li>org.eclipse.swt.custom.CCombo</li>
	 * <li>org.eclipse.swt.custom.CLabel</li>
	 * <li>org.eclipse.swt.widgets.Combo</li>
	 * <li>org.eclipse.swt.widgets.Item</li>
	 * <li>org.eclipse.swt.widgets.Label</li>
	 * <li>org.eclipse.swt.widgets.Link (as of 1.2)</li>
	 * <li>org.eclipse.swt.widgets.Shell</li>
	 * <li>org.eclipse.swt.widgets.StyledText (as of 1.3)</li>
	 * <li>org.eclipse.swt.widgets.Text (as of 1.3)</li>
	 * </ul>
	 * 
	 * @param widget
	 * @return observable value
	 * @throws IllegalArgumentException
	 *             if the type of <code>widget</code> is unsupported
	 * 
	 * @since 1.3
	 */
	public static ISWTObservableValue observeText(Widget widget) {
		return WidgetProperties.text().observe(widget);
	}

	/**
	 * Returns an observable observing the text attribute of the provided
	 * <code>control</code>. The supported types are:
	 * <ul>
	 * <li>org.eclipse.swt.widgets.Button (as of 1.3)</li>
	 * <li>org.eclipse.swt.custom.CCombo</li>
	 * <li>org.eclipse.swt.custom.CLabel</li>
	 * <li>org.eclipse.swt.widgets.Combo</li>
	 * <li>org.eclipse.swt.widgets.Label</li>
	 * <li>org.eclipse.swt.widgets.Link (as of 1.2)</li>
	 * <li>org.eclipse.swt.widgets.Shell</li>
	 * <li>org.eclipse.swt.custom.StyledText (as of 1.3)</li>
	 * <li>org.eclipse.swt.widgets.Text (as of 1.3)</li>
	 * </ul>
	 * 
	 * @param control
	 * @return observable value
	 * @throws IllegalArgumentException
	 *             if <code>control</code> type is unsupported
	 */
	public static ISWTObservableValue observeText(Control control) {
		return observeText((Widget) control);
	}

	/**
	 * Returns an observable observing the message attribute of the provided
	 * <code>widget</code>. the supported types are:
	 * <ul>
	 * <li>org.eclipse.swt.widgets.Text</li>
	 * <li>org.eclipse.swt.widgets.ToolTip</li>
	 * <ul>
	 * 
	 * @param widget
	 * @return an observable observing the message attribute of the provided
	 *         <code>widget</code>.
	 * @since 1.3
	 */
	public static ISWTObservableValue observeMessage(Widget widget) {
		return WidgetProperties.message().observe(widget);
	}

	/**
	 * Returns an observable observing the image attribute of the provided
	 * <code>widget</code>. The supported types are:
	 * <ul>
	 * <li>org.eclipse.swt.widgets.Button</li>
	 * <li>org.eclipse.swt.custom.CLabel</li>
	 * <li>org.eclipse.swt.widgets.Item</li>
	 * <li>org.eclipse.swt.widgets.Label</li>
	 * </ul>
	 * 
	 * @param widget
	 * @return observable value
	 * @throws IllegalArgumentException
	 *             if <code>widget</code> type is unsupported
	 * @since 1.3
	 */
	public static ISWTObservableValue observeImage(Widget widget) {
		return WidgetProperties.image().observe(widget);
	}

	/**
	 * Returns an observable observing the items attribute of the provided
	 * <code>control</code>. The supported types are:
	 * <ul>
	 * <li>org.eclipse.swt.widgets.Combo</li>
	 * <li>org.eclipse.swt.custom.CCombo</li>
	 * <li>org.eclipse.swt.widgets.List</li>
	 * </ul>
	 * 
	 * @param control
	 * @return observable list
	 * @throws IllegalArgumentException
	 *             if <code>control</code> type is unsupported
	 */
	public static IObservableList observeItems(Control control) {
		return WidgetProperties.items().observe(control);
	}

	/**
	 * Returns an observable observing the single selection index attribute of
	 * the provided <code>control</code>. The supported types are:
	 * <ul>
	 * <li>org.eclipse.swt.widgets.Table</li>
	 * <li>org.eclipse.swt.widgets.Combo</li>
	 * <li>org.eclipse.swt.custom.CCombo</li>
	 * <li>org.eclipse.swt.widgets.List</li>
	 * </ul>
	 * 
	 * @param control
	 * @return observable value
	 * @throws IllegalArgumentException
	 *             if <code>control</code> type is unsupported
	 */
	public static ISWTObservableValue observeSingleSelectionIndex(
			Control control) {
		return WidgetProperties.singleSelectionIndex().observe(control);
	}

	/**
	 * Returns an observable value tracking the foreground color of the given
	 * control
	 * 
	 * @param control
	 *            the control to observe
	 * @return an observable value tracking the foreground color of the given
	 *         control
	 */
	public static ISWTObservableValue observeForeground(Control control) {
		return WidgetProperties.foreground().observe(control);
	}

	/**
	 * Returns an observable value tracking the background color of the given
	 * control
	 * 
	 * @param control
	 *            the control to observe
	 * @return an observable value tracking the background color of the given
	 *         control
	 */
	public static ISWTObservableValue observeBackground(Control control) {
		return WidgetProperties.background().observe(control);
	}

	/**
	 * Returns an observable value tracking the font of the given control.
	 * 
	 * @param control
	 *            the control to observe
	 * @return an observable value tracking the font of the given control
	 */
	public static ISWTObservableValue observeFont(Control control) {
		return WidgetProperties.font().observe(control);
	}

	/**
	 * Returns an observable value tracking the size of the given control.
	 * 
	 * @param control
	 *            the control to observe
	 * @return an observable value tracking the size of the given control
	 * @since 1.3
	 */
	public static ISWTObservableValue observeSize(Control control) {
		return WidgetProperties.size().observe(control);
	}

	/**
	 * Returns an observable value tracking the location of the given control.
	 * 
	 * @param control
	 *            the control to observe
	 * @return an observable value tracking the location of the given control
	 * @since 1.3
	 */
	public static ISWTObservableValue observeLocation(Control control) {
		return WidgetProperties.location().observe(control);
	}

	/**
	 * Returns an observable value tracking the focus of the given control.
	 * 
	 * @param control
	 *            the control to observe
	 * @return an observable value tracking the focus of the given control
	 * @since 1.3
	 */
	public static ISWTObservableValue observeFocus(Control control) {
		return WidgetProperties.focused().observe(control);
	}

	/**
	 * Returns an observable value tracking the bounds of the given control.
	 * 
	 * @param control
	 *            the control to observe
	 * @return an observable value tracking the bounds of the given control
	 * @since 1.3
	 */
	public static ISWTObservableValue observeBounds(Control control) {
		return WidgetProperties.bounds().observe(control);
	}

	/**
	 * Returns an observable observing the editable attribute of the provided
	 * <code>control</code>. The supported types are:
	 * <ul>
	 * <li>org.eclipse.swt.custom.CCombo (since 1.6)</li>
	 * <li>org.eclipse.swt.custom.StyledText (since 1.6)</li>
	 * <li>org.eclipse.swt.widgets.Text</li>
	 * </ul>
	 * 
	 * @param control
	 * @return observable value
	 * @throws IllegalArgumentException
	 *             if <code>control</code> type is unsupported
	 */
	public static ISWTObservableValue observeEditable(Control control) {
		return WidgetProperties.editable().observe(control);
	}

	private static class DisplayRealm extends Realm {
		private Display display;

		/**
		 * @param display
		 */
		private DisplayRealm(Display display) {
			this.display = display;
		}

		public boolean isCurrent() {
			return Display.getCurrent() == display;
		}

		public void asyncExec(final Runnable runnable) {
			Runnable safeRunnable = new Runnable() {
				public void run() {
					safeRun(runnable);
				}
			};
			if (!display.isDisposed()) {
				display.asyncExec(safeRunnable);
			}
		}

		public void timerExec(int milliseconds, final Runnable runnable) {
			if (!display.isDisposed()) {
				Runnable safeRunnable = new Runnable() {
					public void run() {
						safeRun(runnable);
					}
				};
				display.timerExec(milliseconds, safeRunnable);
			}
		}

		public int hashCode() {
			return (display == null) ? 0 : display.hashCode();
		}

		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			final DisplayRealm other = (DisplayRealm) obj;
			if (display == null) {
				if (other.display != null)
					return false;
			} else if (!display.equals(other.display))
				return false;
			return true;
		}
	}
}
