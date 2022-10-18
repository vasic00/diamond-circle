package model.threads;

import static application.Application.mf;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

import mainframe.*;
import model.cards.Card;
import model.cards.RegularCard;
import model.cards.SpecialCard;
import model.fields.Hole;
import model.figures.Figure;
import model.figures.HoveringFigure;

public class CardThread extends AbstractGameProcessingThread {

	private final JLabel currentCardLabel;
	private final ArrayDeque<Card> cards;
	private Card card; // card on top of the deck
	private boolean cardUpdated;
	
	public CardThread(JLabel currentCardLabel, ArrayDeque<Card> cards)
	{
		this.currentCardLabel = currentCardLabel;
		this.cards = cards;
	}
		
	public void run()
	{
		{
			ArrayList<IndexPair> updatedHolesArray = new ArrayList<IndexPair>();
			while(!isFinished())
			{
				isPaused();
				
				synchronized(this)
				{
					if (cardUpdated)
						try {
							this.wait();
						} catch (InterruptedException e) {
							Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, e.fillInStackTrace().toString());
						}
				}
				
				synchronized (getFieldsLock())
				{
					
					if (!updatedHolesArray.isEmpty())
					{
						for (int i = 0; i < updatedHolesArray.size(); i++)
						{
							int r = updatedHolesArray.get(i).getRow(), c = updatedHolesArray.get(i).getColumn();
							if (mf.getFields()[r][c] instanceof HoveringFigure)
							{
								isPaused();
								java.awt.EventQueue.invokeLater(new Runnable() {
									public void run()
									{
										mf.getFieldLabels()[r][c].setText(((Figure)mf.getFields()[r][c]).getText());
										mf.getFieldLabels()[r][c].setBackground(((Figure)mf.getFields()[r][c]).getColor().getColorCode());
									}
								});
							}
							else resetField(updatedHolesArray.get(i));
								
						}
						updatedHolesArray.clear();
					}
				}
				
				if (!isFinished())
				{
					Card currentCard = cards.peekFirst();
					cards.offerLast(cards.pollFirst());	
					
					String iconPath, textUnderCard;
					iconPath = currentCard.getPath();
					if (currentCard instanceof RegularCard)
					{
						String valueOfCard = String.valueOf(currentCard.getValue());
						if (currentCard.getValue() == 1)
							textUnderCard = valueOfCard + " POLJE";
						else textUnderCard = valueOfCard + " POLJA";
					} 
					else textUnderCard = "";
					
					isPaused();
					java.awt.EventQueue.invokeLater(new Runnable() {
						public void run()
						{
							currentCardLabel.setText(textUnderCard);
							currentCardLabel.setIcon(new ImageIcon(iconPath));
						}
					});
					
					if (currentCard instanceof SpecialCard)
					{
						synchronized(getFieldsLock())
						{
							ArrayList<IndexPair> tempIndexPairArray = new ArrayList<IndexPair>();
							int n = mf.getIndexPairArraySize();
							for (int i = 0; i < n; i++)
								tempIndexPairArray.add(mf.getIndexPair(i));
							tempIndexPairArray.remove(tempIndexPairArray.size() - 1);
							tempIndexPairArray.remove(0);
							
							for (int i = 0; i < currentCard.getValue(); i++)
							{
								if (tempIndexPairArray.isEmpty())
									break;
								Random rand = new Random();
								int holeIndex = rand.nextInt(tempIndexPairArray.size());
								IndexPair indexPair = tempIndexPairArray.remove(holeIndex);
								int r = indexPair.getRow(), c = indexPair.getColumn();
								if (!(mf.getFields()[r][c] instanceof HoveringFigure))
								{
									if (mf.getFields()[r][c] instanceof Figure)
										((Figure)mf.getFields()[r][c]).fell();
									mf.getFields()[r][c] = new Hole();
								}
								updatedHolesArray.add(indexPair);
							}
							
							isPaused();
							java.awt.EventQueue.invokeLater(new Runnable() {
								public void run()
								{
									for (int i = 0; i < updatedHolesArray.size(); i++)
									{
										int row = updatedHolesArray.get(i).getRow(), column = updatedHolesArray.get(i).getColumn();
										mf.getFieldLabels()[row][column].setText("");
										mf.getFieldLabels()[row][column].setBackground(Hole.color);
									}
								}
							});
			
						}
					}
					synchronized(this)
					{
						card = currentCard;
						cardUpdated = true;
						this.notifyAll();
					}
				}
			}
			
			java.awt.EventQueue.invokeLater(new Runnable() {
				public void run()
				{
					currentCardLabel.setText(Card.invalidCardText);
					currentCardLabel.setIcon(new ImageIcon(Card.invalidCardPath));
				}
			});
		}
	}
	
	boolean getCardUpdated()
	{
		return cardUpdated;
	}
	
	Card getCard()
	{
		return card;
	}
	
	void setCardUpdated(boolean value)
	{
		cardUpdated = value;
	}
}
