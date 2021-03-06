package com.flynnovations.game.client;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.table.DefaultTableModel;

import com.flynnovations.game.shared.GameInfo;
import com.flynnovations.game.shared.PlayerInfo;
import com.flynnovations.game.shared.Question;

public class ClientUI extends JFrame implements Runnable {
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTable tblGames;
	private JTextField tfPlayerName;
	private JTextField tfGameName;
	private JTable tblPlayers;
	private JProgressBar pbQuestionTimer;
	private JTextPane tpAnswer1;
	private JTextPane tpAnswer2;
	private JTextPane tpAnswer3;
	private JTextPane tpAnswer4;
	private JTextPane ftfQuestionText;
	private JButton btAnswer1;
	private JButton btAnswer2;
	private JButton btAnswer3;
	private JButton btAnswer4;
	private JTextPane tpBuzzTimer;
	private JTextPane tpEventInfo;
	private CardLayout cards;
	private javax.swing.Timer gameTimer;
	private javax.swing.Timer answerTimer;
	private JButton btnBuzz;

	private Client client;
	public double gameTimerCounter = 250;
	public double gameTimerMax = 250;
	public final double gameTimerDefault = 250;
	private int buzzTimeCounter = 5;
	private ActionListener gameTimerListener = new ActionListener(){
		public void actionPerformed(ActionEvent ae) {
			gameTimerCounter--;
			pbQuestionTimer.setValue((int) Math.ceil(gameTimerCounter / gameTimerMax *100));
			if (gameTimerCounter == 0) {
				//TODO: Game cycle is over!
				gameTimer.stop();
				gameTimerCounter = gameTimerMax;
				client.endQuestion();

			}
		}
	};

	private ActionListener answerTimerListener = new ActionListener() {
		public void actionPerformed(ActionEvent ae) {
			buzzTimeCounter--;
			tpBuzzTimer.setText("" + buzzTimeCounter);
			if (buzzTimeCounter == 0) {
				answerTimer.stop();
				//Buzz cycle is over!
				sendAnswer(0, true);

				buzzTimeCounter = 5;
			}
		}
	};

	private void sendAnswer(int answerId, boolean answered) {
		toggleAnswerButtons(false);
		answerTimer.stop();
		if (answered) {
			showEventInfo("You answered " + ((answerId==1) ? "correct" : "poorly"));
		}
		client.answerQuestion(answerId, answered);

	}

	private void buzz() {
		btnBuzz.setEnabled(false);
		btnBuzz.setVisible(false);
		gameTimer.stop();
		client.sendBuzz();
		toggleAnswerButtons(true);
		toggleBuzzHideAnswers(true);
		hideEventInfo();
		tpBuzzTimer.setText("5");
		buzzTimeCounter = 5;
		answerTimer.start();
	}

	public void OtherPlayerBuzz(String playerName) {
		gameTimer.stop();
		showEventInfo(playerName + " Buzzed in!");
	}

	public void OtherPlayerWin(String playerName) {
		showEventInfo(playerName + " Answered Correct!");	
	}

	public void OtherPlayerFail() {
		if(btnBuzz.isEnabled())
		{
			hideEventInfo();
			gameTimer.start();
		}
	}

	private void joinGame() {
		int selectedRow = tblGames.getSelectedRow();
		if (selectedRow > -1) {
			DefaultTableModel dm = (DefaultTableModel) tblGames.getModel();
			int gameId = (int) dm.getValueAt(selectedRow, 2);
			//call join game in client
			client.joinGame(gameId);

			//switch to game card
			cards.last(contentPane);
			this.invalidate();

		}
	}

	private void leaveGame() {
		gameTimer.stop();
		showEventInfo("Waiting on quesiton");
		System.out.println("Sending answer now.");
		sendAnswer(0, false);
		System.out.println("Leaving game now.");
		client.leaveGame();
		System.out.println("requesting games now.");
		client.requestGames();
		cards.previous(contentPane);
		cards.previous(contentPane);
		this.invalidate();
	}

	private void toggleAnswerButtons(boolean areEnabled) {
		btAnswer1.setEnabled(areEnabled);
		btAnswer2.setEnabled(areEnabled);
		btAnswer3.setEnabled(areEnabled);
		btAnswer4.setEnabled(areEnabled);

		this.invalidate();
	}

	private void toggleBuzzHideAnswers(boolean showAnswers) {
		tpAnswer1.setVisible(showAnswers);
		tpAnswer2.setVisible(showAnswers);
		tpAnswer3.setVisible(showAnswers);
		tpAnswer4.setVisible(showAnswers);
		tpBuzzTimer.setVisible(showAnswers);

		this.invalidate();
	}

