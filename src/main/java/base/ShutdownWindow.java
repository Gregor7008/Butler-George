package base;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.WindowConstants;

import base.Bot.ShutdownReason;
import net.miginfocom.swing.MigLayout;

public class ShutdownWindow extends JFrame implements FocusListener {
	
	private static final long serialVersionUID = 6729151169880125374L;	
	
	private final JLabel checkLabel = new JLabel("Select shutdown reason:");
	private final JCheckBox reasonOffline = new JCheckBox("Going offline");
	private final JCheckBox reasonMaintenance = new JCheckBox("Doing maintenance");
	private final JCheckBox reasonRestart = new JCheckBox("Restarting");
	private final JCheckBox reasonFatal = new JCheckBox("Fatal error occured");
	private final JTextArea messageAddon = new JTextArea();
	private final JButton cancelButton = new JButton("Cancel");
	private final JButton continueButton = new JButton("Finish");
	
	public ShutdownWindow(BiConsumer<List<ShutdownReason>, String> selectionConsumer) {
		setAlwaysOnTop(true);
		setType(Type.POPUP);
		setSize(600, 300);
		setResizable(false);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setLocationRelativeTo(GUI.INSTANCE);
		getContentPane().setLayout(new MigLayout("", "[][grow][][90:90:90][90:90:90]", "[][25:25:25][25:25:25][][][][][grow][25:25:25]"));
		
		checkLabel.setFont(new Font("Tahoma", Font.BOLD, 12));
		getContentPane().add(checkLabel, "cell 3 2 2 1");
		
		getContentPane().add(reasonOffline, "cell 3 3 2 1");
		
		getContentPane().add(reasonMaintenance, "cell 3 4 2 1");
		
		getContentPane().add(reasonRestart, "cell 3 5 2 1");
		
		getContentPane().add(reasonFatal, "cell 3 6 2 1");
		
		JScrollPane messageAddonScrollPane = new JScrollPane(messageAddon);
		messageAddonScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		messageAddonScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		messageAddon.setLineWrap(true);
		messageAddon.setWrapStyleWord(true);
		messageAddon.addFocusListener(this);
		messageAddon.setForeground(Color.GRAY);
		messageAddon.setName("Input additional message... (optional)");
		messageAddon.setText(messageAddon.getName());
		getContentPane().add(messageAddonScrollPane, "cell 0 0 2 9,grow");
		
		cancelButton.addActionListener(pressEvent -> {
			this.dispose();
		});
		getContentPane().add(cancelButton, "cell 3 8,growx");
		
		continueButton.addActionListener(pressEvent -> {
			List<ShutdownReason> selections = new ArrayList<>();
			if (reasonFatal.isSelected()) {
				selections.add(ShutdownReason.FATAL_ERROR);
			}
			if (reasonRestart.isSelected()) {
				selections.add(ShutdownReason.RESTART);
			}
			if (reasonMaintenance.isSelected()) {
				selections.add(ShutdownReason.MAINTENANCE);
			}
			if (reasonOffline.isSelected()) {
				selections.add(ShutdownReason.OFFLINE);
			}
			String additionalMessage = messageAddon.getText();
			if (messageAddon.getText().equals(messageAddon.getName())) {
				additionalMessage = "";
			}
			selectionConsumer.accept(selections, additionalMessage);
			this.dispose();
		});
		getContentPane().add(continueButton, "cell 4 8,growx");
		
		setVisible(true);
	}

	@Override
	public void focusGained(FocusEvent e) {
		JTextArea target = (JTextArea) e.getComponent();
		if (target.getText().equals(target.getName())) {
			target.setText("");
			target.setForeground(Color.BLACK);
		}
	}

	@Override
	public void focusLost(FocusEvent e) {
		JTextArea target = (JTextArea) e.getComponent();
		if (target.getText().equals("")) {
			target.setText(target.getName());
			target.setForeground(Color.GRAY);
		}
	}
}