package model.threads;

import mainframe.IndexPair;
import mainframe.MainFrame;
import model.cards.SpecialCard;
import model.fields.Diamond;
import model.figures.*;

import static application.Application.mf;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Player extends AbstractGameProcessingThread {
	
	private static final int NUMBER_OF_FIGURES = 4;
	private static final int PLAYER_SLEEP_TIME = 1000;
	private static int counter;
	private static final LinkedList<Figure.ColorEnum> colorList;
	private final String ID;
	private final ArrayList<Figure> figures = new ArrayList<Figure>();
	private final int priority;
	private final String playerNumber;
	private final static Object priorityLock = new Object();
	private static int ordinalOfActivePlayer = 1;
	private Figure currentFigure;
	private static ArrayList<Integer> priorityArray = new ArrayList<Integer>();
	private final CountDownLatch countDownLatch;
	
	private DescriptionThread descriptionThread;
	private CardThread cardThread;
	
	
	static
	{
		colorList = new LinkedList<Figure.ColorEnum>();
		Collections.addAll(colorList, Figure.ColorEnum.values());
	}
	
	public Player(String ID, int priority, CountDownLatch countDownLatch, String path)
	{
		this.ID = ID;
		this.priority = priority;
		this.countDownLatch = countDownLatch;
		playerNumber = "Igrač " + (++counter);
		Random randFigure = new Random();
		Random randColor = new Random();
		Figure.ColorEnum figureColor = colorList.remove(randColor.nextInt(colorList.size()));
		for (int i = 0; i < NUMBER_OF_FIGURES; i++)
		{
			int figNumber = randFigure.nextInt(3);
			switch(figNumber) {
			case 0:
				figures.add(new RegularFigure(figureColor,path));
				break;
			case 1:
				figures.add(new HoveringFigure(figureColor,path));
				break;
			case 2:
				figures.add(new SuperFastFigure(figureColor,path));
				break;
			}
		}
		priorityArray.add(priority);
		priorityArray.sort(null);
	}
	
	public void run()
	{
		synchronized(priorityLock)
		{
			for (int i = 0; i < NUMBER_OF_FIGURES; i++)
			{
				waitForTurn();
				currentFigure = figures.get(i);
				
				while (!currentFigure.isEnded())
				{
					isPaused();
					
					synchronized(cardThread)
					{
						if (!cardThread.getCardUpdated())
						{
							try {
								cardThread.wait();
							} catch (InterruptedException e) {
								Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, e.fillInStackTrace().toString());
							}
						}
						
						if (currentFigure.isFallen())
							break;
						
						if (cardThread.getCard() instanceof SpecialCard)
						{
							cardThread.setCardUpdated(false);
							isPaused();
							descriptionThread.setDescription();
							sleepNow(PLAYER_SLEEP_TIME);
							cardThread.notifyAll();
							try {
								cardThread.wait();
							} catch (InterruptedException e) {
								Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, e.fillInStackTrace().toString());
							}
						}
						else moveCurrentFigure();
					}
					
					waitForTurn();
				}
			}
			updateOrdinalOfActivePlayer();
			priorityArray.remove(Integer.valueOf(priority));
			priorityLock.notifyAll();
		}
		countDownLatch.countDown();
	}
	
	private void waitForTurn()
	{
		while (ordinalOfActivePlayer != priority)
		{
			try {
				priorityLock.wait();
			} catch (InterruptedException e) {
				Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, e.fillInStackTrace().toString());
			}
		}
	}
	
	private void updateOrdinalOfActivePlayer()
	{
		if (!priorityArray.isEmpty())
		{
			if (ordinalOfActivePlayer == priorityArray.get(priorityArray.size()-1))
				ordinalOfActivePlayer = priorityArray.get(0);
			else 
				ordinalOfActivePlayer = priorityArray.get(priorityArray.indexOf(ordinalOfActivePlayer) + 1);
		}
	}
	
	private void moveCurrentFigure()
	{
		if (!currentFigure.isStarted())
		{
			IndexPair iP = mf.getIndexPair(0);
			currentFigure.started();
			currentFigure.setLastPosition(iP);
			if (mf.getFields()[iP.getRow()][iP.getColumn()] instanceof Diamond)
				currentFigure.addDiamond();
			loadCurrentFigure(iP);
		}
		IndexPair indexPairBeforeMove = currentFigure.getLastPosition();
		int indexOfIndexPairBeforeMove = mf.getIndexOfIndexPair(indexPairBeforeMove);
		int indexOfIndexPairAfterMove = 0;
		
		if (currentFigure instanceof SuperFastFigure)
			indexOfIndexPairAfterMove = indexOfIndexPairBeforeMove + 2*cardThread.getCard().getValue();
		else indexOfIndexPairAfterMove = indexOfIndexPairBeforeMove + cardThread.getCard().getValue();
		
		indexOfIndexPairAfterMove = indexOfIndexPairAfterMove + currentFigure.getNumberOfDiamonds();
		int endIndex = mf.getIndexPairArraySize() - 1;
		
		if (indexOfIndexPairAfterMove > endIndex)
			indexOfIndexPairAfterMove = endIndex;
		
		descriptionThread.setDescription(playerNumber, currentFigure.getFigureNumber(), String.valueOf(currentFigure.getNumberOfDiamonds()), indexPairBeforeMove, mf.getIndexPair(indexOfIndexPairAfterMove));
		
		for (int nextIndex = indexOfIndexPairBeforeMove + 1; nextIndex <= indexOfIndexPairAfterMove; nextIndex++)
		{
			int previousIndex = nextIndex - 1;
			
			synchronized (getFieldsLock())
			{
				if (nextIndex == indexOfIndexPairAfterMove)
					while (mf.getFields()[mf.getIndexPair(indexOfIndexPairAfterMove).getRow()][mf.getIndexPair(indexOfIndexPairAfterMove).getColumn()] instanceof Figure)
							indexOfIndexPairAfterMove++;
			}
			
			descriptionThread.setDescription(playerNumber, currentFigure.getFigureNumber(), String.valueOf(currentFigure.getNumberOfDiamonds()), indexPairBeforeMove, mf.getIndexPair(indexOfIndexPairAfterMove));
			
			sleepNow(PLAYER_SLEEP_TIME);
			
			synchronized(getFieldsLock())
			{
				while (nextIndex <= indexOfIndexPairAfterMove && mf.getFields()[mf.getIndexPair(nextIndex).getRow()][mf.getIndexPair(nextIndex).getColumn()] instanceof Figure)
					nextIndex++;
				
				if (mf.getFields()[mf.getIndexPair(nextIndex).getRow()][mf.getIndexPair(nextIndex).getColumn()] instanceof Diamond)
				{
					currentFigure.addDiamond();
					if (indexOfIndexPairAfterMove < endIndex)
						indexOfIndexPairAfterMove++;
				}
				
				isPaused();
				resetField(mf.getIndexPair(previousIndex));
				loadCurrentFigure(mf.getIndexPair(nextIndex)); 
			}
			currentFigure.setLastPosition(mf.getIndexPair(nextIndex));
		}
		
		if (indexOfIndexPairAfterMove == endIndex)
		{
			currentFigure.ended();
			sleepNow(PLAYER_SLEEP_TIME);
			synchronized(getFieldsLock())
			{
				resetField(mf.getIndexPair(endIndex));
			}
		}
		
		updateOrdinalOfActivePlayer();
		cardThread.setCardUpdated(false);
		cardThread.notifyAll();
		priorityLock.notifyAll();
	}
	
	private void loadCurrentFigure(IndexPair indexPair)
	{
		int row = indexPair.getRow();
		int column = indexPair.getColumn();
		mf.getFields()[row][column] = currentFigure;
		
		isPaused();
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run()
			{
				mf.getFieldLabels()[row][column].setText(currentFigure.getText());
				mf.getFieldLabels()[row][column].setBackground(currentFigure.getColor().getColorCode());
			}
		});
	}

	String getPlayerNumber() 
	{
		return playerNumber;
	}
	
	
	public void setDescriptionThread(DescriptionThread descriptionThread)
	{
		if (this.descriptionThread == null)
			this.descriptionThread = descriptionThread;
	}
	
	public void setCardThread(CardThread cardThread)
	{
		if (this.cardThread == null)
			this.cardThread = cardThread;
	}

	public Figure getFigure(int i)
	{
		return figures.get(i);
	}
	
	public static int numberOfFigures()
	{
		return NUMBER_OF_FIGURES;
	}
	
	String getID()
	{
		return ID;
	}
	
	@Override
	public boolean equals(Object other)
	{
		if (other instanceof Player)
		{
			Player p2 = (Player) other;
			if (this.ID.equals(p2.ID))
				return true;
		}
		return false;
	}
	
	@Override
	public int hashCode()
	{
		return ID.hashCode();
	}
	
}