	private void showEventInfo(String eventInfo) {
		tpEventInfo.setText(eventInfo);
		tpEventInfo.setVisible(true);
		this.invalidate();
	}

	private void hideEventInfo() {
		tpEventInfo.setVisible(false);
		this.invalidate();
	}

	/**
	 * Create the frame.
	 */
	public ClientUI(Client client) {
		this.client = client;
		this.gameTimer = new javax.swing.Timer(100, gameTimerListener);
		this.answerTimer = new javax.swing.Timer(1000, answerTimerListener);

		setBackground(Color.WHITE);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 800, 600);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		cards = new CardLayout(0, 0);
		contentPane.setLayout(cards);

		JPanel pnlWelcome = new JPanel();
		pnlWelcome.setBackground(Color.WHITE);
		contentPane.add(pnlWelcome, "name_17086660191406");
		pnlWelcome.setLayout(null);

		JTextPane tpWelcome = new JTextPane();
		tpWelcome.setEditable(false);
		tpWelcome.setBounds(196, 11, 331, 107);
		tpWelcome.setText("Welcome to CRAAAZZYYY TRIVIA\r\n\r\nTo get started, please enter your name");
		tpWelcome.setFont(new Font("Tahoma", Font.BOLD, 16));
		pnlWelcome.add(tpWelcome);

		tfPlayerName = new JTextField();
		tfPlayerName.setFont(new Font("Tahoma", Font.BOLD, 16));
		tfPlayerName.setBounds(327, 132, 200, 30);
		pnlWelcome.add(tfPlayerName);
		tfPlayerName.setColumns(10);

		JLabel lblPlayerName = new JLabel("Player Name:");
		lblPlayerName.setFont(new Font("Tahoma", Font.BOLD, 16));
		lblPlayerName.setBounds(196, 132, 121, 31);
		pnlWelcome.add(lblPlayerName);

