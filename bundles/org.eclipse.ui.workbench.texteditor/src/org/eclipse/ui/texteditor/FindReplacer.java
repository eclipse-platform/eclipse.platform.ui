package org.eclipse.ui.texteditor;

import org.eclipse.swt.graphics.Point;

import org.eclipse.jface.text.IFindReplaceTarget;
import org.eclipse.jface.text.IFindReplaceTargetExtension3;

/**
 * @since 3.18
 */
public class FindReplacer {
	IFindReplaceTarget fTarget;

	private boolean forwardSearch = true;
	private boolean caseSensitive = true;
	private boolean wholeWord = false;
	private boolean regExSearch = false;
	private boolean wrapSearch = true;

	public boolean isCaseSensitive() {
		return caseSensitive;
	}

	public void setCaseSensitive(boolean caseSensitive) {
		this.caseSensitive = caseSensitive;
	}

	public boolean isWholeWord() {
		return wholeWord;
	}

	public void setWholeWord(boolean wholeWord) {
		this.wholeWord = wholeWord;
	}

	public boolean isRegExSearch() {
		return regExSearch;
	}

	public void setRegExSearch(boolean regExSearch) {
		this.regExSearch = regExSearch;
	}

	public void setForwardSearch(boolean forwardSearch) {
		this.forwardSearch = forwardSearch;
	}

	public boolean isForwardSearch() {
		return forwardSearch;
	}

	public void setWrapSearch(boolean wrapSearch) {
		this.wrapSearch = wrapSearch;
	}

	public boolean isWrapSearch() {
		return wrapSearch;
	}

	/**
	 *
	 * @param target The target for the search and replacement
	 */
	public FindReplacer(IFindReplaceTarget target) {
		fTarget = target;
	}

	/**
	 * Searches for a string starting at the given offset and using the specified
	 * search directives. If a string has been found it is selected and its start
	 * offset is returned.
	 *
	 * @param offset        the offset at which searching starts
	 * @param findString    the string which should be found
	 * @param forwardSearch the direction of the search
	 * @param caseSensitive <code>true</code> performs a case sensitive search,
	 *                      <code>false</code> an insensitive search
	 * @param wholeWord     if <code>true</code> only occurrences are reported in
	 *                      which the findString stands as a word by itself
	 * @param regExSearch   if <code>true</code> findString represents a regular
	 *                      expression
	 * @return the position of the specified string, or -1 if the string has not
	 *         been found
	 * @since 3.0
	 */
	public int findAndSelect(int offset, String findString) {
		if (fTarget instanceof IFindReplaceTargetExtension3)
			return ((IFindReplaceTargetExtension3) fTarget).findAndSelect(offset, findString, forwardSearch,
					caseSensitive, wholeWord, regExSearch);
		return fTarget.findAndSelect(offset, findString, forwardSearch, caseSensitive, wholeWord);
	}

	/**
	 * Perform "find" from the last position that was found.
	 *
	 * @param findString    the string which should be found
	 * @param forwardSearch the direction of the search
	 * @param caseSensitive <code>true</code> performs a case sensitive search,
	 *                      <code>false</code> an insensitive search
	 * @param wholeWord     if <code>true</code> only occurrences are reported in
	 *                      which the findString stands as a word by itself
	 * @param regExSearch   if <code>true</code> findString represents a regular
	 *                      expression
	 * @return the position of the specified string, or -1 if the string has not
	 *         been found
	 * @since 3.0
	 */
	public int findAndSelectNext(String findString) {
		int newSearchIndex = computeNewSearchIndex();
		int foundIndex = findAndSelect(newSearchIndex, findString);

		if (wrapSearch && foundIndex == -1) {
			findAndSelect(foundIndex, findString);
		}
		return foundIndex;
	}

	private int computeNewSearchIndex() {
		Point IncrementalBaseLocation = fTarget.getSelection();
		int newSearchIndex = IncrementalBaseLocation.x;
		if (!forwardSearch) { // MW -> @HeikoKlare. I'm not sure what's going on here. Also, I don't know why
								// FindReplaceDialog checks for "fNeedsInitialFindBeforeReplace"
			newSearchIndex -= 1;
		} else {
			newSearchIndex += IncrementalBaseLocation.y;
		}
		return newSearchIndex;
	}
}
