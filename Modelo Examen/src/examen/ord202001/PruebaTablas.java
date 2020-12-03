package examen.ord202001;

import java.awt.Color;
import java.awt.Insets;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.TreeSet;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.table.TableColumn;

import examen.ord202001.TablaEstadistica.CalculadorColumna;
import examen.ord202001.TablaEstadistica.TipoEstad;

/** Clase principal de prueba de algunas tablas de datos, estadísticas y de análisis
 * @author andoni.eguiluz @ ingenieria.deusto.es
 */
public class PruebaTablas {
	
	/** Método principal de prueba
	 * @param args	No utilizado
	 */
	public static void main( String[] args ) {
		SwingUtilities.invokeLater( () -> {
			sacaVentanasPrueba();
		} );
	}
	
	private static Connection conn = null;
	private static Statement stat = null;
	
	private static void sacaVentanasPrueba() {
		try {

			// 1.- Intenta poner Look & Feel Nimbus
			try {  
			    for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
			        if ("Nimbus".equals(info.getName())) {
			            UIManager.setLookAndFeel(info.getClassName());
			            UIManager.getLookAndFeelDefaults().put("Table.cellNoFocusBorder", new Insets(0,0,0,0));
			            UIManager.getLookAndFeelDefaults().put("Table.focusCellHighlightBorder", new Insets(0,0,0,0));
			            UIManager.getLookAndFeelDefaults().put("Button.contentMargins", new Insets(3, 6, 3, 6));		            
			            UIManager.getLookAndFeelDefaults().put("DesktopIcon.contentMargins", new Insets(0,0,0,0));
			            UIManager.getLookAndFeelDefaults().put("Label.contentMargins", new Insets(0,0,0,0));		            
			            break;
			        }
			    }
			} catch (Exception e) {} // Si no está disponible nimbus, no se hace nada
			
			// 2.- Crea una tabla estadística de prueba con datos directos desde código
			TablaEstadistica tabla = new TablaEstadistica();
			tabla.addColumna( "datoI", new Integer(0) );
			tabla.addColumna( "datoD", new Double(0) );
			tabla.addColumna( "datoD2", new Double(0) );
			tabla.addColumna( "datoB", new Boolean(false) );
			tabla.addColumna( "datoS", "" );
			tabla.addColumna( "tipo", "" );
			tabla.addDataLine( new ArrayList<Object>( Arrays.asList( 8, 8.1, 4.5, true, "8", "dataset1" ) ) );
			tabla.addDataLine( new ArrayList<Object>( Arrays.asList( 7, 7.1, 3.2, false, "7", "dataset1" ) ) );
			tabla.addDataLine( new ArrayList<Object>( Arrays.asList( 0, null, 1.8, true, "sin Double", "dataset1" ) ) );
			tabla.addDataLine( new ArrayList<Object>( Arrays.asList( null, 1.0, 7.3, false, "sin Integer", "dataset1" ) ) );
			tabla.addDataLine( new ArrayList<Object>( Arrays.asList( 7, 7.0, 6.2, false, "7", "dataset1" ) ) );
			tabla.addDataLine( new ArrayList<Object>( Arrays.asList( 7, 7.0, 1.7, false, "7", "dataset1" ) ) );
			tabla.addDataLine( new ArrayList<Object>( Arrays.asList( 7, 7.0, 4.3, false, "7", "dataset1" ) ) );
			tabla.addDataLine( new ArrayList<Object>( Arrays.asList( 4, 3.8, 2.8, false, "3.8", "dataset1" ) ) );
			tabla.addDataLine( new ArrayList<Object>( Arrays.asList( 1, 1.0, 2.3, false, "1", "dataset2" ) ) );
			tabla.addDataLine( new ArrayList<Object>( Arrays.asList( 2, 2.8, 1.8, false, "2.8", "dataset2" ) ) );
			tabla.addDataLine( new ArrayList<Object>( Arrays.asList( 0, 0.8, 0.4, false, "0.8", "dataset2" ) ) );
			tabla.addCalcColumn( "Par-Impar", String.class, new CalculadorColumna() {
				@Override
				public Object calcula(TablaEstadistica to, int fila) {
					Object o = to.get( fila, "datoI" ); 
					if (o==null) return "nulo";
					if (o instanceof Integer) {
						Integer i = (Integer) o;
						if (i==0) return "Cero";
						else if (i%2==0) return "Par";
						else return "Impar";
					} else {
						return null;
					}
				}
			});
			System.out.println( tabla );
			System.out.print( "Tipos:" );
			for (int i=0; i<tabla.getWidth(); i++) System.out.print( tabla.getType(i).getSimpleName() + "\t" );
			System.out.println();
			
			// 3.- Crea una ventana general para visualizar las tablas y añade una ventana interna con los datos de prueba
			VentanaGeneral vgt = new VentanaGeneral();
			VentanaTabla v = mainNuevaVent( vgt, tabla, "Test" );
			vgt.setSize( 1100, 700 );
			vgt.setVisible( true );
			v.setVisible( true );
			v.pack();
	
			// 4.- Crea una segunda tabla estadística filtrada de la primera y la añade a la ventana
			TablaEstadistica dataset1 = tabla.creaTablaFiltro( "tipo", true, "DATASET1", null, "datoI", "datoD", "datoD2", "Par-Impar" );
			VentanaTabla vDS = mainNuevaVent( vgt, dataset1, "dataset1" );
			vDS.setLocation( 600, 0 );
			vDS.setVisible( true );
			vDS.pack();

			// 5.- Lee una tabla estadística de datos desde fichero csv y la colorea
			// (t1 a t5 son tiempos en niveles 1 a 5, p1 a p5 puntos en esos niveles)
			ArrayList<Color> lColores = new ArrayList<>( Arrays.asList( new Color(247,202,121), new Color(146,208,80), new Color(255,0,0), new Color(39,228,253), Color.YELLOW  ) );
			ArrayList<Color> lColoresBorde = new ArrayList<>( Arrays.asList( Color.BLACK, Color.BLACK, Color.BLACK, Color.BLACK, Color.BLACK ) );
			TablaEstadistica ta = new TablaEstadistica( Tabla.processCSV(new File( "src/examen/ord202001/datos.csv" ) ) );
			VentanaTabla vDatos = mainNuevaVent( vgt, ta, "Puntuaciones y tiempos" );
			vDatos.setMensaje( "Doble click para cambiar valor" );
			vDatos.setLocation( 0, 300 );
			Color ROJO_CLARO = new Color( 255, 180, 180 );
			Color VERDE_CLARO = new Color( 180, 255, 180 );
			ta.setRenderer4Cuartiles( vDatos, true, "t1", "t5", Color.WHITE, new Color(170,170,255), new Color(90,90,255), Color.BLUE );
			ta.setRenderer4Cuartiles( vDatos, false, "p1", "p5", Color.RED, ROJO_CLARO, VERDE_CLARO, Color.GREEN );
			vDatos.setVisible( true );
			vDatos.pack();
			
			// 6.- Crea una tabla de análisis de medias de tiempos (por equipos) desde la tabla de datos
			TablaAnalisis analTiempos = ta.creaTablaEstad( TipoEstad.MEDIA, "equipo", "t1", "t2", "t3", "t4", "t5" );
			VentanaTabla vTiempos = mainNuevaVent( vgt, analTiempos, "Media de tiempos por equipo" );
			vTiempos.setLocation( 700, 300 );
			vTiempos.setVisible( true );
			vTiempos.pack();

			// 7.- Crea una tabla de análisis de suma de puntos (por equipos) de desde la tabla de datos
			TablaAnalisis analPuntos = ta.creaTablaEstad( TipoEstad.SUMATORIO, "equipo", "p1", "p2", "p3", "p4", "p5" );
			VentanaTabla vPuntos = mainNuevaVent( vgt, analPuntos, "Suma de puntos por equipo" );
			vPuntos.setLocation( 700, 450 );
			vPuntos.setVisible( true );
			vPuntos.pack();
			
			// 8.- Saca gráficos de estas dos tablas de análisis
			analTiempos.sacaGrafico( "Gráfico de tiempos medios por equipo", lColores, lColoresBorde, 40, 5 );
			analPuntos.sacaGrafico( "Gráfico de puntuaciones totales por equipo", lColores, lColoresBorde, 2500, 500 );
			analTiempos.getGrafico().getJFrame().setLocation( 100, 700 );
			analPuntos.getGrafico().getJFrame().setLocation( 750, 700 );
			
			// 9.- Crea la base de datos de esta tabla
			conn = BD.initBD( "parc202001.bd" );
			// BD.borrarBD( conn );  // Si se quieren borrar las tablas
			stat = BD.usarCrearTablasBD( conn, true, ta );
			vgt.addWindowListener( new WindowAdapter() { // Cerrar la bd al cerrar la ventana
				@Override
				public void windowClosed(WindowEvent e) {
					BD.cerrarBD( conn, stat );
				}
			});
			
			// 10.- Añade la gestión de doble click para cambiar valor de la tabla
			VentanaTabla.EventoEnCelda ev = new VentanaTabla.EventoEnCelda() {
				@Override
				public void evento(VentanaTabla tabla, int fila, int columna) {
					if (columna<2) return;  // Solo se pueden editar las columnas 2 en adelante
					Tabla t = tabla.getTabla();
					Class<?> tipoDato = t.getTipos().get( columna );
					Object o = t.get( fila, columna );
					String valAnterior = "";
					if (o!=null) valAnterior = o.toString();
					String nuevoVal = (String) JOptionPane.showInternalInputDialog( tabla, "Introduce nuevo valor:", "Cambio de dato " + t.getCabecera(columna), JOptionPane.QUESTION_MESSAGE, null, null, valAnterior );
					if (nuevoVal!=null) {
						if (tipoDato==String.class) {
							tabla.getTabla().set( fila, columna, nuevoVal );
							cambioDeValor( t, fila, columna, nuevoVal );
						} else if (tipoDato==Integer.class) {
							try {
								Object oNuevo = new Integer( Integer.parseInt( nuevoVal ) );
								tabla.getTabla().set( fila, columna, oNuevo );
								cambioDeValor( t, fila, columna, oNuevo );
							} catch (NumberFormatException e) {
								JOptionPane.showInternalMessageDialog( tabla, "Valor introducido incorrecto:" + nuevoVal, "Error", JOptionPane.ERROR_MESSAGE );
							}
						} else if (tipoDato==Double.class) {
							try {
								Object oNuevo = new Double( Double.parseDouble( nuevoVal ) );
								tabla.getTabla().set( fila, columna, oNuevo );
								cambioDeValor( t, fila, columna, oNuevo );
							} catch (NumberFormatException e) {
								JOptionPane.showInternalMessageDialog( tabla, "Valor introducido incorrecto:" + nuevoVal, "Error", JOptionPane.ERROR_MESSAGE );
							}
						}
					}
				}
			};
			vDatos.setDobleClickCelda( ev );
			
			// T2
			vgt.addMenuAccion( "T2", (e) -> {
				HashMap<Integer,TreeSet<Puntuacion>> mapa = new HashMap<>();
				for (int nivel=1; nivel<=5; nivel++) mapa.put( nivel, new TreeSet<Puntuacion>() );  // Crea los sets vacíos en el mapa
				for (int fila=0; fila<ta.size(); fila++) {
					for (int nivel=1; nivel<=5; nivel++) {
						Integer p = (Integer) ta.get( fila, "p" + nivel );  // Coge la puntuación  p'n
						Double t = (Double) ta.get( fila, "t" + nivel );    // Coge el tiempo  t'n
						String nick = (String) ta.get( fila,  "nick" );     // Coge el nick
						if (p!=null && t!=null) {
							mapa.get( nivel ).add( new Puntuacion( nick, p, t ) );
						}
					}
				}
				for (int nivel=1; nivel<=5; nivel++) {
					System.out.println( "Clasificación de nivel " + nivel + ":" );
					for (Puntuacion punt : mapa.get( nivel )) {
						System.out.println( "  " + punt );
					}
				}
			} );

		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
		// Método llamado tras cambiar el valor de la tabla con doble click y un valor correcto
		private static void cambioDeValor( Tabla tabla, int fila, int columna, Object valorNuevo ) {
			BD.updateValor( stat, tabla, fila, columna );
		}

		private static VentanaTabla mainNuevaVent( VentanaGeneral vgt, Tabla tabla, String tit ) {
			VentanaTabla v = new VentanaTabla( vgt, tit, true );
			v.setTabla( tabla );
			vgt.addVentanaInterna( v, tit );
			for (int i=0; i<tabla.getWidth(); i++) {
				TableColumn tCol = v.getJTable().getColumnModel().getColumn(i);
			    if (tabla.getType(i) == Integer.class) {
			        tCol.setPreferredWidth(35);
			    } else if (tabla.getType(i) == Double.class) {
			        tCol.setPreferredWidth(50);
			    } else if (tabla.getType(i) == Boolean.class) {
			        tCol.setPreferredWidth(20);
			    } else {
			        tCol.setPreferredWidth(80);
			    }
			}
			return v;
		}
	

}
