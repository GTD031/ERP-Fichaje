package controlador;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import modelo.Funciones;
import vista.Administrador;
import vista.Fechas;
import vista.Fichaje;
import vista.Login;

public class Controlador {
	
	public Login Log;
	public Fichaje fich;
	public Fechas date;
	public Administrador adm;
	public Funciones funcion;
	public boolean enProceso1, enProceso2; 

	
	public void ActualizarFecha() {
		if (enProceso2) return;
		enProceso2 = true;
		date.Boton_horas.addActionListener(new ActionListener() {	//ESTO VA PARA EL CONTROLADOR
		public void actionPerformed(ActionEvent e) {
			String Dia;
			int[] array = new int[2];
			boolean bool_usu_adm;
			if (!Controlador.this.fich.rol.equals("ADMINISTRADOR")) {
				Dia = Controlador.this.date.textField_Fecha_Usuario.getText();
				bool_usu_adm = Funciones.CumplePat(Controlador.this.date.PFecha, Controlador.this.date.textField_Fecha_Usuario, "Debe rellenar la fecha", "La fecha debe cumplir el formato indicado.");
				if (bool_usu_adm) {
					LocalDate date = LocalDate.of(Integer.parseInt(Dia.substring(6)),Integer.parseInt(Dia.substring(3,5)),Integer.parseInt(Dia.substring(0,2)));
					Funciones.get_Hours_Day(date, Controlador.this.fich.DNI);	//Aqui solo basta con ejecutarla (el valor nos interesará cuando lo hagamos por meses).
				} 	//si no -> no hace nada
				
			} else {			//REVISAR en caso de ser admin, que es este.
				if (Controlador.this.date.comboBox.getSelectedItem().toString().equals("Seleccione") || Controlador.this.date.comboBox_Mes.getSelectedIndex()==0)
					JOptionPane.showMessageDialog(null, "Debe introducir un usuario, mes y el año a averiguar.", "Error", JOptionPane.ERROR_MESSAGE);
				else {
					ResultSet rs;
					Funciones.conectar();
					try {
						Funciones.pstmt = Funciones.cn.prepareStatement("SELECT DNI, Nombre, Apellido FROM Empleados WHERE Usuario = ?");
						Funciones.pstmt.setString(1, String.valueOf(Controlador.this.date.comboBox.getSelectedItem()));
						rs = Funciones.pstmt.executeQuery();
						if (rs.next()) {
							Controlador.this.date.dni = rs.getString(1);
							Controlador.this.date.nombre = rs.getString(2)+" "+rs.getString(3);
							Administrador admin = new Administrador(Controlador.this.date.dni,Controlador.this.date.nombre, Controlador.this.date.comboBox_Mes.getSelectedItem().toString(),(int) Controlador.this.date.spinner.getValue());
					        
							// Cargar datos al construir el frame
					        
							admin.Numero_mes =  (int) Controlador.this.date.comboBox_Mes.getSelectedIndex(); 	//Esencial para cargar los datos
							admin.cargarDatos();
							array[0] = admin.Suma_horas_minutos(4);
							array[1] = admin.Suma_horas_minutos(5);
							array = admin.conversion(array);
					        admin.lbl_HorasTotales.setText("Tiempo trabajado: "+array[0]+" horas y "+array[1]+" minutos.");
							admin.setVisible(true);
						} else {
							JOptionPane.showMessageDialog(null, "Error: Este usuario no se encuentra en nuestra base de datos.", "Usuario no encontrado", JOptionPane.ERROR_MESSAGE);
						}
					}catch (SQLException u) {
						System.out.println("Error al conseguir datos del usuario.");
					}
				}
			}
		}
	});
	date.Boton_horas.setMnemonic('d');
	date.textField_Fecha_Usuario.addKeyListener(new KeyAdapter() {	//ESTO SI VA A LA VISTA
		@Override
		public void keyReleased(KeyEvent e) {
			boolean bool;
			String Fecha = Controlador.this.date.textField_Fecha_Usuario.getText();
			Matcher MFecha = Controlador.this.date.PFecha.matcher(Fecha);
			bool = MFecha.matches();
			Controlador.this.date.Boton_horas.setEnabled(bool);
			if (Fecha.equals("")) {
				Controlador.this.date.setColor(0,0,0, Controlador.this.date.mensaje); //Negro?
			} else if (!bool) {
				Controlador.this.date.setColor(255,0,0,"Formato inválido"); //Rojo
			} else {
				Controlador.this.date.setColor(0,190,0,"Formato válido"); //Verde
			}
		}
	});
		enProceso2 = false;
	}
	
