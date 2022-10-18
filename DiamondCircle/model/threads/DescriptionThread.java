package model.threads;

import static application.Application.*;

import javax.swing.JTextArea;

import mainframe.IndexPair;

public class DescriptionThread extends AbstractDiamondCircleThread {
	
	private JTextArea description;
	private static final int DESCRIPTION_SLEEP_TIME = 1000;
	private static final String DESCRIPTION_FORMAT = "%s%n%s (%sd)%nPrelazi %s polja%nSa pozicije %d na poziciju %d";
	private static final String TURN_DELAYED_MESSAGE = "Rupe postavljene";
	
	private String currentPlayerNumber;
	private String currentFigureNumber;
	private String numberOfCollectedDiamonds;
	private IndexPair positionBeforeMove;
	private IndexPair positionAfterMove;
	
	private boolean turnDelayed;
	
	public DescriptionThread(JTextArea description)
	{
		this.description = description;
	}
	
	public void run()
	{
		isPaused();
		
		while (true)
		{
			synchronized(this)
			{
				if (positionBeforeMove != null || turnDelayed == true)
					break;
			}
			sleepNow(100);
		}
		
		while (!isFinished())
		{
			synchronized(this)
			{
				java.awt.EventQueue.invokeLater(new Runnable() {
					public void run()
					{
						if (turnDelayed)
							description.setText(TURN_DELAYED_MESSAGE);
						else
						{
							description.setText(String.format(DESCRIPTION_FORMAT, currentPlayerNumber, currentFigureNumber, numberOfCollectedDiamonds,
									String.valueOf(mf.getIndexOfIndexPair(positionAfterMove) - mf.getIndexOfIndexPair(positionBeforeMove)),
									positionBeforeMove.getNumber(), positionAfterMove.getNumber()));
						}
					}
				});
			}
			sleepNow(DESCRIPTION_SLEEP_TIME);
		}
		
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run()
			{
				description.setText("IGRA ZAVRŠENA");
			}
		});
	}
	
	synchronized void setDescription(String playerNumber, String figureNumber, String numberOfDiamonds, IndexPair positionBefore, IndexPair positionAfter)
	{
		currentPlayerNumber = playerNumber;
		currentFigureNumber = figureNumber;
		numberOfCollectedDiamonds = numberOfDiamonds;
		positionBeforeMove = positionBefore;
		positionAfterMove = positionAfter;
		turnDelayed = false;
	}
	
	synchronized void setDescription()
	{
		turnDelayed = true;
	}
}
