package application;

import java.util.logging.Level;
import java.util.logging.Logger;
import mainframe.*;

public class Application {
	
	public static MainFrame mf;
	
	public static void main(String[] args)
	{
		try {
			java.awt.EventQueue.invokeAndWait(new Runnable() {
				public void run()
				{
					try {
						mf = new MainFrame();
					} catch (Exception e) {
						Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, e.fillInStackTrace().toString());
					}
				}
			});
		} catch (Exception e) {
			Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, e.fillInStackTrace().toString());
			System.exit(1);
		}
		
		mf.startGame();
		mf.setVisible(true);
	}
}
