package taxed;

import java.awt.Dialog;
import javax.swing.JDialog;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.JSpinner;
import javax.swing.JButton;
import javax.swing.SpinnerNumberModel;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class ResizeMapDialog extends JDialog {

	public ResizeMapDialog(final TaxEdFrame taxEd)
	{
		setTitle("Resize map");
		setResizable(false);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		getContentPane().setLayout(gridBagLayout);
		
		JLabel lblNewWidth = new JLabel("New width");
		GridBagConstraints gbc_lblNewWidth = new GridBagConstraints();
		gbc_lblNewWidth.anchor = GridBagConstraints.WEST;
		gbc_lblNewWidth.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewWidth.gridx = 0;
		gbc_lblNewWidth.gridy = 0;
		getContentPane().add(lblNewWidth, gbc_lblNewWidth);
		
		final JSpinner spinner = new JSpinner(new SpinnerNumberModel(taxEd.map.getWidth(), 1, 0xFFFF, 1));
		GridBagConstraints gbc_spinner = new GridBagConstraints();
		gbc_spinner.anchor = GridBagConstraints.WEST;
		gbc_spinner.insets = new Insets(0, 0, 5, 0);
		gbc_spinner.gridx = 1;
		gbc_spinner.gridy = 0;
		getContentPane().add(spinner, gbc_spinner);
		
		JLabel lblNewHeight = new JLabel("New height");
		GridBagConstraints gbc_lblNewHeight = new GridBagConstraints();
		gbc_lblNewHeight.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewHeight.anchor = GridBagConstraints.WEST;
		gbc_lblNewHeight.gridx = 0;
		gbc_lblNewHeight.gridy = 1;
		getContentPane().add(lblNewHeight, gbc_lblNewHeight);
		
		final JSpinner spinner_1 = new JSpinner(new SpinnerNumberModel(taxEd.map.getHeight(), 1, 0xFFFF, 1));
		GridBagConstraints gbc_spinner_1 = new GridBagConstraints();
		gbc_spinner_1.anchor = GridBagConstraints.WEST;
		gbc_spinner_1.insets = new Insets(0, 0, 5, 0);
		gbc_spinner_1.gridx = 1;
		gbc_spinner_1.gridy = 1;
		getContentPane().add(spinner_1, gbc_spinner_1);
		
		JButton btnApplyChanges = new JButton("Apply");
		btnApplyChanges.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				taxEd.saveState();
			
				int newWidth = (int)spinner.getValue();
				int newHeight = (int)spinner_1.getValue();
				
				taxEd.map.setTileMap(taxEd.mapData);
				taxEd.map.resize(newWidth, newHeight);
			
				taxEd.reset_display_area(taxEd.map.getTileMap(), taxEd.objList);
				
				taxEd.setModified(true);
				
				dispose();
			}
		});
		GridBagConstraints gbc_btnApplyChanges = new GridBagConstraints();
		gbc_btnApplyChanges.insets = new Insets(0, 0, 0, 5);
		gbc_btnApplyChanges.gridx = 0;
		gbc_btnApplyChanges.gridy = 2;
		getContentPane().add(btnApplyChanges, gbc_btnApplyChanges);
		
		JButton btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		GridBagConstraints gbc_btnCancel = new GridBagConstraints();
		gbc_btnCancel.gridx = 1;
		gbc_btnCancel.gridy = 2;
		getContentPane().add(btnCancel, gbc_btnCancel);
		
		
		pack();
		setLocationRelativeTo(taxEd);
		setVisible(true);
	}
}
