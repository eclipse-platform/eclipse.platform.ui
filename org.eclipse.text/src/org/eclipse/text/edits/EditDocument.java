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
package org.eclipse.text.edits;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.FindReplaceDocumentAdapter;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.IDocumentPartitioningListener;
import org.eclipse.jface.text.IPositionUpdater;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Position;

class EditDocument implements IDocument {

	private StringBuilder fBuffer;

	public EditDocument(String content) {
		fBuffer= new StringBuilder(content);
	}

	@Override
	public void addDocumentListener(IDocumentListener listener) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void addDocumentPartitioningListener(IDocumentPartitioningListener listener) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void addPosition(Position position) throws BadLocationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void addPosition(String category, Position position) throws BadLocationException, BadPositionCategoryException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void addPositionCategory(String category) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void addPositionUpdater(IPositionUpdater updater) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void addPrenotifiedDocumentListener(IDocumentListener documentAdapter) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int computeIndexInCategory(String category, int offset) throws BadLocationException, BadPositionCategoryException {
		throw new UnsupportedOperationException();
	}

	@Override
	public int computeNumberOfLines(String text) {
		throw new UnsupportedOperationException();
	}

	@Override
	public ITypedRegion[] computePartitioning(int offset, int length) throws BadLocationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean containsPosition(String category, int offset, int length) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean containsPositionCategory(String category) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String get() {
		return fBuffer.toString();
	}

	@Override
	public String get(int offset, int length) throws BadLocationException {
		return fBuffer.substring(offset, offset + length);
	}

	@Override
	public char getChar(int offset) throws BadLocationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getContentType(int offset) throws BadLocationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public IDocumentPartitioner getDocumentPartitioner() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String[] getLegalContentTypes() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String[] getLegalLineDelimiters() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getLength() {
		return fBuffer.length();
	}

	@Override
	public String getLineDelimiter(int line) throws BadLocationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public IRegion getLineInformation(int line) throws BadLocationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public IRegion getLineInformationOfOffset(int offset) throws BadLocationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getLineLength(int line) throws BadLocationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getLineOffset(int line) throws BadLocationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getLineOfOffset(int offset) throws BadLocationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getNumberOfLines() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getNumberOfLines(int offset, int length) throws BadLocationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public ITypedRegion getPartition(int offset) throws BadLocationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public String[] getPositionCategories() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Position[] getPositions(String category) throws BadPositionCategoryException {
		throw new UnsupportedOperationException();
	}

	@Override
	public IPositionUpdater[] getPositionUpdaters() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void insertPositionUpdater(IPositionUpdater updater, int index) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void removeDocumentListener(IDocumentListener listener) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void removeDocumentPartitioningListener(IDocumentPartitioningListener listener) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void removePosition(Position position) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void removePosition(String category, Position position) throws BadPositionCategoryException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void removePositionCategory(String category) throws BadPositionCategoryException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void removePositionUpdater(IPositionUpdater updater) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void removePrenotifiedDocumentListener(IDocumentListener documentAdapter) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void replace(int offset, int length, String text) throws BadLocationException {
		fBuffer.replace(offset, offset + length, text);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @deprecated As of 3.0 search is provided by {@link FindReplaceDocumentAdapter}
	 */
	@Deprecated
	@Override
	public int search(int startOffset, String findString, boolean forwardSearch, boolean caseSensitive, boolean wholeWord) throws BadLocationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void set(String text) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setDocumentPartitioner(IDocumentPartitioner partitioner) {
		throw new UnsupportedOperationException();
	}
}
