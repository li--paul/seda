/*
 * Copyright (c) 2002 by The Regents of the University of California. 
 * All rights reserved.
 *
 * Permission to use, copy, modify, and distribute this software and its
 * documentation for any purpose, without fee, and without written agreement is
 * hereby granted, provided that the above copyright notice and the following
 * two paragraphs appear in all copies of this software.
 *
 * IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY FOR
 * DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING OUT
 * OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF THE UNIVERSITY OF
 * CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
 * AND FITNESS FOR A PARTICULAR PURPOSE.  THE SOFTWARE PROVIDED HEREUNDER IS
 * ON AN "AS IS" BASIS, AND THE UNIVERSITY OF CALIFORNIA HAS NO OBLIGATION TO
 * PROVIDE MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
 *
 * Author: Dennis Chi <denchi@uclink4.berkeley.edu> 
 *
 */

package seda.sandStorm.lib.aTLS;

public class aTLSprofiler {
    int totalmeasurements = 0;
    int stage = 0;
    String stageName = null;
    double sum = 0;
    int count = 0;
    static long baseTime = 0;

//      public aTLSprofiler (String stageName) {
//  	totalmeasurements = 200;
//  	this.stageName = stageName;
//      }
    
//      public aTLSprofiler (String stageName, int total) {
//  	totalmeasurements = total;
//  	this.stageName = stageName;
//      }
    

//      public void addMeasurements (long t1, long t2) {
//          sum += (t2 - t1);
//  	long check = t2-t1;
//  	count++;
//  	if (count == totalmeasurements) {
//  	    double total = (sum * 1.0) / (count * 1.0);
//  	    System.err.println("DENCHI: Time to do work in " + stageName + ": " + total +" ms average");
//  	    count = 0; sum = 0;
//  	}
//      }

    public aTLSprofiler (long baseTime) {
	this.baseTime = baseTime;
    }

    public void addMeasurements (long time, String action) {
	long diff = time - baseTime;
	System.err.println ("DENCHI: " + action + " at " + diff + " ms.");
    }
}
