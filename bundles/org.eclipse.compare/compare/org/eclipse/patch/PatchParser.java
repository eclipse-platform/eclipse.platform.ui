/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.patch;

import java.io.*;
import java.util.*;
import java.text.*;

import org.eclipse.jface.util.Assert;


/* package */ class PatchParser {
		
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
	
	//---- public methods
	
	/* package */ PatchParser() {
	}
	
	/* package */ Diff[] parse(BufferedReader reader) throws IOException {
		List diffs= new ArrayList();
		String line= null;
		boolean	reread= false;
		
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
			String fileName= null;
			if (line.startsWith("Index: ")) {
				fileName= line.substring(7);
				continue;
			}
			String diffArgs= null;
			if (line.startsWith("diff")) {
				diffArgs= line.substring(4);
				continue;
			}

			if (line.startsWith("--- ")) {
				line= readUnifiedDiff(diffs, lr, line, diffArgs, fileName);
				reread= true;
			} else if (line.startsWith("*** ")) {
				line= readContextDiff(diffs, lr, line, diffArgs, fileName);
				reread= true;
			}
		}
		
		lr.close();
		
		return (Diff[]) diffs.toArray((Diff[]) new Diff[diffs.size()]);
	}
				
	//---- private helpers
	
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
	
		Diff diff= new Diff(extractFileName(oldArgs, 0), extractDate(oldArgs, 1),
				   			extractFileName(newArgs, 0), extractDate(newArgs, 1));
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
								diff.add(new Hunk(oldRange, newRange, lines));
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
					default:
						break;
					}
				}
				return line;
			}
		} finally {
			if (lines.size() > 0)
				diff.add(new Hunk(oldRange, newRange, lines));
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
						
		Diff diff= new Diff(extractFileName(oldArgs, 0), extractDate(oldArgs, 1),
				   			extractFileName(newArgs, 0), extractDate(newArgs, 1));
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
								diff.add(new Hunk(oldRange, newRange, unifyLines(oldLines, newLines)));
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
				diff.add(new Hunk(oldRange, newRange, unifyLines(oldLines, newLines)));
			diff.finish();
		}
	}
	
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
			System.err.println("can't parse date: <" + line + ">");
		}
		return -1;
	}
	
	private String extractFileName(String[] args, int n) {
		if (n < args.length)
			return args[n];
		return "???";
	}
	
	/**
	 * Tries to extract two integers separated by a comma.
	 * The parsing of the line starts at the position after
	 * the first occurrence of the given character start.
	 */
	private void extractPair(String line, char start, int[] pair) {
		pair[0]= pair[1]= -1;
		int startPos= line.indexOf(start);
		if (startPos < 0)
			return;
		line= line.substring(startPos+1);
		int endPos= line.indexOf(' ');
		int comma= line.indexOf(',');
		if (comma >= 0) {
			pair[1]= Integer.parseInt(line.substring(comma+1, endPos));
			endPos= comma;
		}
		pair[0]= Integer.parseInt(line.substring(0, endPos));
	}
}

