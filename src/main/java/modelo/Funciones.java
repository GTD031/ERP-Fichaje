package modelo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;
import javax.swing.JTextField;

import App_negocio.Fechas;

public class Funciones {

	public static Connection cn;
	public static PreparedStatement pstmt;

	public static void cerrarConexionDML(String str){
		try {
			pstmt.close();
			cn.close();
		} catch(SQLException clo) {
			System.out.println(str);
		}
	}


    public static Connection getConexion() {
        try {
            return DriverManager.getConnection("jdbc:ucanaccess://src/main/resources/BDFichaje.accdb");
        } catch (SQLException e) {
            javax.swing.JOptionPane.showMessageDialog(null, "Conexión incorrecta");
            return null;
        }
    }
    
	public static void conectar() {
		try {
			cn = DriverManager.getConnection("jdbc:ucanaccess://src/main/resources/BDFichaje.accdb");
		} catch (SQLException e) {
			System.out.println("Error al abrir la base de datos");
		} finally {
			if (cn == null) {
				System.out.println("No hubo conexión");
			}
		}
	}

	public static int[] Diferencia_Horas(LocalTime data1, LocalTime data2) {
			int[] array = new int[2];
			
			array[0] = data2.getHour() - data1.getHour();
			array[1] = data2.getMinute() - data1.getMinute();
			
			if (array[1]<0) {
				array[0]--;
				array[1] += 60; 
			}
		return array;
	}

	public static int[] Diferencia_Horas_receso(String CampoReceso) {
		
		int[] array = new int[2];	//el array de las horas calculadas en las que calculare la funcion.
		if (CampoReceso == null || CampoReceso.equals("")) {
			array[0] = 0;
			array[1] = 0;
			return array;
		}
		
		String hora = "[0-9]{1,2}:[0-9]{2}";		//uso el string 'hora' que actua como un patrón.
		LocalTime[] data = {null, null};
		Pattern PHora = Pattern.compile(hora);			//Creo el patron
		Matcher MHora = PHora.matcher(CampoReceso); 	//y uso Matcher para el campo de receso.
		
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm"); 	//Formateo a horas y minutos
		int h=0,m=0, veces = 0;		//valores que almacenan resp. horas, minutos y las veces que coincido con el patrón.		LocalTime[] data = {date1, date2};
		  while (MHora.find()) {
	        try { 
	            data[veces] = LocalTime.parse((MHora.group().length()==4?"0":"")+MHora.group(), formatter); 	//conversion a LocalDateTime
	            veces++;
				if (veces == 2) {
					array = Diferencia_Horas(data[0],data[1]);
					h += array[0];
					m += array[1]; 
					veces = 0;		//Adiciono horas, minutos y luego reseteo la variable 'veces' a 0.
				}
	        } catch (DateTimeParseException e) {
	        	System.out.println("Error al hacer parseo a fechas.");
	            e.printStackTrace(); 
	        }
		  }
		  veces = m%60;		//reutilizo 'veces': Ahora será el residuo de los minutos almacenados al dividirlo entre 60.
		  
		  array[0] = h + (m-veces)/60;		//Asigno a array[0] las horas acumuladas actualizadas.
		  array[1] = veces;			//por último, asigno los nuevos minutos a array[1].
		return array;
	}

	protected static int[] get_Hours_Day(LocalDate Fecha, String campoDNI) {		//campoDNI será Fechas.DNI2.getText()
		ResultSet rs;
		String Receso, SFecha;
		int[] Suma = {0,0};
		int[] Array = new int[2];
		int[] Array1 = new int[2];
		LocalTime date1, date2;
		DateTimeFormatter formatterH = DateTimeFormatter.ofPattern("HH:mm"), formatterF = DateTimeFormatter.ofPattern("dd/MM/yyyy"); 	//Formateo a horas y minutos y tambien a Dia.
		SFecha = Fecha.format(formatterF);
		conectar();
		try {
			pstmt = cn.prepareStatement("SELECT Nombre, Apellido, Fecha, [Hora de entrada], [Hora de receso], [Hora de salida] FROM Jornada WHERE DNI = ? AND Fecha = ?");
			pstmt.setString(1, campoDNI);
			pstmt.setString(2, SFecha);
			rs = pstmt.executeQuery();
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
			cerrarConexionDML("Hubo error al intentar cerrar las base de datos");
		}
		return Suma;
	}
	

	protected static int[] get_Hours_Month_Today(int Mes, int Año) {	//codificar el mes como un número
		int[] array = {0,0};
		int[] arrayS = new int[2];
		int res;
		LocalDate date = LocalDate.of(Año,Mes,1), Today = LocalDate.now();
		while (date.getDayOfMonth() <= Today.getDayOfMonth() && date.getMonthValue() == Mes) {		//el mes es un entero entre 1 y 12. 
			arrayS = Fechas.get_Hours_Day(date);
			array[0] += arrayS[0];
			array[1] += arrayS[1];	//sumo las hora y los minutos todo chueco, para acomodarlo luego.
			date = date.plusDays(1);
		}
		res = array[1]%60;
		array[0] += (array[1]-res)/60;
		array[1] = res;
		return array;
	}
	
	public static String[] Añadir_usuarios() {
		ResultSet rs;
		String[] lista = null;
		int i = 0, N;
		ArrayList<String> array = new ArrayList<String>();
		Funciones.conectar();
		try {
			pstmt = cn.prepareStatement("SELECT Usuario FROM Empleados");
			rs = pstmt.executeQuery();
			array.add("Seleccione"); //añado el primer elemento
			while(rs.next()) {
				array.add(rs.getString(1));
			}
			N = array.size();
			lista = new String[N];
			while(i<N) {
				lista[i] = array.get(i);
				i++;
			}
			rs.close();
		}catch(SQLException u) {
			System.out.println("Error al conectarse a la BD.");
		} finally {
			cerrarConexionDML("Error al cerrar la consulta de usuarios.");
		}
		return lista;
		}
	
	public static boolean CumplePat(Pattern Ptron, JTextField campo, String strNopat, String strVacio) {
			
			Matcher Mtron = Ptron.matcher(campo.getText());
			
			if (campo.getText().equals("")) {
				JOptionPane.showMessageDialog(null, strVacio, "Campo vacío", JOptionPane.ERROR_MESSAGE);
			} else if (!Mtron.matches()) {
				JOptionPane.showMessageDialog(null, strNopat, "No cumple formato", JOptionPane.ERROR_MESSAGE);
			}
			return (!campo.getText().equals("") && Mtron.matches());
		}
	}
