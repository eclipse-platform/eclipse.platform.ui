package org.eclipse.team.internal.ccvs.core.syncinfo;

import java.util.Arrays;

import org.eclipse.core.resources.IResource;

class ResourceSyncInfoMap {
	private static final ResourceSyncInfo[] EMPTY = new ResourceSyncInfo[0];
	private static final int INITIAL_SIZE = 8;
	private ResourceSyncInfo[] elements;
	private int elementsUsed;

	public ResourceSyncInfoMap() {
		elements = EMPTY;
		elementsUsed = 0;
	}

	public void put(ResourceSyncInfo info) {
		int element = Arrays.binarySearch(elements, info);
		if (element >= 0) {
			// found existing element, replace it
			elements[element] = info;
		} else {
			// insert new element
			elementsUsed += 1;
			// index of new element as returned by binary search
			element = -element - 1; 
			ResourceSyncInfo[] oldElements = elements;
			if (elementsUsed > elements.length) {
				// grow array
				int size = elements.length * 2;
				if (size < INITIAL_SIZE)
					size = INITIAL_SIZE;
				elements = new ResourceSyncInfo[size];
				System.arraycopy(oldElements, 0, elements, 0, element);
			}
			System.arraycopy(
				oldElements,
				element,
				elements,
				element + 1,
				elementsUsed - element - 1);
			elements[element] = info;
		}
	}

	public ResourceSyncInfo get(IResource resource) {
		int element = Arrays.binarySearch(elements, new ResourceSyncInfo(resource.getName(), "","","",null,null));
		if (element < 0)
			return null;
		// found element, return it
		return elements[element];
	}

	public void remove(ResourceSyncInfo info) {
		int element = Arrays.binarySearch(elements, info);
		if (element < 0)
			return;
		// found element, remove it
		elementsUsed -= 1;
		ResourceSyncInfo[] oldElements = elements;
		if (elementsUsed <= elements.length / 4
			&& elements.length >= INITIAL_SIZE * 2) {
			// shrink array
			int size = elements.length / 2;
			elements = new ResourceSyncInfo[size];
			System.arraycopy(oldElements, 0, elements, 0, element);
		} else {
			// since we're not creating a new array, we need to clear the last element
			elements[elementsUsed] = null;
		}
		System.arraycopy(
			oldElements,
			element + 1,
			elements,
			element,
			elementsUsed - element);
	}

	// XXX might not even need to copy the array since EclipseSynchronizer
	//     is the only one who uses this and could just as well access it
	//     directly.
	public ResourceSyncInfo[] toArray() {
		ResourceSyncInfo[] infos = new ResourceSyncInfo[elementsUsed];
		System.arraycopy(elements, 0, infos, 0, elementsUsed);
		return infos;
	}
}