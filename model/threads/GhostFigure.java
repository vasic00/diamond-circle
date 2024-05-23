package model.threads;

import static application.Application.mf;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Random;

import mainframe.IndexPair;
import model.fields.*;

public class GhostFigure extends AbstractDiamondCircleThread {
	
	private static GhostFigure singleInstance = null;
	private static final int GHOSTFIGURE_SLEEP_TIME = 5000;
	private static final int MIN_NUMBER_OF_DIAMONDS = 2;
	
	private GhostFigure()
	{
		super();
	}
	
	public static GhostFigure getGhostFigure()
	{
		if (singleInstance == null)
			singleInstance = new GhostFigure();
		
		return singleInstance;
	}
	
	public void run()
	{
		while (!isFinished())
		{
			isPaused();
			
			synchronized(getFieldsLock())
			{
				Random rand = new Random();
				int numberOfDiamonds = rand.nextInt(MIN_NUMBER_OF_DIAMONDS, mf.getMatrixDimension()+1);
				ArrayList<IndexPair> tempIndexPairArray = new ArrayList<IndexPair>();
				int n = mf.getIndexPairArraySize();
				for (int i = 0; i < n; i++)
					tempIndexPairArray.add(mf.getIndexPair(i));
				tempIndexPairArray.remove(tempIndexPairArray.size() - 1);
				
				ArrayList<IndexPair> diamondLabelsArray = new ArrayList<IndexPair>();
				
				for (int i = 0; i < numberOfDiamonds && !tempIndexPairArray.isEmpty(); i++)
				{
					int diamondIndex = rand.nextInt(tempIndexPairArray.size());
					IndexPair indexPair = tempIndexPairArray.remove(diamondIndex);
					int r = indexPair.getRow(), c = indexPair.getColumn();
					if (mf.getFields()[r][c] == null)
					{
						diamondLabelsArray.add(indexPair);
						mf.getFields()[r][c] = new Diamond();
					}
				}
					java.awt.EventQueue.invokeLater(new Runnable() {
						public void run()
						{
							for (int i = 0; i < diamondLabelsArray.size(); i++)
							{
								int row = diamondLabelsArray.get(i).getRow(), column = diamondLabelsArray.get(i).getColumn();
								if (mf.getFieldLabels()[row][column].getBackground().equals(new Color(255,255,255)))
									mf.getFieldLabels()[row][column].setBackground(Diamond.color);
							}
						}
					});

			}
			
			isPaused();
			
			if (!isFinished())
			{
				sleepNow(GHOSTFIGURE_SLEEP_TIME);
			}
		}
	}
	
}
