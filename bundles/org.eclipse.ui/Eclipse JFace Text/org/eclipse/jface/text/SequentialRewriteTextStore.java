package org.eclipse.jface.text;
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
	
	/** Store of the already rewritten portion of the source store */
	private StringBuffer fBuffer= new StringBuffer();
	/** Offset of the first not-yet rewritten character of the source store */
	private int fSourceOffset= 0;
	/** The source store */
	private ITextStore fSource;
	
	
	/**
	 * Creates a new sequential rewrite store for the given source store.
	 */
	public SequentialRewriteTextStore(ITextStore source) {
		fSource= source;
	}
	
	/**
	 * Returns the source store of this rewrite store.
	 * @return  the source store of this rewrite store
	 */
	public ITextStore getSourceStore() {
		return fSource;
	}
	
	/*
	 * @see ITextStore#replace(int, int, String)
	 */
	public void replace(int offset, int length, String text) {
		
		int bufferLength= fBuffer.length();
		
		if (offset < bufferLength)
			throw new IllegalArgumentException();
			
		if (offset > bufferLength) {
			int relative= offset - bufferLength;
			fBuffer.append(fSource.get(fSourceOffset, relative));
			fSourceOffset += relative;
		}
		
		if (text != null && text.length() > 0)
			fBuffer.append(text);
			
		fSourceOffset += length;
	}

	/*
	 * @see ITextStore#set(String)
	 */
	public void set(String text) {
		fBuffer.setLength(0);
		fBuffer.append(text);
		fSourceOffset= fSource.getLength();
	}
	
	/*
	 * @see ITextStore#get(int, int)
	 */
	public String get(int offset, int length) {
		
		int endOffset= offset + length;
		int bufferLength= fBuffer.length();
		
		// everything in buffer
		if (endOffset <= bufferLength)
			return fBuffer.substring(offset, endOffset);
			
		// everthing in source store
		if (offset >= bufferLength) {
			int relative= offset - bufferLength;
			return fSource.get(fSourceOffset + relative, length);
		}
		
		// distributed between buffer and source
		StringBuffer result= new StringBuffer();
		// copy part of buffer
		result.append(fBuffer.substring(offset, bufferLength));
		// copy part of source
		result.append(fSource.get(fSourceOffset, endOffset - bufferLength));
		return result.toString();
	}

	/*
	 * @see ITextStore#get(int)
	 */
	public char get(int offset) {
		int bufferLength= fBuffer.length();
		if (offset < bufferLength)
			return fBuffer.charAt(offset);
		
		int relative= offset - bufferLength;
		return fSource.get(fSourceOffset +  relative);
	}

	/*
	 * @see ITextStore#getLength()
	 */
	public int getLength() {
		return fBuffer.length() + (fSource.getLength() - fSourceOffset);
	}
	
	/**
	 * Disposes this rewrite store.
	 */
	public void dispose() {
		fBuffer= null;
		fSource= null;
	}
}
