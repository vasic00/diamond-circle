package model.threads;

import static application.Application.mf;

import java.awt.Color;
import mainframe.IndexPair;

public abstract class AbstractGameProcessingThread extends AbstractDiamondCircleThread { // nije interface jer zelim da metoda bude vidljiva samo unutar paketa
	
	final void resetField(IndexPair indexPair)
	{
		int row = indexPair.getRow(), column = indexPair.getColumn();
		mf.getFields()[row][column] = null;
		
		isPaused();
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run()
			{
				mf.getFieldLabels()[row][column].setText(String.valueOf(indexPair.getNumber()));
				mf.getFieldLabels()[row][column].setBackground(new Color(255,255,255));
			}
		});

	}
}
