package org.eclipse.core.internal.indexing;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */

class SpaceMapPage extends ObjectStorePage {
	private static int[] SpaceClassSize = {
		7616, 6906, 6196, 5486, 4776, 
		4066, 3356, 2646, 1936, 1226, 
		516, 258, 129, 64, 32, 0};

	private static IPageFactory pageFactory = new IPageFactory() {
		public Page create(PageStore store, int pageNumber) {
			SpaceMapPage p = new SpaceMapPage();
			p.initialize(store, pageNumber);
			return p;
		}
	};

	/**
	 * Default constructor -- used only by the factory.
	 */
	protected SpaceMapPage() {
	}
	/**
	 * Acquires a new page from a page store.
	 */
	public static Page acquire(PageStore store) throws PageStoreException {
		return store.acquire(pageFactory);
	}
	/**
	 * Acquires an existing page from a page store.
	 */
	public static Page acquire(PageStore store, int pageNumber) throws PageStoreException {
		return store.acquire(pageFactory, pageNumber);
	}
	/**
	 * Searches a space map page in the page file for an object page
	 * that has at least "bytesNeeded" bytes free.  Returns 0 if there is no
	 * object page in this space map page that meets this criteria.  0 is not a valid
	 * object page number.  All page numbers that are 0 mod 8192 are space map pages.
	 */
	public int findObjectPageNumberForSize(int bytesNeeded) throws ObjectStoreException {
		for (int i = 1; i < Page.Size; i++) {		// begin at 1, 0 is the space map page
			int spaceClass = contents[i];
			int freeSpace = freeSpaceForClass(spaceClass);
			if (freeSpace >= bytesNeeded) return pageNumber + i;
		}
		return 0;
	}
	/**
	 * Returns the guaranteed amount of free space available for a given space class.
	 */
	public static int freeSpaceForClass(int spaceClass) {
		return SpaceClassSize[spaceClass];
	}
	/**
	 * Determines the initial values for the static field SpaceClassSize.
	 * It is run at development time and the results used for the initializer.  It is
	 * not used at run time.  There are 16 space classes numbered 0-15.  
	 * A space class of 0 indicates the page is empty and can hold an object of maximum size.
	 * Classes 1 to 10 decrease in size in equal steps.  
	 * The classes 11 to 14 decrease in size in steps of decreasing size.
	 * Class 15 indicates the page is full.
	 * This is not run as a static initialization block because the values MUST be absolutely
	 * inviolable for a given version of an object store.
	 */
	public static int[] generateSpaceClassSize() {
		int[] result = new int[16];
		int n = (ObjectPage.ObjectSpaceSize - 512) / 10;
		for (int i = 0; i < 11; i++) result[i] = ObjectPage.ObjectSpaceSize - i * n;
		for (int i = 11; i < 15; i++) result[i] = result[i-1] / 2;
		result[15] = 0;
		return result;
	}
	/**
	 * Initializes an instance of this page.
	 */
	protected void initialize(PageStore store, int pageNumber) {
		super.initialize(store, pageNumber);
	}
	/**
	 * Returns the space class of for a given number of bytes of free space.  
	 * The space class is indicates approximately 
	 * how much of the object space on a page is free.
	 */
	public static byte spaceClass(int freeSpace) {
		byte i = 0;
		while (SpaceClassSize[i] > freeSpace) i++;
		return i;
	}
	/**
	 * Sets the spaceClass for a given object page.
	 */
	public void updateForObjectPage(ObjectPage page) {
		contents[page.getPageNumber() - pageNumber] = page.spaceClass();
		modified();
	}
}
