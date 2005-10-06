/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Martin Burger <m@rtin-burger.de> patch for #93810 and #93901
 *******************************************************************************/
package org.eclipse.compare.internal.patch;

import java.io.*;
import java.text.*;
import java.util.*;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.util.Assert;

import org.eclipse.core.runtime.*;
import org.eclipse.core.resources.*;

import org.eclipse.compare.internal.Utilities;
import org.eclipse.compare.structuremergeviewer.Differencer;

/**
 * A Patcher 
 * - knows how to parse various patch file formats into some in-memory structure,
 * - holds onto the parsed data and the options to use when applying the patches,
 * - knows how to apply the patches to files and folders.
 */
public class Patcher {
	
	private static final boolean DEBUG= false;
	
	private static final String DEV_NULL= "/dev/null"; //$NON-NLS-1$

	static protected final String REJECT_FILE_EXTENSION= ".rej"; //$NON-NLS-1$

	static protected final String MARKER_TYPE= "org.eclipse.compare.rejectedPatchMarker"; //$NON-NLS-1$

	// diff formats
	//	private static final int CONTEXT= 0;
	//	private static final int ED= 1;
	//	private static final int NORMAL= 2;
	//	private static final int UNIFIED= 3;

	// we recognize the following date/time formats
	private static DateFormat[] DATE_FORMATS= new DateFormat[] {
		new SimpleDateFormat("EEE MMM dd kk:mm:ss yyyy"), //$NON-NLS-1$
		new SimpleDateFormat("yyyy/MM/dd kk:mm:ss"), //$NON-NLS-1$
		new SimpleDateFormat("EEE MMM dd kk:mm:ss yyyy", Locale.US) //$NON-NLS-1$
	};
		
	private String fName;
	protected Diff[] fDiffs;
	private IResource fTarget;
	// patch options
	private int fStripPrefixSegments;
	private int fFuzz;
	private boolean fIgnoreWhitespace= false;
	private boolean fIgnoreLineDelimiter= true;
	private boolean fPreserveLineDelimiters= false;
	private boolean fReverse= false;
	private boolean fAdjustShift= true;
	
	
	public Patcher() {
		// nothing to do
	}
	
	//---- options
	
	void setName(String name) {
		fName= name;
	}
	
	String getName() {
		return fName;
	}
	
	/*
	 * Returns an array of Diffs after a sucessfull call to <code>parse</code>.
	 * If <code>parse</code> hasn't been called returns <code>null</code>.
	 */
	public Diff[] getDiffs() {
		return fDiffs;
	}
	
	IPath getPath(Diff diff) {
		IPath path= diff.getPath();
		if (fStripPrefixSegments > 0 && fStripPrefixSegments < path.segmentCount())
			path= path.removeFirstSegments(fStripPrefixSegments);
		return path;
	}

	/*
	 * Returns <code>true</code> if new value differs from old.
	 */
	boolean setStripPrefixSegments(int strip) {
		if (strip != fStripPrefixSegments) {
			fStripPrefixSegments= strip;
			return true;
		}
		return false;
	}
	
	int getStripPrefixSegments() {
		return fStripPrefixSegments;
	}
	
	/*
	 * Returns <code>true</code> if new value differs from old.
	 */
	boolean setFuzz(int fuzz) {
		if (fuzz != fFuzz) {
			fFuzz= fuzz;
			return true;
		}
		return false;
	}
	
	/*
	 * Returns <code>true</code> if new value differs from old.
	 */
	public boolean setReversed(boolean reverse) {
		if (fReverse != reverse) {
			fReverse= reverse;
			
			for (int i= 0; i < fDiffs.length; i++)
				fDiffs[i].reverse();
						
			return true;
		}
		return false;
	}
		
	/*
	 * Returns <code>true</code> if new value differs from old.
	 */
	boolean setIgnoreWhitespace(boolean ignoreWhitespace) {
		if (ignoreWhitespace != fIgnoreWhitespace) {
			fIgnoreWhitespace= ignoreWhitespace;
			return true;
		}
		return false;
	}
		
