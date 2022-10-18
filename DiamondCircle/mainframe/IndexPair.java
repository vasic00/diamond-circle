package mainframe;

import static application.Application.mf;

import java.io.Serializable;

public class IndexPair implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private int row,column;
	
	public IndexPair(int r, int c)
	{
		row = r;
		column = c;
	}

	public int getRow() {
		return row;
	}

	public int getColumn() {
		return column;
	}
	
	public int getNumber()
	{
		return mf.getMatrixDimension()*row + column + 1;
	}
	
	@Override
	public boolean equals(Object other)
	{
		if (other instanceof IndexPair)
		{
			IndexPair otherIndexPair = (IndexPair)other;
			if (row == otherIndexPair.row && column == otherIndexPair.column)
				return true;
		}
		return false;
	}
	
	@Override
	public int hashCode()
	{
		int hash = 3;
		hash = 7 * hash + row;
		hash = 7 * hash + column;
		return hash;
	}
}