/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package hashcodetest;

public class HashCodeTest {

    public static void main(String[] args) {
        HashCodeTest h = new HashCodeTest();
        try {
            h.testHashCode();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    final static int TEST_LENGTH = 1;

    //final static int TEST_NUMBER = 5;

    final static int NUM_OF_THREADS = 10;

    public void testHashCode() throws InterruptedException {
        IntRange rangeArrayA[] = new IntRange[TEST_LENGTH];
        for (int i = 0; i < TEST_LENGTH; i++) {
            rangeArrayA[i] = new IntRange(i); //rangeArrayA[10] = new IntRange(10);
        }
        hashThread[] h = new hashThread[NUM_OF_THREADS]; //h = new hashThread[2]
        for (int i = 0; i < NUM_OF_THREADS; i++) { //NUM_OF_THREADS==2;
            h[i] = new hashThread(rangeArrayA); //h[0] = new hashThread(rangeArrayA); h[1] = new hashThread(rangeArrayA);
        }
        for (int i = 0; i < NUM_OF_THREADS; i++) { //NUM_OF_THREADS==2;
            h[i].start(); //run()
        }
        for (int i = 0; i < NUM_OF_THREADS; i++) {
            h[i].join();
        }
        for (int i = 0; i < TEST_LENGTH; i++) {
            for (int j = 0; j < NUM_OF_THREADS - 1; j++) {
            	//System.out.println(h[j].hash[i]+", "+h[j + 1].hash[i]);
                assert(h[j].hash[i] == h[j + 1].hash[i]);
            }
        }
    }

    class hashThread extends Thread {
		IntRange[] t = new IntRange[TEST_LENGTH];

        public int[] hash;
		
		public hashThread(IntRange[] rangeArrayA) {
            t = new IntRange[rangeArrayA.length];
            for (int i = 0; i < rangeArrayA.length; i++) {
                t[i] = rangeArrayA[i];
            }
        }

        public void run() {
            hash = new int[t.length];
            for (int i = 0; i < t.length; i++) {
                hash[i] = t[i].hashCode();
            }
        }

    }

}
