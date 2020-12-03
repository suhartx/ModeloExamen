package examen.ord202001;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

/** Clase de visualización de gráfico de columnas acumuladas partiendo de tablas de datos estadísticos
 * @author andoni.eguiluz @ ingenieria.deusto.es
 */
public class GraficoColumnas {

	// Atributos de información para el dibujado de las columnas
	
	private static int UMBRAL_SIN_BORDE = 4; // Con estos pixels o menos de ancho, no se dibuja borde
	private static int ESP_ARRIBA = 10;  // Píxels de espaciado superior
	private static int ESP_ABAJO = 30;  // Píxels de margen inferior
	private static int ESP_IZQDA = 60;  // Píxels de margen izquierdo
	private static int ESP_DCHA = 10;  // Píxels de margen derecho
	private static int ESP_ENTRECOLS = 10;  // Píxels entre columnas
	private static Font FONT_MARKS = new Font( "Arial", Font.PLAIN, 14 );  // Tipo de letra de las marcas a la izquierda
	private static double FONT_ESP_HOR = 7.5; // Píxels por carácter
	private static double FONT_ESP_VER = 5; // Píxels de offset abajo de marca
	private static Color COLOR_EJES = Color.DARK_GRAY; // Color de los ejes de referencia
	private static float GROSOR_EJES = 2f; // Grosor de los ejes en píxels
	private static Color COLOR_MARCAS = Color.LIGHT_GRAY; // Color de las líneas de las marcas de escala vertical
	private static float GROSOR_MARCAS = 1f;  // Grosor de las líneas de marca
	private static float GROSOR_LIN_BLOQUES = 1f; // Grosor de las líneas de los bloques 

	private ArrayList<String> etiquetasDim1Hor;  // Textos correspondientes a la dimensión 1 (columnas horizontales)
	private ArrayList<String> etiquetasDim2Ver;  // Textos correspondientes a la dimensión 2 (bloques verticales) (actualmente no se visualizan)
	private ArrayList<Color> coloresDim2;  // Colores de los bloques verticales de abajo hacia arriba
	private ArrayList<Color> coloresDim2Borde;  // Colores de borde de los bloques verticales
	private Double[][] datos;  // Datos numéricos que se representarán
	private VentanaGrafica ventana;  // Ventana gráfica donde se hace la visualización
	private double escalaVIni;  // Escala vertical inicio (normalmente 0)
	private double escalaVFin;  // Escala vertical final (valor representado en la parte superior)
	private double escalaVMarcas;  // Escala de las marcas verticales (menor que escalaVFin)
	private int numDecimalesMarcas = 0;  // Número de decimales a indicar en las marcas verticales

	// ==============================================================
	//  Parte static
	
