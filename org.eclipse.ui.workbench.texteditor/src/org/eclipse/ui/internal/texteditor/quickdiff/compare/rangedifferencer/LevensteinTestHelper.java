/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.texteditor.quickdiff.compare.rangedifferencer;


/**
 * Public accessor for package private methods in Levenstein. Only used for testing,
 * do not use.
 * 
 * @since 3.1
 */
public final class LevensteinTestHelper {

	private final Levenstein fLevenstein;

	public LevensteinTestHelper(Levenstein levenstein) {
		fLevenstein= levenstein;
	}
	public void initMatrix() {
		fLevenstein.initMatrix();
	}
	public void initMatrix(int rows, int columns) {
		fLevenstein.initMatrix(rows, columns);
	}
	public void initRows() {
		fLevenstein.initRows();
	}
	public void initRows(int columns) {
		fLevenstein.initRows(columns);
	}
	public void internalEditDistance(int rStart, int rEnd, int lStart, int lEnd) {
		fLevenstein.internalEditDistance(rStart, rEnd, lStart, lEnd);
	}
	public void internalReverseEditDistance(int rStart, int rEnd, int lStart, int lEnd) {
		fLevenstein.internalReverseEditDistance(rStart, rEnd, lStart, lEnd);
	}
	public int[] getPreviousRow() {
		return fLevenstein.fPreviousRow;
	}
	public void setOptimizedCellComputer() {
		fLevenstein.fCellComputer= fLevenstein.fOptimizedCC;
	}
}
