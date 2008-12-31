/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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

import org.eclipse.ui.internal.texteditor.quickdiff.compare.rangedifferencer.Levenshtein;


/**
 * Accessor for package private methods in Levenshtein.
 *
 * @since 3.1
 */
public final class LevenshteinTestHelper {

	private final Accessor fLevenshtein;

	public LevenshteinTestHelper(Levenshtein levenshtein) {
		fLevenshtein= new Accessor(levenshtein, levenshtein.getClass());
	}
	public void initMatrix() {
		fLevenshtein.invoke("initMatrix", null);
	}
	public void initMatrix(int rows, int columns) {
		fLevenshtein.invoke("initMatrix", new Class[] {int.class, int.class}, new Object[] { new Integer(rows), new Integer(columns)});
	}
	public void initRows() {
		fLevenshtein.invoke("initRows", null);
	}
	public void initRows(int columns) {
		fLevenshtein.invoke("initRows", new Class[] {int.class}, new Integer[] { new Integer(columns)});
	}
	public void internalEditDistance(int rStart, int rEnd, int lStart, int lEnd) {
		Integer[] args= new Integer[] {new Integer(rStart), new Integer(rEnd), new Integer(lStart), new Integer(lEnd)};
		Class[] types= new Class[] {int.class, int.class, int.class, int.class};
		fLevenshtein.invoke("internalEditDistance", types, args);
	}
	public void internalReverseEditDistance(int rStart, int rEnd, int lStart, int lEnd) {
		Integer[] args= new Integer[] {new Integer(rStart), new Integer(rEnd), new Integer(lStart), new Integer(lEnd)};
		Class[] types= new Class[] {int.class, int.class, int.class, int.class};
		fLevenshtein.invoke("internalReverseEditDistance", types, args);
	}
	public int[] getPreviousRow() {
		return (int[])fLevenshtein.get("fPreviousRow");
	}
	public void setOptimizedCellComputer() {
		Object value= fLevenshtein.get("fOptimizedCC");
		fLevenshtein.set("fCellComputer", value);
	}
}
