/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2023 jPOS Software SRL
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.jpos.util;

import java.time.Instant;

/**
 * ThroughputControl limits the throughput 
 * of a process to a maximum number of transactions in 
 * a given period of time.
 *
 * As an example, the following code will cap the transaction count
 * at 15 every second (a.k.a. 15 TPS).
 *
 * <pre>
 *
 *  ThroughputControl throughput = new ThroughputControl(15, 1000);
 *
 *  while (isConditionTrue()) {
 *      throughput.control();
 *      // Do stuff.
 *  }
 *
 * </pre>
 */
public class ThroughputControl {
    private int[] period;
    private int[] max;
    private int[] cnt;
    private long[] start;
    private long[] sleep;

    /**
     * @param maxTransactions Transaction count threshold.
     * @param periodInMillis Time window, expressed in milliseconds.
     */
    public ThroughputControl (int maxTransactions, int periodInMillis) {
        this (new int[] { maxTransactions },
              new int[] { periodInMillis });
    }
    /**
     * @param maxTransactions An array with transaction count thresholds.
     * @param periodInMillis An array of time windows, expressed in milliseconds.
     */
    public ThroughputControl (int[] maxTransactions, int[] periodInMillis) {
        super();
        int l = maxTransactions.length;
        period = new int[l];
        max = new int[l];
        cnt = new int[l];
        start = new long[l];
        sleep = new long[l];
        for (int i=0; i<l; i++) {
            this.max[i]    = maxTransactions[i];
            this.period[i] = periodInMillis[i];
            this.sleep[i]  = Math.min(Math.max (periodInMillis[i]/10, 500L),50L);
            this.start[i]  = Instant.now().toEpochMilli();
        }
    }

    /**
     * This method should be called on every transaction.
     * It will pause the thread for a while when the threshold is reached 
     * in order to control the process throughput.
     * 
     * @return Returns sleep time in milliseconds when threshold is reached. Otherwise, zero.
     */
    public long control() {
        boolean delayed = false;
        long init = Instant.now().toEpochMilli();
        for (int i=0; i<cnt.length; i++) {
            synchronized (this) {
                cnt[i]++;
            }
            do {
                if (cnt[i] > max[i]) {
                    delayed = true;
                    try { 
                        Thread.sleep (sleep[i]); 
                    } catch (InterruptedException e) { }
                }
                synchronized (this) {
                    long now = Instant.now().toEpochMilli();
                    if (now - start[i] > period[i]) {
                        long elapsed = now - start[i];
                        int  allowed = (int) (elapsed * max[i] / period[i]);
                        start[i] = now;
                        cnt[i] = Math.max (cnt[i] - allowed, 0);
                    }
                }
            } while (cnt[i] > max[i]);
        }
        return delayed ? Instant.now().toEpochMilli() - init : 0L;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("ThroughputControl [");
        for (int i = 0; i < max.length; i++) {
            sb.append(String.format(
                "%d: max = %d, period = %dms",
                i, max[i], period[i]
            ));
            if (i < max.length - 1) {
                sb.append("; ");
            }
        }
        sb.append("]");
        return sb.toString();
    }
}

