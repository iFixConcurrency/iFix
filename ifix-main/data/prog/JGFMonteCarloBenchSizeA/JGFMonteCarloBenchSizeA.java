package JGFMonteCarloBenchSizeA;/**************************************************************************
*                                                                         *
*         Java Grande Forum Benchmark Suite - Thread Version 1.0          *
*                                                                         *
*                            produced by                                  *
*                                                                         *
*                  Java Grande Benchmarking Project                       *
*                                                                         *
*                                at                                       *
*                                                                         *
*                Edinburgh Parallel Computing Centre                      *
*                                                                         * 
*                email: epcc-javagrande@epcc.ed.ac.uk                     *
*                                                                         *
*                                                                         *
*      This version copyright (c) The University of Edinburgh, 2001.      *
*                         All rights reserved.                            *
*                                                                         *
**************************************************************************/


import JGFMonteCarloBenchSizeA.montecarlo.*;

import JGFMonteCarloBenchSizeA.jgfutil.JGFInstrumentor;

public class JGFMonteCarloBenchSizeA{ 

  public static int nthreads;

  public static void main(String argv[]){

  if(argv.length != 0 ) {
    nthreads = Integer.parseInt(argv[0]);
  } else {
//    System.out.println("The no of threads has not been specified, defaulting to 1");
//    System.out.println("  ");
    nthreads = 2;
  }

    JGFInstrumentor.printHeader(3,1,nthreads);

    JGFMonteCarloBench mc = new JGFMonteCarloBench(nthreads); 
    mc.JGFrun(0);
 
  }
}


