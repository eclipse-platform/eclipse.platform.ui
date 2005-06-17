/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.workbench.texteditor.quickdiff.tests;

import org.eclipse.text.tests.Accessor;

import org.eclipse.ui.internal.texteditor.quickdiff.compare.rangedifferencer.Levenstein;


/**
 * Accessor for package private methods in Levenstein.
 * 
 * @since 3.1
 */
public final class LevensteinTestHelper {

	private final Accessor fLevenstein;

	public LevensteinTestHelper(Levenstein levenstein) {
		fLevenstein= new Accessor(levenstein, levenstein.getClass());
	}
	public void initMatrix() {
		fLevenstein.invoke("initMatrix", null);
	}
	public void initMatrix(int rows, int columns) {
		fLevenstein.invoke("initMatrix", new Class[] {int.class, int.class}, new Object[] { new Integer(rows), new Integer(columns)});
	}
	public void initRows() {
		fLevenstein.invoke("initRows", null);
	}
	public void initRows(int columns) {
		fLevenstein.invoke("initRows", new Class[] {int.class}, new Integer[] { new Integer(columns)});
	}
	public void internalEditDistance(int rStart, int rEnd, int lStart, int lEnd) {
		Integer[] args= new Integer[] {new Integer(rStart), new Integer(rEnd), new Integer(lStart), new Integer(lEnd)};
		Class[] types= new Class[] {int.class, int.class, int.class, int.class};
		fLevenstein.invoke("internalEditDistance", types, args);
	}
	public void internalReverseEditDistance(int rStart, int rEnd, int lStart, int lEnd) {
		Integer[] args= new Integer[] {new Integer(rStart), new Integer(rEnd), new Integer(lStart), new Integer(lEnd)};
		Class[] types= new Class[] {int.class, int.class, int.class, int.class};
		fLevenstein.invoke("internalReverseEditDistance", types, args);
	}
	public int[] getPreviousRow() {
		return (int[])fLevenstein.get("fPreviousRow");
	}
	public void setOptimizedCellComputer() {
		Object value= fLevenstein.get("fOptimizedCC");
		fLevenstein.set("fCellComputer", value);
	}
}
