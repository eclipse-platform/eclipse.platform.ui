package org.eclipse.jface.text;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
/**
 * A text store that is optimized for sequential rewriting. 
 * Non-sequential modifications are not supported and result in
 * <code>IllegalArgumentException</code>s.
 */
public class SequentialRewriteTextStore implements ITextStore {

	/**
	 * A buffered replace command.
	 */
	private static class Replace {
		public int newOffset;
		public final int offset;
		public final int length;
		public final String text;	
		
		public Replace(int offset, int newOffset, int length, String text) {
			this.newOffset= newOffset;
			this.offset= offset;
			this.length= length;
			this.text= text;
		}
	}

	/** The list of buffered replacements. */
	private List fReplaceList;	
	/** The source store */
	private ITextStore fSource;
	/** A flag to enforce sequential access. */
	private static final boolean ASSERT_SEQUENTIALITY= false;
	
	
	/**
	 * Creates a new sequential rewrite store for the given source store.
	 */
	public SequentialRewriteTextStore(ITextStore source) {
		fReplaceList= new LinkedList();
		fSource= source;
	}
	
	/**
	 * Returns the source store of this rewrite store.
	 * @return  the source store of this rewrite store
	 */
	public ITextStore getSourceStore() {
		commit();
		return fSource;
	}
	
	/*
	 * @see ITextStore#replace(int, int, String)
	 */
	public void replace(int offset, int length, String text) {		

		if (fReplaceList.size() == 0) {
			fReplaceList.add(new Replace(offset, offset, length, text));

		} else {
			Replace firstReplace= (Replace) fReplaceList.get(0);
			Replace lastReplace= (Replace) fReplaceList.get(fReplaceList.size() - 1);

			// backward
			if (offset + length <= firstReplace.newOffset) {
				int delta= text.length() - length;
				if (delta != 0) {
					for (Iterator i= fReplaceList.iterator(); i.hasNext(); ) {
						Replace replace= (Replace) i.next();
						replace.newOffset += delta;
					}	
				}
				
				fReplaceList.add(0, new Replace(offset, offset, length, text));

			// forward				
			} else if (offset >= lastReplace.newOffset + lastReplace.text.length()) {
				int delta= getDelta(lastReplace);					
				fReplaceList.add(new Replace(offset - delta, offset, length, text));

			} else if (ASSERT_SEQUENTIALITY) {
				throw new IllegalArgumentException();

			} else {				
				commit();
				fSource.replace(offset, length, text);
			}
		}
	}

	/*
	 * @see ITextStore#set(String)
	 */
	public void set(String text) {
		fSource.set(text);
		fReplaceList.clear();
	}
	
	/*
	 * @see ITextStore#get(int, int)
	 */
	public String get(int offset, int length) {

		if (fReplaceList.size() == 0) {
			return fSource.get(offset, length);

		} else {
			Replace firstReplace= (Replace) fReplaceList.get(0);
			Replace lastReplace= (Replace) fReplaceList.get(fReplaceList.size() - 1);

			// before
			if (offset + length <= firstReplace.newOffset) {
				return fSource.get(offset, length);

			// after				
			} else if (offset >= lastReplace.newOffset + lastReplace.text.length()) {
				int delta= getDelta(lastReplace);
				return fSource.get(offset - delta, length);

			} else if (ASSERT_SEQUENTIALITY) {
				throw new IllegalArgumentException();

			} else {

				int delta= 0;
				for (Iterator i= fReplaceList.iterator(); i.hasNext(); ) {
					Replace replace= (Replace) i.next();				
		
					if (offset + length < replace.newOffset) {
						return fSource.get(offset - delta, length);	
						
					} else if (offset >= replace.newOffset && offset + length <= replace.newOffset + replace.text.length()) {
						return replace.text.substring(offset - replace.newOffset, length);
						
					} else if (offset >= replace.newOffset + replace.text.length()) {
						delta= getDelta(replace);
						continue;		
		
					} else {				
						commit();
						return fSource.get(offset, length);
					}
				}
				
				return fSource.get(offset - delta, length);
			}
		}
	}
	
	/**
	 * Returns the difference between current and original offset after the replace position.
	 */
	private static final int getDelta(Replace replace) {
		return
			replace.newOffset - replace.offset +
			replace.text.length() - replace.length;
	}

	/*
	 * @see ITextStore#get(int)
	 */
	public char get(int offset) {
		if (fReplaceList.size() == 0) {
			return fSource.get(offset);
				
		} else {
			Replace firstReplace= (Replace) fReplaceList.get(0);
			Replace lastReplace= (Replace) fReplaceList.get(fReplaceList.size() - 1);

			// before
			if (offset < firstReplace.newOffset) {
				return fSource.get(offset);

			// after				
			} else if (offset >= lastReplace.newOffset + lastReplace.text.length()) {
				int delta= getDelta(lastReplace);
				return fSource.get(offset - delta);

			} else if (ASSERT_SEQUENTIALITY) {
				throw new IllegalArgumentException();

			} else {				

				int delta= 0;
				for (Iterator i= fReplaceList.iterator(); i.hasNext(); ) {
					Replace replace= (Replace) i.next();
					
					if (offset < replace.newOffset)
						return fSource.get(offset - delta);	
						
					else if (offset < replace.newOffset + replace.text.length())
						return replace.text.charAt(offset - replace.newOffset);					
					
					delta= getDelta(replace);
				}
								
				return fSource.get(offset - delta);
			}
		}		
	}

	/*
	 * @see ITextStore#getLength()
	 */
	public int getLength() {
		if (fReplaceList.size() == 0) {
			return fSource.getLength();
			
		} else {
			Replace lastReplace= (Replace) fReplaceList.get(fReplaceList.size() - 1);
			return fSource.getLength() + getDelta(lastReplace);
		}
	}
	
	/**
	 * Disposes this rewrite store.
	 */
	public void dispose() {
		fReplaceList= null;
		fSource= null;
	}
	
	/**
	 * Commits all buffered replace commands.
	 */
	private void commit() {

		if (fReplaceList.size() == 0)
			return;
		
		StringBuffer buffer= new StringBuffer();		

		int delta= 0;				
		for (Iterator i= fReplaceList.iterator(); i.hasNext(); ) {
			Replace replace= (Replace) i.next();

			int offset= buffer.length() - delta;
			buffer.append(fSource.get(offset, replace.offset - offset));
			buffer.append(replace.text);
			delta= getDelta(replace);
		}
		
		int offset= buffer.length() - delta;
		buffer.append(fSource.get(offset, fSource.getLength() - offset));
		
		fSource.set(buffer.toString());
		fReplaceList.clear();
	}

}
