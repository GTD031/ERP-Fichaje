package App_negocio;

import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import modelo.Funciones;

public class Login extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField textField_usuario;
	private JTextField textField_passw;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Login frame = new Login();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

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
		
		JButton btnContinuar = new JButton("Continuar");
		btnContinuar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Funciones.conectar();
				String nombre, apellido, dni, rol;
				ResultSet rs;
				try {
					Funciones.pstmt = Funciones.cn.prepareStatement("SELECT DNI, Nombre, Apellido, Usuario, Rol FROM Empleados WHERE Usuario = ? AND Contraseña = ?");
					Funciones.pstmt.setString(1, textField_usuario.getText());
					Funciones.pstmt.setString(2, textField_passw.getText());
					rs = Funciones.pstmt.executeQuery();
					if (rs.next()) {
						dni = rs.getString(1);
						nombre = rs.getString(2);
						apellido = rs.getString(3);
						rol = rs.getString(5);
						dispose();
						JOptionPane.showMessageDialog(null, "Bienvenido "+nombre+" "+apellido+".", "Acceso aprobado", JOptionPane.INFORMATION_MESSAGE);
						if (rol.equals("ADMINISTRADOR"))
							JOptionPane.showMessageDialog(null, "Eres Administrador", "Administrador", JOptionPane.INFORMATION_MESSAGE);
						Fichaje frame2 = new Fichaje(dni, nombre, apellido, rol);
						frame2.setVisible(true);
					} else {
						JOptionPane.showMessageDialog(null, "Error de login.", "Error", JOptionPane.ERROR_MESSAGE);
						JOptionPane.showMessageDialog(null, "Verifica que usuario y contraseña estén bien escritos, son sensible a mayúsculas.", "Error", JOptionPane.INFORMATION_MESSAGE);
					}
				}catch (SQLException q) {
					System.out.println("Error al realizar este logueo.");
					q.getErrorCode();		//para ver que es.
					q.printStackTrace();
				} finally {
					Funciones.cerrarConexionDML("Error al cerrar esta conexión y el pstmt.");
				}
			}
		});
		btnContinuar.setFont(new Font("Tahoma", Font.BOLD, 14));
		btnContinuar.setBounds(150, 199, 129, 40);
		contentPane.add(btnContinuar);

	}
}