		JButton btnGetStarted = new JButton("Get Started!");
		btnGetStarted.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String playerName = tfPlayerName.getText().equals("") ? "Anon" : tfPlayerName.getText();
				client.setPlayerName(playerName);
				cards.next(contentPane);
				client.requestGames();
				//loadGames(null);
			}
		});


		btnGetStarted.setFont(new Font("Tahoma", Font.BOLD, 16));
		btnGetStarted.setBounds(281, 211, 142, 36);
		pnlWelcome.add(btnGetStarted);

		JPanel pnlGameSelect = new JPanel();
		pnlGameSelect.setBackground(Color.WHITE);
		contentPane.add(pnlGameSelect, "name_17080732122213");
		pnlGameSelect.setLayout(null);

		JScrollPane spGames = new JScrollPane();
		spGames.setBounds(126, 143, 472, 299);
		pnlGameSelect.add(spGames);

		tblGames = new JTable();
		spGames.setViewportView(tblGames);
		tblGames.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		tblGames.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tblGames.setModel(new DefaultTableModel(
				new Object[][] {
					{"Test Game", new Integer(5), new Integer(0)},
				},
				new String[] {
						"Game Name", "Number Of Players", "Game ID"
				}
				) {
			private static final long serialVersionUID = 1L;
			Class[] columnTypes = new Class[] {
					String.class, Integer.class, Integer.class
			};
			public Class getColumnClass(int columnIndex) {
				return columnTypes[columnIndex];
			}
		});
		tblGames.getColumnModel().getColumn(0).setPreferredWidth(280);
		tblGames.getColumnModel().getColumn(1).setPreferredWidth(100);
		tblGames.setCellSelectionEnabled(true);


		JButton btnJoinGame = new JButton("Join Game!");
		btnJoinGame.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				joinGame();
			}
		});
		btnJoinGame.setBounds(126, 453, 120, 25);
		pnlGameSelect.add(btnJoinGame);

		JButton btnCreateGame = new JButton("Create Game");
		btnCreateGame.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cards.next(contentPane);
			}
		});
		btnCreateGame.setBounds(478, 453, 120, 25);
		pnlGameSelect.add(btnCreateGame);

		JTextPane txtpnCurrentGameList = new JTextPane();
		txtpnCurrentGameList.setEditable(false);
		txtpnCurrentGameList.setFont(new Font("Tahoma", Font.BOLD, 16));
		txtpnCurrentGameList.setText("Here is a listing of the current games running.\r\n\r\nPlease select a game, or if you want create a new one!");
		txtpnCurrentGameList.setBounds(126, 11, 472, 98);
		pnlGameSelect.add(txtpnCurrentGameList);

		JButton btRefresh = new JButton("Refresh");
		btRefresh.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				client.requestGames();

			}
		});
		btRefresh.setBounds(306, 453, 120, 25);
		pnlGameSelect.add(btRefresh);

		JPanel pnlCreateGame = new JPanel();
		pnlCreateGame.setBackground(Color.WHITE);
		contentPane.add(pnlCreateGame, "name_17089734636462");
		pnlCreateGame.setLayout(null);

		JTextPane tpCreateGame = new JTextPane();
		tpCreateGame.setEditable(false);
		tpCreateGame.setBounds(161, 5, 452, 126);
		tpCreateGame.setText("Create A new Game\r\n\r\nTo create a game, enter the name of the game, you will automatically be joined.");
		tpCreateGame.setFont(new Font("Tahoma", Font.BOLD, 16));
		pnlCreateGame.add(tpCreateGame);

		tfGameName = new JTextField();
		tfGameName.setFont(new Font("Tahoma", Font.BOLD, 16));
		tfGameName.setColumns(10);
		tfGameName.setBounds(292, 157, 215, 30);
		pnlCreateGame.add(tfGameName);

		JLabel lblGameName = new JLabel("Game Name:");
		lblGameName.setFont(new Font("Tahoma", Font.BOLD, 16));
		lblGameName.setBounds(161, 157, 121, 31);
		pnlCreateGame.add(lblGameName);

		JButton btnDoCreate = new JButton("Create Game!");
		btnDoCreate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String gameName = tfGameName.getText().equals("") ? "" : tfGameName.getText();
				client.createGame(gameName);
				tfGameName.setText("");
				cards.previous(contentPane);
			}
		});
		btnDoCreate.setFont(new Font("Tahoma", Font.BOLD, 16));
		btnDoCreate.setBounds(161, 236, 160, 36);
		pnlCreateGame.add(btnDoCreate);

		JButton btnCancelCreateGame = new JButton("Cancel");
		btnCancelCreateGame.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cards.previous(contentPane);
			}
		});
		btnCancelCreateGame.setFont(new Font("Tahoma", Font.BOLD, 16));
		btnCancelCreateGame.setBounds(347, 236, 160, 36);
		pnlCreateGame.add(btnCancelCreateGame);

		JPanel pnlGame = new JPanel();
		pnlGame.setBackground(Color.WHITE);
		contentPane.add(pnlGame, "name_17091426414931");
		pnlGame.setLayout(null);

		JScrollPane spPlayers = new JScrollPane();
		spPlayers.setBounds(10, 50, 300, 450);
		pnlGame.add(spPlayers);

		tblPlayers = new JTable();
		spPlayers.setViewportView(tblPlayers);
		tblPlayers.setModel(new DefaultTableModel(
				new Object[][] {
				},
				new String[] {
						"Player Name", "Score"
				}
				));
		tblPlayers.getColumnModel().getColumn(0).setPreferredWidth(250);
		tblPlayers.getColumnModel().getColumn(1).setPreferredWidth(50);

		JButton btnLeaveGame = new JButton("Leave Game");
		btnLeaveGame.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				leaveGame();
			}
		});
		btnLeaveGame.setBounds(10, 506, 120, 35);
		pnlGame.add(btnLeaveGame);
		
				tpEventInfo = new JTextPane();
				tpEventInfo.setEditable(false);
				tpEventInfo.setFont(new Font("Tahoma", Font.BOLD, 36));
				tpEventInfo.setForeground(Color.WHITE);
				tpEventInfo.setBackground(Color.BLACK);
				tpEventInfo.setText("Waiting On Question");
				tpEventInfo.setBounds(320, 50, 444, 450);
				pnlGame.add(tpEventInfo);

		tpBuzzTimer = new JTextPane();
		tpBuzzTimer.setEditable(false);
		tpBuzzTimer.setFont(new Font("Tahoma", Font.BOLD, 72));
		tpBuzzTimer.setText("5");
		tpBuzzTimer.setBounds(491, 419, 92, 81);
		pnlGame.add(tpBuzzTimer);

		//TODO: buzz timer logic
		tpBuzzTimer.setVisible(false);
		
		JTextPane tpQuestionDisplay = new JTextPane();
		tpQuestionDisplay.setEditable(false);
		tpQuestionDisplay.setText("Crazy Trivia Time!");
		tpQuestionDisplay.setFont(new Font("Tahoma", Font.BOLD, 16));
		tpQuestionDisplay.setBounds(320, 11, 452, 35);
		pnlGame.add(tpQuestionDisplay);

		ftfQuestionText = new JTextPane();
		ftfQuestionText.setBounds(320, 54, 444, 100);
		pnlGame.add(ftfQuestionText);

		btAnswer1 = new JButton("1");
		btAnswer1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				sendAnswer(1, true);
			}
		});
		btAnswer1.setBounds(320, 215, 60, 40);
		pnlGame.add(btAnswer1);

		btAnswer2 = new JButton("2");
		btAnswer2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				sendAnswer(2, true);
			}
		});
		btAnswer2.setBounds(320, 266, 60, 40);
		pnlGame.add(btAnswer2);

		btAnswer3 = new JButton("3");
		btAnswer3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				sendAnswer(3, true);
			}
		});
		btAnswer3.setBounds(320, 317, 60, 40);
		pnlGame.add(btAnswer3);

		btAnswer4 = new JButton("4");
		btAnswer4.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				sendAnswer(4, true);
			}
		});
		btAnswer4.setBounds(320, 368, 60, 40);
		pnlGame.add(btAnswer4);

		tpAnswer1 = new JTextPane();
		tpAnswer1.setEditable(false);
		tpAnswer1.setBounds(390, 218, 374, 37);
		pnlGame.add(tpAnswer1);

		tpAnswer2 = new JTextPane();
		tpAnswer2.setEditable(false);
		tpAnswer2.setBounds(390, 269, 374, 37);
		pnlGame.add(tpAnswer2);

		tpAnswer3 = new JTextPane();
		tpAnswer3.setEditable(false);
		tpAnswer3.setBounds(390, 320, 374, 37);
		pnlGame.add(tpAnswer3);

		tpAnswer4 = new JTextPane();
		tpAnswer4.setEditable(false);
		tpAnswer4.setBounds(390, 371, 374, 37);
		pnlGame.add(tpAnswer4);

		btnBuzz = new JButton("BUZZ IN!");
		btnBuzz.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				buzz();
			}
		});
		btnBuzz.setFont(new Font("Tahoma", Font.BOLD, 16));
		btnBuzz.setBounds(320, 419, 444, 81);
		pnlGame.add(btnBuzz);

		pbQuestionTimer = new JProgressBar();
		pbQuestionTimer.setValue(100);
		pbQuestionTimer.setBounds(320, 171, 444, 20);
		pnlGame.add(pbQuestionTimer);
	}

	public void loadGames(ArrayList<GameInfo> games) {
		DefaultTableModel dm = (DefaultTableModel) tblGames.getModel();

		int rowCount = dm.getRowCount();
		for (int i = 0; i < rowCount; i++){
			dm.removeRow(0);
		}

		dm.fireTableRowsDeleted(0, rowCount);

		for (int i=0;i<games.size();i++) {
			dm.addRow(new Object[] {games.get(i).gameName, games.get(i).numberOfPlayers, games.get(i).gameId});	
			dm.fireTableRowsInserted(0, i);
		}
		dm.fireTableDataChanged();
		this.invalidate();

	}

	public void loadPlayers(HashSet<PlayerInfo> playerInfo) {
		DefaultTableModel dm = (DefaultTableModel) tblPlayers.getModel();

		int rowCount = dm.getRowCount();
		for (int i=0; i<rowCount;i++) {
			dm.removeRow(0);
		}

		dm.fireTableRowsDeleted(0, rowCount);

		for (PlayerInfo p : playerInfo) {
			dm.addRow(new Object[] {p.playerName, p.score});
			dm.fireTableRowsInserted(0, dm.getRowCount());
		}
		dm.fireTableDataChanged();
		this.invalidate();
	}

	public void resetBuzz() {
		answerTimer.stop();
		btnBuzz.setEnabled(true);
		btnBuzz.setVisible(true);
		toggleAnswerButtons(false);
		toggleBuzzHideAnswers(false);
		tpBuzzTimer.setText("5");
		tpBuzzTimer.setVisible(false);
		buzzTimeCounter = 5;
	}
	
	public void displayQuestion(Question q) {
		this.gameTimerCounter = gameTimerMax;
		gameTimer.start();
		toggleAnswerButtons(false);
		toggleBuzzHideAnswers(false);
		hideEventInfo();
		this.ftfQuestionText.setText(q.getQuestion());
		this.tpAnswer1.setText(q.getAnswer1());
		this.tpAnswer2.setText(q.getAnswer2());
		this.tpAnswer3.setText(q.getAnswer3());
		this.tpAnswer4.setText(q.getAnswer4());
		btnBuzz.setEnabled(true);
		btnBuzz.setVisible(true);
		contentPane.repaint();
		gameTimer.start();
	}

	@Override
	public void run() {
		try {
			this.setVisible(true);
		} catch (Exception e) {
			//last chance exception
			e.printStackTrace();
		}
	}
}
