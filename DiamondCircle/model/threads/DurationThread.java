package model.threads;

import javax.swing.JTextField;

public class DurationThread extends AbstractDiamondCircleThread {
	
	private JTextField duration;
	private static final int DURATION_SLEEP_TIME = 1000;
	private static final String DURATION_FORMAT = "Vrijeme trajanja igre: %dh %dm %ds";
	int hours, minutes, seconds;
	
	public DurationThread(JTextField duration)
	{
		this.duration = duration;
	}
	public void run()
	{
		isPaused();
		
		long time1 = System.currentTimeMillis();
		while (!isFinished())
		{
			
			long time2 = System.currentTimeMillis();
			
			long elapsedTime = (time2-time1)/1000;
			
			hours = (int) elapsedTime / 3600;
			minutes = (int) (elapsedTime % 3600) / 60;
			seconds = (int) elapsedTime % 60;
			
			java.awt.EventQueue.invokeLater(new Runnable() {
				public void run()
				{
					duration.setText(String.format(DURATION_FORMAT, hours, minutes, seconds));
				}
			});
			
			sleepNow(DURATION_SLEEP_TIME);
		}
	}
	
	public int getHours()
	{
		return hours;
	}
	
	public int getMinutes()
	{
		return minutes;
	}
	
	public int getSeconds()
	{
		return seconds;
	}
}
