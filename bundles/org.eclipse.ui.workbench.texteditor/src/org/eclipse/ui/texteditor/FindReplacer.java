package org.eclipse.ui.texteditor;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.PatternSyntaxException;

import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.text.IFindReplaceTarget;
import org.eclipse.jface.text.IFindReplaceTargetExtension;
import org.eclipse.jface.text.IFindReplaceTargetExtension3;
import org.eclipse.jface.text.IFindReplaceTargetExtension4;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;

/**
 * @since 3.18
 */
public class FindReplacer {
	IFindReplaceTarget target;
	Shell activeShell;

	private boolean forwardSearch = true;
	private boolean caseSensitive = true;
	private boolean wholeWord = false;
	private boolean regExSearch = false;
	private boolean wrapSearch = true;
	private boolean isTargetEditable = true;

	public void setTargetEditable(boolean isTargetEditable) {
		this.isTargetEditable = isTargetEditable;
	}

	public boolean isTargetEditable() {
		return isTargetEditable();
	}

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
	public FindReplacer(Shell shell, IFindReplaceTarget target) {
		activeShell = shell;
		this.target = target;
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
		if (target instanceof IFindReplaceTargetExtension3)
			return ((IFindReplaceTargetExtension3) target).findAndSelect(offset, findString, forwardSearch,
					caseSensitive, wholeWord, regExSearch);
		return target.findAndSelect(offset, findString, forwardSearch, caseSensitive, wholeWord);
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
	public int performSelectNext(String findString) {
		int newSearchIndex = computeNewSearchIndex();
		int foundIndex = findAndSelect(newSearchIndex, findString);

		if (wrapSearch && foundIndex == -1) {
			findAndSelect(foundIndex, findString);
		}
		return foundIndex;
	}

	public int selectAll(String findString) {

		int replaceCount = 0;
		int position = 0;

		List<Region> selectedRegions = new ArrayList<>();
		int index = 0;
		do {
			index = findAndSelect(position, findString);
			if (index != -1) { // substring not contained from current position
				Point selection = target.getSelection();
				selectedRegions.add(new Region(selection.x, selection.y));
				replaceCount++;
				position = selection.x + selection.y;
			}
		} while (index != -1);
		if (target instanceof IFindReplaceTargetExtension4) {
			((IFindReplaceTargetExtension4) target).setSelection(selectedRegions.toArray(IRegion[]::new));
		}

		return replaceCount;
	}

	public int performSelectAll(String findString) {
		class SelectAllRunnable implements Runnable {
			public int numberOfOccurrences;

			@Override
			public void run() {
				numberOfOccurrences = selectAll(findString);
			}
		}

		SelectAllRunnable runnable = new SelectAllRunnable();
		BusyIndicator.showWhile(activeShell.getDisplay(), runnable);
		return runnable.numberOfOccurrences;
	}

	private int computeNewSearchIndex() {
		Point IncrementalBaseLocation = target.getSelection();
		int newSearchIndex = IncrementalBaseLocation.x;
		if (!forwardSearch) { // MW -> @HeikoKlare. I'm not sure what's going on here. Also, I don't know why
								// FindReplaceDialog checks for "fNeedsInitialFindBeforeReplace"
			newSearchIndex -= 1;
		} else {
			newSearchIndex += IncrementalBaseLocation.y;
		}
		return newSearchIndex;
	}

	public void performReplaceAll(String findString, String replaceString) {
		replaceAll(findString, replaceString);
	}

	/**
	 * Validates the state of the find/replace target.
	 *
	 * @return <code>true</code> if target can be changed, <code>false</code>
	 *         otherwise
	 * @since 2.1
	 */
	private boolean validateTargetState() {

		if (target instanceof IFindReplaceTargetExtension2) {
			IFindReplaceTargetExtension2 extension = (IFindReplaceTargetExtension2) target;
			if (!extension.validateTargetState()) {
//				statusError(EditorMessages.FindReplaceDialog_read_only);
				return false;
			}
		}
		return isEditable();
	}

	/**
	 * Returns whether the target is editable.
	 *
	 * @return <code>true</code> if target is editable
	 */
	private boolean isEditable() {
		boolean isEditable = (target == null ? false : target.isEditable());
		return isTargetEditable && isEditable;
	}

	/**
	 * Replaces all occurrences of the user's findString with the replace string.
	 * Returns the number of replacements that occur.
	 *
	 * @param findString    the string to search for
	 * @param replaceString the replacement string
	 * @param caseSensitive should the search be case sensitive
	 * @param wholeWord     does the search string represent a complete word
	 * @param regExSearch   if <code>true</code> findString represents a regular
	 *                      expression
	 * @return the number of occurrences
	 *
	 * @since 3.0
	 */
	private int replaceAll(String findString, String replaceString) {
		int replaceCount = 0;
		int findReplacePosition = 0;

		findReplacePosition = 0;

		if (!validateTargetState())
			return replaceCount;

		if (target instanceof IFindReplaceTargetExtension)
			((IFindReplaceTargetExtension) target).setReplaceAllMode(true);

		try {
			int index = 0;
			while (index != -1) {
				index = findAndSelect(findReplacePosition, findString);
				if (index != -1) { // substring not contained from current position
					Point selection = replaceSelection(replaceString);
					replaceCount++;
					findReplacePosition = selection.x + selection.y;
				}
			}
		} finally {
			if (target instanceof IFindReplaceTargetExtension)
				((IFindReplaceTargetExtension) target).setReplaceAllMode(false);
		}

		return replaceCount;
	}

	/**
	 * Replaces the selection with <code>replaceString</code>. If
	 * <code>regExReplace</code> is <code>true</code>, <code>replaceString</code> is
	 * a regex replace pattern which will get expanded if the underlying target
	 * supports it. Returns the region of the inserted text; note that the returned
	 * selection covers the expanded pattern in case of regex replace.
	 *
	 * @param replaceString the replace string (or a regex pattern)
	 * @param regExReplace  <code>true</code> if <code>replaceString</code> is a
	 *                      pattern
	 * @return the selection after replacing, i.e. the inserted text
	 * @since 3.0
	 */
	Point replaceSelection(String replaceString) {
		if (target instanceof IFindReplaceTargetExtension3)
			((IFindReplaceTargetExtension3) target).replaceSelection(replaceString, isRegExSearch());
		else
			target.replaceSelection(replaceString);

		return target.getSelection();
	}

	/**
	 * Replaces the current selection of the target with the user's replace string.
	 *
	 * @return <code>true</code> if the operation was successful
	 */
	private boolean performReplaceSelection(String replaceString) {
		if (!validateTargetState())
			return false;

		if (replaceString == null)
			replaceString = ""; //$NON-NLS-1$

		boolean replaced;
		try {
			replaceSelection(replaceString);
			replaced = true;
		} catch (PatternSyntaxException ex) {
			replaced = false;
		} catch (IllegalStateException ex) {
			replaced = false;
		}

		return replaced;
	}

	// TODO: "perform" is a horrible prefix for the method names, I need to change
	// them ASAP
	public void performReplaceNext(String findString, String replaceString) {
		// TODO Auto-generated method stub
		performSelectNext(findString);
		performReplaceSelection(replaceString);

	}
}
