package base;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.TimerTask;

import javax.security.auth.login.LoginException;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableModel;

import base.Bot.ShutdownReason;
import engines.base.ConsoleCommandListener;
import engines.data.ConfigManager;
import engines.logging.ConsoleEngine;
import net.dv8tion.jda.api.entities.Guild;
import net.miginfocom.swing.MigLayout;

public class GUI extends JFrame implements FocusListener {
	
	public static GUI INSTANCE;
	private static final long serialVersionUID = 5923282583431103590L;
	private TimerTask runtimeMeasuringTask;
	
	private final JLabel greenLED = new JLabel();
	private final JLabel redLED = new JLabel();
	private final JButton startButton = new JButton("Start");
	private final JButton stopButton = new JButton("Stop");
	private final JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
	private final JTable infoTable = new JTable();
	private final JTable commandTable = new JTable();
	private final JButton showPassword = new JButton("");
	private final JCheckBox shutdownWindowBox = new JCheckBox("");
	private final JLabel sdWLabel = new JLabel("Custom shutdown reason:");
    
    private final Font default_font;
    private final Font console_font;
	
	public final JTextArea console = new JTextArea();
	public final JTextField consoleIn = new JTextField();
	
	public final JTextField databaseIP = new JTextField();
	public final JTextField databaseName = new JTextField();
	public final JTextField botToken = new JTextField();
	public final JTextField databasePort = new JTextField();
	public final JTextField username = new JTextField();
	public final JPasswordField password = new JPasswordField();
	
