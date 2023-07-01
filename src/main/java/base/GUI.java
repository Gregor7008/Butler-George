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
import java.util.concurrent.TimeUnit;

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

import org.jetbrains.annotations.Nullable;

import base.Bot.ShutdownReason;
import engines.base.CentralTimer;
import engines.base.ConsoleCommandListener;
import engines.base.ScrollEngine;
import engines.base.Toolbox;
import engines.data.ConfigLoader;
import engines.logging.ConsoleEngine;
import net.dv8tion.jda.api.entities.Guild;
import net.miginfocom.swing.MigLayout;

public class GUI extends JFrame implements FocusListener {
	
	public static GUI INSTANCE;
	
	private static final long serialVersionUID = 5923282583431103590L;
    
    public final JTextArea console = new JTextArea();
    public final JTextField consoleIn = new JTextField();
    public final JPasswordField password = new JPasswordField();
    
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
	
	private ImageIcon greenLEDOn;
	private ImageIcon greenLEDOff;
	private ImageIcon redLEDOn;
	private ImageIcon redLEDOff;
	private ImageIcon eyeIconRaw;
	private ImageIcon windowIcon;
    
    private long runtimeMeasuringTaskId;
    private OffsetDateTime startTime = null;
    private Duration additional = Duration.ZERO;
    private boolean invalidArguments, autostart = false;
    private String licenseKey = "";
	
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
		} catch (Throwable e) {}
		try {
            new GUI(args);
        } catch (IOException | FontFormatException e) {
            e.printStackTrace();
        }
	}
	
	public GUI(String[] args) throws IOException, FontFormatException {
		INSTANCE = this;
		this.processArguments(args);
		
		ClassLoader loader = this.getClass().getClassLoader();
		greenLEDOn = new ImageIcon(loader.getResourceAsStream("gui/green_on.png").readAllBytes());
		greenLEDOff = new ImageIcon(loader.getResourceAsStream("gui/green_off.png").readAllBytes());
		redLEDOn = new ImageIcon(loader.getResourceAsStream("gui/red_on.png").readAllBytes());
		redLEDOff = new ImageIcon(loader.getResourceAsStream("gui/red_off.png").readAllBytes());
		eyeIconRaw = new ImageIcon(loader.getResourceAsStream("gui/eye_icon.png").readAllBytes());
		windowIcon = new ImageIcon(loader.getResourceAsStream("misc/self_avatar.png").readAllBytes());
        default_font = Font.createFont(Font.TRUETYPE_FONT, loader.getResourceAsStream("gui/default_font.ttf")).deriveFont(0, 12f);
        console_font = Font.createFont(Font.TRUETYPE_FONT, loader.getResourceAsStream("gui/console_font.ttf")).deriveFont(0, 12f);
		
		setIconImage(windowIcon.getImage());
		setSize(1200, 600);
		setTitle(Bot.NAME + " - " + Bot.VERSION);
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		
		getContentPane().setLayout(new MigLayout("", "[600,grow][125:125:125][75:75:75][140:140:140][30:30:30][30:30:30]", "[30:n][20:n][20:n][20:n][20:n][510,grow][20:n]"));
		
		JScrollPane consoleScrollPane = new JScrollPane(console);
		consoleScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		consoleScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        new ScrollEngine(consoleScrollPane);
		console.setEditable(false);
		console.setFont(console_font);
		getContentPane().add(consoleScrollPane, "flowx,cell 0 0 1 6,grow");

		greenLED.setIcon(greenLEDOff);
		getContentPane().add(greenLED, "cell 4 0,alignx center");
		
		redLED.setIcon(redLEDOn);
		getContentPane().add(redLED, "flowx,cell 5 0,alignx center");
		if (!this.licenseKey.isBlank()) {
            password.setText(this.licenseKey);
            password.setForeground(Color.BLACK);
            password.setEchoChar('*');
		}
		
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
		getContentPane().add(password, "cell 1 1 4 1,grow");
		getContentPane().add(showPassword, "cell 5 1,alignx left");
		
		startButton.addActionListener(e -> {
			this.startBot();
		});
		startButton.setFont(default_font);
		getContentPane().add(startButton, "flowx,cell 1 2 2 1,grow");
		
		stopButton.addActionListener(e -> {
			if (shutdownWindowBox.isSelected()) {
				new ShutdownWindow((reasons, additionalMessage) -> this.shutdownBot(reasons.get(0), true, additionalMessage));
			} else {
				this.shutdownBot(ShutdownReason.OFFLINE, true, null);
			}
		});
		stopButton.setEnabled(false);
		stopButton.setFont(default_font);
		getContentPane().add(stopButton, "cell 3 2 3 1,grow");
		
		sdWLabel.setFont(default_font);
		getContentPane().add(sdWLabel, "cell 3 3 2 1,alignx right,aligny center");
		
		infoTable.setShowGrid(false);
		infoTable.setFont(default_font);
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
				{"J2C-Channels", 0},
				{"Push Cycle Period:", String.valueOf(ConfigLoader.PUSH_CYCLE_PERIOD) + " min."},
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
		commandTable.setFont(default_font);
		commandTable.setModel(new DefaultTableModel(
			new Object[][] {
				{"stop", "[Send Message]"},
				{"restart", ""},
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
		
		getContentPane().add(shutdownWindowBox, "cell 5 3,alignx left,aligny center");
		
		tabbedPane.addTab("Commands", null, commandTable, null);
		tabbedPane.setFont(default_font);
		getContentPane().add(tabbedPane, "cell 1 4 5 3,grow");
		
		consoleIn.addActionListener(new ConsoleCommandListener());
		consoleIn.setFont(console_font);
		getContentPane().add(consoleIn, "cell 0 6,growx,aligny center");
		
		setVisible(true);
		
		if (this.invalidArguments) {
		    ConsoleEngine.getLogger(this).warn("Encountered a problem whilst parsing arguments!");
		}
		if (this.autostart) {
		    this.startBot();
		}
	}

	public void startBot() {
		if (Bot.isShutdown()) {
		    if (this.licenseKey.isBlank()) {
		        ConsoleEngine.getLogger(this).warn("License key is empty, please provide valid key.");
		    } else {
		        if (ConfigLoader.connect(this.licenseKey)) {
		            try {
                        new Bot();
                    } catch (InterruptedException e) {
                        ConsoleEngine.getLogger(this).debug("Connection to Discords servers failed, please contact support!");
                    }
		        }
		    }
		} else {
		    ConsoleEngine.getLogger(this).debug("Bot is already running!");
		}
	}
	
	public void restartBot() {
	    if (!Bot.isShutdown()) {
	        Runtime.getRuntime().removeShutdownHook(Bot.get().getShutdownThread());
            Bot.get().shutdown(ShutdownReason.RESTART, false, null, () -> startBot());
        }
	}
	
	public void shutdownBot(ShutdownReason reason, boolean sendMessage, @Nullable String additionalMessage) {
		if (!Bot.isShutdown()) {
			Runtime.getRuntime().removeShutdownHook(Bot.get().getShutdownThread());
			Bot.get().shutdown(reason, sendMessage, additionalMessage, null);
		}
	}
    
    public void startRuntimeMeasuring() {
        startTime = OffsetDateTime.now();
        this.runtimeMeasuringTaskId = CentralTimer.get().schedule(new Runnable() {
            @Override
            public void run() {
                Duration diff = Duration.between(startTime, OffsetDateTime.now()).plus(additional);
                GUI.INSTANCE.setTableValue(3, Toolbox.convertDurationToString(diff));
            }
        }, TimeUnit.MILLISECONDS,  0, TimeUnit.SECONDS, 1);
    }
    
    public void stopRuntimeMeasuring() {
        if (startTime != null) {
            additional = Duration.between(startTime, OffsetDateTime.now());
        }
        CentralTimer.get().cancel(this.runtimeMeasuringTaskId);
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
		this.increaseCounter(4);
	}
	
	public void increaseExecutionsCounter() {
		this.increaseCounter(5);
	}
    
    public void increaseMemberCounter() {
        this.increaseCounter(7);
    }
    
    public void decreaseMemberCounter() {
        this.decreaseCounter(7);
    }
	
	public void increaseJ2CCounter() {
	    this.increaseCounter(8);
	}
	
	public void decreaseJ2CCounter() {
	    this.decreaseCounter(8);
	}
	
	public void increasePushCounter() {
		this.increaseCounter(10);
	}
	
	private void increaseCounter(int position) {
	    this.modifyCounter(position, 1);
	}
	
	private void decreaseCounter(int position) {
        this.modifyCounter(position, -1);
	}
	
	private void modifyCounter(int position, int value) {
        try {
            this.setTableValue(position, (int) this.getTableValue(position) + value);
        } catch (ClassCastException e) {
            ConsoleEngine.getLogger(this).debug("Couldn't cast table entry to counter for position: " + String.valueOf(position));
        }
	}
    
    public void updateStatistics() {
        int guildCount = 0;
        int userCount = 0;
        for (Guild guild : Bot.getAPI().getGuilds()) {
            guildCount++;
            userCount += guild.getMemberCount();
        }
        this.setTableValue(6, guildCount);
        this.setTableValue(7, userCount);
    }
    
    private void processArguments(String[] args) {
        for (int i = 0; i < args.length; i++) {
            String[] input = args[i].split(" ");
            if (input.length < 2) {
                this.invalidArguments = true;
            } else {
                for (int a = 0; a < input.length; a++) {
                    if (input[a].startsWith("--")) {
                        switch (input[a]) {
                            case "--license":
                                if (a+1 < input.length && !input[a+1].startsWith("--")) {
                                    a += 1;
                                    String value = input[a];
                                    if (!value.isBlank()) {
                                        this.licenseKey = value; 
                                    }
                                }
                                break;
                            case "--autostart":
                                this.autostart = true;
                                break;
                            default:
                                this.invalidArguments = true;
                        }
                    } else {
                        this.invalidArguments = true;
                    }
                }
            }
        }
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