	public void ActualizarFichaje() {
		if (enProceso1) return;
		enProceso1 = true;
		fich.btnHoras_trabajadas.addActionListener(new ActionListener() { 	//ESTO TAMBIEN VA PARA EL CONTROLADOR
				public void actionPerformed(ActionEvent e) {
					boolean bool = Controlador.this.fich.rol.equals("ADMINISTRADOR");
					String msj = bool ? "Mes":"Introduzca la fecha en formato dd/MM/yyyy";
					date = new Fechas(msj, bool);
					date.DNI2.setText(fich.dni);	//Debes hacer algo como 
					date.lblNombre.setText(Controlador.this.fich.EtiquetaNombre.getText());
					date.PFecha = Pattern.compile(bool?"[a-zA-ZáéíóúÁÉÍÓÚüÜñÑ]+":"\\d{2}/\\d{2}/\\d{4}");
					ActualizarFecha();
					date.setVisible(true);
			}
		});
		fich.btnHoras_trabajadas.setMnemonic('T');
		fich.botonReceso.addActionListener(new ActionListener() {	//ESTO VA PARA EL CONTROLADOR
			public void actionPerformed(ActionEvent e) {
				String texto = Controlador.this.fich.textField_Hora.getText(), hora = Controlador.this.fich.ConvertirFecha(new Date(),2);
				Controlador.this.fich.EtiquetaHora.setText("Hora de receso:");
				Controlador.this.fich.botonSalida.setEnabled(!Controlador.this.fich.botonReceso.isSelected());
				
				//De aqui en adelante el principio es el mismo: Actualizo en la BD y luego en el campo, variando segun si se ha seleccionado una vez el boton y si es o no la primera vez. 
				
				if (Controlador.this.fich.veces == 1 && Controlador.this.fich.botonReceso.isSelected()) {
					//hora = RegistrarHora("UPDATE Jornada SET [Hora de receso] = ? WHERE Id = ?"); //Actualizo la BD
					
					//Ahora actualizo el campo
					Controlador.this.fich.textField_Hora.setText(hora);
					Controlador.this.fich.HorasReceso += hora;
				} else if (Controlador.this.fich.veces == 1 && !Controlador.this.fich.botonReceso.isSelected()){
					//hora = RegistrarHora("UPDATE Jornada SET [Hora de receso] = CONCAT(CONCAT([Hora de receso],' - '),?) WHERE Id = ?");
					Controlador.this.fich.textField_Hora.setText(texto+" - "+hora);	//IMPORTANTE, AQUI SOLO COMILLAS DOBLES.
					Controlador.this.fich.HorasReceso += " - "+hora;
				} else if (Controlador.this.fich.veces > 1 && Controlador.this.fich.botonReceso.isSelected()) {
					//hora = RegistrarHora("UPDATE Jornada SET [Hora de receso] = CONCAT(CONCAT([Hora de receso],', '),?) WHERE Id = ?");
					Controlador.this.fich.textField_Hora.setText(hora);
					Controlador.this.fich.HorasReceso += ", "+hora;
				} else {
					//hora = RegistrarHora("UPDATE Jornada SET [Hora de receso] = CONCAT(CONCAT([Hora de receso],' - '),?) WHERE Id = ?");
					Controlador.this.fich.textField_Hora.setText(texto+" - "+hora);
					Controlador.this.fich.HorasReceso += " - "+hora;
				}
				//Cambiar el titulo del campo segun este seleccionado o no.
				if (!Controlador.this.fich.botonReceso.isSelected()) {
					Controlador.this.fich.EtiquetaHora.setText("");
					Controlador.this.fich.veces++;
				}
			}
		});
		enProceso1 = false;
	}

	
	public Controlador(Login log, Funciones func) {
		this.Log = log;
		this.funcion = func;
		
		Log.btnContinuar.addActionListener(new ActionListener() {	
			public void actionPerformed(ActionEvent e) {
				String nombre, apellido, dni, rol;	//Parametros de la clase Fichaje
				ResultSet rs;
				Funciones.conectar();
				try {
					Funciones.pstmt = Funciones.cn.prepareStatement("SELECT DNI, Nombre, Apellido, Usuario, Rol FROM Empleados WHERE Usuario = ? AND Contraseña = ?");
					Funciones.pstmt.setString(1, Controlador.this.Log.textField_usuario.getText());
					Funciones.pstmt.setString(2, Controlador.this.Log.textField_passw.getText());
					rs = Funciones.pstmt.executeQuery();
					if (rs.next()) {
						dni = rs.getString(1);
						nombre = rs.getString(2);
						apellido = rs.getString(3);
						rol = rs.getString(5);
						Controlador.this.Log.dispose();	//Aqui cierro Login
						JOptionPane.showMessageDialog(null, "Bienvenido "+nombre+" "+apellido+".", "Acceso aprobado", JOptionPane.INFORMATION_MESSAGE);
						if (rol.equals("ADMINISTRADOR"))
							JOptionPane.showMessageDialog(null, "Eres Administrador", "Administrador", JOptionPane.INFORMATION_MESSAGE);
						System.out.println("Creacion de Fichaje");
						Controlador.this.fich = new Fichaje(dni, nombre, apellido, rol);
						System.out.println("Añado eventos a Fichaje.");
						ActualizarFichaje();
						Controlador.this.fich.setVisible(true);
					} else {
						JOptionPane.showMessageDialog(null, "Error de login. Datos incorrectos", "Error", JOptionPane.ERROR_MESSAGE);
						JOptionPane.showMessageDialog(null, "Verifica que usuario y contraseña estén bien escritos, son sensibles a mayúsculas.", "Error", JOptionPane.INFORMATION_MESSAGE);
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
	}
}
