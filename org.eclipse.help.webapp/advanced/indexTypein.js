/*******************************************************************************
 * Copyright (c) 2005, 2006 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *     IBM 2006, refactored index view into a single frame
 *******************************************************************************/

var typein;
var currentId;

function compare(keyword, pattern) {
	var kI = 0, pI = 0;
	var kCh, pCh;

	while (kI < keyword.length && pI < pattern.length) {
		kCh = keyword.charAt(kI).toLowerCase();
		pCh = pattern.charAt(pI).toLowerCase();
		if (kCh > pCh) {
			return 1;
		} else if (kCh < pCh) {
			return -1;
		}
		kI++;
		pI++;
	}
	if (keyword.length >= pattern.length) {
		return 0;
	} else {
		return -1;
	}
}

function searchPattern(pattern) {

	var from = 0;
	var to = ids.length;
	var i;
	var res;

	while (to > from) {
		i = Math.floor((to + from) / 2);
		res = compare(ids[i], pattern);
		if (res == 0) {
			while (i > 0) {
				res = compare(ids[--i], pattern);
				if (res != 0) {
					i++;
					break;
				}
			}
			return ids[i];
		} else if (res < 0) {
			from = i + 1;
		} else {
			to = i;
		}
	}

	return null;
}

function typeinKeyDownHandler(e) {
	var key = getKeycode(e);	

	if (key != 13 && key != 38 && key != 40)
		return true;

	if (isIE) {
		window.event.cancelBubble = true;
	} else {	    
		e.cancelBubble = true;
	}

	if (key == 13) { // enter
		// display topic corresponded to selected list item
		setTypeinValue(getSelection());
		doDisplay();
	} if (key == 38) { // up arrow
		selectNextUp();
	} else if (key == 40) { // down arrow
		selectNextDown();
	}

	return false;
}

/**
  * Select the corresponding item in the index list on typein value change.
  * Check is performed periodically in short interval.
  */
function intervalHandler() {
	if (typein.value != typein.previous) {
		// typein value has been changed
		typein.previous = typein.value;
		var id = searchPattern(typein.value);
		if (id && id != currentId) {
			// the value has became to fit to other item
			if (selectTopicById(id)) {
				currentId = id;
			}
		}
	}
}
