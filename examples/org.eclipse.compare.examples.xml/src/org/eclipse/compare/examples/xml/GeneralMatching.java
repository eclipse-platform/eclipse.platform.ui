/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.compare.examples.xml;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Vector;

import org.eclipse.core.runtime.IProgressMonitor;

/** This class is used to find a mapping between the nodes of two xml parse trees
 *  When an identifier for a specific node is known, it will be used.
 *  Otherwise a min-cost bipartite matching must be solved.
 *  This algorithm uses the algorithm described in the paper
 *  "X-Diff: A Fast Change Detection Algorithm for XML Documents"
 */
public class GeneralMatching extends AbstractMatching {

	HungarianMethod fH;

	boolean fUseOrdered;
	String[] fOrdered;
	boolean fIgnoreStartsWith;

	public GeneralMatching() {
		fOrdered= null;
		fUseOrdered= false;
		fIgnoreStartsWith= false;
	}

	public GeneralMatching(ArrayList ordered) {
		if (ordered != null && !ordered.isEmpty()) {
			fUseOrdered= true;
			fOrdered= new String[ordered.size()];
			int i=0;
			for (Iterator iter= ordered.iterator(); iter.hasNext(); i++) {
				fOrdered[i]= (String) iter.next();
			}
		} else {
			fUseOrdered= false;
			fOrdered= null;
		}
		//fOrderedElements= XMLPlugin.getDefault().getOrderedElements();
	}

