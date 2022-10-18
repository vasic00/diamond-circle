package model.figures;

import mainframe.IndexPair;
import mainframe.MainFrame;

import static application.Application.*;
import model.fields.Field;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class Figure implements Field {
	
	public enum ColorEnum
	{
		CRVENA(Color.RED),
		PLAVA(new Color(51,153,255)), // light blue
		ZELENA(Color.GREEN),
		ŽUTA(Color.YELLOW);
		
		private Color colorCode;
		private ColorEnum(Color colorCode)
		{
			this.colorCode = colorCode;
		}
		
		public Color getColorCode()
		{
			return colorCode;
		}
	}
	
	private static int counter;
	private final ColorEnum color; 
	private final String text;
	private final String figureNumber;
	private volatile boolean fallen;
	private boolean ended;
	private int numberOfDiamonds;
	private long timeSpentMoving = -1;
	private IndexPair lastPosition;
	private final String movementResultPath;
	
	public Figure(ColorEnum color, String text, String path)
	{
		this.color = color;
		this.text = text;
		figureNumber = "Figura " + (++counter);
		movementResultPath = path + File.separator + figureNumber + ".ser";
	}
	
	public ColorEnum getColor()
	{
		return color;
	}
	
	synchronized public void started()
	{
		if (timeSpentMoving < 0)
			timeSpentMoving = System.currentTimeMillis();
	}
	
	public boolean isStarted()
	{
		return timeSpentMoving > 0;
	}
	
	public void ended()
	{
		if (ended == false && fallen == false)
		{
			ended = true;
			timeSpentMoving = System.currentTimeMillis() - timeSpentMoving;
			serializeMovement();
		}
	}
	
	public boolean isEnded()
	{
		return ended;
	}
	
	public boolean isFallen()
	{
		return fallen;
	}
	
	synchronized public void setLastPosition(IndexPair indexPair)
	{
		lastPosition = indexPair;
	}
	
	synchronized public IndexPair getLastPosition()
	{
		return lastPosition;
	}
	
	public void fell()
	{
		if (ended == false && fallen == false)
		{
			fallen = true;
			timeSpentMoving = System.currentTimeMillis() - timeSpentMoving;
			serializeMovement();
		}
	}
	
	private void serializeMovement()
	{
		try {
			ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(movementResultPath));
			ArrayList<IndexPair> passedFields = new ArrayList<IndexPair>();
			for (int i = 0; i <= mf.getIndexOfIndexPair(lastPosition); i++)
				passedFields.add(mf.getIndexPair(i));
			output.writeObject(passedFields);
			output.writeLong(timeSpentMoving);
			output.close();
		} catch (FileNotFoundException e) {
			Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, e.fillInStackTrace().toString());
		} catch (IOException e) {
			Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, e.fillInStackTrace().toString());
		}
	}

	public String getText() 
	{
		return text;
	}
	
	public void addDiamond()
	{
		numberOfDiamonds++;
	}
	
	public int getNumberOfDiamonds()
	{
		return numberOfDiamonds;
	}
	
	public long getTimeSpentMoving()
	{
		return timeSpentMoving;
	}
	
	public String getFigureNumber() 
	{
		return figureNumber;
	}
}
