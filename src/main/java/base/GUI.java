package base;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Timer;
import java.util.TimerTask;

import javax.security.auth.login.LoginException;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;

import components.base.ConsoleEngine;
import net.miginfocom.swing.MigLayout;

public class GUI extends JFrame implements WindowListener{
	
	public static GUI get;
	private static final long serialVersionUID = 5923282583431103590L;
	
	public static JTextArea console = new JTextArea();
	public static JScrollPane scrollPane = new JScrollPane(console);
	public static JLabel greenLED = new JLabel();
	public static JLabel redLED = new JLabel();
	public static JTextField token = new JTextField();
	public static JButton startButton = new JButton("Start");
	public static JButton stopButton = new JButton("Stop");
	public static JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
	public static JTextField consoleIn = new JTextField();
	public static JProgressBar progressBar = new JProgressBar();
	public static JLabel progressLabel = new JLabel("0%");
	public static JTable infoTable = new JTable();
	
	private Timer runtimeRefresher;
	public ImageIcon greenLEDOn;
	public ImageIcon greenLEDOff;
	public ImageIcon redLEDOn;
	public ImageIcon redLEDOff;
	
	
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
		} catch (Throwable e) {
			e.printStackTrace();
		}
		new GUI(args);
	}
	
	public GUI(String[] args) {
		get = this;
		try {
			greenLEDOn = new ImageIcon(this.getClass().getClassLoader().getResourceAsStream("gui/green_on.png").readAllBytes());
			greenLEDOff = new ImageIcon(this.getClass().getClassLoader().getResourceAsStream("gui/green_off.png").readAllBytes());
			redLEDOn = new ImageIcon(this.getClass().getClassLoader().getResourceAsStream("gui/red_on.png").readAllBytes());
			redLEDOff = new ImageIcon(this.getClass().getClassLoader().getResourceAsStream("gui/red_off.png").readAllBytes());
		} catch (IOException e) {}
		
		setSize(1200, 600);
		setTitle(Bot.name + " - " + Bot.version);
		setResizable(false);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(this);
		getContentPane().setLayout(new MigLayout("", "[600,grow][200:200:200][140:140:140][30:30:30][30:30:30]", "[30:n][20:n][20:n][510][20:n]"));
		
		console.setEditable(false);
		
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		getContentPane().add(scrollPane, "flowx,cell 0 0 1 4,grow");
		
		greenLED.setIcon(greenLEDOff);
		getContentPane().add(greenLED, "cell 3 0");
		
		redLED.setIcon(redLEDOn);
		getContentPane().add(redLED, "flowx,cell 4 0");
		
		token.setText("");
		try {
			token.setText(args[0]);
		} catch (IndexOutOfBoundsException e) {}
		getContentPane().add(token, "cell 1 1 4 1,grow");
		token.setColumns(10);
		
		startButton.addActionListener(e -> {
			if (Bot.run == null) {
				try {
					new Bot(token.getText());
				} catch (LoginException | InterruptedException | IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		getContentPane().add(startButton, "flowx,cell 1 2,growx,aligny center");
		
		stopButton.addActionListener(e -> {
			if (Bot.run != null && Bot.run.jda != null) {
				Bot.run.shutdown(true);	
			}
		});
		getContentPane().add(stopButton, "cell 2 2 3 1,growx,aligny center");
		
		getContentPane().add(tabbedPane, "cell 1 3 4 1,grow");
		infoTable.setShowGrid(false);
		infoTable.setModel(new DefaultTableModel(
			new Object[][] {
				{"Name:", Bot.name},
				{"Version:", Bot.version},
				{"ID:", Bot.id},
				{"Runtime:", "00:00:00:00"},
				{"Errors:", 0},
				{"Executions:", 0},
				{"Servers:", 0},
				{"Users:", 0},
				{"Push Paused:", false},
				{null, null},
				{null, null},
				{null, null},
				{null, null},
				{null, null},
				{null, null},
				{null, null},
				{null, null},
				{null, null},
				{null, null},
				{null, null},
				{null, null},
				{null, null},
				{null, null},
				{null, null},
				{null, null},
			},
			new String[] {
				"key", "value"
			}
		) {private static final long serialVersionUID = -4012626449837340333L;
		
		   public boolean isCellEditable(int row, int column) {
				return false;
		   }
		});
		infoTable.getColumnModel().getColumn(0).setResizable(false);
		
		tabbedPane.addTab("Info", null, infoTable, null);
		
		getContentPane().add(consoleIn, "cell 0 4,growx,aligny center");
		consoleIn.setColumns(10);
		consoleIn.addActionListener(new ConsoleEngine());
		
		getContentPane().add(progressBar, "cell 1 4 3 1,growx,aligny center");
		
		getContentPane().add(progressLabel, "cell 4 4,alignx left");
		
		setVisible(true);
	}
	
	public void setBotRunning(boolean status) {
		if (status) {
			greenLED.setIcon(greenLEDOn);
			redLED.setIcon(redLEDOff);
		} else {
			redLED.setIcon(redLEDOn);
			greenLED.setIcon(greenLEDOff);
		}
	}
	
	public void setProgress(int progress) {
		if (0 <= progress && progress <= 100) {
			progressBar.setValue(progress);
			progressLabel.setText(String.valueOf(progress) + "%");
		}
	}
	
	public void increaseErrorCounter() {
		this.setTableValue(4, (int) this.getTableValue(4) + 1);
	}
	
	public void increaseExecutionsCounter() {
		this.setTableValue(5, (int) this.getTableValue(5) + 1);
	}
	
	public void updateStatistics() {
		GUI.get.setTableValue(6, Bot.run.jda.getGuilds().size());
		GUI.get.setTableValue(7, Bot.run.jda.getUsers().size());
	}
	
	public void startRuntimeMeasuring() {
		OffsetDateTime startTime = OffsetDateTime.now();
		runtimeRefresher = new Timer();
		runtimeRefresher.schedule(new TimerTask() {
			@Override
			public void run() {
				Duration diff = Duration.between(startTime, OffsetDateTime.now());
				GUI.get.setTableValue(3, String.format("%02d:%02d:%02d:%02d",
						diff.toDays(),
                        diff.toHours(), 
                        diff.toMinutesPart(), 
                        diff.toSecondsPart()));
			}
		}, 0, 1000);
	}
	
	public void stopRuntimeMeasuring() {
		runtimeRefresher.cancel();
		runtimeRefresher = null;
	}
	
	public void setTableValue(int row, Object value) {
		infoTable.getModel().setValueAt(value, row, 1);
		infoTable.repaint();
	}
	
	public Object getTableValue(int row) {
		return infoTable.getModel().getValueAt(row, 1);
	}

	@Override
	public void windowOpened(WindowEvent e) {}

	@Override
	public void windowClosing(WindowEvent e) {
		if (Bot.run != null && Bot.run.jda != null) {
			Bot.run.shutdown(true);	
		}
		e.getWindow().dispose();
	}

	@Override
	public void windowClosed(WindowEvent e) {
		System.exit(0);
	}

	@Override
	public void windowIconified(WindowEvent e) {}

	@Override
	public void windowDeiconified(WindowEvent e) {}

	@Override
	public void windowActivated(WindowEvent e) {}

	@Override
	public void windowDeactivated(WindowEvent e) {}	
}