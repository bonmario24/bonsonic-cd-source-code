package taxed;
import javax.swing.JFrame;
import javax.swing.JDialog;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import java.awt.GridBagConstraints;
import javax.swing.JTextField;
import java.awt.Insets;
import java.awt.Dialog;
import javax.swing.SwingConstants;
import javax.swing.JButton;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class MapInfoDialog extends JDialog
{
	private JLabel textField;
	private JTextField textField_1;
	private JButton btnApply;
	private JButton btnCancel;
	
	public MapInfoDialog(final TaxEdFrame taxEd) 
	{
		setResizable(false);
		setTitle("Map information");
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		getContentPane().setLayout(gridBagLayout);
		
		JLabel lblMapSize = new JLabel("Map size");
		GridBagConstraints gbc_lblMapSize = new GridBagConstraints();
		gbc_lblMapSize.anchor = GridBagConstraints.EAST;
		gbc_lblMapSize.insets = new Insets(0, 0, 5, 5);
		gbc_lblMapSize.gridx = 0;
		gbc_lblMapSize.gridy = 0;
		getContentPane().add(lblMapSize, gbc_lblMapSize);
		
		textField = new JLabel();
		GridBagConstraints gbc_textField = new GridBagConstraints();
		gbc_textField.insets = new Insets(0, 0, 5, 0);
		gbc_textField.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField.gridx = 1;
		gbc_textField.gridy = 0;
		getContentPane().add(textField, gbc_textField);
		textField.setText(taxEd.map.getWidth() + "x" + taxEd.map.getHeight());
		
		JLabel lblZoneName = new JLabel("Zone name");
		GridBagConstraints gbc_lblZoneName = new GridBagConstraints();
		gbc_lblZoneName.anchor = GridBagConstraints.EAST;
		gbc_lblZoneName.insets = new Insets(0, 0, 5, 5);
		gbc_lblZoneName.gridx = 0;
		gbc_lblZoneName.gridy = 1;
		getContentPane().add(lblZoneName, gbc_lblZoneName);
		
		textField_1 = new JTextField();
		GridBagConstraints gbc_textField_1 = new GridBagConstraints();
		gbc_textField_1.insets = new Insets(0, 0, 5, 0);
		gbc_textField_1.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField_1.gridx = 1;
		gbc_textField_1.gridy = 1;
		getContentPane().add(textField_1, gbc_textField_1);
		textField_1.setColumns(10);
		textField_1.setText(taxEd.map.getZoneName());
		
		btnApply = new JButton("Apply");
		btnApply.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				taxEd.saveState();
				taxEd.map.setZoneName(textField_1.getText());
				taxEd.zoneName = textField_1.getText();
				taxEd.setAppTitle(textField_1.getText());
				taxEd.setModified(true);
				dispose();
			}
		});
		btnApply.setHorizontalAlignment(SwingConstants.RIGHT);
		GridBagConstraints gbc_btnApply = new GridBagConstraints();
		gbc_btnApply.anchor = GridBagConstraints.EAST;
		gbc_btnApply.insets = new Insets(0, 0, 0, 5);
		gbc_btnApply.gridx = 0;
		gbc_btnApply.gridy = 2;
		getContentPane().add(btnApply, gbc_btnApply);
		
		btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		btnCancel.setHorizontalAlignment(SwingConstants.LEFT);
		GridBagConstraints gbc_btnCancel = new GridBagConstraints();
		gbc_btnCancel.gridx = 1;
		gbc_btnCancel.gridy = 2;
		getContentPane().add(btnCancel, gbc_btnCancel);
		
		pack();
		setLocationRelativeTo(taxEd);
		setVisible(true);
	}

}