	//---- parsing patch files

	public void parse(LineReader lr, String line) throws IOException {
		List diffs= new ArrayList();
		boolean reread= false;
		String diffArgs= null;
		String fileName= null;

		// read leading garbage
		reread= line!=null;
		while (true) {
			if (!reread)
				line= lr.readLine();
			reread= false;
			if (line == null)
				break;
			if (line.length() < 4)
				continue;	// too short
								
			// remember some infos
			if (line.startsWith("Index: ")) { //$NON-NLS-1$
				fileName= line.substring(7).trim();
				continue;
			}
			if (line.startsWith("diff")) { //$NON-NLS-1$
				diffArgs= line.substring(4).trim();
				continue;
			}

			if (line.startsWith("--- ")) { //$NON-NLS-1$
				line= readUnifiedDiff(diffs, lr, line, diffArgs, fileName);
				diffArgs= fileName= null;
				reread= true;
			} else if (line.startsWith("*** ")) { //$NON-NLS-1$
				line= readContextDiff(diffs, lr, line, diffArgs, fileName);
				diffArgs= fileName= null;
				reread= true;
			}
		}
		
		lr.close();
		
		fDiffs= (Diff[]) diffs.toArray(new Diff[diffs.size()]);
	}

	/*
	 * Returns the next line that does not belong to this diff
	 */
	protected String readUnifiedDiff(List diffs, LineReader reader, String line, String args, String fileName) throws IOException {

		String[] oldArgs= split(line.substring(4));

		// read info about new file
		line= reader.readLine();
		if (line == null || !line.startsWith("+++ ")) //$NON-NLS-1$
			return line;
			
		String[] newArgs= split(line.substring(4));
	
		Diff diff= new Diff(extractPath(oldArgs, 0, fileName), extractDate(oldArgs, 1),
				   			extractPath(newArgs, 0, fileName), extractDate(newArgs, 1));
		diffs.add(diff);
				   
		int[] oldRange= new int[2];
		int[] newRange= new int[2];
		List lines= new ArrayList();

		try {
			// read lines of hunk
			while (true) {
				
				line= reader.readLine();
				if (line == null)
					return null;
					
				if (reader.lineContentLength(line) == 0) {
					//System.out.println("Warning: found empty line in hunk; ignored");
					//lines.add(' ' + line);
					continue;
				}
				
				char c= line.charAt(0);
				switch (c) {
				case '@':
					if (line.startsWith("@@ ")) { //$NON-NLS-1$
						// flush old hunk
						if (lines.size() > 0) {
							new Hunk(diff, oldRange, newRange, lines);
							lines.clear();
						}
								
						// format: @@ -oldStart,oldLength +newStart,newLength @@
						extractPair(line, '-', oldRange);
						extractPair(line, '+', newRange);
						continue;
					}
					break;
				case ' ':
				case '+':
				case '-':
					lines.add(line);
					continue;
				case '\\':
					if (line.indexOf("newline at end") > 0) { //$NON-NLS-1$
						int lastIndex= lines.size();
						if (lastIndex > 0) {
							line= (String) lines.get(lastIndex-1);
							int end= line.length()-1;
							char lc= line.charAt(end);
							if (lc == '\n') {
								end--;
								if (end > 0 && line.charAt(end-1) == '\r')
									end--;
							} else if (lc == '\r') {
								end--;
							}
							line= line.substring(0, end+1);
							lines.set(lastIndex-1, line);
						}
						continue;
					}
					break;
				default:
					if (DEBUG) {
						int a1= c, a2= 0;
						if (line.length() > 1)
							a2= line.charAt(1);
						System.out.println("char: " + a1 + " " + a2); //$NON-NLS-1$ //$NON-NLS-2$
					}
					break;
				}
				return line;
			}
		} finally {
			if (lines.size() > 0)
				new Hunk(diff, oldRange, newRange, lines);
			diff.finish();
		}
	}
	
