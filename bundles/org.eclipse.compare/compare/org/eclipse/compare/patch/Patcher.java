package org.eclipse.compare.patch;

import java.io.*;
import java.text.*;
import java.util.*;

import org.eclipse.jface.util.Assert;

import org.eclipse.core.runtime.*;

/**
 * A Patcher 
 * - knows how to parse various patch file formats into some in-memory structure,
 * - holds onto the parsed data and the options to use when applying the patches,
 * - knows how to apply the patches to files and folders.
 */
public class Patcher {
	
	private static final String DEV_NULL= "/dev/null";

	// diff formats
	private static final int CONTEXT= 0;
	private static final int ED= 1;
	private static final int NORMAL= 2;
	private static final int UNIFIED= 3;
	
	// we recognize the following date/time formats
	private static DateFormat[] DATE_FORMATS= new DateFormat[] {
		new SimpleDateFormat("EEE MMM dd kk:mm:ss yyyy"),
		new SimpleDateFormat("yyyy/MM/dd kk:mm:ss"),
		new SimpleDateFormat("EEE MMM dd kk:mm:ss yyyy", Locale.US)
	};
		
	private String fName;
	private Diff[] fDiffs;
	// patch options
	private int fStripPrefixSegments;
	private int fFuzz;
	private boolean fIgnoreWhitespace;
	
	
	Patcher() {
	}
	
	//---- options
	
	void setName(String name) {
		fName= name;
	}
	
	String getName() {
		return fName;
	}
	
	/**
	 * Returns an array of Diffs after a sucessfull call to <code>parse</code>.
	 * If <code>parse</code> hasn't been called returns <code>null</code>.
	 */
	Diff[] getDiffs() {
		return fDiffs;
	}
	
	IPath getPath(Diff diff) {
		IPath path= diff.getPath();
		if (fStripPrefixSegments > 0 && fStripPrefixSegments < path.segmentCount())
			path= path.removeFirstSegments(fStripPrefixSegments);
		return path;
	}

	/**
	 * Returns <code>true</code> if new value differs from old.
	 */
	boolean setStripPrefixSegments(int strip) {
		if (strip != fStripPrefixSegments) {
			fStripPrefixSegments= strip;
			return true;
		}
		return false;
	}
	
	/**
	 * Returns <code>true</code> if new value differs from old.
	 */
	boolean setFuzz(int fuzz) {
		if (fuzz != fFuzz) {
			fFuzz= fuzz;
			return true;
		}
		return false;
	}
	
	/**
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
		
	/* package */ void parse(BufferedReader reader) throws IOException {
		List diffs= new ArrayList();
		String line= null;
		boolean reread= false;
		String diffArgs= null;
		String fileName= null;
		
		LineReader lr= new LineReader(reader);
		
		// read leading garbage
		while (true) {
			if (!reread)
				line= lr.readLine();
			reread= false;
			if (line == null)
				break;
			if (line.length() < 4)
				continue;	// too short
								
			// remember some infos
			if (line.startsWith("Index: ")) {
				fileName= line.substring(7).trim();
				continue;
			}
			if (line.startsWith("diff")) {
				diffArgs= line.substring(4).trim();
				continue;
			}

			if (line.startsWith("--- ")) {
				line= readUnifiedDiff(diffs, lr, line, diffArgs, fileName);
				diffArgs= fileName= null;
				reread= true;
			} else if (line.startsWith("*** ")) {
				line= readContextDiff(diffs, lr, line, diffArgs, fileName);
				diffArgs= fileName= null;
				reread= true;
			}
		}
		
		lr.close();
		
		fDiffs= (Diff[]) diffs.toArray((Diff[]) new Diff[diffs.size()]);
	}