	public ImageIcon greenLEDOn;
	public ImageIcon greenLEDOff;
	public ImageIcon redLEDOn;
	public ImageIcon redLEDOff;
	public ImageIcon eyeIconRaw;
	public ImageIcon windowIcon;
	
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
		} catch (Throwable e) {
			e.printStackTrace();
		}
		try {
            new GUI(args);
        } catch (IOException | FontFormatException e) {
            e.printStackTrace();
        }
	}
	
	public GUI(String[] args) throws IOException, FontFormatException {
		INSTANCE = this;
		
		ClassLoader loader = this.getClass().getClassLoader();
		greenLEDOn = new ImageIcon(loader.getResourceAsStream("gui/green_on.png").readAllBytes());
		greenLEDOff = new ImageIcon(loader.getResourceAsStream("gui/green_off.png").readAllBytes());
		redLEDOn = new ImageIcon(loader.getResourceAsStream("gui/red_on.png").readAllBytes());
		redLEDOff = new ImageIcon(loader.getResourceAsStream("gui/red_off.png").readAllBytes());
		eyeIconRaw = new ImageIcon(loader.getResourceAsStream("gui/eye_icon.png").readAllBytes());
		windowIcon = new ImageIcon(loader.getResourceAsStream("misc/self_avatar.png").readAllBytes());
        default_font = Font.createFont(Font.TRUETYPE_FONT, loader.getResourceAsStream("gui/default_font.ttf")).deriveFont(0, 11f);
        console_font = Font.createFont(Font.TRUETYPE_FONT, loader.getResourceAsStream("gui/console_font.ttf")).deriveFont(0, 13f);
		
		setIconImage(windowIcon.getImage());
		setSize(1200, 600);
		setTitle(Bot.NAME + " - " + Bot.VERSION);
		setFont(default_font);
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		getContentPane().setLayout(new MigLayout("", "[600,grow][125:125:125][75:75:75][140:140:140][30:30:30][30:30:30]", "[30:n][20:n][20:n][20:n][20:n][510,grow][20:n]"));
		
		JScrollPane consoleScrollPane = new JScrollPane(console);
		consoleScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		consoleScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		console.setEditable(false);
		console.setFont(console_font);
		getContentPane().add(consoleScrollPane, "flowx,cell 0 0 1 6,grow");

		greenLED.setIcon(greenLEDOff);
		getContentPane().add(greenLED, "cell 4 0,alignx center");
		
		redLED.setIcon(redLEDOn);
		getContentPane().add(redLED, "flowx,cell 5 0,alignx center");
		
		this.setupTextField(args, databaseIP, "Server IP", 0);
		getContentPane().add(databaseIP, "cell 1 0,growx,aligny bottom");
		
		this.setupTextField(args, databasePort, "Port", 1);
		getContentPane().add(databasePort, "cell 2 0,growx,aligny bottom");
		
		this.setupTextField(args, databaseName, "Database name", 2);
		getContentPane().add(databaseName, "cell 3 0,growx,aligny bottom");
		
		this.setupTextField(args, username, "Username", 3);
		getContentPane().add(username, "cell 1 1 2 1,grow");
		
		password.setEchoChar((char) 0);
		password.setForeground(Color.GRAY);
		password.setFont(default_font);
		password.setName("Password");
		password.setText(password.getName());
		password.addFocusListener(new FocusListener() {		
			@Override
			public void focusLost(FocusEvent e) {
				JPasswordField field = (JPasswordField) e.getComponent();
				if (String.copyValueOf(field.getPassword()).equals("")) {
					field.setText(field.getName());
					field.setForeground(Color.GRAY);
					field.setEchoChar((char) 0);
				}
			}
			@Override
			public void focusGained(FocusEvent e) {
				JPasswordField field = (JPasswordField) e.getComponent();
				if (String.copyValueOf(field.getPassword()).equals(field.getName())) {
					field.setText("");
					field.setForeground(Color.BLACK);
					field.setEchoChar('*');
				}
			}
		});
		try {
			password.setText(args[4]);
			password.setForeground(Color.BLACK);
			password.setEchoChar('*');
		} catch (IndexOutOfBoundsException e) {}
		getContentPane().add(password, "cell 3 1 2 1,grow");
		
		showPassword.setSize(30, 20);
		showPassword.setMargin(new Insets(0,0,0,0));
		Image eyeIconRescaled = eyeIconRaw.getImage().getScaledInstance(15, 15, Image.SCALE_SMOOTH);
		showPassword.setIcon(new ImageIcon(eyeIconRescaled));
		showPassword.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				password.setEchoChar((char) 0);
	        }
			@Override
	        public void mouseReleased(MouseEvent e) {
				password.setEchoChar('*');
	        }
		});
		getContentPane().add(showPassword, "cell 5 1,alignx left");
		
		this.setupTextField(args, botToken, "Bot Token", 5);
		getContentPane().add(botToken, "cell 1 2 5 1,grow");
		
		startButton.addActionListener(e -> {
			this.startBot();
		});
		getContentPane().add(startButton, "flowx,cell 1 3 2 1,grow");
		
		stopButton.addActionListener(e -> {
			if (shutdownWindowBox.isSelected()) {
				new ShutdownWindow((reasons, additionalMessage) -> this.shutdownBot(reasons.get(0), additionalMessage));
			} else {
				this.shutdownBot(ShutdownReason.OFFLINE, null);
			}
		});
		stopButton.setEnabled(false);
		getContentPane().add(stopButton, "cell 3 3 3 1,grow");
		
		getContentPane().add(sdWLabel, "cell 3 4 2 1,alignx right,aligny center");
		
		getContentPane().add(shutdownWindowBox, "cell 5 4,alignx left,aligny center");
		
		infoTable.setShowGrid(false);
		infoTable.setFont(default_font.deriveFont(11f));
		infoTable.setModel(new DefaultTableModel(
			new Object[][] {
				{"Name:", Bot.NAME},
				{"Version:", Bot.VERSION},
				{"ID:", Bot.ID},
				{"Runtime:", "00:00:00:00"},
				{"Errors:", 0},
				{"Executions:", 0},
				{"Servers:", 0},
				{"Users:", 0},
				{"Push Paused:", false},
				{"Push Cycle Period:", String.valueOf(ConfigManager.PUSH_CYCLE_PERIOD) + " min."},
				{"Total Pushs:", 0}
			},
			new String[] {
				"key", "value"
			}
		) {private static final long serialVersionUID = 4012626449837340333L;
			
		   public boolean isCellEditable(int row, int column) {
				return false;
		   }
		});
		infoTable.getColumnModel().getColumn(0).setResizable(false);
		tabbedPane.addTab("Info", null, infoTable, null);

		commandTable.setShowGrid(false);
		commandTable.setFont(default_font.deriveFont(11f));
		commandTable.setModel(new DefaultTableModel(
			new Object[][] {
				{"stop", ""},
				{"exit", ""},
				{"warn", "[Guild ID]  [User ID]"},
				{"pushCache", ""},
				{"printCache", ""},
				{"clearCache", ""},
				{"printEventAwaiter", ""},
				{"clearEventAwaiter", ""}
			},
			new String[] {
				"key", "value"
			}
		) {private static final long serialVersionUID = 7041769283649777050L;

		public boolean isCellEditable(int row, int column) {
				return false;
		   }
		});
		commandTable.getColumnModel().getColumn(0).setResizable(false);
		
		tabbedPane.addTab("Commands", null, commandTable, null);
		getContentPane().add(tabbedPane, "cell 1 5 5 2,grow");
		
		consoleIn.addActionListener(new ConsoleCommandListener());
		getContentPane().add(consoleIn, "cell 0 6,growx,aligny center");
		
		setVisible(true);
		
		try {
			if (Boolean.parseBoolean(args[6])) {
				this.startBot();
			}
		} catch (IndexOutOfBoundsException e) {}
	}

	private void startBot() {
		if (Bot.INSTANCE == null || Bot.INSTANCE.isShutdown()) {
			try {
				new Bot(botToken.getText(), databaseIP.getText(), databasePort.getText(), databaseName.getText(), username.getText(), String.copyValueOf(password.getPassword()));
			} catch (LoginException | InterruptedException | IOException e) {
				ConsoleEngine.getLogger(Bot.class).error("Bot instanciation failed - Check token validity!");
				Bot.INSTANCE.kill();
			} catch (IllegalArgumentException e) {
				ConsoleEngine.getLogger(Bot.class).error("Bot instanciation failed - " + e.getMessage());
				Bot.INSTANCE.kill();
			}
		}
	}
	
	private void setupTextField(String[] args, JTextField textField, String name, int argsIndex) {
		textField.setForeground(Color.GRAY);
		textField.setFont(default_font);
		textField.setName(name);
		textField.setText(name);
		textField.addFocusListener(this);
		try {
			textField.setText(args[argsIndex]);
			textField.setForeground(Color.BLACK);
		} catch (IndexOutOfBoundsException e) {}
	}
	
	public void shutdownBot(ShutdownReason reason, String additionalMessage) {
		if (Bot.INSTANCE != null && !Bot.INSTANCE.isShutdown()) {
			Runtime.getRuntime().removeShutdownHook(Bot.INSTANCE.getShutdownThread());
			Bot.INSTANCE.shutdown(reason, additionalMessage);
		}
	}
	
	public void setBotRunning(boolean status) {
		if (status) {
			greenLED.setIcon(greenLEDOn);
			redLED.setIcon(redLEDOff);
		} else {
			redLED.setIcon(redLEDOn);
			greenLED.setIcon(greenLEDOff);
		}
		stopButton.setEnabled(status);
		startButton.setEnabled(!status);
	}
	
	public void increaseErrorCounter() {
		this.setTableValue(4, (int) this.getTableValue(4) + 1);
	}
	
	public void increaseExecutionsCounter() {
		this.setTableValue(5, (int) this.getTableValue(5) + 1);
	}
	
	public void increasePushCounter() {
		this.setTableValue(10, (int) this.getTableValue(10) + 1);
	}
	
	public void increaseMemberCounter() {
		this.setTableValue(7, (int) this.getTableValue(7) + 1);
	}
	
	public void decreaseMemberCounter() {
		this.setTableValue(7, (int) this.getTableValue(7) - 1);
	}
	
	public void updateErrorBoolean(boolean newValue) {
		this.setTableValue(8, newValue);
	}
	
	public boolean getErrorBoolean() {
	    return (boolean) this.getTableValue(8);
	}
	
	public void updateStatistics() {
		int guildCount = 0;
		int userCount = 0;
		for (Guild guild : Bot.INSTANCE.jda.getGuilds()) {
			guildCount++;
			userCount += guild.getMemberCount();
		}
		this.setTableValue(6, guildCount);
		this.setTableValue(7, userCount);
	}
	
	OffsetDateTime startTime = null;
	Duration additional = Duration.ZERO;
	
	public void startRuntimeMeasuring() {
		startTime = OffsetDateTime.now();
		runtimeMeasuringTask = new TimerTask() {
			@Override
			public void run() {
				Duration diff = Duration.between(startTime, OffsetDateTime.now()).plus(additional);
				GUI.INSTANCE.setTableValue(3, ConfigManager.convertDurationToString(diff));
			}
		};
		Bot.INSTANCE.getTimer().schedule(runtimeMeasuringTask, 0, 1000);
	}
	
	public void stopRuntimeMeasuring() {
	    if (startTime != null) {
	        additional = Duration.between(startTime, OffsetDateTime.now());
	    }
		runtimeMeasuringTask.cancel();
	}
	
	public void setTableValue(int row, Object value) {
		infoTable.getModel().setValueAt(value, row, 1);
		infoTable.repaint();
	}
	
	public Object getTableValue(int row) {
		return infoTable.getModel().getValueAt(row, 1);
	}
	
	@Override
	public void focusGained(FocusEvent e) {
		JTextField textField = (JTextField) e.getComponent();
		if (textField.getText().equals(textField.getName())) {
			textField.setForeground(Color.BLACK);
			textField.setText("");
		}
	}

	@Override
	public void focusLost(FocusEvent e) {
		JTextField textField = (JTextField) e.getComponent();
		if (textField.getText().equals(textField.getName()) || textField.getText().equals("")) {
			textField.setForeground(Color.GRAY);
			textField.setText(textField.getName());
		}
	}	
}