	/*
	 * Returns the next line that does not belong to this diff
	 */
	private String readContextDiff(List diffs, LineReader reader, String line, String args, String fileName) throws IOException {
		
		String[] oldArgs= split(line.substring(4));
		
		// read info about new file
		line= reader.readLine();
		if (line == null || !line.startsWith("--- ")) //$NON-NLS-1$
			return line;
		
		String[] newArgs= split(line.substring(4));
						
		Diff diff= new Diff(extractPath(oldArgs, 0, fileName), extractDate(oldArgs, 1),
				   			extractPath(newArgs, 0, fileName), extractDate(newArgs, 1));
		diffs.add(diff);
				   
		int[] oldRange= new int[2];
		int[] newRange= new int[2];
		List oldLines= new ArrayList();
		List newLines= new ArrayList();
		List lines= oldLines;
		
		try {
			// read lines of hunk
			while (true) {
				
				line= reader.readLine();
				if (line == null)
					return line;
				
				int l= line.length();
				if (l == 0)
					continue;
				if (l > 1) {
					switch (line.charAt(0)) {
					case '*':	
						if (line.startsWith("***************")) {	// new hunk //$NON-NLS-1$
							// flush old hunk
							if (oldLines.size() > 0 || newLines.size() > 0) {
								new Hunk(diff, oldRange, newRange, unifyLines(oldLines, newLines));
								oldLines.clear();
								newLines.clear();
							}
							continue;
						}
						if (line.startsWith("*** ")) {	// old range //$NON-NLS-1$
							// format: *** oldStart,oldEnd ***
							extractPair(line, ' ', oldRange);
							oldRange[1]= oldRange[1]-oldRange[0]+1;
							lines= oldLines;
							continue;
						}
						break;
					case ' ':	// context line
					case '+':	// addition
					case '!':	// change
						if (line.charAt(1) == ' ') {
							lines.add(line);
							continue;
						}
						break;
					case '-':
						if (line.charAt(1) == ' ') {	// deletion
							lines.add(line);
							continue;
						}
						if (line.startsWith("--- ")) {	// new range //$NON-NLS-1$
							// format: *** newStart,newEnd ***
							extractPair(line, ' ', newRange);
							newRange[1]= newRange[1]-newRange[0]+1;
							lines= newLines;
							continue;
						}
						break;
					default:
						break;
					}
				}
				return line;
			}
		} finally {
			// flush last hunk
			if (oldLines.size() > 0 || newLines.size() > 0)
				new Hunk(diff, oldRange, newRange, unifyLines(oldLines, newLines));
			diff.finish();
		}
	}
	
