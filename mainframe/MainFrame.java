package mainframe;

import model.cards.*;
import model.fields.Field;
import model.figures.Figure;
import model.threads.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.swing.*;
import javax.swing.border.LineBorder;

import exceptions.IllegalMatrixDimensionException;
import exceptions.IllegalNumberOfPlayersException;

import javax.swing.border.EtchedBorder;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;
import java.util.logging.*;


public class MainFrame extends JFrame {
	
	private static final int MAX_NUMBER_OF_PLAYERS = 4;
	private static final int MIN_NUMBER_OF_PLAYERS = 2;
	private static final int MIN_DIM = 7;
	private static final int MAX_DIM = 10;
	private static final int NUMBER_OF_REGULAR_CARDS = 10;
    private static final int NUMBER_OF_SPECIAL_CARDS = 12;
    private static final String GAMES_RESULTS_PATH = "resources" + File.separator + "results" + File.separator + "games";
    private static final String MOVEMENTS_RESULTS_PATH = "resources" + File.separator + "results" + File.separator + "movements";
    private static final String LOGS_PATH = "resources" + File.separator + "logs" + File.separator + "MainFrame.log";
    private static final String CONFIG_PATH = "resources" + File.separator + "config" + File.separator + "config.properties";
	private final JTextArea txtrDescription = new JTextArea();
	private final JTextField durationTextField = new JTextField(); 
	private final JLabel[][] fieldLabels;
	private final Field[][] fields;
	private final JLabel currentCardLabel = new JLabel();
	private final Player[] players;
	private final ArrayDeque<Card> cards = new ArrayDeque<Card>();
	private final ArrayList<IndexPair> indexPairArray = new ArrayList<IndexPair>();
	private final int matrixDimension, numberOfPlayers;
	private final CountDownLatch countDownLatch;
	private final JTextField txtTotalGamesPlayed = new JTextField();
	private final JButton startButton = new JButton("Pokreni / Zaustavi");
	private final int n;
	
	
	static
	{
		try {
            Handler fileHandler = new FileHandler(LOGS_PATH, true);
            Logger.getLogger(MainFrame.class.getName()).setUseParentHandlers(false);
            Logger.getLogger(MainFrame.class.getName()).addHandler(fileHandler);
        } catch (IOException e) {
            Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).log(Level.SEVERE, e.fillInStackTrace().toString());
        }
	}
	
	
	public MainFrame() throws IOException, IllegalMatrixDimensionException, IllegalNumberOfPlayersException, NumberFormatException
	{
		
		Properties properties = new Properties();
		FileInputStream fileInputStream;
		fileInputStream = new FileInputStream(CONFIG_PATH);
		properties.load(fileInputStream);
		
		matrixDimension = Integer.parseInt(properties.getProperty("matrixDimension"));
		if (matrixDimension < MIN_DIM || matrixDimension > MAX_DIM)
			throw new IllegalMatrixDimensionException();
		numberOfPlayers = Integer.parseInt(properties.getProperty("numberOfPlayers"));
		if (numberOfPlayers < MIN_NUMBER_OF_PLAYERS || numberOfPlayers > MAX_NUMBER_OF_PLAYERS)
			throw new IllegalNumberOfPlayersException();
		n = Integer.parseInt(properties.getProperty("n"));
		
	        
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setSize(1100, 800);
		getContentPane().setLayout(null);
		setResizable(false);
		
		fieldLabels = new JLabel[matrixDimension][matrixDimension];
		fields = new Field[matrixDimension][matrixDimension];
		players = new Player[numberOfPlayers];
		countDownLatch = new CountDownLatch(numberOfPlayers);
		
		configurePlayers();
		configureMatrix();
		configurePathing();
		configureCards();
		configureFigures();
		configureTitle();
		configureDescriptionAndDuration();
		configureResults();
	}
	
	private void configurePlayers()
	{
		ArrayList<Integer> priorities = new ArrayList<Integer>();
		for (int i = 1; i <= numberOfPlayers; i++)
			priorities.add(i);
		Collections.shuffle(priorities);
		
		HashSet<String> uniqueIDs = new HashSet<String>(); 
		
		for (int i = 0; i < numberOfPlayers; i++)
		{
			String uniqueID = UUID.randomUUID().toString().substring(0,8);
			if (!uniqueIDs.contains(uniqueID))
			{
				players[i] = new Player(uniqueID, priorities.get(i), countDownLatch, MOVEMENTS_RESULTS_PATH);
				uniqueIDs.add(uniqueID);
			}
		}
		
		JPanel playersPanel = new JPanel();
		playersPanel.setBounds(0, 90, 1084, 42);
		playersPanel.setBorder(new LineBorder(new Color(0, 0, 0)));
		playersPanel.setBackground(Color.LIGHT_GRAY);
		getContentPane().add(playersPanel);
		playersPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 100, 10));
		
		for (int i = 0; i < numberOfPlayers; i++)
		{
			JTextField playerTextField = new JTextField();
			playerTextField.setEditable(false);
			playerTextField.setForeground(players[i].getFigure(0).getColor().getColorCode());
			playerTextField.setHorizontalAlignment(SwingConstants.CENTER);
			playerTextField.setText("Igrač " + String.valueOf(i+1));
			playerTextField.setFont(new Font("Tahoma", Font.BOLD, 16));
			playerTextField.setBackground(Color.LIGHT_GRAY);
			playersPanel.add(playerTextField);
			playerTextField.setColumns(10);
			playerTextField.setBorder(null);
		}
	}
	
	private void configureMatrix()
	{
		JPanel matrixPanel = new JPanel();
		matrixPanel.setBorder(new LineBorder(new Color(0, 0, 0)));
		matrixPanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		matrixPanel.setBounds(313, 159, 433, 433);
		matrixPanel.setLayout(new GridLayout(matrixDimension, matrixDimension, 0, 0));
		getContentPane().add(matrixPanel);
		for (int i = 0, fieldNumber = 0; i < matrixDimension; i++)
		{
			for (int j = 0; j < matrixDimension; j++)
			{
				fieldNumber++;
				fieldLabels[i][j] = new JLabel();
				fieldLabels[i][j].setBorder(new LineBorder(new Color(0, 0, 0)));
				fieldLabels[i][j].setHorizontalAlignment(SwingConstants.CENTER);
				fieldLabels[i][j].setVerticalAlignment(SwingConstants.CENTER);
				fieldLabels[i][j].setHorizontalTextPosition(SwingConstants.CENTER);
				fieldLabels[i][j].setVerticalTextPosition(SwingConstants.CENTER);
				fieldLabels[i][j].setText(String.valueOf(fieldNumber));
				fieldLabels[i][j].setOpaque(true);
				fieldLabels[i][j].setBackground(new Color(255, 255, 255));
				matrixPanel.add(fieldLabels[i][j]);
			}
		}
	}
	
	private void configurePathing()
	{
		int i = 0, j = matrixDimension/2, g = 0;
		while (true)
		{
			for (; j != matrixDimension && !indexPairArray.contains(new IndexPair(i,j-1)); i++, j++)
			{
				indexPairArray.add(new IndexPair(i,j));
			}
			j = j - 2;
			for (; i != matrixDimension && !indexPairArray.contains(new IndexPair(i-1,j)); i++, j--)
			{
				indexPairArray.add(new IndexPair(i,j));
			}
			i = i - 2;
			for (; j >= g; i--, j--)
			{
				indexPairArray.add(new IndexPair(i,j));
			}
			g++;
			j = j + 2;
			for (; !indexPairArray.contains(new IndexPair(i,j)); i--, j++)
			{
				indexPairArray.add(new IndexPair(i,j));
			}
			i++;
			indexPairArray.add(new IndexPair(i,j));
			if (matrixDimension % 2 == 0 && indexPairArray.contains(new IndexPair(matrixDimension/2 - 1, matrixDimension/2)) && !indexPairArray.contains(new IndexPair(matrixDimension/2, matrixDimension/2 - 1)))
			{
				indexPairArray.add(new IndexPair(matrixDimension/2, matrixDimension/2-1));
				break;
			}
			else if (matrixDimension % 2 != 0 && indexPairArray.contains(new IndexPair(matrixDimension/2, matrixDimension/2)))
			{
				break;
			}
			i++;
			j++;
		}
	}
	
	private void configureCards()
	{
		JPanel cardsPanel = new JPanel();
		cardsPanel.setBorder(new LineBorder(new Color(0, 0, 0)));
		cardsPanel.setBounds(860, 159, 214, 379);
		getContentPane().add(cardsPanel);
		cardsPanel.setLayout(null);
		JLabel lblNewLabel = new JLabel("Trenutna karta");
		lblNewLabel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		lblNewLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		lblNewLabel.setOpaque(true);
		lblNewLabel.setBackground(Color.LIGHT_GRAY);
		lblNewLabel.setFont(new Font("Tahoma", Font.PLAIN, 18));
		lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel.setBounds(10, 11, 194, 43);
		cardsPanel.add(lblNewLabel);
		currentCardLabel.setOpaque(true);
		currentCardLabel.setBackground(new Color(255, 228, 225));
		currentCardLabel.setBorder(new LineBorder(new Color(138, 43, 226)));
		currentCardLabel.setFont(new Font("Tahoma", Font.PLAIN, 18));
		currentCardLabel.setIconTextGap(25);
		currentCardLabel.setHorizontalAlignment(SwingConstants.CENTER);
		currentCardLabel.setHorizontalTextPosition(SwingConstants.CENTER);
		currentCardLabel.setVerticalTextPosition(SwingConstants.BOTTOM);
		currentCardLabel.setText(Card.invalidCardText);
		currentCardLabel.setIcon(new ImageIcon(Card.invalidCardPath));
		currentCardLabel.setBounds(10, 65, 194, 309);
		cardsPanel.add(currentCardLabel);
		
		ArrayList<Card> cardList = new ArrayList<Card>();
		for (int i = 0; i < NUMBER_OF_REGULAR_CARDS; i++)
		{
			cardList.add(new RegularCard(1));
			cardList.add(new RegularCard(2));
			cardList.add(new RegularCard(3));
			cardList.add(new RegularCard(4));
		}
		
		for (int i = 0; i < NUMBER_OF_SPECIAL_CARDS; i++)
		{
			cardList.add(new SpecialCard(n));
		}
		Collections.shuffle(cardList);
		cards.addAll(cardList);
	}
	
	private void resetMovementsResults()
	{
		try
		{
			Files.list(Paths.get(MOVEMENTS_RESULTS_PATH)).forEach(p ->{
				try {
					Files.delete(p);
				} catch (IOException e) {
					Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, e.fillInStackTrace().toString());
				}
			});
		} catch (IOException e) {
			Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, e.fillInStackTrace().toString());
		}
	}
	
	private void configureFigures()
	{
		resetMovementsResults();
		
		JPanel figuresPanel = new JPanel();
		figuresPanel.setBorder(new LineBorder(new Color(0, 0, 0)));
		figuresPanel.setBounds(10, 159, 158, 591);
		figuresPanel.setLayout(new GridLayout(numberOfPlayers*4, 1, 0, 3));
		getContentPane().add(figuresPanel);
		
		JButton[][] buttons = new JButton[numberOfPlayers][Player.numberOfFigures()];
		
		for (int i = 0; i < numberOfPlayers; i++)
		{
			for (int j = 0; j < Player.numberOfFigures(); j++)
			{
				buttons[i][j] = new JButton(players[i].getFigure(j).getFigureNumber());
				figuresPanel.add(buttons[i][j]);
				int numberOfPlayer = i, numberOfFigure = j;
				
				buttons[i][j].addActionListener(new ActionListener() {
					JFrame frame = new JFrame();
					@Override
					public void actionPerformed(ActionEvent ev)
					{
						frame.setVisible(false);
						frame.dispose();
						frame = new JFrame();
						
						frame.setSize(600,600);
						frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
						
						JPanel panel = new JPanel();
						panel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
						panel.setBounds(313, 159, 433, 433);
						panel.setLayout(new GridLayout(matrixDimension, matrixDimension, 0, 0));
						
						frame.getContentPane().add(panel);
						
						JLabel[][] labels = new JLabel[matrixDimension][matrixDimension];
						
						for (int i = 0, fieldNumber = 0; i < matrixDimension; i++)
						{
							for (int j = 0; j < matrixDimension; j++)
							{
								fieldNumber++;
								labels[i][j] = new JLabel();
								labels[i][j].setBorder(new LineBorder(new Color(0, 0, 0)));
								labels[i][j].setHorizontalAlignment(SwingConstants.CENTER);
								labels[i][j].setVerticalAlignment(SwingConstants.CENTER);
								labels[i][j].setHorizontalTextPosition(SwingConstants.CENTER);
								labels[i][j].setVerticalTextPosition(SwingConstants.CENTER);
								labels[i][j].setText(String.valueOf(fieldNumber));
								labels[i][j].setOpaque(true);
								labels[i][j].setBackground(new Color(255, 255, 255));
								panel.add(labels[i][j]);
							}
						}
						
						new Thread() {
							public void run() {
								
								Figure fig = players[numberOfPlayer].getFigure(numberOfFigure);
								
								int numberOfFields = indexPairArray.indexOf(fig.getLastPosition());
								
								java.awt.EventQueue.invokeLater(new Runnable() {
									public void run()
									{
										for (int i = 0; i <= numberOfFields; i++)
										{
											int row = indexPairArray.get(i).getRow(), column = indexPairArray.get(i).getColumn();
											labels[row][column].setText(fig.getText());
											labels[row][column].setBackground(fig.getColor().getColorCode());
										}
										frame.setVisible(true);
									}
								});
							}
						}.start();
					}
				});
				
			}
		}
	}
	
	private void configureTitle() throws IOException
	{
		JPanel startPanel = new JPanel();
		startPanel.setBorder(new LineBorder(new Color(0, 0, 0)));
		startPanel.setBounds(0, 0, 1084, 87);
		startPanel.setLayout(null);
		getContentPane().add(startPanel);
		
		startButton.setFont(new Font("Tahoma", Font.PLAIN, 18));
		startButton.setBounds(870, 11, 204, 65);
		startPanel.add(startButton);
		
		JTextField txtDiamondCircle = new JTextField();
		txtDiamondCircle.setBorder(new LineBorder(new Color(184, 134, 11)));
		txtDiamondCircle.setHorizontalAlignment(SwingConstants.CENTER);
		txtDiamondCircle.setText("Diamond Circle");
		txtDiamondCircle.setForeground(new Color(139, 69, 19));
		txtDiamondCircle.setFont(new Font("Cambria", Font.BOLD | Font.ITALIC, 24));
		txtDiamondCircle.setBackground(SystemColor.info);
		txtDiamondCircle.setBounds(452, 11, 180, 65);
		txtDiamondCircle.setEditable(false);
		startPanel.add(txtDiamondCircle);
		txtDiamondCircle.setColumns(10);
		txtTotalGamesPlayed.setForeground(new Color(75, 0, 130));
		txtTotalGamesPlayed.setBorder(null);
		txtTotalGamesPlayed.setFont(new Font("Tahoma", Font.BOLD, 16));
		
		long count = Files.list(Paths.get(GAMES_RESULTS_PATH)).count();
		
		txtTotalGamesPlayed.setText("Broj odigranih igara: " + count);
		txtTotalGamesPlayed.setBackground(SystemColor.control);
		txtTotalGamesPlayed.setBounds(10, 24, 219, 40);
		txtTotalGamesPlayed.setEditable(false);
		startPanel.add(txtTotalGamesPlayed);
		txtTotalGamesPlayed.setColumns(10);
	}
	
	private void configureDescriptionAndDuration()
	{
		JPanel descriptionPanel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) descriptionPanel.getLayout();
		flowLayout.setVgap(15);
		descriptionPanel.setBackground(SystemColor.info);
		descriptionPanel.setBorder(new LineBorder(new Color(0, 0, 0)));
		descriptionPanel.setBounds(356, 617, 347, 133);
		
		getContentPane().add(descriptionPanel);
		txtrDescription.setBackground(SystemColor.info);
		
		txtrDescription.setFont(new Font("Monospaced", Font.PLAIN, 16));
		txtrDescription.setEditable(false);
		descriptionPanel.add(txtrDescription);
		
		durationTextField.setHorizontalAlignment(SwingConstants.CENTER);
		durationTextField.setBackground(SystemColor.info);
		durationTextField.setBounds(860, 562, 214, 30);
		durationTextField.setEditable(false);
		getContentPane().add(durationTextField);
		durationTextField.setColumns(10);
	}
	
	private void configureResults()
	{
		JPanel resultsPanel = new JPanel();
		resultsPanel.setBackground(Color.LIGHT_GRAY);
		resultsPanel.setBorder(new LineBorder(new Color(0, 0, 0)));
		resultsPanel.setBounds(859, 617, 215, 133);
		resultsPanel.setLayout(null);
		
		JButton btnNewButton = new JButton("<html>Prikaz liste<br />fajlova sa<br />rezultatima</html>");
		btnNewButton.setBounds(53, 27, 109, 80);
		resultsPanel.add(btnNewButton);
		
		getContentPane().add(resultsPanel);
			
		btnNewButton.addActionListener(new ActionListener(){
			JFrame frame = new JFrame();
			@Override
			public void actionPerformed(ActionEvent ev)
			{
				frame.setVisible(false);
				frame.dispose();
				frame = new JFrame();
				frame.setSize(600,600);
				frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
				
				JPanel panel = new JPanel();
				panel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
				panel.setBounds(313, 159, 433, 433);
				panel.setLayout(new GridLayout(10,0,0,0));
				frame.getContentPane().add(panel);
				java.util.List<Path> paths = null;
				
				try {
					paths = Files.list(Paths.get(GAMES_RESULTS_PATH)).collect(Collectors.toList());
				} catch (IOException e) {
					 Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, e.fillInStackTrace().toString());
				}
					
				for (int i = 0; i < paths.size(); i++)
				{
					JButton fileButton = new JButton(paths.get(i).getFileName().toString());
					File file = paths.get(i).toFile();
					fileButton.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent ev)
						{
							try {
								Desktop.getDesktop().open(file);
							} catch (IOException e) {
								Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, e.fillInStackTrace().toString());
							}
						}
					});
					panel.add(fileButton);
				}
				
				frame.setVisible(true);
					
			}
		});
	}
	
	
	public void startGame()
	{
		Object gamePausedLock = new Object();
		Object fieldsLock = new Object();
		
		ArrayList<Thread> DiamondCircleThreads = new ArrayList<Thread>();
		
		DurationThread durationThread = new DurationThread(durationTextField);
		durationThread.setGamePausedLock(gamePausedLock);
		DiamondCircleThreads.add(durationThread);
		durationThread.start();
		
		DescriptionThread descriptionThread = new DescriptionThread(txtrDescription);
		descriptionThread.setGamePausedLock(gamePausedLock);
		DiamondCircleThreads.add(descriptionThread);
		descriptionThread.start();
		
		GhostFigure ghostFigureThread = GhostFigure.getGhostFigure();
		ghostFigureThread.setGamePausedLock(gamePausedLock);
		ghostFigureThread.setFieldsLock(fieldsLock);
		DiamondCircleThreads.add(ghostFigureThread);
		ghostFigureThread.start();
		
		CardThread cardThread = new CardThread(currentCardLabel, cards);
		cardThread.setGamePausedLock(gamePausedLock);
		cardThread.setFieldsLock(fieldsLock);
		DiamondCircleThreads.add(cardThread);
		cardThread.start();
		
		for (int i = 0; i < numberOfPlayers; i++)
		{
			DiamondCircleThreads.add(players[i]);
			players[i].setGamePausedLock(gamePausedLock);
			players[i].setFieldsLock(fieldsLock);
			players[i].setCardThread(cardThread);
			players[i].setDescriptionThread(descriptionThread);
			players[i].start();
		}
		
		GameEnderThread gameEnderThread = new GameEnderThread(DiamondCircleThreads, countDownLatch, GAMES_RESULTS_PATH, txtTotalGamesPlayed);
		gameEnderThread.start();
		
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run()
			{
				startButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent ev)
					{
						new GameStarterThread(DiamondCircleThreads).start();
					}
				});
			}
		});
		
		
		/*
		try {
			gameEnderThread.join();
		} catch (InterruptedException e) {
			Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, e.fillInStackTrace().toString());
		}
		deserializeMovements();
		*/
	  
	}
	
	private void deserializeMovements()
	{
		for (int i = 0; i < numberOfPlayers; i++)
			for (int j = 0; j < Player.numberOfFigures(); j++)
			{
				try {
					System.out.println(players[i].getFigure(j).getFigureNumber());
					ObjectInputStream input = new ObjectInputStream(new FileInputStream("resources" + File.separator + "results" + File.separator + "movements" + File.separator + players[i].getFigure(j).getFigureNumber() + ".ser"));
					try {
						
						ArrayList<IndexPair> array = (ArrayList<IndexPair>)input.readObject();
						StringBuilder stringBuilder = new StringBuilder();
						
						for (IndexPair ip : array)
							stringBuilder.append(ip.getNumber() + "-");
						stringBuilder.deleteCharAt(stringBuilder.length()-1);
						System.out.println(stringBuilder);
						
						long timeSpentMoving = input.readLong();
						System.out.println(timeSpentMoving/1000 + " sekundi");
						System.out.println();
						
						input.close();
						
					} catch (ClassNotFoundException e) {
						Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, e.fillInStackTrace().toString());
					}
				} catch (FileNotFoundException e) {
					Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, e.fillInStackTrace().toString());
				} catch (IOException e) {
					Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, e.fillInStackTrace().toString());
				}
			}
	}
	
	public Field[][] getFields()
	{
		return fields;
	}
	
	public int getMatrixDimension()
	{
		return matrixDimension;
	}
	
	public int getNumberOfPlayers()
	{
		return numberOfPlayers;
	}
	
	public int getIndexOfIndexPair(IndexPair indexPair)
	{
		return indexPairArray.indexOf(indexPair);
	}
	
	public int getIndexPairArraySize()
	{
		return indexPairArray.size();
	}
	
	public IndexPair getIndexPair(int i)
	{
		return indexPairArray.get(i);
	}
	
	public JLabel[][] getFieldLabels()
	{
		return fieldLabels;
	}	
}