	//x and y have children xc_orig and yc_orig, respectively
	protected int unorderedMatch(XMLNode x, XMLNode y, Object[] xc_orig, Object[] yc_orig) {
		ArrayList DTMatching = new ArrayList();//Mathing Entry in fDT_Matchings
		int distance = 0;//distance
		
		Vector xc_vect = new Vector();
		Vector yc_vect = new Vector();
		for (int i=0; i<xc_orig.length; i++) {
			if ( ((XMLNode)xc_orig[i]).usesIDMAP() ) {
				int j;
				for (j=0; j<yc_orig.length && !((XMLNode)yc_orig[j]).getOrigId().equals(((XMLNode)xc_orig[i]).getOrigId()); j++);
				if ( j<yc_orig.length ) {
					/* not calculating their distance and adding it to variable "distance" to save time,
					 * as matching of subtrees is not performed.
					 * but better result might be achieved if this were done.
					 */
					//int d= dist( (XMLNode)xc_orig[i], (XMLNode)yc_orig[j] );
					//distance += d;
					//fDT[indexOfLN((XMLNode)xc_orig[i])][indexOfRN((XMLNode)yc_orig[j])]= d;
					DTMatching.add(new Match( (XMLNode)xc_orig[i], (XMLNode)yc_orig[j] ));
				}
			} else
				xc_vect.add(xc_orig[i]);
		}
		XMLNode[] xc= (XMLNode[]) xc_vect.toArray(new XMLNode[xc_vect.size()]);
		for (int j=0; j<yc_orig.length; j++) {
			if ( !((XMLNode)yc_orig[j]).usesIDMAP() )
				yc_vect.add(yc_orig[j]);
		}
		XMLNode[] yc= (XMLNode[]) yc_vect.toArray(new XMLNode[yc_vect.size()]);
		if ( xc.length == 0 || yc.length == 0) {
			if (xc.length == 0) {
				for (int j=0; j<yc.length; j++) {
					distance += countNodes((XMLNode)yc[j]);
				}
			} else {//yc.length == 0
				for (int i=0; i<xc.length; i++) {
					distance += countNodes((XMLNode)xc[i]);
				}
			}
		} else {
			for (int i=0; i<xc.length; i++) {
				for (int j=0; j<yc.length; j++) {
					if (fDT[indexOfLN( xc[i] )][indexOfRN( yc[j] )] == NO_ENTRY)
						dist(xc[i], yc[j]);
				}
			}
		
			/* look for Wmin (p.11)
			 * xc and yc are the two partitions that have to be mapped.
			 * But, they may not have same number of nodes
			 * HungarianMethod.java solves weighted matching only on complete bipatite graphs
			 * We must add nodes and edges to make graph complete
			 */
			final int array_size = (xc.length > yc.length)?xc.length:yc.length;
			final int array_rowsize = array_size+1;
			final int array_colsize = array_size+2;
			int[][] A = new int[array_rowsize][array_colsize];
			for (int j=0; j<array_colsize; j++) {
				A[0][j] = 0;
			}
			/* now: A[0] = new int[] {0,0,0, ... ,0}. This first row is not used by HungarianMethod
			 * (Fortran77 counts Array index from 1)
			 */
			for (int i=1; i<array_rowsize; i++) {
				A[i][0] = 0;
				for (int j=1; j<array_colsize-1; j++) {
					A[i][j] = -1;
				}
				A[i][array_colsize-1] = 0;
			}
			/* now A = 0, 0, 0, ...  0,0
			 *         0,-1,-1, ... -1,0
			 *         0,-1,-1, ... -1,0
			 *         ...
			 *         0,-1,-1, ... -1,0
			 */
			for (int i_xc = 0; i_xc < xc.length; i_xc++) {
				for (int i_yc = 0; i_yc < yc.length; i_yc++) {
					A[i_xc+1][i_yc+1] = fDT[indexOfLN( xc[i_xc] )][indexOfRN( yc[i_yc] )];
				}
			}
			int dummyCost=0;
			/* cost of dummy nodes not associated with a node in Tree, but needed
			 * to have a complete bipartite graph
			 */
		
			//set dummyCost to larger than any cost in A
			if (xc.length > yc.length) {
				for (int i=1; i<array_rowsize; i++) {
					for (int j=1; j<=yc.length; j++)
						if (A[i][j] > dummyCost) dummyCost = A[i][j];
				}
			} else if (xc.length < yc.length) {
				for (int i=1; i<=xc.length; i++) {
					for (int j=1; j<array_colsize-1; j++)
						if (A[i][j] > dummyCost) dummyCost = A[i][j];
				}
			} else {//xc.length == yc.length
				dummyCost = Integer.MAX_VALUE-1;
			}
			dummyCost += 1;
		
			if (xc.length > yc.length) {
				for (int i=1; i<array_rowsize; i++) {
					for (int j=yc.length+1; j<array_colsize-1; j++) {
						A[i][j] = dummyCost;
					}
				}
			} else if (xc.length < yc.length) {
				for (int j=1; j<array_colsize-1; j++) {
					for (int i=xc.length+1; i<array_rowsize; i++) {
						A[i][j] = dummyCost;
					}
				}
			}
		
			//A is built. Now perform matching
			int[] Matching = new int[array_rowsize];
			int[][] A2 = new int[array_rowsize][array_colsize];
			for (int i=0; i<array_rowsize; i++) {
				for (int j=0; j<array_colsize; j++)
					A2[i][j] = A[i][j];
			}
			fH.solve( A2,Matching);
			//now Matching contains the min-cost matching of A
		
			for (int m=1; m<Matching.length; m++) {
				if (A[Matching[m]][m] == dummyCost) {
					if (xc.length > yc.length) {
						distance += countNodes( xc[Matching[m]-1] );
						//added here
						DTMatching.add(new Match( (XMLNode)xc[Matching[m]-1] , null));
					} else if (xc.length < yc.length) {
						distance += countNodes( yc[m-1] );
						//added here
						DTMatching.add(new Match( null , yc[m-1]));
					}
				} else {
						int index_x = indexOfLN( xc[Matching[m]-1] );
						int index_y = indexOfRN( yc[m-1]);
						distance += fDT[ index_x ][ index_y ];
						if ( (xc[Matching[m]-1]).getSignature().equals( (yc[m-1]).getSignature() ))
							DTMatching.add(new Match( xc[Matching[m]-1] , yc[m-1] ));
						else {
							DTMatching.add(new Match( xc[Matching[m]-1] , null));
							DTMatching.add(new Match( null , yc[m-1] ));
						}
				}
			}
		}
		fDT[indexOfLN(x)][indexOfRN(y)] = distance;
		fDT_Matchings[indexOfLN(x)][indexOfRN(y)] = DTMatching;
		return distance;
	}
	
	
	protected int orderedMath(XMLNode x, XMLNode y) {
		//assumes x and y have children

		boolean old_isw= fIgnoreStartsWith;
		fIgnoreStartsWith= true;
		
		//both x and y have children
		Object[] xc = x.getChildren();
		Object[] yc = y.getChildren();
		
		ArrayList xc_elementsAL= new ArrayList();
		ArrayList xc_attrsAL= new ArrayList();

		ArrayList yc_elementsAL= new ArrayList();
		ArrayList yc_attrsAL= new ArrayList();
		
		//find attributes and elements and put them in xc_elementsAL and xc_attrsAL, respectively
		for (int i= 0; i < xc.length; i++) {
			XMLNode x_i= (XMLNode) xc[i];
			if (x_i.getXMLType().equals(XMLStructureCreator.TYPE_ELEMENT)) {
				xc_elementsAL.add(x_i);
			} else if (x_i.getXMLType().equals(XMLStructureCreator.TYPE_ATTRIBUTE)) {
				xc_attrsAL.add(x_i);
			}
		}

		//do the same for yc				
		for (int i= 0; i < yc.length; i++) {
		XMLNode y_i= (XMLNode) yc[i];
			if (y_i.getXMLType().equals(XMLStructureCreator.TYPE_ELEMENT)) {
				yc_elementsAL.add(y_i);
			} else if (y_i.getXMLType().equals(XMLStructureCreator.TYPE_ATTRIBUTE)) {
				yc_attrsAL.add(y_i);
			}
		}
		
		Object[] xc_elements= xc_elementsAL.toArray();
		Object[] yc_elements= yc_elementsAL.toArray();
		Object[] xc_attrs= xc_attrsAL.toArray();
		Object[] yc_attrs= yc_attrsAL.toArray();

		ArrayList DTMatching= null;//Mathing to be added to Entry in fDT_Matchings
		int distance = 0;//distance to be added to entry in fDT

		//perform unordered matching on attributes
		//this updates fDT and fDT_Matchings
		if (xc_attrs.length > 0 || yc_attrs.length > 0) {
			if (xc_attrs.length == 0)
				distance += yc_attrs.length;
			else if (yc_attrs.length == 0)
				distance += xc_attrs.length;
			else {
				unorderedMatch(x, y, xc_attrs, yc_attrs);
				distance += fDT[indexOfLN(x)][indexOfRN(y)];
				DTMatching= fDT_Matchings[indexOfLN(x)][indexOfRN(y)];
			}
		}
		if (DTMatching == null)
			DTMatching= new ArrayList();
		//perform ordered matching on element children, i.e. number them in order of appearance
		
		/* start new */
		distance= handleRangeDifferencer(xc_elements, yc_elements, DTMatching, distance);
		/* end new */
					
		/* start: Naive ordered compare /*
//			int minlength= (xc_elements.length > yc_elements.length)?yc_elements.length:xc_elements.length;
//			for (int i= 0; i < minlength; i++) {
//				distance += dist((XMLNode) xc_elements[i], (XMLNode) yc_elements[i]);
//				DTMatching.add(new Match( (XMLNode)xc_elements[i], (XMLNode)yc_elements[i]));
//			}
//			if (xc_elements.length > yc_elements.length) {
//				for (int i= minlength; i < xc_elements.length; i++) {
//					distance += countNodes((XMLNode) xc_elements[i]);
//				}
//			} else if (xc_elements.length < yc_elements.length) {
//				for (int i= minlength; i < yc_elements.length; i++) {
//					distance += countNodes((XMLNode) yc_elements[i]);
//				}
//			}
		/* end: Naive ordered compare */
		
		fIgnoreStartsWith= old_isw;
		
		fDT[indexOfLN(x)][indexOfRN(y)] = distance;
		fDT_Matchings[indexOfLN(x)][indexOfRN(y)] = DTMatching;
		return distance;

	}

	
	
