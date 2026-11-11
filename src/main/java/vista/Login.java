package vista;

import java.awt.Font;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

public class Login extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	public JTextField textField_usuario;
	public JTextField textField_passw;
	public JButton btnContinuar;


	/**
	 * Create the frame.
	 */
	public Login() {
		setTitle("Login");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		setResizable(false); 	// sin modificar el tamaño
		setLocationRelativeTo(null);	//para mantenerla centrado.
		
		JLabel lbl_Titulo = new JLabel("Inicia sesión");
		lbl_Titulo.setFont(new Font("Tahoma", Font.BOLD, 16));
		lbl_Titulo.setHorizontalAlignment(SwingConstants.CENTER);
		lbl_Titulo.setBounds(150, 11, 129, 35);
		contentPane.add(lbl_Titulo);
		
		JLabel lbl_Usuario = new JLabel("Usuario");
		lbl_Usuario.setFont(new Font("Arial", Font.BOLD, 14));
		lbl_Usuario.setHorizontalAlignment(SwingConstants.CENTER);
		lbl_Usuario.setBounds(80, 71, 99, 35);
		contentPane.add(lbl_Usuario);
		
		JLabel lbl_password = new JLabel("Contraseña");
		lbl_password.setHorizontalAlignment(SwingConstants.CENTER);
		lbl_password.setFont(new Font("Arial", Font.BOLD, 14));
		lbl_password.setBounds(80, 132, 99, 35);
		contentPane.add(lbl_password);
		
		textField_usuario = new JTextField();
		textField_usuario.setBounds(215, 71, 144, 35);
		contentPane.add(textField_usuario);
		textField_usuario.setColumns(10);
		
		textField_passw = new JTextField();
		textField_passw.setColumns(10);
		textField_passw.setBounds(215, 132, 144, 35);
		contentPane.add(textField_passw);
		
		btnContinuar = new JButton("Continuar");	//ESTO VA PARA EL CONTROLADOR
		btnContinuar.setFont(new Font("Tahoma", Font.BOLD, 14));
		btnContinuar.setBounds(150, 199, 129, 40);
		contentPane.add(btnContinuar);

	}
}
