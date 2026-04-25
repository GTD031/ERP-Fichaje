package App_negocio;

import java.awt.Font;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import modelo.Funciones;

public class App_Administrador2 extends JFrame {

    private static final long serialVersionUID = 1L;
    private JPanel contentPane;
    private JTable table;
    private DefaultTableModel dtm;
    private String dni;
    private DateTimeFormatter formatterH = DateTimeFormatter.ofPattern("HH:mm");//, formatterF = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    protected int año, Numero_mes;
    protected JLabel lbl_HorasTotales;

    // ─── Helpers ─────────────────────────────────────────────────────────────────

   protected int Suma_horas_minutos(int indexColumn) {
	   int S = 0, i = 0, len = dtm.getRowCount();
	   
	   while(i<len) {
		   S += (int) dtm.getValueAt(i, indexColumn);
		   i++;
	   }
	   return S;
   }

   protected int[] conversion(int[] horas_minutos) {
	   int res;
	   
	   res = horas_minutos[1]%60;
	   horas_minutos[0] += (horas_minutos[1]-res)/60;
	   horas_minutos[1] = res;
	   return horas_minutos;
   }
    // ─── Carga de datos ──────────────────────────────────────────────────────────

    /**
     * Columnas JTable:
     *   Fecha | Hora de entrada | Receso | Hora de salida | Horas | Minutos
     *
     * - Receso   → string "X H, Y m"  (horas de pausa acumuladas del día)
     * - Horas    → horas netas trabajadas ese registro  (salida − entrada − receso)
     * - Minutos  → minutos netos del mismo cálculo
     */
    protected void cargarDatos() {

        dtm.setColumnIdentifiers(new Object[]{
            "Fecha", "Hora de entrada", "Hora de receso", "Hora de salida", "Horas", "Minutos"
        });
        dtm.setRowCount(0);

        String query =
            "SELECT Fecha, [Hora de entrada], [Hora de receso], [Hora de salida] " +
            "FROM Jornada WHERE DNI = ? AND MID(Fecha,4,2) = ? "+
            "AND Val(MID(Fecha,7)) = ?";

        try (Connection cn = Funciones.getConexion();
             PreparedStatement pstmt = cn.prepareStatement(query)) {
            pstmt.setString(1, dni);
            pstmt.setString(2, (String.valueOf(this.Numero_mes).length()<2?"0":"")+String.valueOf(this.Numero_mes)); //Me aseguro que el string tenga dos caracteres.
            pstmt.setInt(3, this.año);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {

            	String fecha       = rs.getString(1);
                String horaEntrada = rs.getString(2);
                String campoReceso = rs.getString(3); 
                String horaSalida  = rs.getString(4);
                
                // Receso acumulado del día 
                int[] receso = Funciones.Diferencia_Horas_receso(campoReceso), diff = new int[2];
                String recesoStr = receso[0] + " H, " + receso[1] + " m";

               diff  = Funciones.Diferencia_Horas(LocalTime.parse(rs.getString(2), formatterH),LocalTime.parse(rs.getString(4), formatterH) );
                // Ajuste de acarreo en minutos
               
               diff[0] -= receso[0];
               diff[1] -= receso[1];
               if (diff[1] < 0) {
            	   diff[0]--; 
            	   diff[1] += 60;
            	   }

                dtm.addRow(new Object[]{
                    fecha, horaEntrada, recesoStr, horaSalida, diff[0], diff[1]
                });
            }

        } catch (SQLException e) {
            javax.swing.JOptionPane.showMessageDialog(this,
                "Error al consultar la base de datos:\n" + e.getMessage());
            e.printStackTrace();
        }
    }

    // ─── Constructor ─────────────────────────────────────────────────────────────

    public App_Administrador2(String dni, String nombre, String mes, int año) {
        this.dni = dni;
        this.año = año;
        
        setTitle("Tabla de horas trabajadas");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setBounds(100, 100, 820, 526);
        setLocationRelativeTo(null);
        setResizable(false);

        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(null);

        // Título
        JLabel lbl_titulo = new JLabel("Horas trabajadas");
        lbl_titulo.setFont(new Font("Tahoma", Font.BOLD, 18));
        lbl_titulo.setHorizontalAlignment(SwingConstants.CENTER);
        lbl_titulo.setBounds(260, 10, 300, 40);
        contentPane.add(lbl_titulo);

        // Etiqueta DNI
        JLabel lblDNI = new JLabel("DNI: " + dni);
        lblDNI.setFont(new Font("Tahoma", Font.BOLD, 13));
        lblDNI.setBounds(60, 55, 300, 28);
        contentPane.add(lblDNI);

        // Tabla
        dtm = new DefaultTableModel(new Object[][]{}, new Object[]{}) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // ninguna celda editable
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                // Las columnas Horas (4) y Minutos (5) son enteros → alineación correcta
                if (columnIndex == 4 || columnIndex == 5) return Integer.class;
                return String.class;
            }
        };

        table = new JTable(dtm);
        table.setShowVerticalLines(true);
        table.setShowHorizontalLines(true);
        table.getTableHeader().setReorderingAllowed(false);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBounds(60, 95, 690, 280);
        contentPane.add(scrollPane);

        // Botón cerrar
        JButton botonCerrar = new JButton("CERRAR");
        botonCerrar.setFont(new Font("Tahoma", Font.BOLD, 12));
        botonCerrar.setBounds(341, 448, 120, 30);
        botonCerrar.addActionListener(e -> dispose());
        contentPane.add(botonCerrar);
        
        lbl_HorasTotales = new JLabel("");
        lbl_HorasTotales.setHorizontalAlignment(SwingConstants.CENTER);
        lbl_HorasTotales.setFont(new Font("Tahoma", Font.BOLD, 15));
        lbl_HorasTotales.setBounds(160, 398, 512, 40);
        contentPane.add(lbl_HorasTotales);
        
        JLabel lblNombre = new JLabel(nombre);
        lblNombre.setHorizontalAlignment(SwingConstants.CENTER);
        lblNombre.setFont(new Font("Tahoma", Font.BOLD, 15));
        lblNombre.setBounds(160, 60, 512, 28); // centrado en el frame
        contentPane.add(lblNombre);

        JLabel lblFecha = new JLabel(mes + " " + año);
        lblFecha.setHorizontalAlignment(SwingConstants.RIGHT);
        lblFecha.setFont(new Font("Tahoma", Font.BOLD, 13));
        lblFecha.setBounds(538, 60, 212, 28); // misma posición donde estaba el nombre
        contentPane.add(lblFecha);
        
//        // Cargar datos al construir el frame
//        *cargarDatos();
//        'tiempo[0] = *.Suma_horas_minutos(4);
//        'tiempo[1] = *.Suma_horas_minutos(5);
//        'res = 'tiempo[1]%60;			
//        'tiempo[0] += ('tiempo[1]-'res)/60;
//        'tiempo[1] = 'res;
//        lbl_HorasTotales.setText("Tiempo trabajado: "+'tiempo[0]+" horas y "+'tiempo[1]+" minutos.");
    }
}