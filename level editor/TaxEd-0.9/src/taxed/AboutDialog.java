package taxed;

import javax.swing.JDialog;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import java.awt.GridBagConstraints;
import javax.swing.JButton;
import java.awt.Insets;
import javax.swing.ImageIcon;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Component;
import java.awt.Dimension;
import javax.swing.Box;
import javax.swing.SwingConstants;
import java.awt.Font;
import java.io.File;

public class AboutDialog extends JDialog {
	public AboutDialog(final TaxEdFrame taxEd)
	{
		setTitle("About TaxEd...");
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setModalityType(JDialog.ModalityType.APPLICATION_MODAL);
		setSize(new Dimension(300, 400));
		setResizable(false);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		getContentPane().setLayout(gridBagLayout);
		
		JLabel lblTest = new JLabel(new ImageIcon(this.getClass().getResource("/taxed/sonic.gif")));
		GridBagConstraints gbc_lblTest = new GridBagConstraints();
		gbc_lblTest.insets = new Insets(0, 0, 5, 0);
		gbc_lblTest.fill = GridBagConstraints.BOTH;
		gbc_lblTest.gridwidth = 2;
		gbc_lblTest.gridx = 0;
		gbc_lblTest.gridy = 0;
		
		lblTest.setToolTipText("Drawing by A-Scream");
		getContentPane().add(lblTest, gbc_lblTest);
		
		JLabel lblTaxed = new JLabel("TaxEd");
		lblTaxed.setFont(new Font("Dialog", Font.BOLD | Font.ITALIC, 20));
		lblTaxed.setHorizontalAlignment(SwingConstants.CENTER);
		GridBagConstraints gbc_lblTaxed = new GridBagConstraints();
		gbc_lblTaxed.fill = GridBagConstraints.BOTH;
		gbc_lblTaxed.gridwidth = GridBagConstraints.REMAINDER;
		gbc_lblTaxed.insets = new Insets(0, 0, 5, 0);
		gbc_lblTaxed.gridx = 0;
		gbc_lblTaxed.gridy = 1;
		getContentPane().add(lblTaxed, gbc_lblTaxed);
		
		JLabel lblTheRetrosonicLevel = new JLabel("The Retro-Sonic Level Editor");
		lblTheRetrosonicLevel.setFont(new Font("Dialog", Font.BOLD | Font.ITALIC, 14));
		lblTheRetrosonicLevel.setHorizontalAlignment(SwingConstants.CENTER);
		GridBagConstraints gbc_lblTheRetrosonicLevel = new GridBagConstraints();
		gbc_lblTheRetrosonicLevel.fill = GridBagConstraints.BOTH;
		gbc_lblTheRetrosonicLevel.gridwidth = GridBagConstraints.REMAINDER;
		gbc_lblTheRetrosonicLevel.insets = new Insets(0, 0, 5, 0);
		gbc_lblTheRetrosonicLevel.gridx = 0;
		gbc_lblTheRetrosonicLevel.gridy = 2;
		getContentPane().add(lblTheRetrosonicLevel, gbc_lblTheRetrosonicLevel);
		
		JButton btnOk = new JButton("OK");
		btnOk.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		
		JLabel lblVersion = new JLabel("Version 0.9");
		lblVersion.setHorizontalAlignment(SwingConstants.CENTER);
		GridBagConstraints gbc_lblVersion = new GridBagConstraints();
		gbc_lblVersion.fill = GridBagConstraints.BOTH;
		gbc_lblVersion.gridwidth = GridBagConstraints.REMAINDER;
		gbc_lblVersion.insets = new Insets(0, 0, 5, 0);
		gbc_lblVersion.gridx = 0;
		gbc_lblVersion.gridy = 3;
		getContentPane().add(lblVersion, gbc_lblVersion);
		
		Component verticalStrut = Box.createVerticalStrut(20);
		GridBagConstraints gbc_verticalStrut = new GridBagConstraints();
		gbc_verticalStrut.insets = new Insets(0, 0, 5, 0);
		gbc_verticalStrut.gridx = 0;
		gbc_verticalStrut.gridy = 4;
		getContentPane().add(verticalStrut, gbc_verticalStrut);
		
		JLabel lblCodedByNextvolume = new JLabel("Coded by nextvolume");
		lblCodedByNextvolume.setFont(new Font("Dialog", Font.ITALIC, 12));
		lblCodedByNextvolume.setHorizontalAlignment(SwingConstants.CENTER);
		GridBagConstraints gbc_lblCodedByNextvolume = new GridBagConstraints();
		gbc_lblCodedByNextvolume.insets = new Insets(0, 0, 5, 0);
		gbc_lblCodedByNextvolume.fill = GridBagConstraints.BOTH;
		gbc_lblCodedByNextvolume.gridwidth = GridBagConstraints.REMAINDER;
		gbc_lblCodedByNextvolume.gridx = 0;
		gbc_lblCodedByNextvolume.gridy = 5;
		getContentPane().add(lblCodedByNextvolume, gbc_lblCodedByNextvolume);
		GridBagConstraints gbc_btnOk = new GridBagConstraints();
		gbc_btnOk.gridx = 0;
		gbc_btnOk.gridy = 6;
		//gbc_lblTest.fill = GridBagConstraints.BOTH;
		gbc_btnOk.gridwidth = GridBagConstraints.REMAINDER;
		getContentPane().add(btnOk, gbc_btnOk);
		
//		pack();
//		System.out.println(getWidth() + ", " + getHeight());
		
		setLocationRelativeTo(taxEd);
		setVisible(true);
	}
}
