/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.link;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.Assert;

import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;


/**
 * A group of positions in multiple documents that are simultaneously modified -
 * if one gets edited, all other positions in a group are edited the same way.
 * <p>
 * All linked positions in a group should have the same content.
 * Before 3.5.400, this was enforced. Now, if one position of a mixed group gets edited,
 * the content of all other positions is replaced by the edited position's content.
 * </p>
 * <p>
 * Normally, new positions are given a {@link LinkedPosition#getSequenceNumber() sequence number} which can be used by
 * clients, e.g. in the UI as tab stop weight. If {@link #NO_STOP} is used as weight, a position will not be visited.
 * If {@link #NO_STOP} is used for all positions, the first position in a document is taken as
 * the only stop as to comply with the behavior of the old linked position
 * infrastructure.
 * </p>
 * <p>
 * Clients may instantiate this class.
 * </p>
 *
 * @since 3.0
 * @noextend This class is not intended to be subclassed by clients.
 */
public class LinkedPositionGroup {

	/** {@link LinkedPosition#getSequenceNumber() Sequence number} constant declaring that a position should not be stopped by. */
	public static final int NO_STOP= -1;

	/* members */

	/** The linked positions of this group. */
	private final List<LinkedPosition> fPositions= new LinkedList<>();
	/** Whether we are sealed or not. */
	private boolean fIsSealed= false;
	/**
	 * <code>true</code> if there are custom iteration weights. For backward
	 * compatibility.
	 */
	private boolean fHasCustomIteration= false;

	/*
	 * iteration variables, set to communicate state between isLegalEvent and
	 * handleEvent
	 */
	/** The position including the most recent <code>DocumentEvent</code>. */
	private LinkedPosition fLastPosition;
	/** The region covered by <code>fLastPosition</code> before the document
	 * change.
	 */
	private IRegion fLastRegion;

	/**
	 * <code>true</code> iff not all positions contain the same content.
	 * In that case, the contents of the last edited position will replace the
	 * contents of all other linked positions.
	 */
	private boolean fMustEnforceEqualContents= false;

	/**
	 * Adds a position to this group. The document region defined by the
	 * position should contain the same content as all of the other positions
	 * already in this group.
	 * All positions added must be valid and disjoint; otherwise a
	 * <code>BadLocationException</code> is thrown.
	 * <p>
	 * Positions added using this method are owned by this group afterwards and
	 * may not be updated or modified thereafter.
	 * </p>
	 * <p>
	 * Once a group has been added to a <code>LinkedModeModel</code>, it
	 * becomes <em>sealed</em> and no positions may be added any more.
	 * </p>
	 *
	 * @param position the position to add
	 * @throws BadLocationException if the position is invalid or conflicts with
	 *         other positions in the group
	 * @throws IllegalStateException if the group has already been added to a
	 *         model
	 */
	public void addPosition(LinkedPosition position) throws BadLocationException {
		/*
		 * Enforces constraints and sets the custom iteration flag. If the
		 * position is already in this group, nothing happens.
		 */
		Assert.isNotNull(position);
		if (fIsSealed)
			throw new IllegalStateException("cannot add positions after the group is added to an model"); //$NON-NLS-1$

		if (!fPositions.contains(position)) {
			enforceDisjoint(position);
			checkContent(position);
			fPositions.add(position);
			fHasCustomIteration |= position.getSequenceNumber() != LinkedPositionGroup.NO_STOP;
		} else
			return; // nothing happens
	}

	/**
	 * Checks whether all positions contain the same string as the given position.
	 * If not, then {@link #fMustEnforceEqualContents} is set to <code>true</code>.
	 *
	 * @param position the position to check
	 * @throws BadLocationException if the position is invalid
	 */
	private void checkContent(LinkedPosition position) throws BadLocationException {
		if (!fPositions.isEmpty()) {
			LinkedPosition groupPosition= fPositions.get(0);
			String groupContent= groupPosition.getContent();
			String positionContent= position.getContent();
			if (!fMustEnforceEqualContents && !groupContent.equals(positionContent)) {
				fMustEnforceEqualContents= true;
			}
		}
	}

