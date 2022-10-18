package model.threads;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JTextField;

import mainframe.IndexPair;
import mainframe.MainFrame;
import model.figures.Figure;

import java.io.*;

import static application.Application.*;

public class GameEnderThread extends AbstractGameContinuityThread {
	
	private CountDownLatch countDownLatch;
	private final String resultPath;
	private final JTextField totalGamesPlayed;
	private final static String PLAYER_RESULT_FORMAT = "%s - Unique ID: %s%n";
	private final static String FIGURE_RESULT_FORMAT = "%s (%s,%s) - pređeni put (%s) - stigla do cilja: %s%n";
	private final static String DURATION_RESULT_FORMAT = "%nUkupno vrijeme trajanja igre: %dh %dm %ds";
	
	public GameEnderThread(ArrayList<Thread> threads, CountDownLatch countDownLatch, String resultPath, JTextField totalGamesPlayed)
	{
		super(threads);
		this.countDownLatch = countDownLatch;
		this.resultPath = resultPath;
		this.totalGamesPlayed = totalGamesPlayed;
	}
	
	public void run()
	{
		try {
			countDownLatch.await();
		} catch (InterruptedException e) {
			Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, e.fillInStackTrace().toString());
		}
		
		modifyGameContinuity();
		
		CardThread cardThread = null;
		DurationThread durationThread = null;
		ArrayList<Player> players = new ArrayList<Player>();
		
		for (Thread thread : getThreads())
		{
			if (thread instanceof CardThread)
				cardThread = (CardThread)thread;
			else if (thread instanceof DurationThread)
				durationThread = (DurationThread)thread;
			else if (thread instanceof Player)
				players.add((Player)thread);		
		}
		
		synchronized(cardThread)
		{
			cardThread.setCardUpdated(false);
			cardThread.notifyAll();
		}
		
		for (int i = 0; i < mf.getMatrixDimension(); i++)
			for (int j = 0; j < mf.getMatrixDimension(); j++)
				cardThread.resetField(new IndexPair(i,j));
		
		try {
			
			String localTimeString = java.time.LocalTime.now().toString();
			String[] localTimeStringArray = localTimeString.split(":");
			localTimeString = localTimeStringArray[0] + "-" + localTimeStringArray[1] + "-" + localTimeStringArray[2].substring(0, localTimeStringArray[2].indexOf("."));
			BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(resultPath + File.separator + "IGRA_" + localTimeString + ".txt"));
			
			for (int i = 0; i < players.size(); i++)
			{
				Player p = players.get(i);
				bufferedWriter.write(String.format(PLAYER_RESULT_FORMAT, p.getPlayerNumber(), p.getID()));
				
				for (int j = 0; j < Player.numberOfFigures(); j++)
				{
					Figure f = p.getFigure(j);
					IndexPair lastIndexPair = f.getLastPosition();
					StringBuilder traveledPath = new StringBuilder();
					
					int n = mf.getIndexOfIndexPair(lastIndexPair);
					
					for (int k = 0; k <= n; k++)
					{
						traveledPath.append(mf.getIndexPair(k).getNumber());
						traveledPath.append("-");
					}
					traveledPath.deleteCharAt(traveledPath.length()-1);
					
					String reachedTheEnd = "";
					if (f.isEnded())
						reachedTheEnd = "da";
					else reachedTheEnd = "ne";
					bufferedWriter.write("\t" + String.format(FIGURE_RESULT_FORMAT, f.getFigureNumber(), f.getText(), f.getColor().toString(), traveledPath.toString(), reachedTheEnd));
				}
			}
		
			bufferedWriter.write(String.format(DURATION_RESULT_FORMAT, durationThread.getHours(), durationThread.getMinutes(), durationThread.getSeconds()));
			bufferedWriter.close();
			
			java.awt.EventQueue.invokeLater(new Runnable() {
				public void run()
				{
					String numberChanger = totalGamesPlayed.getText();
					String[] strArr = numberChanger.split(" ");
					Integer parsedNumber = Integer.parseInt(strArr[3]);
					parsedNumber = parsedNumber + 1;
					totalGamesPlayed.setText(strArr[0] + " " + strArr[1] + " " + strArr[2] + " " + parsedNumber.toString());
				}
			});
			
		} catch (IOException e) {
			Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, e.fillInStackTrace().toString());
		}
		
	}
	
	void modifyGameContinuity()
	{
		for (Thread thread : getThreads())
		{
			((AbstractDiamondCircleThread)thread).finish();
		}
	}
	
}
