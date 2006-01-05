/*******************************************************************************
 * Copyright (c) 2002, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ua.tests.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Tree;

/*
 * A utility for finding widgets inside dialogs.
 */
public class WidgetFinder {
		
	/*
	 * Finds all controls of the given type.
	 */
	public static Control[] findControls(Control c, Class type) {
		if (c != null) {
			if (c instanceof Composite) {
				List allChildren = new ArrayList();
				Control[] children = ((Composite)c).getChildren();
				for (int i=0;i<children.length;++i) {
					Control[] controls = findControls(children[i], type);
					if (controls != null) {
						allChildren.addAll(Arrays.asList(controls));
					}
				}
				
				boolean isAssignable = type.isAssignableFrom(c.getClass()); 
				Control[] array = new Control[allChildren.size() + (isAssignable ? 1 : 0)];
				allChildren.toArray(array);
				if (isAssignable) {
					array[array.length - 1] = c;
				}
				return array;
			}
			else if (type.isAssignableFrom(c.getClass())) {
				return new Control[] { c };
			}
		}
		return new Control[0];
	}
	
	/*
	 * Finds all the Trees under the given Control/Composite.
	 */
	public static Tree[] findTrees(Control c) {
		Control[] controls = findControls(c, Tree.class);
		Tree[] trees = new Tree[controls.length];
		System.arraycopy(controls, 0, trees, 0, controls.length);
		return trees;
	}

	/*
	 * Finds all the Labels under the given Control/Composite.
	 */
	public static Label[] findLabels(Control c) {
		Control[] controls = findControls(c, Label.class);
		Label[] labels = new Label[controls.length];
		System.arraycopy(controls, 0, labels, 0, controls.length);
		return labels;
	}
	
	/*
	 * Finds any widget that has a getText() method that matches the given text.
	 */
	public static boolean containsText(String text, Control c) {
		// don't take \r into account when comparing
		text = text.replaceAll("\r", "");
		
		Control[] controls = findControls(c, Control.class);
		for (int i=0;i<controls.length;++i) {
			try {
				Method m = controls[i].getClass().getMethod("getText", null);
				Object obj = m.invoke(controls[i], null);
				if (obj != null && obj instanceof String) {
					String widgetText = (String)obj;
					widgetText = widgetText.replaceAll("\r", "");
					if (text.equals(widgetText)) {
						return true;
					}
				}
			}
			catch (Exception e) {
				// the method didn't exist; skip this one
			}
		}
		return false;
	}
}