	/**
	 * Enforces the invariant that all positions must be disjoint.
	 *
	 * @param position the position to check
	 * @throws BadLocationException if the disjointness check fails
	 */
	private void enforceDisjoint(LinkedPosition position) throws BadLocationException {
		for (LinkedPosition p : fPositions) {
			if (p.overlapsWith(position))
				throw new BadLocationException();
		}
	}

	/**
	 * Enforces the disjointness for another group
	 *
	 * @param group the group to check
	 * @throws BadLocationException if the disjointness check fails
	 */
	void enforceDisjoint(LinkedPositionGroup group) throws BadLocationException {
		Assert.isNotNull(group);
		for (LinkedPosition p : group.fPositions) {
			enforceDisjoint(p);
		}
	}

	/**
	 * Checks whether <code>event</code> is a legal event for this group. An
	 * event is legal if it touches at most one position contained within this
	 * group.
	 *
	 * @param event the document event to check
	 * @return <code>true</code> if <code>event</code> is legal
	 */
	boolean isLegalEvent(DocumentEvent event) {
		fLastPosition= null;
		fLastRegion= null;

		for (LinkedPosition pos : fPositions) {
			if (overlapsOrTouches(pos, event)) {
				if (fLastPosition != null) {
					fLastPosition= null;
					fLastRegion= null;
					return false;
				}

				fLastPosition= pos;
				fLastRegion= new Region(pos.getOffset(), pos.getLength());
			}
		}

		return true;
	}

	/**
	 * Checks whether the given event touches the given position. To touch means
	 * to overlap or come up to the borders of the position.
	 *
	 * @param position the position
	 * @param event the event
	 * @return <code>true</code> if <code>position</code> and
	 *         <code>event</code> are not absolutely disjoint
	 * @since 3.1
	 */
	private boolean overlapsOrTouches(LinkedPosition position, DocumentEvent event) {
		return position.getDocument().equals(event.getDocument()) && position.getOffset() <= event.getOffset() + event.getLength() && position.getOffset() + position.getLength() >= event.getOffset();
	}

	/**
	 * Creates an edition of a document change that will forward any
	 * modification in one position to all linked siblings. The return value is
	 * a map from <code>IDocument</code> to <code>TextEdit</code>.
	 *
	 * @param event the document event to check
	 * @return a map of edits, grouped by edited document, or <code>null</code>
	 *         if there are no edits
	 */
	Map<IDocument, TextEdit> handleEvent(DocumentEvent event) {

		if (fLastPosition != null) {

			Map<IDocument, List<ReplaceEdit>> map= new HashMap<>();


			int relativeOffset= event.getOffset() - fLastRegion.getOffset();
			if (relativeOffset < 0) {
				relativeOffset= 0;
			}

			int eventEnd= event.getOffset() + event.getLength();
			int lastEnd= fLastRegion.getOffset() + fLastRegion.getLength();
			int length;
			if (eventEnd > lastEnd)
				length= lastEnd - relativeOffset - fLastRegion.getOffset();
			else
				length= eventEnd - relativeOffset - fLastRegion.getOffset();

			String text= event.getText();
			if (text == null)
				text= ""; //$NON-NLS-1$

			for (LinkedPosition p : fPositions) {
				if (p == fLastPosition || p.isDeleted())
					continue; // don't re-update the origin of the change

				List<ReplaceEdit> edits= map.get(p.getDocument());
				if (edits == null) {
					edits= new ArrayList<>();
					map.put(p.getDocument(), edits);
				}

				if (fMustEnforceEqualContents) {
					try {
						edits.add(new ReplaceEdit(p.getOffset(), p.getLength(), fLastPosition.getContent()));
					} catch (BadLocationException e) {
						throw new RuntimeException(e); // should not happen
					}
				} else {
					edits.add(new ReplaceEdit(p.getOffset() + relativeOffset, length, text));
				}
			}
			fMustEnforceEqualContents= false;

			try {
				Map<IDocument, TextEdit> result= new HashMap<>();
				for (Entry<IDocument, List<ReplaceEdit>> edits : map.entrySet()) {
					TextEdit edit= new MultiTextEdit(0, edits.getKey().getLength());
					edit.addChildren(edits.getValue().toArray(new TextEdit[edits.getValue().size()]));
					result.put(edits.getKey(), edit);
				}

				return result;
			} catch (MalformedTreeException x) {
				// may happen during undo, as LinkedModeModel does not know
				// that the changes technically originate from a parent environment
				// if this happens, post notification changes are not accepted anyway and
				// we can simply return null - any changes will be undone by the undo
				// manager
				return null;
			}

		}

		return null;
	}

