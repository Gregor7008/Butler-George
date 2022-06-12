package base;

import java.awt.Canvas;
import java.awt.SystemColor;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;

import javax.security.auth.login.LoginException;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;

import components.base.ConsoleEngine;
import net.miginfocom.swing.MigLayout;

public class GUI extends JFrame implements WindowListener{
	
	private static final long serialVersionUID = 5923282583431103590L;
	public static JTextArea console = new JTextArea();
	public static Canvas greenLED = new Canvas();
	public static Canvas redLED = new Canvas();
	public static JTextField token = new JTextField();
	public static JButton startButton = new JButton("Start");
	public static JButton stopButton = new JButton("Stop");
	public static JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
	public static JPanel panel = new JPanel();
	public static JTable table = new JTable();
	public static JTextField consoleIn = new JTextField();
	public static JProgressBar progressBar = new JProgressBar();
	public static JLabel progressLabel = new JLabel("0%");
	
	public static void main(String[] args) {
		new GUI(args);
	}
	
	public GUI(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		setSize(800, 600);
		setResizable(false);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(this);
		getContentPane().setLayout(new MigLayout("", "[300,grow][200:200:200][140:140:140][30:30:30][30:30:30]", "[30:n][20:n][20:n][240,grow][20:n]"));
		
		console.setEditable(false);
		getContentPane().add(console, "flowx,cell 0 0 1 4,grow");
		
		//TODO Create and paint green LED
		getContentPane().add(greenLED, "cell 3 0");
		
		//TODO Create and paint red LED
		getContentPane().add(redLED, "flowx,cell 4 0");
		
		try {
			token.setText(args[0]);
		} catch (IndexOutOfBoundsException e) {}
		getContentPane().add(token, "cell 1 1 4 1,growx");
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
			if (Bot.run != null) {
				if (Bot.run.jda != null) {
					Bot.run.shutdown(true);	
				}
			}
		});
		getContentPane().add(stopButton, "cell 2 2 3 1,growx,aligny center");
		
		getContentPane().add(tabbedPane, "cell 1 3 4 1,grow");
		
		tabbedPane.addTab("Info", null, panel, null);
		
		table.setBackground(SystemColor.control);
		table.setModel(new DefaultTableModel(
			new Object[][] {
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
		) {
			private static final long serialVersionUID = 6695966376904960851L;
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		});
		table.getColumnModel().getColumn(0).setResizable(false);
		table.getColumnModel().getColumn(0).setMinWidth(100);
		table.getColumnModel().getColumn(1).setResizable(false);
		table.getColumnModel().getColumn(1).setMinWidth(100);
		table.setShowHorizontalLines(false);
		table.setRowSelectionAllowed(false);
		table.setShowVerticalLines(false);
		table.setShowGrid(false);
		panel.add(table);
		
		getContentPane().add(consoleIn, "cell 0 4,growx,aligny center");
		consoleIn.setColumns(10);
		consoleIn.addActionListener(new ConsoleEngine());
		
		getContentPane().add(progressBar, "cell 1 4 3 1,growx,aligny center");
		
		getContentPane().add(progressLabel, "cell 4 4,alignx left");
		
		setVisible(true);
	}

	@Override
	public void windowOpened(WindowEvent e) {}

	@Override
	public void windowClosing(WindowEvent e) {
		if (Bot.run != null) {
			if (Bot.run.jda != null) {
				Bot.run.shutdown(true);	
			}
		}
		e.getWindow().dispose();
	}

	@Override
	public void windowClosed(WindowEvent e) {}

	@Override
	public void windowIconified(WindowEvent e) {}

	@Override
	public void windowDeiconified(WindowEvent e) {}

	@Override
	public void windowActivated(WindowEvent e) {}

	@Override
	public void windowDeactivated(WindowEvent e) {}	
}