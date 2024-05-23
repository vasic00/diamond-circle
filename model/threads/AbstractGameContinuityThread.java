package model.threads;

import java.util.ArrayList;

abstract public class AbstractGameContinuityThread extends Thread {

	private final ArrayList<Thread> threads;
	
	AbstractGameContinuityThread(ArrayList<Thread> threads)
	{
		this.threads = threads;
	}
	
	final ArrayList<Thread> getThreads()
	{
		return threads;
	}
	
	abstract void modifyGameContinuity();
}