	/**
	 * Sets the model of this group. Once a model has been set, no
	 * more positions can be added and the model cannot be changed.
	 */
	void seal() {
		Assert.isTrue(!fIsSealed);
		fIsSealed= true;

		if (!fHasCustomIteration && !fPositions.isEmpty()) {
			fPositions.get(0).setSequenceNumber(0);
		}
	}

	IDocument[] getDocuments() {
		IDocument[] docs= new IDocument[fPositions.size()];
		int i= 0;
		for (Iterator<LinkedPosition> it= fPositions.iterator(); it.hasNext(); i++) {
			LinkedPosition pos= it.next();
			docs[i]= pos.getDocument();
		}
		return docs;
	}

	void register(LinkedModeModel model) throws BadLocationException {
		for (LinkedPosition pos : fPositions) {
			model.register(pos);
		}
	}

	/**
	 * Returns the position in this group that encompasses all positions in
	 * <code>group</code>.
	 *
	 * @param group the group to be adopted
	 * @return a position in the receiver that contains all positions in <code>group</code>,
	 *         or <code>null</code> if none can be found
	 * @throws BadLocationException if more than one position are affected by
	 *         <code>group</code>
	 */
	LinkedPosition adopt(LinkedPositionGroup group) throws BadLocationException {
		LinkedPosition found= null;
		for (LinkedPosition pos : group.fPositions) {
			LinkedPosition localFound= null;
			for (LinkedPosition myPos : fPositions) {
				if (myPos.includes(pos)) {
					if (found == null)
						found= myPos;
					else if (found != myPos)
						throw new BadLocationException();
					if (localFound == null)
						localFound= myPos;
				}
			}

			if (localFound != found)
				throw new BadLocationException();
		}
		return found;
	}

	/**
	 * Finds the closest position to <code>toFind</code>.
	 *
	 * @param toFind the linked position for which to find the closest position
	 * @return the closest position to <code>toFind</code>.
	 */
	LinkedPosition getPosition(LinkedPosition toFind) {
		for (LinkedPosition p : fPositions) {
			if (p.includes(toFind))
				return p;
		}
		return null;
	}

	/**
	 * Returns <code>true</code> if <code>offset</code> is contained in any
	 * position in this group.
	 *
	 * @param offset the offset to check
	 * @return <code>true</code> if offset is contained by this group
	 */
	boolean contains(int offset) {
		for (LinkedPosition pos : fPositions) {
			if (pos.includes(offset)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns whether this group contains any positions.
	 *
	 * @return <code>true</code> if this group is empty, <code>false</code> otherwise
	 * @since 3.1
	 */
	public boolean isEmpty() {
		return fPositions.isEmpty();
	}

	/**
	 * Returns whether this group contains any positions.
	 *
	 * @return <code>true</code> if this group is empty, <code>false</code> otherwise
	 * @deprecated As of 3.1, replaced by {@link #isEmpty()}
	 */
	@Deprecated
	public boolean isEmtpy() {
		return isEmpty();
	}

	/**
	 * Returns the positions contained in the receiver as an array. The
	 * positions are the actual positions and must not be modified; the array
	 * is a copy of internal structures.
	 *
	 * @return the positions of this group in no particular order
	 */
	public LinkedPosition[] getPositions() {
		return fPositions.toArray(new LinkedPosition[0]);
	}

	/**
	 * Returns <code>true</code> if the receiver contains <code>position</code>.
	 *
	 * @param position the position to check
	 * @return <code>true</code> if the receiver contains <code>position</code>
	 */
	boolean contains(Position position) {
		for (LinkedPosition p : fPositions) {
			if (position.equals(p))
				return true;
		}
		return false;
	}
}