	/**
	 * Returns the next line that does not belong to this diff
	 */
	private String readUnifiedDiff(List diffs, LineReader reader, String line, String args, String fileName) throws IOException {
								
		String[] oldArgs= split(line.substring(4));

		// read info about new file
		line= reader.readLine();
		if (line == null || !line.startsWith("+++ "))
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
					
				if (line.length() > 0) {
					char c= line.charAt(0);
					switch (c) {
					case '@':
						if (line.startsWith("@@ ")) {
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
						if (line.startsWith("No newline at end of file", 2)) {
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
								line= line.substring(0, end);
								lines.set(lastIndex-1, line);
							}
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
			if (lines.size() > 0)
				new Hunk(diff, oldRange, newRange, lines);
			diff.finish();
		}
	}
	
	/**
	 * Returns the next line that does not belong to this diff
	 */
	private String readContextDiff(List diffs, LineReader reader, String line, String args, String fileName) throws IOException {
		
		String[] oldArgs= split(line.substring(4));
		
		// read info about new file
		line= reader.readLine();
		if (line == null || !line.startsWith("--- "))
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
						if (line.startsWith("***************")) {	// new hunk
							// flush old hunk
							if (oldLines.size() > 0 || newLines.size() > 0) {
								new Hunk(diff, oldRange, newRange, unifyLines(oldLines, newLines));
								oldLines.clear();
								newLines.clear();
							}
							continue;
						}
						if (line.startsWith("*** ")) {	// old range
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
						if (line.startsWith("--- ")) {	// new range
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
	
	/**
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
					Assert.isTrue(o.equals(n), "non matching context lines");
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
			
			Assert.isTrue(false, "unexpected char <" + oc + "> <" + nc + ">");
		}
		
		return result;
	}
	
	/**
	 * Breaks the given string into tab separated substrings.
	 * Leading and trailing whitespace is removed from each token.
	 */ 
	private String[] split(String line) {
		List l= new ArrayList();
		StringTokenizer st= new StringTokenizer(line, "\t");
		while (st.hasMoreElements()) {
			String token= st.nextToken().trim();
			if (token.length() > 0)
 				l.add(token);
		}
		return (String[]) l.toArray(new String[l.size()]);
	}
	
	/**
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
				}
			}
			// System.err.println("can't parse date: <" + line + ">");
		}
		return -1;
	}
	
	/**
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
				// System.out.println("path mismatch: " + path2);
				path= path2;
			}
			return new Path(path);
		}
		return null;
	}
	
	/**
	 * Tries to extract two integers separated by a comma.
	 * The parsing of the line starts at the position after
	 * the first occurrence of the given character start an ends
	 * at the first blank (or the end of the line).
	 * If only a single number is found this is assumed to be the length of the range.
	 * In this case the start of the range is set to 1.
	 * If an error occurs the range -1,-1 is returned.
	 */
	private void extractPair(String line, char start, int[] pair) {
		pair[0]= pair[1]= -1;
		int startPos= line.indexOf(start);
		if (startPos < 0) {
			System.out.println("parsing error in extractPair: couldn't find \'" + start + "\'");
			return;
		}
		line= line.substring(startPos+1);
		int endPos= line.indexOf(' ');
		if (endPos < 0) {
			System.out.println("parsing error in extractPair: couldn't find end blank");
			return;
		}
		line= line.substring(0, endPos);
		int comma= line.indexOf(',');
		if (comma >= 0) {
			pair[0]= Integer.parseInt(line.substring(0, comma));
			pair[1]= Integer.parseInt(line.substring(comma+1));
		} else {
			pair[0]= 1;
			pair[1]= Integer.parseInt(line.substring(comma+1));
		}
	}
	
		//---- applying a patch file
	
	/**
	 * Tries to patch the given lines with the specified Diff.
	 * Any hunk that couldn't be applied is returned in the list failedHunks.
	 */
	/* package */ void patch(Diff diff, List lines, List failedHunks) {
		
		int shift= 0;
		Iterator iter= diff.fHunks.iterator();
		while (iter.hasNext()) {
			Hunk hunk= (Hunk) iter.next();
			shift= patch(hunk, lines, shift, failedHunks);
		}
	}

	/**
	 * Tries to patch the contents of the given reader with the specified Diff.
	 * Any hunk that couldn't be applied is returned in the list failedHunks.
	 */
	/* package */ String patch(Diff diff, BufferedReader reader, List failedHunks) {
		
		List lines= new LineReader(reader).readLines();
		if (lines == null)
			lines= new ArrayList();

		patch(diff, lines, failedHunks);
		
		StringBuffer sb= new StringBuffer();
		Iterator iter= lines.iterator();
		while (iter.hasNext())
			sb.append((String)iter.next());
		return sb.toString();
	}

	/**
	 * Tries to apply the specified hunk to the given lines.
	 * If the hunk cannot be applied at the original position
	 * the methods tries Fuzz lines before and after.
	 * If this fails the Hunk is added to the given list of failed hunks.
	 */
	private int patch(Hunk hunk, List lines, int shift, List failedHunks) {
		if (tryPatch(hunk, lines, shift)) {
			shift+= doPatch(hunk, lines, shift);
		} else {
			boolean found= false;
			int oldShift= shift;
			
			for (int i= shift-1; i > shift-fFuzz; i--) {
				if (tryPatch(hunk, lines, i)) {
					shift= i;
					found= true;
					break;
				}
			}
			
			if (! found) {
				for (int i= shift+1; i < shift+fFuzz; i++) {
					if (tryPatch(hunk, lines, i)) {
						shift= i;
						found= true;
						break;
					}
				}
			}
			
			if (found) {
				//System.out.println("patched hunk at offset: " + (shift-oldShift));
				shift+= doPatch(hunk, lines, shift);
			} else {
				if (failedHunks != null)
					failedHunks.add(hunk);
				System.out.println("hunk ignored");
			}
		}
		return shift;
	}
		
	private boolean tryPatch(Hunk hunk, List lines, int shift) {
		int pos= hunk.fOldStart + shift;
		int contextMatches= 0;
		int deleteMatches= 0;
		for (int i= 0; i < hunk.fLines.length; i++) {
			String s= hunk.fLines[i];
			Assert.isTrue(s.length() > 0);
			String line= s.substring(1);
			switch (s.charAt(0)) {
			case ' ':	// context lines
				while (true) {
					if (pos < 0 || pos >= lines.size())
						return false;
					if (linesMatch(line, (String) lines.get(pos))) {
						contextMatches++;
						pos++;
						break;
					}
					if (contextMatches <= 0)
						return false;
					pos++;
				}
				break;
			case '-':	// deleted lines
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
				break;
			case '+':	// added lines
				break;
			}
		}
		return true;
	}
	
	private int doPatch(Hunk hunk, List lines, int shift) {
		int pos= hunk.fOldStart + shift;
		for (int i= 0; i < hunk.fLines.length; i++) {
			String s= hunk.fLines[i];
			Assert.isTrue(s.length() > 0);
			String line= s.substring(1);
			char type= s.charAt(0);
			switch (type) {
			case ' ':	// context lines
				while (true) {
					Assert.isTrue(pos < lines.size(), "3");
					if (linesMatch(line, (String) lines.get(pos))) {
						pos++;
						break;
					}
					pos++;
				}
				break;
			case '-':	// deleted lines
				while (true) {
					Assert.isTrue(pos < lines.size(), "3");
					if (linesMatch(line, (String) lines.get(pos))) {
						break;
					}
					pos++;
				}
				lines.remove(pos);
				break;			
			case '+':	// added lines
				lines.add(pos,  line);
				pos++;
				break;
			}
		}
		return hunk.fNewLength - hunk.fOldLength;
	}

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
	
	/**
	 * Compares two strings.
	 * If fIgnoreWhitespace is true whitespace and line endings are ignored.
	 */
	private boolean linesMatch(String line1, String line2) {
		if (fIgnoreWhitespace)
			return stripWhiteSpace(line1).equals(stripWhiteSpace(line2));
		return line1.equals(line2);
	}
	
	/**
	 * Returns the length (exluding end-of-line characters) of the given string.
	 */
	/* package */ static int length(String s) {
		int l= s.length();
		if (l > 0 && s.charAt(l-1) == '\n')
			l--;
		if (l > 1 && s.charAt(l-2) == '\r')
			l--;
		return l;
	}
}
