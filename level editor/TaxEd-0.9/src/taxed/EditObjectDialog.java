package taxed;

import javax.swing.JDialog;
import javax.swing.JRadioButton;
import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JComboBox;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.JButton;
import javax.swing.ButtonGroup;
import java.awt.Dimension;
import rsonic.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class EditObjectDialog extends JDialog {
	private String namesArray[];

	public EditObjectDialog(final TaxEdFrame taxEd, RSObject objRef) {
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setModalityType(JDialog.ModalityType.APPLICATION_MODAL);
		setResizable(false);
	
		final boolean isNewObject = (objRef == null);

		setTitle(isNewObject?"New Object":"Edit Object");
		final RSObject obj = isNewObject?(new RSObject(0, 0, taxEd.popupX, taxEd.popupY)):objRef;		
			
		GridBagLayout gridBagLayout = new GridBagLayout();
		/*gridBagLayout.columnWidths = new int[] {101, 0, 30, 0};
		gridBagLayout.rowHeights = new int[]{23, 0, 0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, 0.0, 0.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};*/
		
		
		getContentPane().setLayout(gridBagLayout);
		
		JRadioButton rdbtnSelectTypeFrom = new JRadioButton("Select type from a list");

		GridBagConstraints gbc_rdbtnSelectTypeFrom = new GridBagConstraints();
		gbc_rdbtnSelectTypeFrom.insets = new Insets(0, 0, 5, 5);
		gbc_rdbtnSelectTypeFrom.anchor = GridBagConstraints.NORTH;
		gbc_rdbtnSelectTypeFrom.fill = GridBagConstraints.HORIZONTAL;
		gbc_rdbtnSelectTypeFrom.gridx = 0;
		gbc_rdbtnSelectTypeFrom.gridy = 0;
		getContentPane().add(rdbtnSelectTypeFrom, gbc_rdbtnSelectTypeFrom);
		
		JLabel lblXPosition = new JLabel("X Position");
		GridBagConstraints gbc_lblXPosition = new GridBagConstraints();
		gbc_lblXPosition.anchor = GridBagConstraints.WEST;
		gbc_lblXPosition.insets = new Insets(0, 0, 5, 5);
		gbc_lblXPosition.gridx = 0;
		gbc_lblXPosition.gridy = 5;
		getContentPane().add(lblXPosition, gbc_lblXPosition);
		

		
		JRadioButton rdbtnNewRadioButton = new JRadioButton("Specify type directly");

		GridBagConstraints gbc_rdbtnNewRadioButton = new GridBagConstraints();
		gbc_rdbtnNewRadioButton.insets = new Insets(0, 0, 5, 5);
		gbc_rdbtnNewRadioButton.anchor = GridBagConstraints.WEST;
		gbc_rdbtnNewRadioButton.gridx = 0;
		gbc_rdbtnNewRadioButton.gridy = 1;
		getContentPane().add(rdbtnNewRadioButton, gbc_rdbtnNewRadioButton);
		
		rdbtnNewRadioButton.setSelected(true);
		rdbtnSelectTypeFrom.setSelected(false);
		
		ButtonGroup btnGroup = new ButtonGroup();
		btnGroup.add(rdbtnSelectTypeFrom);
		btnGroup.add(rdbtnNewRadioButton);
		
		
		final JLabel lblListOfTypes = new JLabel("List of types");
		GridBagConstraints gbc_lblListOfTypes = new GridBagConstraints();
		gbc_lblListOfTypes.insets = new Insets(0, 0, 5, 5);
		gbc_lblListOfTypes.anchor = GridBagConstraints.WEST;
		gbc_lblListOfTypes.gridx = 0;
		gbc_lblListOfTypes.gridy = 2;
		getContentPane().add(lblListOfTypes, gbc_lblListOfTypes);
		
		lblListOfTypes.setEnabled(false);
		
		namesArray = taxEd.map.getObjectTypeNames();
		final JComboBox<String> comboBox = new JComboBox<String>(namesArray);
		comboBox.setPreferredSize(new Dimension(192, 24));
		GridBagConstraints gbc_comboBox = new GridBagConstraints();
		gbc_comboBox.insets = new Insets(0, 0, 5, 0);
		gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBox.gridx = 1;
		gbc_comboBox.gridy = 2;
		getContentPane().add(comboBox, gbc_comboBox);
		
		if(namesArray.length == 0)
			rdbtnSelectTypeFrom.setEnabled(false);
		
		comboBox.setEnabled(false);
		
		if(obj.getType() < namesArray.length)
			comboBox.setSelectedIndex(obj.getType());
		
		JLabel lblTypeNumber = new JLabel("Type number");
		GridBagConstraints gbc_lblTypeNumber = new GridBagConstraints();
		gbc_lblTypeNumber.anchor = GridBagConstraints.WEST;
		gbc_lblTypeNumber.insets = new Insets(0, 0, 5, 5);
		gbc_lblTypeNumber.gridx = 0;
		gbc_lblTypeNumber.gridy = 3;
		getContentPane().add(lblTypeNumber, gbc_lblTypeNumber);
		
		final JSpinner type_spinner = new JSpinner(new SpinnerNumberModel(0, 0, 0xFFFF, 1));
		GridBagConstraints gbc_type_spinner = new GridBagConstraints();
		gbc_type_spinner.anchor = GridBagConstraints.EAST;
		gbc_type_spinner.insets = new Insets(0, 0, 5, 0);
		gbc_type_spinner.gridx = 1;
		gbc_type_spinner.gridy = 3;
		getContentPane().add(type_spinner, gbc_type_spinner);
		
		JLabel lblSubtypeNumber = new JLabel("Subtype number");
		GridBagConstraints gbc_lblSubtypeNumber = new GridBagConstraints();
		gbc_lblSubtypeNumber.anchor = GridBagConstraints.WEST;
		gbc_lblSubtypeNumber.insets = new Insets(0, 0, 5, 5);
		gbc_lblSubtypeNumber.gridx = 0;
		gbc_lblSubtypeNumber.gridy = 4;
		getContentPane().add(lblSubtypeNumber, gbc_lblSubtypeNumber);
		
		final JSpinner subtype_spinner = new JSpinner(new SpinnerNumberModel(0, 0, 0xFFFF, 1));
		GridBagConstraints gbc_subtype_spinner = new GridBagConstraints();
		gbc_subtype_spinner.anchor = GridBagConstraints.EAST;
		gbc_subtype_spinner.insets = new Insets(0, 0, 5, 0);
		gbc_subtype_spinner.gridx = 1;
		gbc_subtype_spinner.gridy = 4;
		getContentPane().add(subtype_spinner, gbc_subtype_spinner);
		
		final JSpinner xpos_spinner = new JSpinner(new SpinnerNumberModel());
		GridBagConstraints gbc_xpos_spinner = new GridBagConstraints();
		gbc_xpos_spinner.anchor = GridBagConstraints.EAST;
		gbc_xpos_spinner.insets = new Insets(0, 0, 5, 0);
		gbc_xpos_spinner.gridx = 1;
		gbc_xpos_spinner.gridy = 5;
		getContentPane().add(xpos_spinner, gbc_xpos_spinner);
		
		JLabel lblYPosition = new JLabel("Y Position");
		GridBagConstraints gbc_lblYPosition = new GridBagConstraints();
		gbc_lblYPosition.anchor = GridBagConstraints.WEST;
		gbc_lblYPosition.insets = new Insets(0, 0, 5, 5);
		gbc_lblYPosition.gridx = 0;
		gbc_lblYPosition.gridy = 6;
		getContentPane().add(lblYPosition, gbc_lblYPosition);
		
		final JSpinner ypos_spinner = new JSpinner(new SpinnerNumberModel());
		GridBagConstraints gbc_ypos_spinner = new GridBagConstraints();
		gbc_ypos_spinner.anchor = GridBagConstraints.EAST;
		gbc_ypos_spinner.insets = new Insets(0, 0, 5, 0);
		gbc_ypos_spinner.gridx = 1;
		gbc_ypos_spinner.gridy = 6;
		getContentPane().add(ypos_spinner, gbc_ypos_spinner);
		
		JButton btnNewObject = new JButton(isNewObject ? "New object" : "Update object");
		btnNewObject.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				taxEd.saveState();

// Let's update obj with the new values
				obj.setType((int)type_spinner.getValue());
				obj.setSubtype((int)subtype_spinner.getValue());
				obj.setXPos((int)xpos_spinner.getValue());
				obj.setYPos((int)ypos_spinner.getValue());
				
// Update the selected object coordinates for the map display component				
				taxEd.display.setSelectedObjectCoords(obj.getXPos(), obj.getYPos());
				
// If this is a new object, we need to add it to the object list
				if(isNewObject)
					taxEd.objList.add(obj);				
			
				taxEd.display.repaint();
				taxEd.setModified(true);
				dispose();
			}
		});
		GridBagConstraints gbc_btnNewObject = new GridBagConstraints();
		gbc_btnNewObject.insets = new Insets(0, 0, 0, 5);
		gbc_btnNewObject.gridx = 0;
		gbc_btnNewObject.gridy = 7;
		getContentPane().add(btnNewObject, gbc_btnNewObject);
		
		JButton btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		GridBagConstraints gbc_btnCancel = new GridBagConstraints();
		gbc_btnCancel.gridx = 1;
		gbc_btnCancel.gridy = 7;
		getContentPane().add(btnCancel, gbc_btnCancel);
		
		rdbtnSelectTypeFrom.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				lblListOfTypes.setEnabled(true);
				comboBox.setEnabled(true);
			}
		});
		
		rdbtnNewRadioButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				lblListOfTypes.setEnabled(false);
				comboBox.setEnabled(false);
			}
		});
		
		type_spinner.setValue(obj.getType());
		subtype_spinner.setValue(obj.getSubtype());
		xpos_spinner.setValue(obj.getXPos());
		ypos_spinner.setValue(obj.getYPos());
		
		pack();
		setLocationRelativeTo(taxEd);
		setVisible(true);
	}

}
