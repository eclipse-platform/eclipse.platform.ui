/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matt Carter - bug 170668
 *     Brad Reynolds - bug 170848
 *******************************************************************************/
package org.eclipse.jface.databinding.swt;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.jface.internal.databinding.internal.swt.ButtonObservableValue;
import org.eclipse.jface.internal.databinding.internal.swt.CComboObservableList;
import org.eclipse.jface.internal.databinding.internal.swt.CComboObservableValue;
import org.eclipse.jface.internal.databinding.internal.swt.CLabelObservableValue;
import org.eclipse.jface.internal.databinding.internal.swt.ComboObservableList;
import org.eclipse.jface.internal.databinding.internal.swt.ComboObservableValue;
import org.eclipse.jface.internal.databinding.internal.swt.ControlObservableValue;
import org.eclipse.jface.internal.databinding.internal.swt.LabelObservableValue;
import org.eclipse.jface.internal.databinding.internal.swt.ListObservableList;
import org.eclipse.jface.internal.databinding.internal.swt.ListObservableValue;
import org.eclipse.jface.internal.databinding.internal.swt.SWTProperties;
import org.eclipse.jface.internal.databinding.internal.swt.SpinnerObservableValue;
import org.eclipse.jface.internal.databinding.internal.swt.TableObservableValue;
import org.eclipse.jface.internal.databinding.internal.swt.TextObservableValue;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;

/**
 * A factory for creating observables for SWT widgets
 * 
 * @since 1.1
 * 
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
	 * @param control
	 * @return an observable value tracking the enabled state of the given
	 *         control
	 */
	public static ISWTObservableValue observeEnabled(Control control) {
		return new ControlObservableValue(control, SWTProperties.ENABLED);
	}

	/**
	 * @param control
	 * @return an observable value tracking the visible state of the given
	 *         control
	 */
	public static ISWTObservableValue observeVisible(Control control) {
		return new ControlObservableValue(control, SWTProperties.VISIBLE);
	}

	/**
	 * @param control
	 * @return an observable value tracking the tooltip text of the given
	 *         control
	 */
	public static ISWTObservableValue observeTooltipText(Control control) {
		return new ControlObservableValue(control, SWTProperties.TOOLTIP_TEXT);
	}

	/**
	 * @param spinner
	 * @return an observable value tracking the selection of the given spinner
	 */
	public static ISWTObservableValue observeSelection(Spinner spinner) {
		return new SpinnerObservableValue(spinner, SWTProperties.SELECTION);
	}

	/**
	 * @param spinner
	 * @return an observable value tracking the min of the given spinner
	 */
	public static ISWTObservableValue observeMin(Spinner spinner) {
		return new SpinnerObservableValue(spinner, SWTProperties.MIN);
	}

	/**
	 * @param spinner
	 * @return an observable value tracking the max of the given spinner
	 */
	public static ISWTObservableValue observeMax(Spinner spinner) {
		return new SpinnerObservableValue(spinner, SWTProperties.MAX);
	}

	/**
	 * @param text
	 * @param event
	 *            the SWT event type to use when adding the change listener (one
	 *            of {@link SWT#Modify} or {@link SWT#FocusOut}
	 * @return an observable value tracking the current text of the given text
	 *         control
	 */
	public static ISWTObservableValue observeText(Text text, int event) {
		return new TextObservableValue(text, event);
	}

	/**
	 * @param label
	 * @return an observable value tracking the text of the given label
	 */
	public static ISWTObservableValue observeText(Label label) {
		return new LabelObservableValue(label);
	}

	/**
	 * @param cLabel
	 * @return an observable value tracking the text of the given CLabel
	 */
	public static ISWTObservableValue observeText(CLabel cLabel) {
		return new CLabelObservableValue(cLabel);
	}

	/**
	 * @param button
	 * @return an observable value tracking the selection state of the given
	 *         button
	 */
	public static ISWTObservableValue observeSelection(Button button) {
		return new ButtonObservableValue(button);
	}

	/**
	 * @param combo
	 * @return an observable value tracking the text of the given combo
	 */
	public static ISWTObservableValue observeText(Combo combo) {
		return new ComboObservableValue(combo, SWTProperties.TEXT);
	}

	/**
	 * @param combo
	 * @return an observable value tracking the selection of the given combo
	 */
	public static ISWTObservableValue observeSelection(Combo combo) {
		return new ComboObservableValue(combo, SWTProperties.SELECTION);
	}

	/**
	 * @param combo
	 * @return an observable list tracking the items of the given combo
	 */
	public static IObservableList observeItems(Combo combo) {
		return new ComboObservableList(combo);
	}

	/**
	 * @param combo
	 * @return an observable value tracking the text of the given combo
	 */
	public static ISWTObservableValue observeText(CCombo combo) {
		return new CComboObservableValue(combo, SWTProperties.TEXT);
	}

	/**
	 * @param combo
	 * @return an observable value tracking the selection state of the given
	 *         combo
	 */
	public static ISWTObservableValue observeSelection(CCombo combo) {
		return new CComboObservableValue(combo, SWTProperties.SELECTION);
	}

	/**
	 * @param combo
	 * @return an observable list tracking the items of the given combo
	 */
	public static IObservableList observeItems(CCombo combo) {
		return new CComboObservableList(combo);
	}

	/**
	 * @param list
	 * @return an observable value tracking the selection of the given list
	 */
	public static ISWTObservableValue observeSelection(List list) {
		return new ListObservableValue(list);
	}

	/**
	 * @param list
	 * @return an observable list tracking the items of the given list
	 */
	public static IObservableList observeItems(List list) {
		return new ListObservableList(list);
	}

	/**
	 * @param table
	 * @return an observable value tracking the (single) selection of the given
	 *         table
	 */
	public static ISWTObservableValue observeSingleSelectionIndex(Table table) {
		return new TableObservableValue(table, SWTProperties.SELECTION);
	}

	/**
	 * @param control
	 * @return an observable value tracking the foreground color of the given
	 *         control
	 */
	public static ISWTObservableValue observeForeground(Control control) {
		return new ControlObservableValue(control, SWTProperties.FOREGROUND);
	}

	/**
	 * @param control
	 * @return an observable value tracking the background color of the given
	 *         control
	 */
	public static ISWTObservableValue observeBackground(Control control) {
		return new ControlObservableValue(control, SWTProperties.BACKGROUND);
	}

	/**
	 * @param control
	 * @return an observable value tracking the font of the given control
	 */
	public static ISWTObservableValue observeFont(Control control) {
		return new ControlObservableValue(control, SWTProperties.FONT);
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

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#hashCode()
		 */
		public int hashCode() {
			return (display == null) ? 0 : display.hashCode();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
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
