package nachos.threads;

import nachos.machine.*;
import java.util.PriorityQueue;
import java.util.HashMap;
import java.util.ArrayList;

/**
 * Uses the hardware timer to provide preemption, and to allow threads to sleep
 * until a certain time.
 */
public class Alarm {
	/**
	 * Allocate a new Alarm. Set the machine's timer interrupt handler to this
	 * alarm's callback.
	 * 
	 * <p>
	 * <b>Note</b>: Nachos will not function correctly with more than one alarm.
	 */
	public Alarm() {
		Machine.timer().setInterruptHandler(new Runnable() {
			public void run() {
				timerInterrupt();
			}
		});

		pq = new PriorityQueue<Long>();
		mp = new HashMap<Long, ArrayList<KThread> >();
	}

	/**
	 * The timer interrupt handler. This is called by the machine's timer
	 * periodically (approximately every 500 clock ticks). Causes the current
	 * thread to yield, forcing a context switch if there is another thread that
	 * should be run.
	 */
	public void timerInterrupt() {
		while(!pq.isEmpty()){
			Long wtime = pq.peek();
			if(wtime <= Machine.timer().getTime()){
				Machine.interrupt().disable();
				pq.poll();
				for(KThread wthread : mp.get(wtime)){
					//if(wthread.status != 4) 
						wthread.ready();
				}
			}else{break;}
		}
		KThread.currentThread().yield();
	}

	/**
	 * Put the current thread to sleep for at least <i>x</i> ticks, waking it up
	 * in the timer interrupt handler. The thread must be woken up (placed in
	 * the scheduler ready set) during the first timer interrupt where
	 * 
	 * <p>
	 * <blockquote> (current time) >= (WaitUntil called time)+(x) </blockquote>
	 * 
	 * @param x the minimum number of clock ticks to wait.
	 * 
	 * @see nachos.machine.Timer#getTime()
	 */
	public void waitUntil(long x) {
		// for now, cheat just to get something working (busy waiting is bad)
		Long wakeTime = Machine.timer().getTime() + x;
		if(!mp.containsKey(wakeTime)){
			pq.add(wakeTime);
			ArrayList<KThread> arr = new ArrayList<KThread>();
			arr.add(KThread.currentThread());
			mp.put(wakeTime,arr);
		}else{
			mp.get(wakeTime).add(KThread.currentThread());
		}
		boolean intStatus = Machine.interrupt().disable();
		KThread.currentThread().sleep();
		Machine.interrupt().restore(intStatus);	
	}

	private static PriorityQueue<Long> pq;
	private static HashMap<Long, ArrayList<KThread> > mp;
}
