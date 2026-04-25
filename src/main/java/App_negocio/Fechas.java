package App_negocio;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import modelo.Funciones;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

public class Fechas extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	protected static JLabel DNI2, lblNombre, lbl_Introducir_fecha;
	private JTextField textField_Fecha_Usuario;
	protected JButton Boton_horas;
	protected Pattern PFecha;
	protected static String nombre, dni;
	protected JComboBox<String> comboBox;
	protected static String[] usuarios;
	protected static DefaultComboBoxModel<String> comb;
	private static boolean enProceso = false;
	private static DocumentListener DocuList;
	
	private static int[] Diferencia_Horas(LocalTime data1, LocalTime data2) {
			int[] array = new int[2];
			
			array[0] = data2.getHour() - data1.getHour();
			array[1] = data2.getMinute() - data1.getMinute();
			
			if (array[1]<0) {
				array[0]--;
				array[1] += 60; 
			}
		return array;
	}

	protected static int[] Diferencia_Horas_receso(String CampoReceso) {
		String hora = "[0-9]{1,2}:[0-9]{2}";		//uso el string 'hola' que actua como un patrón.
		LocalTime[] data = {null, null};
		Pattern PHora = Pattern.compile(hora);		//Creo el patron
		Matcher MHora = PHora.matcher(CampoReceso); 	//y uso Matcher para el campo de receso.
		
		int[] array = new int[2];	//el array de las horas calculadas en las que calculare la funcion.
		if (CampoReceso == null) {
			array[0] = 0;
			array[1] = 0;
			return array;
		}
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm"); 	//Formateo a horas y minutos
		int h=0,m=0, veces = 0;		//valores que almacenan resp. horas, minutos y las veces que coincido con el patrón.		LocalTime[] data = {date1, date2};
		  while (MHora.find()) {
	        try { 
	            data[veces] = LocalTime.parse((MHora.group().length()==4?"0":"")+MHora.group(), formatter); 	//conversion a LocalTime
	            veces++;
				if (veces == 2) {
					array = Diferencia_Horas(data[0],data[1]);
					h += array[0];
					m += array[1]; 
					veces = 0;		//Adiciono horas, minutos y luego reseteo la variable veces a 0.
				}
	        } catch (DateTimeParseException e) {
	        	System.out.println("Error al hacer parseo a fechas.");
	            e.printStackTrace(); 
	        }
		  }	//si un receso no se completó (veces = 1), entonces la jornada no se registra.
		  
		  veces = m%60;		//reutilizo 'veces': Ahora será el residuo de los minutos almacenados.
		  array[0] = h + (m-veces)/60;		//Asigno a array[0] las horas acumuladas actualizadas.
		  array[1] = veces;			//por último, asigno los nuevos minutos a array[1].
		return array;
	}
	//HASTA AQUI, VEREMOS SI SE PUEDE OMITIR
	public static int[] get_Hours_Day(LocalDate Fecha) {
		ResultSet rs;
		String Receso, SFecha;
		int[] Suma = {0,0};
		int[] Array = new int[2], Array1 = new int[2];
		LocalTime date1, date2;
		DateTimeFormatter formatterH = DateTimeFormatter.ofPattern("HH:mm"), formatterF = DateTimeFormatter.ofPattern("dd/MM/yyyy"); 	//Formateo a horas y minutos y tambien a Dia.
		SFecha = Fecha.format(formatterF);
		Funciones.conectar();
		try {
			Funciones.pstmt = Funciones.cn.prepareStatement("SELECT Nombre, Apellido, Fecha, [Hora de entrada], [Hora de receso], [Hora de salida] FROM Jornada WHERE DNI = ? AND Fecha = ?");
			Funciones.pstmt.setString(1, DNI2.getText());
			Funciones.pstmt.setString(2, SFecha);
			rs = Funciones.pstmt.executeQuery();
			if (!rs.next()) {
				JOptionPane.showMessageDialog(null, "Usted no está en la base de datos de la empresa ó no laboró en el día especificado.", "Sin datos", JOptionPane.ERROR_MESSAGE);
			} else {
				do {
					date1 = LocalTime.parse(rs.getString(4), formatterH);
					date2 = LocalTime.parse(rs.getString(6), formatterH);
					Receso = rs.getString(5);
					Array = Diferencia_Horas_receso(Receso);
					Array1 = Diferencia_Horas(date1, date2);
					
					Suma[0] += Array1[0]-Array[0];		//(*) desde aqui..
					Suma[1] += Array1[1]-Array[1];
					if (Suma[1]<0) {
						Suma[0]--;
						Suma[1] += 60;
					}			//hasta aqui es la resta de horas, tal como en la funcion Diferencia_Horas(), y las voy sumando. 
				}while(rs.next());
				JOptionPane.showMessageDialog(null, "Ud tiene en total "+Suma[0]+" horas trabajadas con "+Suma[1]+" minutos.");
			}  	//REVISAR 
		} catch(SQLException d) {
			System.out.println("No se pudo conectar a la base de datos.");
			d.printStackTrace();
		} finally {
			Funciones.cerrarConexionDML("Hubo error al intentar cerrar las base de datos");
		}
		return Suma;
	}
	
	public static String[] FiltrarUsuarios(String str) {
		int i = 1, N = usuarios.length;
		ArrayList<String> filtrados = new ArrayList<String>();
		Pattern filtro = Pattern.compile("^"+str.toLowerCase()+".*");	//coincide con lo que empieza con este string (str). 
		comb = new DefaultComboBoxModel<String>(usuarios);
		String elemento;
		String[] retorno;
		while (i<N) {
			elemento = comb.getElementAt(i);
			if (filtro.matcher(elemento.toLowerCase()).matches())	//colocar el objeto
				filtrados.add(elemento);
			i++;
		}
		N = filtrados.size();
		i = 1;				//reutilizo ambas variables i, N.
		retorno = new String[++N];
		retorno[0] = str;
		while (i<N) {
			retorno[i] = filtrados.get(i-1);
			i++;
		}
		return retorno;
	}

	/**
	 * Create the frame.
	 */
	public Fechas(String mensaje, boolean bool) {
		
		usuarios = Funciones.Añadir_usuarios();
		comb = new DefaultComboBoxModel<String>(usuarios);
		
		setTitle("Horas trabajadas");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 583, 562);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel label_titulo = new JLabel("Gestor de horas");
		label_titulo.setForeground(new Color(0, 128, 64));
		label_titulo.setHorizontalAlignment(SwingConstants.CENTER);
		label_titulo.setFont(new Font("Tahoma", Font.BOLD, 22));
		label_titulo.setBounds(132, 32, 276, 49);
		contentPane.add(label_titulo);
		
		JLabel label_DNI = new JLabel("DNI");
		label_DNI.setHorizontalAlignment(SwingConstants.CENTER);
		label_DNI.setFont(new Font("Tahoma", Font.BOLD, 18));
		label_DNI.setBounds(76, 81, 200, 28);
		contentPane.add(label_DNI);
		
		DNI2 = new JLabel("");
		DNI2.setHorizontalAlignment(SwingConstants.CENTER);
		DNI2.setFont(new Font("Tahoma", Font.BOLD, 18));
		DNI2.setBounds(233, 81, 200, 28);
		contentPane.add(DNI2);
		
		comboBox = new JComboBox<String>();
		comboBox.setVisible(bool);
		comboBox.setModel(comb);
		comboBox.setEditable(true);

		JTextField editor = (JTextField) comboBox.getEditor().getEditorComponent();	//hecho un casteo a Jtextfield para acceder al contenido con el método getText().
		DocuList = new DocumentListener() {

			public void Actualizar() {
				
					if (enProceso) return;
					
					SwingUtilities.invokeLater(() -> { //pospone la ejecucion al final

					enProceso = true;
					String src = editor.getText();
					String[] lista = {};
					
					if (src.equals("")) {
						comb = new DefaultComboBoxModel<String>(usuarios);
					} else {
						lista = FiltrarUsuarios(src);
						comb = new DefaultComboBoxModel<String>(lista);
					}
					comboBox.setModel(comb);
					comboBox.setPopupVisible(!src.equals(""));
					editor.setText(src);
					enProceso = false;
				});
			}
			
			public void insertUpdate(DocumentEvent e) {
				Actualizar();
			}
		    @Override
		    public void removeUpdate(DocumentEvent e) {
		    	Actualizar();
		    }

		    @Override
		    public void changedUpdate(DocumentEvent e) {
		    	System.out.println("Formato cambiado");
		    };
		};
		editor.getDocument().addDocumentListener(DocuList);
		editor.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {	//falta evitar que se autocomplete al viajar por las opciones con las flechas.
				
				if (e.getKeyCode()==38 || e.getKeyCode()==40) {
					enProceso = true;
					comboBox.setPopupVisible(true);	//mantenerlo visible el menú
				} else {
					comboBox.setPopupVisible(!(e.getKeyCode()==10));
					enProceso = false;
				}
			}
		});
		comboBox.setBounds(10, 243, 159, 39);
		contentPane.add(comboBox);	

		JSpinner spinner = new JSpinner();
		spinner.setModel(new SpinnerNumberModel(2026, 1910, 2026, 1));
		spinner.setFont(new Font("Tahoma", Font.BOLD, 15));
		spinner.setBounds(415, 241, 132, 39);
		contentPane.add(spinner);
		spinner.setVisible(bool);
		
		JLabel lblAño = new JLabel("Año");
		lblAño.setHorizontalAlignment(SwingConstants.CENTER);
		lblAño.setFont(new Font("Tahoma", Font.BOLD, 15));
		lblAño.setBounds(405, 197, 142, 34);
		contentPane.add(lblAño);
		lblAño.setVisible(bool);

		JComboBox<String> comboBox_Mes = new JComboBox<String>();
		comboBox_Mes.setModel(new DefaultComboBoxModel<String>(new String[] {"Seleccione", "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre ", "Octubre", "Noviembre", "Diciembre"}));
		comboBox_Mes.setBounds(192, 243, 181, 39);
		contentPane.add(comboBox_Mes);
		comboBox_Mes.setVisible(bool);
		
		Boton_horas = new JButton("Ver horas");
		Boton_horas.setMnemonic('d');
		Boton_horas.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String Dia;
				int[] array = new int[2];
				boolean bool_usu_adm;
				if (!bool) {
					Dia = textField_Fecha_Usuario.getText();
					bool_usu_adm = Funciones.CumplePat(PFecha, textField_Fecha_Usuario, "Debe rellenar la fecha", "La fecha debe cumplir el formato indicado.");
					if (bool_usu_adm) {
						LocalDate date = LocalDate.of(Integer.parseInt(Dia.substring(6)),Integer.parseInt(Dia.substring(3,5)),Integer.parseInt(Dia.substring(0,2)));
						get_Hours_Day(date);	//Aqui solo basta con ejecutarla (el valor nos interesará cuando lo hagamos por meses).
					} 	//si no -> no hace nada
					
				} else {			//REVISAR en caso de ser admin, que es este.
					if (comboBox.getSelectedItem().toString().equals("Seleccione") || comboBox_Mes.getSelectedIndex()==0)
						JOptionPane.showMessageDialog(null, "Debe introducir un usuario, mes y el año a averiguar.", "Error", JOptionPane.ERROR_MESSAGE);
					else {
						ResultSet rs;
						Funciones.conectar();
						try {
							Funciones.pstmt = Funciones.cn.prepareStatement("SELECT DNI, Nombre, Apellido FROM Empleados WHERE Usuario = ?");
							Funciones.pstmt.setString(1, String.valueOf(comboBox.getSelectedItem()));
							rs = Funciones.pstmt.executeQuery();
							if (rs.next()) {
								dni = rs.getString(1);
								nombre = rs.getString(2)+" "+rs.getString(3);
								Administrador admin = new Administrador(dni,nombre, comboBox_Mes.getSelectedItem().toString(),(int) spinner.getValue());
						        
								// Cargar datos al construir el frame
						        
								admin.Numero_mes =  (int) comboBox_Mes.getSelectedIndex(); 	//Esencial para cargar los datos
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
		Boton_horas.setFont(new Font("Tahoma", Font.BOLD, 18));
		Boton_horas.setBounds(186, 350, 191, 49);
		contentPane.add(Boton_horas);
		
		lblNombre = new JLabel("");
		lblNombre.setFont(new Font("Tahoma", Font.BOLD, 15));
		lblNombre.setHorizontalAlignment(SwingConstants.CENTER);
		lblNombre.setBounds(156, 132, 230, 39);
		contentPane.add(lblNombre);
		
		lbl_Introducir_fecha = new JLabel(mensaje);
		lbl_Introducir_fecha.setFont(new Font("Tahoma", Font.BOLD, 14));
		lbl_Introducir_fecha.setHorizontalAlignment(SwingConstants.CENTER);
		lbl_Introducir_fecha.setBounds(86, 198, 380, 33);
		contentPane.add(lbl_Introducir_fecha);
		
		textField_Fecha_Usuario = new JTextField();
		textField_Fecha_Usuario.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				boolean bool;
				String Fecha = textField_Fecha_Usuario.getText();
				Matcher MFecha = PFecha.matcher(Fecha);
				bool = MFecha.matches();
				Boton_horas.setEnabled(bool);
				if (Fecha.equals("")) {
					lbl_Introducir_fecha.setForeground(new Color(0,0,0)); //Negro?
					lbl_Introducir_fecha.setText(mensaje);
				} else if (!bool) {
					lbl_Introducir_fecha.setForeground(new Color(255,0,0)); //Rojo
					lbl_Introducir_fecha.setText("Formato inválido.");
				} else {
					lbl_Introducir_fecha.setForeground(new Color(0,190,0)); //Verde
					lbl_Introducir_fecha.setText("Formato válido.");
				}
			}
		});
		textField_Fecha_Usuario.setBounds(186, 238, 200, 49);
		contentPane.add(textField_Fecha_Usuario);
		textField_Fecha_Usuario.setColumns(10);
		textField_Fecha_Usuario.setVisible(!bool);
		
		JButton btnCerrar = new JButton("Cerrar");
		btnCerrar.addActionListener(e -> dispose());
		btnCerrar.setFont(new Font("Tahoma", Font.BOLD, 15));
		btnCerrar.setBounds(182, 450, 191, 49);
		contentPane.add(btnCerrar);
		
		JLabel lblNombre_usuario = new JLabel("Usuario");
		lblNombre_usuario.setFont(new Font("Tahoma", Font.BOLD, 15));
		lblNombre_usuario.setHorizontalAlignment(SwingConstants.CENTER);
		lblNombre_usuario.setBounds(27, 198, 142, 34);
		contentPane.add(lblNombre_usuario);
		lblNombre_usuario.setVisible(bool);

	}
}