	// Lista de ventanas para escuchador de teclado (zoom con Ctrl++ y Ctrl+-)
	private static ArrayList<GraficoColumnas> lVentanas = new ArrayList<>();
	static {  // Escuchador de teclado global para Ctrl++ y Ctrl+- para hacer zoom+ y zoom-
		VentanaGrafica.addKeyListener( new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {  // Ctrl++ y Ctrl+-
				if (e.getSource() instanceof JFrame) {
					for (GraficoColumnas v : lVentanas) {
						if (v.getJFrame() == e.getSource()) {
							if (e.isControlDown()) {
								if (e.getKeyCode() == KeyEvent.VK_PLUS) {
									v.escalaVFin = v.escalaVFin / 1.25;
									v.escalaVMarcas = v.escalaVMarcas / 1.25;
								} else if (e.getKeyCode() == KeyEvent.VK_MINUS) {
									v.escalaVFin = v.escalaVFin * 1.25;
									v.escalaVMarcas = v.escalaVMarcas * 1.25;
								}
								v.dibuja();
							}
						}
					}
				}
			}
		});
	}
	
	
	// ==============================================================
	//  Métodos
	
	/** Crea un nuevo gráfico de columnas agrupadas
	 * @param titulo	Título de la ventana
	 */
	public GraficoColumnas( String titulo ) {
		lVentanas.add( this );
		String tit = titulo==null ? "Columnas agrupadas" : titulo;
		ventana = new VentanaGrafica( anchura, altura, tit ); 
		ventana.setDibujadoInmediato( false ); 
		ventana.getJFrame().addComponentListener( new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				dibuja();
			}
		});
		ventana.getJFrame().getContentPane().addMouseListener( escuchadorBotonDerecho );
		ventana.getPanel().addMouseListener( escuchadorBotonDerecho );
	}
	
	public JFrame getJFrame() {
		return ventana.getJFrame();
	}
	
	/** Carga los datos del gráfico desde una tabla de análisis de datos objetos formateada de la siguiente forma:<br/>
	 * - Columna 1: Los nombres de las barras verticales
	 * - Columnas 2 a n: Los datos de las filas
	 * @param to	Tabla desde la que cargar datos
	 * @param colores	Lista de colores de relleno a usar (se usan solo los n primeros)
	 * @param coloresBorde	Lista de colores de borde a usar
	 */
	public void importaDatosDesdeTabla( TablaAnalisis to, List<Color> colores, List<Color> coloresBorde ) {
		ArrayList<String> barras = new ArrayList<>();
		for (int fila=0; fila<to.size(); fila++) {
			Object val = to.get( fila, 0);
			barras.add( val==null ? "null" : val.toString() );
		}
		setDim1Hor( barras );
		ArrayList<String> alturas = new ArrayList<>();
		int nCol = 1;
		while (nCol<to.getCabeceras().size() && !to.getCabeceras().get(nCol).equals("N")) {
			alturas.add( to.getCabeceras().get(nCol) );
			nCol++;
		}
		setDim2Ver( alturas );
		setDim2Col( new ArrayList<Color>( colores ) );
		setDim2ColBorde( new ArrayList<Color>( coloresBorde ) );
		Double[][] tablaDatos = new Double[barras.size()][alturas.size()];
		for (int fila=0; fila<barras.size(); fila++) {
			for (int col=0; col<alturas.size(); col++) {
				Object val = to.get( fila, col+1 );
				if (val==null) tablaDatos[fila][col] = null;
				else if (val instanceof Double) tablaDatos[fila][col] = (Double) val;
				else if (val instanceof Integer) tablaDatos[fila][col] = new Double( ((Integer)val).intValue() );
				else if (val instanceof Long) tablaDatos[fila][col] = new Double( ((Long)val).longValue() );
			}
		}
		setDatos( tablaDatos );
	}
	
	/** Cambia el número de decimales para las marcas verticales
	 * @param numDecs	Número de decimales (0 para sin decimales)
	 */
	public void setNumDecimalesMarcas( int numDecs ) {
		numDecimalesMarcas = numDecs;
	}
	
	/** Modifica las etiquetas de la dimensión horizontal (nombres de las columnas)
	 * @param etiquetasDim1	Lista de etiquetas
	 */
	public void setDim1Hor( ArrayList<String> etiquetasDim1 ) {
		etiquetasDim1Hor = etiquetasDim1;
	}
	
	/** Modifica las etiquetas de la dimensión horizontal (nombres de las columnas)
	 * @param etiquetasDim1	Etiquetas
	 */
	public void setDim1Hor( String... etiquetasDim1 ) {
		etiquetasDim1Hor = new ArrayList<>();
		for (String s : etiquetasDim1) etiquetasDim1Hor.add( s );
	}
	
	/** Modifica las etiquetas de la dimensión vertical (nombres de los bloques de valores)
	 * @param etiquetasDim2	Lista de etiquetas
	 */
	public void setDim2Ver( ArrayList<String> etiquetasDim2 ) {
		etiquetasDim2Ver = etiquetasDim2;
	}
	
	/** Modifica las etiquetas de la dimensión vertical (nombres de los bloques de valores)
	 * @param etiquetasDim2	Etiquetas
	 */
	public void setDim2Ver( String... etiquetasDim2 ) {
		etiquetasDim2Ver = new ArrayList<>();
		for (String s : etiquetasDim2) etiquetasDim2Ver.add( s );
	}
	
	/** Modifica los colores a utilizar en vertical
	 * @param coloresDim2	Lista de colores de abajo arriba
	 */
	public void setDim2Col( ArrayList<Color> coloresDim2 ) {
		this.coloresDim2 = coloresDim2;
	}
	
	/** Modifica los colores a utilizar en vertical
	 * @param coloresDim2	Colores de abajo arriba
	 */
	public void setDim2Col( Color... coloresDim2 ) {
		this.coloresDim2 = new ArrayList<>();
		for (Color c : coloresDim2) this.coloresDim2.add( c );
	}
	
	/** Modifica los colores de borde a utilizar en vertical
	 * @param coloresDim2	Lista de colores de borde de abajo arriba
	 */
	public void setDim2ColBorde( ArrayList<Color> coloresDim2 ) {
		this.coloresDim2Borde = coloresDim2;
	}
	
	/** Modifica los colores de borde a utilizar en vertical
	 * @param coloresDim2	Colores de borde de abajo arriba
	 */
	public void setDim2ColBorde( Color... coloresDim2 ) {
		this.coloresDim2Borde = new ArrayList<>();
		for (Color c : coloresDim2) this.coloresDim2Borde.add( c );
	}
	
	/** Cambia los datos a visualizar
	 * @param tablaDatos	Tabla de datos
	 */
	public void setDatos( Double[][] tablaDatos ) {
		datos = tablaDatos;
	}
		
	/** Cambia los valores de la escala vertical
	 * @param escIni	Valor mínimo
	 * @param escFin	Valor máximo
	 * @param escMarcas	Distancia entre marcas
	 */
	public void setEscalaAltura( double escIni, double escFin, double escMarcas ) {
		escalaVIni = escIni;
		escalaVFin = escFin;
		escalaVMarcas = escMarcas;
	}
		
	/** Visualiza la ventana gráfica donde se dibujarán las columnas
	 * @param anchura
	 * @param altura
	 */
	public void visualiza( int anchura, int altura ) {
		ventana.getJFrame().setSize( anchura, altura );
		if (SwingUtilities.isEventDispatchThread())
			ventana.getJFrame().setVisible( true );
		else
			try {
				SwingUtilities.invokeAndWait( () -> ventana.getJFrame().setVisible( true ) );
			} catch (Exception e) {
				e.printStackTrace();
			}
	}
	
	
	/** Dibuja en la ventana las columnas partiendo de los datos y el resto de configuración
	 */
	public void dibuja() {
		ventana.borra();
		calculaCoords();
		dibujaEjes();
		dibujaGrafico();
	}
	
	// ==============================================================
	//  Proceso interno de dibujado
	
	// Variables internas para dibujado
	private int altura;
	private int anchura;
	private int altoDibujo;
	private int anchoDibujo;
	private double anchoColumna;

		private void calculaCoords() {
			if (etiquetasDim1Hor==null || etiquetasDim2Ver==null || datos==null) return;
			altura = ventana.getAltura();
			anchura = ventana.getAnchura();
			altoDibujo = altura-ESP_ARRIBA-ESP_ABAJO;
			anchoDibujo = anchura-ESP_IZQDA-ESP_DCHA;
			anchoColumna = (1.0 * (anchoDibujo - ESP_ENTRECOLS) / etiquetasDim1Hor.size()) - ESP_ENTRECOLS;
		}
		private void dibujaEjes() {
			if (etiquetasDim1Hor==null || etiquetasDim2Ver==null || datos==null) return;
			ventana.dibujaLinea( ESP_IZQDA, ESP_ARRIBA, ESP_IZQDA, ESP_ARRIBA + altoDibujo, GROSOR_EJES, COLOR_EJES );
			ventana.dibujaLinea( ESP_IZQDA, ESP_ARRIBA + altoDibujo, anchoDibujo, ESP_ARRIBA + altoDibujo, GROSOR_EJES, COLOR_EJES );
			ventana.dibujaTexto( ESP_IZQDA - FONT_ESP_HOR * 2, ESP_ARRIBA + altoDibujo + FONT_ESP_VER, "0", FONT_MARKS, COLOR_EJES );
			double marca = escalaVIni + escalaVMarcas;
			double valor = escalaVMarcas;
			while (marca <= escalaVFin) {
				String texto = String.format( "%." + numDecimalesMarcas + "f", valor );
				double xTexto = ESP_IZQDA - (texto.length() + 1) * FONT_ESP_HOR;
				ventana.dibujaLinea( ESP_IZQDA, yAVentana( marca ), anchoDibujo, yAVentana( marca ), GROSOR_MARCAS, COLOR_MARCAS );
				ventana.dibujaTexto( xTexto, yAVentana( marca ) + FONT_ESP_VER, texto, FONT_MARKS, COLOR_EJES );
				marca += escalaVMarcas;
				valor += escalaVMarcas;
			}
			for (int horizontal=0; horizontal<etiquetasDim1Hor.size(); horizontal++) {
				double anchoBloque = anchoAVentana( horizontal );				
				String texto = etiquetasDim1Hor.get(horizontal);
				ventana.dibujaTexto( xAVentana(horizontal,0.0) + anchoBloque/2.0 - texto.length()*FONT_ESP_HOR/2.0, 
						yAVentana( 0 ) + FONT_ESP_VER * 4, texto, FONT_MARKS, COLOR_EJES );
			}
		}
		private int xAVentana( int columna, double porcentaje ) {
			return (int) Math.round( ESP_IZQDA + ESP_ENTRECOLS + (columna * (anchoColumna + ESP_ENTRECOLS)) + anchoColumna * porcentaje );
		}
		private double anchoAVentana( int columna ) {
			return anchoColumna;
		}
		private int yAVentana( double y ) {
			double posY = (y - escalaVIni) / (escalaVFin - escalaVIni) * altoDibujo;  // 0..1 -> 0..altura en píxels
			return ESP_ARRIBA + altoDibujo - (int) Math.round(posY);
		}	
		private double altoAVentana( double alto ) {
			return (alto - escalaVIni) / (escalaVFin - escalaVIni) * altoDibujo;  // 0..1 -> 0..altura en píxels
		}
		private void dibujaGrafico() {
			if (etiquetasDim1Hor==null || etiquetasDim2Ver==null || datos==null) return;
			double yActual[] = new double[ etiquetasDim1Hor.size() ];  // ys a cero en cada columna para empezar
			for (int vertical=0; vertical<etiquetasDim2Ver.size(); vertical++) {
				for (int horizontal=0; horizontal<etiquetasDim1Hor.size(); horizontal++) {
					Double y = datos[horizontal][vertical];
					if (y!=null) {
						ventana.dibujaRect( xAVentana(horizontal,0.0), yAVentana( y+yActual[horizontal] ), anchoAVentana(horizontal), altoAVentana( y ),
								GROSOR_LIN_BLOQUES, filtraUmbral(anchoAVentana(horizontal),coloresDim2Borde.get( vertical ),coloresDim2.get(vertical)), coloresDim2.get( vertical ) );
						yActual[horizontal] += y;
					}
				}
			}
			ventana.repaint();
		}
			private Color filtraUmbral(double ancho, Color cBorde, Color cFondo ) {
				if (ancho<=UMBRAL_SIN_BORDE) return cFondo; else return cBorde;
			}
		
	// ==============================================================
	//  Métodos de animación
	
	/** Dibuja el gráfico haciendo una animación
	 * @param milis	Milisegundos entre bloque y bloque
	 */
	public void animaGrafico( long milis ) {
		if (etiquetasDim1Hor==null || etiquetasDim2Ver==null || datos==null) return;
		ventana.borra();
		calculaCoords();
		dibujaEjes();
		Thread t = new Thread( () -> {
			double yActual[] = new double[ etiquetasDim1Hor.size() ];  // ys a cero en cada columna para empezar
			for (int vertical=0; vertical<etiquetasDim2Ver.size(); vertical++) {
				for (int horizontal=0; horizontal<etiquetasDim1Hor.size(); horizontal++) {
						Double y = datos[horizontal][vertical];
						if (y!=null) {
						Color borde = filtraUmbral( anchoAVentana(horizontal), coloresDim2Borde.get(vertical), coloresDim2.get(vertical) );
						ventana.dibujaRect( xAVentana(horizontal,0.0), yAVentana( y+yActual[horizontal] ), anchoAVentana(horizontal), altoAVentana( y ),
								GROSOR_LIN_BLOQUES, borde, coloresDim2.get( vertical ) );
						yActual[horizontal] += y;
					}
				}
				ventana.repaint();
				ventana.espera( milis );
			}
		} );
		t.setDaemon( true );
		t.start();
	}
	
	/** Dibuja el gráfico haciendo una animación fina
	 * @param milisNivel	Milisegundos entre fin de dibujado de cada nivel y principio de dibujado del siguiente
	 * @param milisPixels	Milisegundos entre píxels de altura fina de cada nivel
	 * @param pixels	Píxels de altura de dibujado fino en cada nivel
	 */
	public void animaGrafico( long milisNivel, long milisPixels, int pixels ) {
		if (etiquetasDim1Hor==null || etiquetasDim2Ver==null || datos==null) return;
		ventana.borra();
		calculaCoords();
		dibujaEjes();
		Thread t = new Thread( () -> {
			double yActual[] = new double[ etiquetasDim1Hor.size() ];  // ys a cero en cada columna para empezar
			for (int vertical=0; vertical<etiquetasDim2Ver.size(); vertical++) {
				int pixelsAct = pixels;
				boolean algunoMayor = true;
				while (algunoMayor) {
					algunoMayor = false;
					for (int horizontal=0; horizontal<etiquetasDim1Hor.size(); horizontal++) {
						Double y = datos[horizontal][vertical];
						if (y!=null) {
							double altoFinal = altoAVentana( y );
							if (altoFinal>pixelsAct) {
								algunoMayor = true;
								int yTemporal = yAVentana( yActual[horizontal] ) - pixelsAct;
								Color borde = filtraUmbral( anchoAVentana(horizontal), coloresDim2Borde.get(vertical), coloresDim2.get(vertical) );
								ventana.dibujaRect( xAVentana(horizontal,0.0), yTemporal, anchoAVentana(horizontal), pixelsAct,
										GROSOR_LIN_BLOQUES, borde, coloresDim2.get( vertical ) );
							} else {
								int yFinal = yAVentana( y+yActual[horizontal] );
								Color borde = filtraUmbral( anchoAVentana(horizontal), coloresDim2Borde.get(vertical), coloresDim2.get(vertical) );
								ventana.dibujaRect( xAVentana(horizontal,0.0), yFinal, anchoAVentana(horizontal), altoFinal,
										GROSOR_LIN_BLOQUES, borde, coloresDim2.get( vertical ) );
							}
						}
					}
					ventana.repaint();
					ventana.espera( milisPixels );
					pixelsAct += pixels;
				}
				for (int horizontal=0; horizontal<etiquetasDim1Hor.size(); horizontal++) {
					Double y = datos[horizontal][vertical];
					if (y!=null) {
						yActual[horizontal] += y;
					}
				}
				ventana.espera( milisNivel );
			}
		} );
		t.setDaemon( true );
		t.start();
	}

	// ==============================================================
	// Popup de la ventana gráfica y escuchador de botón derecho
	
	@SuppressWarnings("serial")
	private class MiPopup extends JPopupMenu {
		JMenuItem item;
		public MiPopup() {
			item = new JMenuItem( "Cambiar escalado vertical" );
			add( item );
			item.addActionListener( new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					JSpinner spinner1 = new JSpinner();
					spinner1.setValue( (int) Math.round(escalaVFin) );
					spinner1.setPreferredSize( new Dimension( 65, 28 ) );
					JPanel p1 = new JPanel();
					p1.add( new JLabel( "Máximo vertical" ) );
					p1.add( spinner1 );
					JSpinner spinner2 = new JSpinner();
					spinner2.setValue( (int) Math.round(escalaVMarcas ) );
					spinner2.setPreferredSize( new Dimension( 60, 28 ) );
					JPanel p2 = new JPanel();
					p2.add( new JLabel( "Marcas verticales" ) );
					p2.add( spinner2 );
					Object[] params = { p1, p2 };  
					int resp = JOptionPane.showConfirmDialog(null, params, "Cambia escala vertical", JOptionPane.OK_CANCEL_OPTION );
					if (resp==JOptionPane.OK_OPTION) {
						escalaVFin = (Integer) spinner1.getValue();
						escalaVMarcas = (Integer) spinner2.getValue();
						dibuja();
					}
				}
			});
			item = new JMenuItem( "Animación" );
			add( item );
			item.addActionListener( new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					animaGrafico( 500 );
				}
			});
			item = new JMenuItem( "Animación fina" );
			add( item );
			item.addActionListener( new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					animaGrafico( 500, 10, 2 );
				}
			});
		}
	}
	
	private RightClickListener escuchadorBotonDerecho = new RightClickListener();
	private class RightClickListener extends MouseAdapter {
		MiPopup menu;
	    public void mousePressed(MouseEvent e) {
	        if (e.isPopupTrigger())
	        	doPop(e);
	    }
	    public void mouseReleased(MouseEvent e) {
	        if (e.isPopupTrigger())
	            doPop(e);
	    }
	    private void doPop(MouseEvent e) {
	        if (menu==null) menu = new MiPopup();
	        menu.show(e.getComponent(), e.getX(), e.getY());
	    }
	}
	
}
