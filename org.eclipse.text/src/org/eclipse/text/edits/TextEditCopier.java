/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.text.edits;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Assert;


/**
 * Copies a tree of text edits. A text edit copier keeps a map
 * between original and new text edits. It can be used to map
 * a copy back to its original edit.
 *
 * @since 3.0
 */
public final class TextEditCopier {

	private TextEdit fEdit;
	private Map<TextEdit, TextEdit> fCopies;

	/**
	 * Constructs a new <code>TextEditCopier</code> for the
	 * given edit. The actual copy is done by calling <code>
	 * perform</code>.
	 *
	 * @param edit the edit to copy
	 *
	 * @see #perform()
	 */
	public TextEditCopier(TextEdit edit) {
		super();
		Assert.isNotNull(edit);
		fEdit= edit;
		fCopies= new HashMap<>();
	}

	/**
	 * Performs the actual copying.
	 *
	 * @return the copy
	 */
	public TextEdit perform() {
		TextEdit result= doCopy(fEdit);
		if (result != null) {
			for (Iterator<TextEdit> iter= fCopies.keySet().iterator(); iter.hasNext();) {
				TextEdit edit= iter.next();
				edit.postProcessCopy(this);
			}
		}
		return result;
	}

	/**
	 * Returns the copy for the original text edit.
	 *
	 * @param original the original for which the copy
	 *  is requested
	 * @return the copy of the original edit or <code>null</code>
	 *  if the original isn't managed by this copier
	 */
	public TextEdit getCopy(TextEdit original) {
		Assert.isNotNull(original);
		return fCopies.get(original);
	}

	//---- helper methods --------------------------------------------

	private TextEdit doCopy(TextEdit edit) {
		TextEdit result= edit.doCopy();
		List<TextEdit> children= edit.internalGetChildren();
		if (children != null) {
			List<TextEdit> newChildren= new ArrayList<>(children.size());
			for (Iterator<TextEdit> iter= children.iterator(); iter.hasNext();) {
				TextEdit childCopy= doCopy(iter.next());
				childCopy.internalSetParent(result);
				newChildren.add(childCopy);
			}
			result.internalSetChildren(newChildren);
		}
		addCopy(edit, result);
		return result;
	}

	private void addCopy(TextEdit original, TextEdit copy) {
		fCopies.put(original, copy);
	}
}
