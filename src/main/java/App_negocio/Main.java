package App_negocio;

import java.awt.EventQueue;

import javax.swing.JFrame;

import controlador.Controlador;
import modelo.Funciones;
import vista.Login;

public class Main extends JFrame {

	private static final long serialVersionUID = 1L;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Controlador contr = new Controlador(new Login(), new Funciones());
					contr.Log.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
}