	/* matches two trees according to paper "X-Diff", p. 16 */
	public void match(XMLNode LeftTree, XMLNode RightTree, boolean rightTreeIsAncestor, IProgressMonitor monitor) throws InterruptedException {

		//if (monitor != null) monitor.beginTask("",10);
		fH = new HungarianMethod();
		fNLeft = new Vector();//numbering LeftTree: Mapping nodes in LeftTree to numbers to be used as array indexes
		fNRight = new Vector();//numbering RightTree: Mapping nodes in RightTree to numbers to be used as array indexes
		numberNodes(LeftTree, fNLeft);
		numberNodes(RightTree, fNRight);
		fDT = new int[fNLeft.size()][fNRight.size()];
		fDT_Matchings = new ArrayList[fNLeft.size()][fNRight.size()];
		for (int i=0; i<fDT.length; i++) {
			fDT[i] = new int[fNRight.size()];
			for (int j=0; j<fDT[0].length; j++) {
				fDT[i][j] = NO_ENTRY;
			}
		}
		
		ArrayList NLeft = new ArrayList();
		ArrayList NRight = new ArrayList();
		findLeaves(LeftTree, NLeft);
		findLeaves(RightTree, NRight);
		
		/* Matching Algorithm */
		/* Step 1: Compute editing distance for (LeftTree -> RightTree)*/
		while (!NLeft.isEmpty() || !NRight.isEmpty()) {
			for (ListIterator itNLeft=NLeft.listIterator(); itNLeft.hasNext(); ) {
				XMLNode x = (XMLNode) itNLeft.next();
				for (ListIterator itNRight=NRight.listIterator(); itNRight.hasNext(); ) {
					XMLNode y = (XMLNode) itNRight.next();
					if ( x.getSignature().equals(y.getSignature()) ) {
//						System.out.println("x: "+x.getName());
//						System.out.println("y: "+y.getName());
						if (monitor != null && monitor.isCanceled()) {
//							throw new OperationCanceledException();
							throw new InterruptedException();
//							return;
						}

						//if signature starts with root>project
						//do not calculate dist
						
						//if signature is root>project
						//do ordered search on children
						//do unordered search on childrenb
						
						dist(x,y);
					}
				}
			}
			ArrayList NLeft_new = new ArrayList();
			ArrayList NRight_new = new ArrayList();
			for (ListIterator itNLeft=NLeft.listIterator(); itNLeft.hasNext(); ) {
				XMLNode node = (XMLNode) itNLeft.next();
				if ( node.getParent() != null && !NLeft_new.contains(node.getParent()) )
					NLeft_new.add(node.getParent());
			}
			for (ListIterator itNRight=NRight.listIterator(); itNRight.hasNext(); ) {
				XMLNode node = (XMLNode) itNRight.next();
				if ( node.getParent() != null && !NRight_new.contains(node.getParent()) )
					NRight_new.add(node.getParent());
			}
			NLeft = NLeft_new;
			NRight = NRight_new;
		}
		//end of Step1
		/* Step 2: mark matchings on LeftTree and RightTree */
		fMatches = new Vector();
		if ( !LeftTree.getSignature().equals(RightTree.getSignature()) ) {
			//matching is empty	
		} else {
			fMatches.add(new Match(LeftTree,RightTree));
			for (int i_M = 0; i_M<fMatches.size(); i_M++) {
				Match m = (Match) fMatches.elementAt(i_M);
				if ( !isLeaf(m.fx) && !isLeaf(m.fy) ) {
//					if (fDT_Matchings[ indexOfLN(m.fx) ][ indexOfRN(m.fy) ] == null)
//						System.out.println("Error: ID not unique for " + m.fx.getId());
//					else
//						fMatches.addAll(fDT_Matchings[ indexOfLN(m.fx) ][ indexOfRN(m.fy) ]);
					if (fDT_Matchings[ indexOfLN(m.fx) ][ indexOfRN(m.fy) ] != null)
						fMatches.addAll(fDT_Matchings[ indexOfLN(m.fx) ][ indexOfRN(m.fy) ]);
				}
			}
		}
		//end of Step2
		/* Renumber Id of Nodes to follow Matches. Or for ancestor, copy over Id to ancestor */
		if (rightTreeIsAncestor) {
			for (ListIterator it_M = fMatches.listIterator(); it_M.hasNext(); ) {
				Match m = (Match) it_M.next();
				if (m.fx != null && m.fy != null)
					m.fy.setId(m.fx.getId());
			}
		} else {
			int newId = 0;
			for (ListIterator it_M = fMatches.listIterator(); it_M.hasNext(); newId++) {
				Match m = (Match) it_M.next();
				if (m.fx != null)
					m.fx.setId(Integer.toString(newId));
				if (m.fy != null)
					m.fy.setId(Integer.toString(newId));
//				System.out.println("Matching: "+ ((m.fx != null)?m.fx.getOrigId():"null")+" -> "+((m.fx != null)?m.fx.getId():"null")+" , "+((m.fy != null)?m.fy.getOrigId():"null")+" -> "+((m.fy != null)?m.fy.getId():"null")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			}
		}
		//if (monitor != null) monitor.done();
	}

	protected int handleXandYnotLeaves(XMLNode x, XMLNode y) {
		int ret= NO_ENTRY;
		Object[] xc_orig = x.getChildren();
		Object[] yc_orig = y.getChildren();

		/* handle ordered entries */
		if (fUseOrdered) {
			boolean starts_with_sig= false;
			boolean equals_sig= false;
			String x_sig= x.getSignature();
			String y_sig= y.getSignature();
			
			int i_ordered;
			
			if (!fIgnoreStartsWith) {
				/* Normal case when algorithm runs.
				 * Algorithm runs bottom up from leaves to root.
				 * check x_sig.startsWith(fOrdered[i_ordered]) || y_sig.startsWith(fOrdered[i_ordered])
				 * because if this is the case and
				 * !(x_sig.equals(fOrdered[j_ordered]+SIGN_ELEMENT) && y_sig.equals(fOrdered[j_ordered]+SIGN_ELEMENT))
				 * i.e. the nodes are not marked for an ordered compare but x and/or y has an ancestor that is,
				 * then nodes x and/or y will be handled by that ancestor in orderedMatch(), which is a top-down algorithm.
				 * Thus, we exit the procedure dist() if this is the case.
				 */
				for (i_ordered= 0; i_ordered < fOrdered.length; i_ordered++) {
					if (x_sig.startsWith(fOrdered[i_ordered]) || y_sig.startsWith(fOrdered[i_ordered])) {
						starts_with_sig= true;
						if (x_sig.equals(y_sig)) {
							for (int j_ordered=i_ordered ; j_ordered<fOrdered.length; j_ordered++) {
								if (x_sig.equals(fOrdered[j_ordered]+SIGN_ELEMENT)) {
									equals_sig= true;
									break;
								}
								break;
							}
						}
					}
				}

				if (starts_with_sig) {
					if (equals_sig) {
						return orderedMath(x, y);
					} else {
						return ret;
					}
				}

			} else {
				/* when inside orderedMatch(x, y), algorithm runs recursively from a node to the leaves of the
				 * subtree rooted at this node.
				 * In this case we do not check x_sig.startsWith(fOrdered[i_ordered]) || y_sig.startsWith(fOrdered[i_ordered])
				 */
				if (x_sig.equals(y_sig)) {
					for (i_ordered= 0; i_ordered < fOrdered.length; i_ordered++) {
							if (x_sig.equals(fOrdered[i_ordered]+SIGN_ELEMENT)) {
								equals_sig= true;
								break;
							}
					}
				}

				if (equals_sig) {
					return orderedMath(x, y);
				}
			}
			
		}
		/* end of handle ordered entries */


		return unorderedMatch(x, y, xc_orig, yc_orig);
	}
	
}

