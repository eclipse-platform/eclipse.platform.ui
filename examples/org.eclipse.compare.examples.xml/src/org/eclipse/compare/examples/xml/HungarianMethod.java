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

/** This algorithm solves the assignment problem.
 * It was translated from Fortran to Java.
 * The original algorithm was taken from http://www.netlib.no/netlib/toms/548
 */
public class HungarianMethod {
    
    public int solve(int[][] A, int[] C) {
	
	final int N = A.length-1;
	final int NP1 = A[0].length-1;
	int[] CH = new int[A.length];
	int[] LC = new int[A.length];
	int[] LR = new int[A.length];
	int[] LZ = new int[A.length];
	int[] NZ = new int[A.length];
	int[] RH = new int[A[0].length];
	int[] SLC = new int[A.length];
	int[] SLR = new int[A.length];
	int[] U = new int[A[0].length];
	int H, Q, R, S, T;
	boolean while100goto120 = false;

//       SUBROUTINE ASSCT ( N, A, C, T )                                         10
//       INTEGER A(130,131), C(130), CH(130), LC(130), LR(130),
//      *        LZ(130), NZ(130), RH(131), SLC(130), SLR(130),
//      *        U(131)
//       INTEGER H, Q, R, S, T
//       EQUIVALENCE (LZ,RH), (NZ,CH)
// C
// C THIS SUBROUTINE SOLVES THE SQUARE ASSIGNMENT PROBLEM
// C THE MEANING OF THE INPUT PARAMETERS IS
// C N = NUMBER OF ROWS AND COLUMNS OF THE COST MATRIX, WITH
// C     THE CURRENT DIMENSIONS THE MAXIMUM VALUE OF N IS 130
// C A(I,J) = ELEMENT IN ROW I AND COLUMN J OF THE COST MATRIX
// C ( AT THE END OF COMPUTATION THE ELEMENTS OF A ARE CHANGED)
// C THE MEANING OF THE OUTPUT PARAMETERS IS
// C C(J) = ROW ASSIGNED TO COLUMN J (J=1,N)
// C T = COST OF THE OPTIMAL ASSIGNMENT
// C ALL PARAMETERS ARE INTEGER
// C THE MEANING OF THE LOCAL VARIABLES IS
// C A(I,J) = ELEMENT OF THE COST MATRIX IF A(I,J) IS POSITIVE,
// C          COLUMN OF THE UNASSIGNED ZERO FOLLOWING IN ROW I
// C          (I=1,N) THE UNASSIGNED ZERO OF COLUMN J (J=1,N)
// C          IF A(I,J) IS NOT POSITIVE
// C A(I,N+1) = COLUMN OF THE FIRST UNASSIGNED ZERO OF ROW I
// C            (I=1,N)
// C CH(I) = COLUMN OF THE NEXT UNEXPLORED AND UNASSIGNED ZERO
// C         OF ROW I (I=1,N)
// C LC(J) = LABEL OF COLUMN J (J=1,N)
// C LR(I) = LABEL OF ROW I (I=1,N)
// C LZ(I) = COLUMN OF THE LAST UNASSIGNED ZERO OF ROW I(I=1,N)
// C NZ(I) = COLUMN OF THE NEXT UNASSIGNED ZERO OF ROW I(I=1,N)
// C RH(I) = UNEXPLORED ROW FOLLOWING THE UNEXPLORED ROW I
// C         (I=1,N)
// C RH(N+1) = FIRST UNEXPLORED ROW
// C SLC(K) = K-TH ELEMENT CONTAINED IN THE SET OF THE LABELLED
// C          COLUMNS
// C SLR(K) = K-TH ELEMENT CONTAINED IN THE SET OF THE LABELLED
// C          ROWS
// C U(I) = UNASSIGNED ROW FOLLOWING THE UNASSIGNED ROW I
// C        (I=1,N)
// C U(N+1) = FIRST UNASSIGNED ROW
// C
// C THE VECTORS C,CH,LC,LR,LZ,NZ,SLC,SLR MUST BE DIMENSIONED
// C AT LEAST AT (N), THE VECTORS RH,U AT  LEAST AT (N+1),
// C THE MATRIX A AT LEAST AT (N,N+1)
// C
// C INITIALIZATION

	int KSLC = -1;
	int L = -1;
	int I = -1;
	int M = -1;
	int J = -1;
	int K = -1;
	int LJ = -1;
	int LM = -1;
	int KSLR = -1;
	int NL = -1;
	int NM = -1;

	//N = N-1;
	//NP1 = NP1-1;
	//      NP1 = N+1
	
	for (J=1; J<=N; J++) {
	    //      DO 10 J=1,N
	    C[J] = 0;
	    LZ[J] = 0;
	    NZ[J] = 0;
	    U[J] = 0;
	}//for (int J=0; J<N; J++)
	//10 CONTINUE
	U[NP1] = 0;
	T = 0;
	//C REDUCTION OF THE INITIAL COST MATRIX
	
	for (J=1; J<=N; J++) {
	    //      DO 40 J=1,N
	    S = A[1][J];
	    for (L=2; L<=N; L++) {
		//DO 20 L=2,N
		if (A[L][J] < S) S = A[L][J];
		//IF ( A(L,J) .LT. S ) S = A(L,J)
	    }//for (int L=1; L<N; L++)
	    //20   CONTINUE
	    T = T+S;
	    for (I=1; I<=N; I++) {
		//DO 30 I=1,N
		A[I][J] = A[I][J]-S;
	    }//for (int I=0; I<N; I++)
	    //30   CONTINUE
	    
	}//for (int J=0; J<N; J++)
	//   40 CONTINUE
	for (I=1; I<=N; I++) {
	    //DO 70 I=1,N
	    Q = A[I][1];
	    for (L=2; L<=N; L++) {
		//DO 50 L=2,N
		//System.out.println("I="+I+"  L="+L);
		if (A[I][L] < Q) Q = A[I][L];
		//IF ( A(I,L) .LT. Q ) Q = A(I,L)
	    }//for (int L=1; L<N; L++)
	    //50   CONTINUE
	    T = T+Q;
	    L = NP1;
	    for (J=1; J<=N; J++) {
		//DO 60 J=1,N
		A[I][J] = A[I][J]-Q;
		if (A[I][J] != 0) continue;
		//IF ( A[I,J] .NE. 0 ) GO TO 60
		A[I][L] = -J;
		L = J;
	    }//for (int J=0; J<N; J++)
	    //60   CONTINUE
	}//for (int I=0; I<N; N++)
	//70 CONTINUE
	//C CHOICE OF THE INITIAL SOLUTION
	
	
	K = NP1;
	for140:
	for (I=1; I<=N; I++) {
	    //      DO 140 I=1,N
	    LJ = NP1;
	    J = -A[I][NP1];
	    
	    do {
		if (C[J] == 0) {
		    //	80   IF ( C(J) .EQ. 0 ) { GO TO 130
		    
		    C[J] = I;
		    A[I][LJ] = A[I][J];
		    NZ[I] = -A[I][J];
		    LZ[I] = LJ;
		    A[I][J] = 0;
		    continue for140; //break??
		}
		
		LJ = J;
		J = -A[I][J];
		
	    } while (J != 0);
	    //        IF ( J .NE. 0 ) GO TO 80
	    LJ = NP1;
	    J = -A[I][NP1];
	    
		do90:
	    do {
		R = C[J];
		LM = LZ[R];
		M = NZ[R];
		
		while100goto120 = false;
		while100:
		while (true) {
		    if (M == 0) break while100;
		    //  100   IF ( M .EQ. 0 ) GO TO 110
		    if (C[M] == 0){
		    	while100goto120 = true;	
		    	break do90;
		    }
		    //  IF ( C(M) .EQ. 0 ) GO TO 120// M != 0 && C[m] == 0
		    LM = M;
		    M = -A[R][M];
		}
		//      GO TO 100
		
		//110
			LJ = J;
			J = -A[I][J];
	    } while (J != 0);
	    //        IF ( J .NE. 0 ) GO TO 90
	    
	    if ( !while100goto120 ) {
		U[K] = I;
		K = I;
		continue for140;
		//	    GO TO 140
	    }
	    //	120
	   	while100goto120 = false;
	    NZ[R] = -A[R][M];
	    LZ[R] = J;
	    A[R][LM] = -J;
	    A[R][J] = A[R][M];
	    A[R][M] = 0;
	    C[M] = R;
	    
	    //130
	    C[J] = I;
	    A[I][LJ] = A[I][J];
	    NZ[I] = -A[I][J];
	    LZ[I] = LJ;
	    A[I][J] = 0;
	    
	    
	    //  140 CONTINUE
	}
	//C RESEARCH OF A NEW ASSIGNMENT
	while392:
	while (true) {
	    if (U[NP1] == 0) return T;
	    //  150 IF ( U(NP1) .EQ. 0 ) RETURN
	    
	    for (I=1; I<=N; I++) {
		//      DO 160 I=1,N
		CH[I] = 0;
		LC[I] = 0;
		LR[I] = 0;
		RH[I] = 0;
	    }
	    //  160 CONTINUE
	    RH[NP1] = -1;
	    KSLC = 0;
	    KSLR = 1;
	    R = U[NP1];
	    //System.out.println("R: "+R);
	    LR[R] = -1;
	    SLR[1] = R;
	    
	    boolean goto190 = false;
	    while360:
	    while(true) {
		do350:
		do {
		    //System.out.println("R: "+R+"  NP1: "+NP1);
			if (goto190 || A[R][NP1] != 0) {
			    //      IF ( A(R,NP1) .EQ. 0 ) GO TO 220
		    
		    
		    do200:
		    do {
			if (!goto190) {
			    //  170
			    L = -A[R][NP1];
			    
			    if (A[R][L] != 0) {
				//      IF ( A(R,L) .EQ. 0 ) GO TO 180
				if (RH[R] == 0) {
				    //  IF ( RH(R) .NE. 0 ) GO TO 180
				    RH[R] = RH[NP1];
				    CH[R] = -A[R][L];
				    RH[NP1] = R;
				}
			    }
			}// if (!goto190)
			//boolean goto210 = false;
			while190:
			while (true) {
			    if (!goto190) {
				if (LC[L] ==0) break while190;
				//180 IF ( LC(L) .EQ. 0 ) GO TO 200
				
				if (RH[R] == 0) {
				    break do200;
				    // goto210 = true;
				    //break;
				}
				//      IF ( RH(R) .EQ. 0 ) GO TO 210
			    }// if (!goto190)
			    goto190 = false;
			    //  190
			    L = CH[R];
			    CH[R] = -A[R][L];
			    if (A[R][L] != 0) continue while190;
			    //IF ( A(R,L) .NE. 0 ) GO TO 180
			    RH[NP1] = RH[R];
			    RH[R] = 0;
			    
			    //GO TO 180
			    
			}//end while190
			
			//if (!goto210) {
			//200
			LC[L] = R;
			
			if (C[L] == 0) break while360;
			//IF ( C(L) .EQ. 0 ) GO TO 360
			
			KSLC = KSLC+1;
			SLC[KSLC] = L;
			R = C[L];
			LR[R] = L;
			KSLR = KSLR+1;
			SLR[KSLR] = R;
			//}
		    } while (A[R][NP1] != 0); //do200
		    //      IF ( A(R,NP1) .NE. 0 ) GO TO 170
		    //}// if (!goto210)
		    //    goto210 = false;
		    //  210 CONTINUE
		    
		    if (RH[NP1] > 0) break do350;
		    //IF ( RH(NP1) .GT. 0 ) GO TO 350
			
			}// if (A[R,L] != 0)
		//C REDUCTION OF THE CURRENT COST MATRIX
		//  220
		H = Integer.MAX_VALUE;
		
		for240:
		for (J=1; J<=N; J++) {
		    //      DO 240 J=1,N
		    
		    if (LC[J] != 0) continue for240;
		    //        IF ( LC(J) .NE. 0 ) GO TO 240
		    
		    for (K=1; K<=KSLR; K++) {
			//        DO 230 K=1,KSLR
			I = SLR[K];
			if (A[I][J] < H) H = A[I][J];
			//          IF ( A(I,J) .LT. H ) H = A(I,J)
		    }// for (int K=0; K<KSLR; K++)
		    //  230   CONTINUE
		    
		}// for (int J; J<N; J++)
		//  240 CONTINUE
		
		T = T+H;
		
		for290:
		for (J=1; J<=N; J++) {
		    //      DO 290 J=1,N
		    
		    if (LC[J] != 0 ) continue for290;
		    //        IF ( LC(J) .NE. 0 ) GO TO 290
		    
		    for280:
		    for (K=1; K<=KSLR; K++) {
			//        DO 280 K=1,KSLR
			I = SLR[K];
			A[I][J] = A[I][J]-H;
			if (A[I][J] != 0) continue for280;
			//          IF ( A(I,J) .NE. 0 ) GO TO 280
			
			if (RH[I] == 0) {
			    //          IF ( RH(I) .NE. 0 ) GO TO 250
			    RH[I] = RH[NP1];
			    CH[I] = J;
			    RH[NP1] = I;
			}// if (RH[I] == 0)
			//250
			L = NP1; 
			//260
			while (true) {
			    NL = -A[I][L];
			    if (NL == 0) break;
			    //          IF ( NL .EQ. 0 ) GO TO 270
			    L = NL;
			}// while (true)
			//          GO TO 260
			//270
			A[I][L] = -J;
			
		    }// for (int K=0; K<KSLR; K++)
		    //  280   CONTINUE
		    
		}// for (int J=0; J<N; J++)
		//  290 CONTINUE
		
		if (KSLC != 0) {
		    //IF ( KSLC .EQ. 0 ) GO TO 350
		    
		    for340:
		    for (I=1; I<=N; I++) {
			//      DO 340 I=1,N
			
			if (LR[I] != 0) continue for340;
			//IF ( LR(I) .NE. 0 ) GO TO 340
			
			for330:
			for (K=1; K<=KSLC; K++) {
			    //DO 330 K=1,KSLC
			    J = SLC[K];
			    
			    boolean enter_if = false;
				if (A[I][J] <= 0) {
				    //IF ( A(I,J) .GT. 0 ) GO TO 320
				    L = NP1;
				    //300
				    while (true) {
					NL = - A[I][L];
					if (NL == J) break;
					//IF ( NL .EQ. J ) GO TO 310
					L = NL;
				    }//while (true)
				    //GO TO 300
				    //310
				    A[I][L] = A[I][J];
				    A[I][J] = H;
				    //GO TO 330
				    enter_if = true;
				}//if (A[I,J] <=0)
			    if (!enter_if) {
				//320
				A[I][J] = A[I][J]+H;
			    }
			}//for (int K=0; K<KSLC; K++)
			//  330   CONTINUE
		    }//for (int I=0; I<N; I++)
		    //  340 CONTINUE
		}// if (KSLC != 0)
		
		} while (false);//do350
		//350
		R = RH[NP1];
	    //System.out.println("R: "+R+"  at 350");
		goto190 = true;
	    }//while360
	    //      GO TO 190
	    //C ASSIGNMENT OF A NEW ROW
		while389:
	    while (true) {
		//360
		C[L] = R;
		M = NP1;
		//370
		while (true) {
		    NM = -A[R][M];
		    if (NM == L) break;
		    //      IF ( NM .EQ. L ) GO TO 380
		    M = NM;
		}//while (true)
		//     GO TO 370
		//380
		A[R][M] = A[R][L];
		A[R][L] = 0;
		if (LR[R] < 0) break while389;
		//      IF ( LR(R) .LT. 0 ) GO TO 390
		L = LR[R];
		A[R][L] = A[R][NP1];
		A[R][NP1] = -L;
		R = LC[L];
	    }//while389
	    //      GO TO 360
	    //390
	    U[NP1] = U[R];
	    U[R] = 0;
	}
	//      GO TO 150
	//END
    }

}