	/*
	 * Creates a List of lines in the unified format from
	 * two Lists of lines in the 'classic' format.
	 */
	private List unifyLines(List oldLines, List newLines) {
		List result= new ArrayList();

		String[] ol= (String[]) oldLines.toArray(new String[oldLines.size()]);
		String[] nl= (String[]) newLines.toArray(new String[newLines.size()]);
		
		int oi= 0, ni= 0;
		
		while (true) {
			
			char oc= 0;
			String o= null;
			if (oi < ol.length) {
				o= ol[oi];
				oc= o.charAt(0);
			}
			
			char nc= 0;
			String n= null;
			if (ni < nl.length) {
				n= nl[ni];
				nc= n.charAt(0);
			}
			
			// EOF
			if (oc == 0 && nc == 0)
				break;
				
			// deletion in old
			if (oc == '-') {
				do {
					result.add('-' + o.substring(2));
					oi++;
					if (oi >= ol.length)
						break;
					o= ol[oi];
				} while (o.charAt(0) == '-');
				continue;
			}
			
			// addition in new
			if (nc == '+') {
				do {
					result.add('+' + n.substring(2));
					ni++;
					if (ni >= nl.length)
						break;
					n= nl[ni];
				} while (n.charAt(0) == '+');
				continue;
			}
			
			// differing lines on both sides
			if (oc == '!' && nc == '!') {
				// remove old
				do {
					result.add('-' + o.substring(2));
					oi++;
					if (oi >= ol.length)
						break;
					o= ol[oi];
				} while (o.charAt(0) == '!');
				
				// add new
				do {
					result.add('+' + n.substring(2));
					ni++;
					if (ni >= nl.length)
						break;
					n= nl[ni];
				} while (n.charAt(0) == '!');
				
				continue;
			}
			
			// context lines
			if (oc == ' ' && nc == ' ') {
				do {
					Assert.isTrue(o.equals(n), "non matching context lines"); //$NON-NLS-1$
					result.add(' ' + o.substring(2));
					oi++;
					ni++;
					if (oi >= ol.length || ni >= nl.length)
						break;
					o= ol[oi];
					n= nl[ni];
				} while (o.charAt(0) == ' ' && n.charAt(0) == ' ');
				continue;
			}
			
			if (oc == ' ') {
				do {
					result.add(' ' + o.substring(2));
					oi++;
					if (oi >= ol.length)
						break;
					o= ol[oi];
				} while (o.charAt(0) == ' ');
				continue;
			}

			if (nc == ' ') {
				do {
					result.add(' ' + n.substring(2));
					ni++;
					if (ni >= nl.length)
						break;
					n= nl[ni];
				} while (n.charAt(0) == ' ');
				continue;
			}
			
			Assert.isTrue(false, "unexpected char <" + oc + "> <" + nc + ">"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
		
		return result;
	}
	
	/*
	 * Breaks the given string into tab separated substrings.
	 * Leading and trailing whitespace is removed from each token.
	 */ 
	private String[] split(String line) {
		List l= new ArrayList();
		StringTokenizer st= new StringTokenizer(line, "\t"); //$NON-NLS-1$
		while (st.hasMoreElements()) {
			String token= st.nextToken().trim();
			if (token.length() > 0)
 				l.add(token);
		}
		return (String[]) l.toArray(new String[l.size()]);
	}
	
	/*
	 * @return the parsed time/date in milliseconds or -1 on error
	 */
	private long extractDate(String[] args, int n) {
		if (n < args.length) {
			String line= args[n];
			for (int i= 0; i < DATE_FORMATS.length; i++) {
				DATE_FORMATS[i].setLenient(true);
				try {
					Date date= DATE_FORMATS[i].parse(line);
					return date.getTime();		
				} catch (ParseException ex) {
					// silently ignored
				}
			}
			// System.err.println("can't parse date: <" + line + ">");
		}
		return -1;
	}
	
	/*
	 * Returns null if file name is "/dev/null".
	 */
	private IPath extractPath(String[] args, int n, String path2) {
		if (n < args.length) {
			String path= args[n];
			if (DEV_NULL.equals(path))
				return null;
			int pos= path.lastIndexOf(':');
			if (pos >= 0)
				path= path.substring(0, pos);
			if (path2 != null && !path2.equals(path)) {
				if (DEBUG) System.out.println("path mismatch: " + path2); //$NON-NLS-1$
				path= path2;
			}
			return new Path(path);
		}
		return null;
	}
	
	/*
	 * Tries to extract two integers separated by a comma.
	 * The parsing of the line starts at the position after
	 * the first occurrence of the given character start an ends
	 * at the first blank (or the end of the line).
	 * If only a single number is found this is assumed to be the start of a one line range.
	 * If an error occurs the range -1,-1 is returned.
	 */
	private void extractPair(String line, char start, int[] pair) {
		pair[0]= pair[1]= -1;
		int startPos= line.indexOf(start);
		if (startPos < 0) {
			if (DEBUG) System.out.println("parsing error in extractPair: couldn't find \'" + start + "\'"); //$NON-NLS-1$ //$NON-NLS-2$
			return;
		}
		line= line.substring(startPos+1);
		int endPos= line.indexOf(' ');
		if (endPos < 0) {
			if (DEBUG) System.out.println("parsing error in extractPair: couldn't find end blank"); //$NON-NLS-1$
			return;
		}
		line= line.substring(0, endPos);
		int comma= line.indexOf(',');
		if (comma >= 0) {
			pair[0]= Integer.parseInt(line.substring(0, comma));
			pair[1]= Integer.parseInt(line.substring(comma+1));
		} else {	// abbreviated form for one line patch
			pair[0]= Integer.parseInt(line);
			pair[1]= 1;
		}
	}
	
	//---- applying a patch file
	
	/*
	 * Tries to patch the given lines with the specified Diff.
	 * Any hunk that couldn't be applied is returned in the list failedHunks.
	 */
	public void patch(Diff diff, List lines, List failedHunks) {
		
		int shift= 0;
		Iterator iter= diff.fHunks.iterator();
		while (iter.hasNext()) {
			Hunk hunk= (Hunk) iter.next();
			hunk.fMatches= false;
			shift= patch(hunk, lines, shift, failedHunks);
		}
	}

	/*
	 * Tries to apply the specified hunk to the given lines.
	 * If the hunk cannot be applied at the original position
	 * the methods tries Fuzz lines before and after.
	 * If this fails the Hunk is added to the given list of failed hunks.
	 */
	private int patch(Hunk hunk, List lines, int shift, List failedHunks) {
		if (tryPatch(hunk, lines, shift)) {
			if (hunk.isEnabled())
				shift+= doPatch(hunk, lines, shift);
		} else {
			boolean found= false;
			int oldShift= shift;
						
			for (int i= 1; i <= fFuzz; i++) {
				if (tryPatch(hunk, lines, shift-i)) {
					if (fAdjustShift)
						shift-= i;
					found= true;
					break;
				}
			}
			
			if (! found) {
				for (int i= 1; i <= fFuzz; i++) {
					if (tryPatch(hunk, lines, shift+i)) {
						if (fAdjustShift)
							shift+= i;
						found= true;
						break;
					}
				}
			}
			
			if (found) {
				if (DEBUG) System.out.println("patched hunk at offset: " + (shift-oldShift)); //$NON-NLS-1$
				shift+= doPatch(hunk, lines, shift);
			} else {
				if (failedHunks != null) {
					if (DEBUG) System.out.println("failed hunk"); //$NON-NLS-1$
					failedHunks.add(hunk);
				}
			}
		}
		return shift;
	}
	
	/*
	 * Tries to apply the given hunk on the specified lines.
	 * The parameter shift is added to the line numbers given
	 * in the hunk.
	 */
	private boolean tryPatch(Hunk hunk, List lines, int shift) {
		int pos= hunk.fOldStart + shift;
		int deleteMatches= 0;
		for (int i= 0; i < hunk.fLines.length; i++) {
			String s= hunk.fLines[i];
			Assert.isTrue(s.length() > 0);
			String line= s.substring(1);
			char controlChar= s.charAt(0);
			if (controlChar == ' ') {	// context lines
				while (true) {
					if (pos < 0 || pos >= lines.size())
						return false;
					if (linesMatch(line, (String) lines.get(pos))) {
						pos++;
						break;
					}
					return false;
				}
			} else if (controlChar == '-') {
				// deleted lines
				while (true) {
					if (pos < 0 || pos >= lines.size())
						return false;
					if (linesMatch(line, (String) lines.get(pos))) {
						deleteMatches++;
						pos++;
						break;
					}
					if (deleteMatches <= 0)
						return false;
					pos++;
				}
			} else if (controlChar == '+') {
				// added lines
				// we don't have to do anything for a 'try'
			} else
				Assert.isTrue(false, "tryPatch: unknown control character: " + controlChar); //$NON-NLS-1$
		}
		return true;
	}
	
	private int doPatch(Hunk hunk, List lines, int shift) {
		int pos= hunk.fOldStart + shift;
		for (int i= 0; i < hunk.fLines.length; i++) {
			String s= hunk.fLines[i];
			Assert.isTrue(s.length() > 0);
			String line= s.substring(1);
			char controlChar= s.charAt(0);
			if (controlChar == ' ') {	// context lines
				while (true) {
					Assert.isTrue(pos < lines.size(), "doPatch: inconsistency in context"); //$NON-NLS-1$
					if (linesMatch(line, (String) lines.get(pos))) {
						pos++;
						break;
					}
					pos++;
				}
			} else if (controlChar == '-') {
				// deleted lines				
				while (true) {
					Assert.isTrue(pos < lines.size(), "doPatch: inconsistency in deleted lines"); //$NON-NLS-1$
					if (linesMatch(line, (String) lines.get(pos))) {
						break;
					}
					pos++;
				}
				lines.remove(pos);
			} else if (controlChar == '+') {
				// added lines
				if (hunk.fOldLength == 0 && pos+1 < lines.size())
					lines.add(pos+1, line);
				else
					lines.add(pos, line);
				pos++;
			} else
				Assert.isTrue(false, "doPatch: unknown control character: " + controlChar); //$NON-NLS-1$
		}
		hunk.fMatches= true;
		return hunk.fNewLength - hunk.fOldLength;
	}

	public void applyAll(IProgressMonitor pm, Shell shell, String title) throws CoreException {

		final int WORK_UNIT= 10;
		
		int i;
		
		IFile singleFile= null;	// file to be patched
		IContainer container= null;
		if (fTarget instanceof IContainer)
			container= (IContainer) fTarget;
		else if (fTarget instanceof IFile) {
			singleFile= (IFile) fTarget;
			container= singleFile.getParent();
		} else {
			Assert.isTrue(false);
		}
		
		// get all files to be modified in order to call validateEdit
		List list= new ArrayList();
		if (singleFile != null)
			list.add(singleFile);
		else {
			for (i= 0; i < fDiffs.length; i++) {
				Diff diff= fDiffs[i];
				if (diff.isEnabled()) {
					switch (diff.getType()) {
					case Differencer.CHANGE:
						list.add(createPath(container, getPath(diff)));
						break;
					}
				}
			}
		}
		if (! Utilities.validateResources(list, shell, title))
			return;
		
		if (pm != null) {
			String message= PatchMessages.Patcher_Task_message;	
			pm.beginTask(message, fDiffs.length*WORK_UNIT);
		}
		
		for (i= 0; i < fDiffs.length; i++) {
			
			int workTicks= WORK_UNIT;
			
			Diff diff= fDiffs[i];
			if (diff.isEnabled()) {
				
				IPath path= getPath(diff);
				if (pm != null)
					pm.subTask(path.toString());
			
				IFile file= singleFile != null
								? singleFile
								: createPath(container, path);
					
				List failed= new ArrayList();
				List result= null;
				
				int type= diff.getType();
				switch (type) {
				case Differencer.ADDITION:
					// patch it and collect rejected hunks
					result= apply(diff, file, true, failed);
					store(createString(result), file, new SubProgressMonitor(pm, workTicks));
					workTicks-= WORK_UNIT;
					break;
				case Differencer.DELETION:
					file.delete(true, true, new SubProgressMonitor(pm, workTicks));
					workTicks-= WORK_UNIT;
					break;
				case Differencer.CHANGE:
					// patch it and collect rejected hunks
					result= apply(diff, file, false, failed);
					store(createString(result), file, new SubProgressMonitor(pm, workTicks));
					workTicks-= WORK_UNIT;
					break;
				}

				if (failed.size() > 0) {
					IPath pp= null;
					if (path.segmentCount() > 1) {
						pp= path.removeLastSegments(1);
						pp= pp.append(path.lastSegment() + REJECT_FILE_EXTENSION);
					} else
						pp= new Path(path.lastSegment() + REJECT_FILE_EXTENSION);
					file= createPath(container, pp);
					if (file != null) {
						store(getRejected(failed), file, pm);
						try {
							IMarker marker= file.createMarker(MARKER_TYPE);
							marker.setAttribute(IMarker.MESSAGE, PatchMessages.Patcher_Marker_message);	
							marker.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_HIGH);
						} catch (CoreException ex) {
							// NeedWork
						}
					}
				}
			}
			
			if (pm != null) {
				if (pm.isCanceled())
					break;
				if (workTicks > 0)
					pm.worked(workTicks);
			}
		}
	}
	
	/*
	 * Reads the contents from the given file and returns them as
	 * a List of lines.
	 */
	List load(IFile file, boolean create) {
		List lines= null;
		if (!create && file != null) {
			// read current contents
			InputStream is= null;
			try {
				is= file.getContents();
				
				Reader streamReader= null;
				try {
					streamReader= new InputStreamReader(is, Utilities.getCharset(file));
				} catch (UnsupportedEncodingException x) {
					// use default encoding
					streamReader= new InputStreamReader(is);
				}
				
				BufferedReader reader= new BufferedReader(streamReader);
				LineReader lr= new LineReader(reader);
				if (!"carbon".equals(SWT.getPlatform()))	//$NON-NLS-1$
					lr.ignoreSingleCR();
				lines= lr.readLines();
			} catch(CoreException ex) {
				// NeedWork
			} finally {
				if (is != null)
					try {
						is.close();
					} catch(IOException ex) {
						// silently ignored
					}
			}
		}
		
		if (lines == null)
			lines= new ArrayList();
		return lines;
	}
	
	List apply(Diff diff, IFile file, boolean create, List failedHunks) {
		List lines= load(file, create);
		patch(diff, lines, failedHunks);
		return lines;
	}
	
	/*
	 * Converts the string into bytes and stores them in the given file.
	 */
	protected void store(String contents, IFile file, IProgressMonitor pm) throws CoreException {

		byte[] bytes;
		try {
			bytes= contents.getBytes(Utilities.getCharset(file));
		} catch (UnsupportedEncodingException x) {
			// uses default encoding
			bytes= contents.getBytes();
		}
		
		InputStream is= new ByteArrayInputStream(bytes);
		try {
			if (file.exists()) {
				file.setContents(is, false, true, pm);
			} else {
				file.create(is, false, pm);
			}
		} finally {
			if (is != null)
				try {
					is.close();
				} catch(IOException ex) {
					// silently ignored
				}
		}
	}

	/*
	 * Concatenates all strings found in the given List.
	 */
	protected String createString(List lines) {
		StringBuffer sb= new StringBuffer();
		Iterator iter= lines.iterator();
		if (fPreserveLineDelimiters) {
			while (iter.hasNext())
				sb.append((String)iter.next());
		} else {
			String lineSeparator= System.getProperty("line.separator"); //$NON-NLS-1$
			while (iter.hasNext()) {
				String line= (String)iter.next();
				int l= length(line);
				if (l < line.length()) {	// line has delimiter
					sb.append(line.substring(0, l));
					sb.append(lineSeparator);
				} else {
					sb.append(line);
				}
			}
		}
		return sb.toString();
	}

	String getRejected(List failedHunks) {
		if (failedHunks.size() <= 0)
			return null;
		
		String lineSeparator= System.getProperty("line.separator"); //$NON-NLS-1$
		StringBuffer sb= new StringBuffer();
		Iterator iter= failedHunks.iterator();
		while (iter.hasNext()) {
			Hunk hunk= (Hunk) iter.next();
			sb.append(hunk.getRejectedDescription());
			sb.append(lineSeparator);
			sb.append(hunk.getContent());
		}
		return sb.toString();
	}
	
	/*
	 * Ensures that a file with the given path exists in
	 * the given container. Folder are created as necessary.
	 */
	protected IFile createPath(IContainer container, IPath path) throws CoreException {
		if (path.segmentCount() > 1) {
			IFolder f= container.getFolder(path.uptoSegment(1));
			if (!f.exists())
				f.create(false, true, null);
			return createPath(f, path.removeFirstSegments(1));
		}
		// a leaf
		return container.getFile(path);
	}

	/*
	 * Returns the given string with all whitespace characters removed.
	 * Whitespace is defined by <code>Character.isWhitespace(...)</code>.
	 */
	private static String stripWhiteSpace(String s) {
		StringBuffer sb= new StringBuffer();
		int l= s.length();
		for (int i= 0; i < l; i++) {
			char c= s.charAt(i);
			if (!Character.isWhitespace(c))
				sb.append(c);
		}
		return sb.toString();
	}
	
	/*
	 * Compares two strings.
	 * If fIgnoreWhitespace is true whitespace is ignored.
	 */
	private boolean linesMatch(String line1, String line2) {
		if (fIgnoreWhitespace)
			return stripWhiteSpace(line1).equals(stripWhiteSpace(line2));
		if (fIgnoreLineDelimiter) {
			int l1= length(line1);
			int l2= length(line2);
			if (l1 != l2)
				return false;
			return line1.regionMatches(0, line2, 0, l1);
		}
		return line1.equals(line2);
	}
	
	/*
	 * Returns the length (exluding a line delimiter CR, LF, CR/LF)
	 * of the given string.
	 */
	/* package */ static int length(String s) {
		int l= s.length();
		if (l > 0) {
			char c= s.charAt(l-1);
			if (c == '\r')
				return l-1;
			if (c == '\n') {
				if (l > 1 && s.charAt(l-2) == '\r')
					return l-2;
				return l-1;
			}
		}
		return l;
	}

	int calculateFuzz(Hunk hunk, List lines, int shift, IProgressMonitor pm, int[] fuzz) {
		
		hunk.fMatches= false;
		if (tryPatch(hunk, lines, shift)) {
			shift+= doPatch(hunk, lines, shift);
			fuzz[0]= 0;
		} else {
			boolean found= false;
			int hugeFuzz= lines.size();	// the maximum we need for this file
			fuzz[0]= -2;	// not found
			
			for (int i= 1; i <= hugeFuzz; i++) {
				if (pm.isCanceled()) {
					fuzz[0]= -1;
					return 0;
				}
				if (tryPatch(hunk, lines, shift-i)) {
					fuzz[0]= i;
					if (fAdjustShift)
						shift-= i;
					found= true;
					break;
				}
			}
			
			if (! found) {
				for (int i= 1; i <= hugeFuzz; i++) {
					if (pm.isCanceled()) {
						fuzz[0]= -1;
						return 0;
					}
					if (tryPatch(hunk, lines, shift+i)) {
						fuzz[0]= i;
						if (fAdjustShift)
							shift+= i;
						found= true;
						break;
					}
				}
			}
			
			if (found)
				shift+= doPatch(hunk, lines, shift);
		}
		return shift;
	}

	public IResource getTarget() {
		return fTarget;
	}

	public void setTarget(IResource target) {
		fTarget= target;
	}

	/**
	 * Iterates through all of the resources contained in the Patch Wizard target
	 * and looks to for a match to the passed in file 
	 * @param path
	 * @return IFile which matches the passed in path or null if none found
	 */
	public IFile existsInTarget(IPath path) {
		if (fTarget instanceof IFile) { // special case
			IFile file= (IFile) fTarget;
			if (matches(file.getFullPath(), path))
				return file;
		} else if (fTarget instanceof IContainer) {
			IContainer c= (IContainer) fTarget;
			if (c.exists(path))
				return c.getFile(path);
		}
		return null;
	}

	/**
	 * Returns true if path completely matches the end of fullpath
	 * @param fullpath 
	 * @param path 
	 * @return true if path matches, false otherwise
	 */
	private boolean matches(IPath fullpath, IPath path) {
		for (IPath p= fullpath; path.segmentCount()<=p.segmentCount(); p= p.removeFirstSegments(1)) {
			if (p.equals(path))
				return true;
		}
		return false;
	}

	public int calculatePrefixSegmentCount() {
		//Update prefix count - go through all of the diffs and find the smallest
		//path segment contained in all diffs.
		int length= 99;
		if (fDiffs!=null)
			for (int i= 0; i<fDiffs.length; i++) {
				Diff diff= fDiffs[i];
				if (diff.fOldPath!=null)
					length= Math.min(length, diff.fOldPath.segmentCount());
				if (diff.fNewPath!=null)
					length= Math.min(length, diff.fNewPath.segmentCount());
			}
		return length;
	}
}
