package model.threads;

import java.util.ArrayList;

public class GameStarterThread extends AbstractGameContinuityThread {
	
	public GameStarterThread(ArrayList<Thread> threads)
	{
		super(threads);
	}
	public void run()
	{
		modifyGameContinuity();
	}
	
	void modifyGameContinuity()
	{
		for (Thread thread : getThreads())
		{
			((AbstractDiamondCircleThread)thread).paused();
		}
	}

}
