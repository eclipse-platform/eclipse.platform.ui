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

import junit.framework.*;
import org.eclipse.compare.examples.xml.HungarianMethod;

public class TestMinCostBipartiteMatching extends TestCase {

	int[][] fA; //matrix that represents instance of matching problem
	int[] fC; //solution returned by HungarianMethod
	int fT; //cost of solution returned by HungarianMethod
	int[] fC2; //actual solution of matching
	int fT2; //actual cost of matching
	HungarianMethod fH;

	public TestMinCostBipartiteMatching(String name) {
		super(name);
	}
	public static void main(String[] args) {
		//junit.textui.TestRunner.run (suite());
		//TestRunner.run(suite());
	}

	protected void setUp() {
		System.out.println("TestMinCostBipartiteMatching.name()==" + getName()); //$NON-NLS-1$
		fH= new HungarianMethod();
	}

	protected void tearDown() throws Exception {
		//remove set-up
	}

	public static Test suite() {
		return new TestSuite(TestMinCostBipartiteMatching.class);
	}

	public void test0() {
		fA= new int[][] { { 0, 0, 0, 0, 0, 0, 0 }, {
				0, 7, 2, 1, 9, 4, 0 }, {
				0, 9, 6, 9, 5, 5, 0 }, {
				0, 3, 8, 3, 1, 8, 0 }, {
				0, 7, 9, 4, 2, 2, 0 }, {
				0, 8, 4, 7, 4, 8, 0 }
		};
		fC= new int[6];
		;

		fT2= 15;
		//optimal matching: {(1,3), (2,5), (3,1), (4,4), (5,2)}
		fC2= new int[] { 0, 3, 5, 1, 4, 2 };

		fT= fH.solve(fA, fC);

		for (int J= 1; J <= 5; J++) {
			assertTrue(fC[J] == fC2[J]);
		}
		assertTrue(fT == fT2);
	}

	public void test1() {
		/* checks case where number of vertices on the two sides are not equal
		 * and dummy vertices (here, 2nd right vertice (see 3rd column)) are introduced
		 * with dummy cost
		 */
		fA= new int[][] { { 0, 0, 0, 0 }, {
				0, 2, 3, 0 }, {
				0, 0, 3, 0 }
		};
		fC= new int[3];

		fT2= 3;
		//optimal matching: {(1,2), (2,1)}
		fC2= new int[] { 0, 2, 1 };

		fT= fH.solve(fA, fC);

		for (int J= 1; J < fC.length; J++) {
			assertTrue(fC[J] == fC2[J]);
		}
		assertTrue(fT == fT2);
	}

	public void test2() {
		fA= new int[][] { { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, {
				0,
					1542,
					3533,
					2787,
					1891,
					3833,
					3558,
					1173,
					2187,
					3307,
					2836,
					3792,
					2106,
					1444,
					1924,
					0 },
					{
				0,
					1510,
					3766,
					3186,
					1815,
					4931,
					3221,
					1478,
					2107,
					3344,
					2830,
					4816,
					2359,
					1223,
					1936,
					0 },
					{
				0,
					1160,
					3901,
					2100,
					1545,
					4484,
					3326,
					1355,
					1824,
					3088,
					2563,
					3627,
					2197,
					1354,
					1689,
					0 },
					{
				0,
					1203,
					4049,
					2295,
					1586,
					3556,
					4009,
					1110,
					2282,
					3990,
					2692,
					3751,
					2399,
					1691,
					1786,
					0 },
					{
				0,
					1426,
					3163,
					2242,
					1659,
					4617,
					3240,
					1712,
					1987,
					3637,
					3037,
					4471,
					2166,
					1356,
					1878,
					0 },
					{
				0,
					1172,
					3912,
					1951,
					1469,
					4272,
					3239,
					1546,
					1924,
					3560,
					2513,
					4694,
					2127,
					1951,
					1693,
					0 },
					{
				0,
					1647,
					3889,
					2097,
					1646,
					3749,
					3656,
					970,
					1957,
					3373,
					2678,
					3711,
					1788,
					1279,
					1752,
					0 },
					{
				0,
					1219,
					3754,
					2348,
					1686,
					4297,
					3677,
					1364,
					1995,
					4133,
					2888,
					3643,
					1993,
					1481,
					1880,
					0 },
					{
				0,
					1637,
					3895,
					2165,
					1575,
					4512,
					3903,
					1499,
					1935,
					2760,
					3151,
					3162,
					2306,
					1493,
					1710,
					0 },
					{
				0,
					1391,
					3992,
					1942,
					1846,
					4450,
					3211,
					1626,
					1952,
					3495,
					2951,
					4541,
					2014,
					1639,
					1932,
					0 },
					{
				0,
					1282,
					4292,
					3048,
					2074,
					4458,
					3460,
					1300,
					1952,
					3495,
					2951,
					4541,
					2014,
					1639,
					1932,
					0 },
					{
				0,
					1598,
					3721,
					2457,
					1880,
					4073,
					3164,
					1829,
					1952,
					3495,
					2951,
					4541,
					2014,
					1639,
					1932,
					0 },
					{
				0,
					1384,
					1742,
					2447,
					1858,
					4367,
					3189,
					1774,
					1699,
					3040,
					2499,
					3911,
					2203,
					1433,
					1676,
					0 },
					{
				0,
					1474,
					3815,
					2214,
					1997,
					4515,
					3202,
					1352,
					1942,
					3274,
					2502,
					5138,
					2395,
					1767,
					2136,
					0 }
		};

		fT2= 29858;
		//optimal matching: {(8,1) (13,2) (10,3) (6,4) (4,5) (12,6) (1,7) (11,8) (3,9) (14,10) (9,11) (7,12) (2,13) (5,14)}
		fC2= new int[] { 0, 8, 13, 10, 6, 4, 12, 1, 11, 3, 14, 9, 7, 2, 5 };

		fC= new int[15];

		fT= fH.solve(fA, fC);

		//		for (int J=1; J<fC.length; J++) {
		//		    System.out.print("("+fC[J]+","+J+") ");
		//		}
		//		System.out.println();
		//		System.out.println("Cost: "+fT);

		for (int J= 1; J < fC.length; J++) {
			assertTrue(fC[J] == fC2[J]);
		}
		assertTrue(fT == fT2);
	}
}
