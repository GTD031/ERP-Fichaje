package App_negocio;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import modelo.Funciones;

public class Fichaje extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private static JTextField textField_Hora;
	private static String DNI, HoraEntrada, HorasReceso = "", HoraSalida, fecha, nombre, apellido;
	protected String rol, dni;
	private JLabel EtiquetaID, EtiquetaHora;
	private static JLabel EtiquetaNombre;
	private static JButton botonEntrada, botonSalida;
	private static JToggleButton botonReceso;
	private static JTextArea textArea;
	private static int veces = 1;
	private JButton btnHoras_trabajadas;
	
	private static String ConvertirFecha(Date Fecha, int a) {
		
		  // Paso 1: Crear un objeto Date (aqui ya esta pasado como primer parametro). 
		  Date date = new Date();  
		 
		  // Paso 2: Convertir Date a LocalDateTime 
		  LocalDateTime localDateTime =         
		  date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime(); 
		 
		  // Paso 3: Crear un DateTimeFormatter con el patrón deseado 
		 // DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"); 
		 
		  DateTimeFormatter formatter = DateTimeFormatter.ofPattern(a == 1 ? "dd/MM/yyyy":"HH:mm"); 
		  
		  // Paso 4: Formatear LocalDateTime a String 
		  String formattedDate = localDateTime.format(formatter); 
		 
		  // Devolver el resultado 
		  return formattedDate;
	}

	private static void RegistrarJornada() {	//Nueva función, reestructuración de la lógica del código.
		String[] lista = {DNI, nombre, apellido, fecha, HoraEntrada, HorasReceso, HoraSalida, textArea.getText()};
		int i = 0;
		Funciones.conectar();
		try {
			Funciones.pstmt = Funciones.cn.prepareStatement("INSERT INTO Jornada (DNI, Nombre, Apellido, Fecha, [Hora de entrada], [Hora de receso], [Hora de salida], Motivo) VALUES (?,?,?,?,?,?,?,?)");
			while (i<8) {
				Funciones.pstmt.setString(i+1, lista[i]);
				i++;
			}
			Funciones.pstmt.executeUpdate();
		} catch (SQLException j) {
			System.out.println("Error al registrar jornada.");
			j.printStackTrace();
		} finally {
			Funciones.cerrarConexionDML("Error al cerrar la insert.");
		}
	}

	/**
	 * Create the frame.
	 */
	public Fichaje(String dni, String name, String apellido, String roll) {
		this.rol = roll;
		DNI = dni;
		nombre = name;
		Fichaje.apellido = apellido;
		
		setTitle("App Fichaje");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 902, 472);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		setResizable(false);
		setLocationRelativeTo(null);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				JOptionPane.showMessageDialog(null,"¿Está seguro que desea finalizar la jornada?", "Fin de jornada", JOptionPane.INFORMATION_MESSAGE);
				Date date = new Date();
				String Hora;
				fecha = ConvertirFecha(date, 1);
				veces = 0;
				if (textField_Hora.getText()==null) {
					JOptionPane.showMessageDialog(null, "No se registró jornada", "Jornada no registrada", JOptionPane.INFORMATION_MESSAGE);
				} else if (EtiquetaHora.getText().equals("Hora de entrada:")){
					
					Hora = ConvertirFecha(date, 2);
					HoraSalida = Hora;
					RegistrarJornada();
				} else {
					Hora = "\\d{1,2}:\\d{2}";
					Pattern PHora = Pattern.compile(Hora+" - "+Hora);
					Hora = ConvertirFecha(date, 2); 	//reutilizo la variable Hora.
					
					if (!PHora.matcher(textField_Hora.getText()).matches()) {
						textField_Hora.setText(textField_Hora.getText()+" - "+Hora); //completo con la hora.
						HorasReceso += " - "+Hora; //solo si dice hora de receso
					}
					
					HoraSalida = Hora;
					RegistrarJornada();
				}
				dispose();
				}
			});
		
		JLabel Título = new JLabel("Fichaje");
		Título.setForeground(new Color(0, 0, 128));
		Título.setFont(new Font("Tahoma", Font.BOLD, 26));
		Título.setHorizontalAlignment(SwingConstants.CENTER);
		Título.setBounds(232, 11, 416, 49);
		contentPane.add(Título);
		
		EtiquetaID = new JLabel(dni);
		EtiquetaID.setFont(new Font("Tahoma", Font.BOLD, 13));
		EtiquetaID.setHorizontalAlignment(SwingConstants.CENTER);
		EtiquetaID.setBounds(39, 105, 155, 35);
		contentPane.add(EtiquetaID);
		
		JLabel EtiquetaDNI = new JLabel("DNI/NIE");
		EtiquetaDNI.setHorizontalAlignment(SwingConstants.CENTER);
		EtiquetaDNI.setFont(new Font("Tahoma", Font.BOLD, 15));
		EtiquetaDNI.setBounds(55, 63, 122, 35);
		contentPane.add(EtiquetaDNI);
		
		JLabel EtiquetaNom = new JLabel("NOMBRE");
		EtiquetaNom.setHorizontalAlignment(SwingConstants.CENTER);
		EtiquetaNom.setFont(new Font("Tahoma", Font.BOLD, 15));
		EtiquetaNom.setBounds(653, 63, 122, 35);
		contentPane.add(EtiquetaNom);

		EtiquetaHora = new JLabel("");
		EtiquetaHora.setHorizontalAlignment(SwingConstants.CENTER);
		EtiquetaHora.setFont(new Font("Tahoma", Font.BOLD, 12));
		EtiquetaHora.setBounds(39, 258, 167, 35);
		contentPane.add(EtiquetaHora);
		
		textField_Hora = new JTextField();
		textField_Hora.setEditable(false);
		textField_Hora.setBounds(39, 306, 167, 35);
		contentPane.add(textField_Hora);
		textField_Hora.setColumns(10);
		
		btnHoras_trabajadas = new JButton("Horas trabajadas");
		btnHoras_trabajadas.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				boolean bool = rol.equals("ADMINISTRADOR");
				String msj = bool ? "Mes":"Introduzca la fecha en formato dd/MM/yyyy";
				Fechas Horas = new Fechas(msj, bool);
				Fechas.DNI2.setText(DNI);
				Fechas.lblNombre.setText(EtiquetaNombre.getText());
				Horas.PFecha = Pattern.compile(bool?"[a-zA-ZáéíóúÁÉÍÓÚüÜñÑ]+":"\\d{2}/\\d{2}/\\d{4}");
				Horas.setVisible(true);
			}
		});
		btnHoras_trabajadas.setMnemonic('T');
		btnHoras_trabajadas.setForeground(new Color(0, 64, 128));
		btnHoras_trabajadas.setFont(new Font("Tahoma", Font.BOLD, 16));
		btnHoras_trabajadas.setBounds(253, 294, 210, 55);
		contentPane.add(btnHoras_trabajadas);
		
		botonReceso = new JToggleButton("Hora de receso");
		botonReceso.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String texto = textField_Hora.getText(), hora =  ConvertirFecha(new Date(),2);
				EtiquetaHora.setText("Hora de receso:");
				botonSalida.setEnabled(!botonReceso.isSelected());
				
				//De aqui en adelante el principio es el mismo: Actualizo en la BD y luego en el campo, variando segun si se ha seleccionado una vez el boton y si es o no la primera vez. 
				
				if (veces == 1 && botonReceso.isSelected()) {
					//hora = RegistrarHora("UPDATE Jornada SET [Hora de receso] = ? WHERE Id = ?"); //Actualizo la BD
					
					//Ahora actualizo el campo
					textField_Hora.setText(hora);
					HorasReceso += hora;
				} else if (veces == 1 && !botonReceso.isSelected()){
					//hora = RegistrarHora("UPDATE Jornada SET [Hora de receso] = CONCAT(CONCAT([Hora de receso],' - '),?) WHERE Id = ?");
					textField_Hora.setText(texto+" - "+hora);	//IMPORTANTE, AQUI SOLO COMILLAS DOBLES.
					HorasReceso += " - "+hora;
				} else if (veces > 1 && botonReceso.isSelected()) {
					//hora = RegistrarHora("UPDATE Jornada SET [Hora de receso] = CONCAT(CONCAT([Hora de receso],', '),?) WHERE Id = ?");
					textField_Hora.setText(hora);
					HorasReceso += ", "+hora;
				} else {
					//hora = RegistrarHora("UPDATE Jornada SET [Hora de receso] = CONCAT(CONCAT([Hora de receso],' - '),?) WHERE Id = ?");
					textField_Hora.setText(texto+" - "+hora);
					HorasReceso += " - "+hora;
				}
				//Cambiar el titulo del campo segun este seleccionado o no.
				if (!botonReceso.isSelected()) {
					EtiquetaHora.setText("");
					veces++;
				}
			}
		});
		botonReceso.setForeground(Color.BLACK);
		botonReceso.setFont(new Font("Tahoma", Font.BOLD, 13));
		botonReceso.setBounds(351, 172, 167, 35);
		contentPane.add(botonReceso);
		botonReceso.setEnabled(false);
		
		textArea = new JTextArea();
		textArea.setBorder(new LineBorder(new Color(0, 0, 0), 2));
		textArea.setBounds(510, 272, 354, 150);
		contentPane.add(textArea);
		
		botonSalida = new JButton("Hora de salida");
		botonSalida.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				EtiquetaHora.setText("Hora de salida:");
				//RegistrarHora(botonSalida);
				JOptionPane.showMessageDialog(null, "Ha finalizado tu jornada. Tu tiempo en la app ha terminado.", "Salida", JOptionPane.INFORMATION_MESSAGE);
				botonSalida.setEnabled(false);
				botonReceso.setEnabled(false);	
				botonEntrada.setEnabled(false);
				btnHoras_trabajadas.setEnabled(true);
				Date fechaS = new Date();
				HoraSalida = ConvertirFecha(fechaS, 2);
				textField_Hora.setText(HoraSalida);
				//Enviar motivo en caso de salir antes de lo debido.
				textArea.setText((textArea.getText().isBlank()?" - ":textArea.getText()));
				fecha = ConvertirFecha(fechaS, 1);
				RegistrarJornada();
				dispose(); 	//registro la jornada y cierro.
			}
		});
		botonSalida.setForeground(Color.BLACK);
		botonSalida.setFont(new Font("Tahoma", Font.BOLD, 13));
		botonSalida.setBounds(638, 172, 167, 35);
		contentPane.add(botonSalida);
		botonSalida.setEnabled(false);
		
		botonEntrada = new JButton("Hora de entrada");
		botonEntrada.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				EtiquetaHora.setText("Hora de entrada:");
