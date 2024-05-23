package model.threads;

import java.util.logging.Level;
import java.util.logging.Logger;

import mainframe.MainFrame;

public abstract class AbstractDiamondCircleThread extends Thread {
	
	private Object fieldsLock;
	
	private Object pausedLock;
	private volatile boolean paused = true;
	
	private volatile boolean finished;
	
	void sleepNow(int ms)
	{
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, e.fillInStackTrace().toString());
		}
	}
	
	void isPaused()
	{
		if (paused)
			synchronized(pausedLock)
			{
				try {
					pausedLock.wait();
				} catch (InterruptedException e) {
					Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, e.fillInStackTrace().toString());
				}
			}
	}
	
	void paused()
	{
		paused = !paused;
		if (!paused)
			synchronized(pausedLock)
			{
				pausedLock.notifyAll();
			}
	}
	
	boolean isFinished()
	{
		return finished;
	}
	
	void finish()
	{
		finished = true;
	}
	
	public void setGamePausedLock(Object lock)
	{
		if (pausedLock == null)
			pausedLock = lock;
	}
	
	public void setFieldsLock(Object lock)
	{
		if (fieldsLock == null)
			fieldsLock = lock;
	}
	
	Object getFieldsLock()
	{
		return fieldsLock;
	}
}