//				RegistrarHora(botonEntrada);
				 HoraEntrada = ConvertirFecha(new Date(),2);
				 textField_Hora.setText(HoraEntrada);
				 botonEntrada.setEnabled(false);
				 botonReceso.setEnabled(true);
				 botonSalida.setEnabled(true);
				 btnHoras_trabajadas.setEnabled(false);
			}
		});
		botonEntrada.setFont(new Font("Tahoma", Font.BOLD, 13));
		botonEntrada.setForeground(new Color(0, 0, 0));
		botonEntrada.setBounds(39, 172, 167, 35);
		contentPane.add(botonEntrada);
		botonEntrada.setEnabled(true);

		JLabel EtiquetaMotivo = new JLabel("Motivo");
		EtiquetaMotivo.setFont(new Font("Tahoma", Font.BOLD | Font.ITALIC, 12));
		EtiquetaMotivo.setHorizontalAlignment(SwingConstants.CENTER);
		EtiquetaMotivo.setBounds(507, 243, 91, 26);
		contentPane.add(EtiquetaMotivo);
		
		EtiquetaNombre = new JLabel(name+" "+apellido);
		EtiquetaNombre.setHorizontalAlignment(SwingConstants.CENTER);
		EtiquetaNombre.setFont(new Font("Tahoma", Font.BOLD, 13));
		EtiquetaNombre.setBounds(638, 105, 155, 35);
		contentPane.add(EtiquetaNombre);

	}
}
