package examen.ord202001;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;

/** Clase de ventana para muestra de datos de tabla en una JTable integrada. Necesita una instancia de la clase Tabla que tiene los datos que se visualizarán<br/>
 * Hereda de JInternalFrame, de modo que debe incluirse en una ventana que esté preparada para ventanas internas
 * (Clase asociada preparada: VentanaGeneral)
 * @author andoni.eguiluz @ ingenieria.deusto.es
 */
@SuppressWarnings("serial")
public class VentanaTabla extends JInternalFrame {
	
	/** Interfaz para gestionar eventos en las celdas de la JTable
	 * @author andoni.eguiluz @ ingenieria.deusto.es
	 */
	public static interface EventoEnCelda {
		/** Evento que se lanzará al actuar sobre la celda
		 * @param vTabla	Ventana de tabla de datos
		 * @param fila	Fila de la celda
		 * @param columna	Columna de la celda
		 */
		public void evento( VentanaTabla vTabla, int fila, int columna );
	}
	
	private JTable tDatos;    // JTable de datos de la ventana
	private Tabla tablaDatos; // Tabla de datos de la ventana
	private JScrollPane spDatos; // Scrollpane de la jtable
	private JLabel lMensaje;  // Label de mensaje
	private JPanel pBotonera; // Panel de botones
	private VentanaGeneral ventMadre;  // Ventana madre

	private EventoEnCelda dobleClick;
	private EventoEnCelda dobleClickHeader;
	private EventoEnCelda enter;
	
	/** Añade un botón a la ventana
	 * @param texto	Texto del botón
	 * @param runnable	Objeto runnable con código a ejecutar (run()) cuando el botón se pulse
	 */
	public void addBoton( String texto, Runnable runnable ) {
		JButton b = new JButton( texto );
		pBotonera.add( b );
		b.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				runnable.run();
			}
		});
	}
	
	@Override
	public void pack() {
		if (tDatos!=null) {
			spDatos.setPreferredSize( new Dimension( tDatos.getWidth() + 10, tDatos.getHeight() + 30 ) );
		}
		super.pack();
		if (getWidth() > ventMadre.getJDesktopPane().getWidth()) { setSize( ventMadre.getJDesktopPane().getWidth(), getHeight() ); }
		if (getHeight() > ventMadre.getJDesktopPane().getHeight()) { setSize( getWidth(), ventMadre.getJDesktopPane().getHeight() ); }
	}
	
	/** Devuelve la JTable asociada a esta ventana
	 * @return	Objeto de tabla visual
	 */
	public JTable getJTable() { return tDatos; }
	
	/** Devuelve la tabla general asociada a esta ventana
	 * @return	Ventana principal
	 */
	public VentanaGeneral getVentMadre() { return ventMadre; }
	
	/** Crea una nueva ventana de tabla
	 * @param ventMadre	Ventana principal en la que se incluirá
	 * @param titulo	Título de la ventana
	 * @param horizontalScroll	true si se quiere que la tabla se adapte al panel de scroll
	 */
	public VentanaTabla( VentanaGeneral ventMadre, String titulo, boolean... horizontalScroll ) {
	    super( titulo, true, true, true, true ); //  resizable, closable, maximizable, iconifiable
	    this.ventMadre = ventMadre;
		// Configuración general
		setTitle( titulo );
		setSize( 800, 600 ); // Tamaño por defecto
		setDefaultCloseOperation( JFrame.HIDE_ON_CLOSE );
		// Creación de componentes y contenedores
		pBotonera = new JPanel();
		tDatos = new JTable() {
			// Para tooltips de headers
			protected JTableHeader createDefaultTableHeader() {
		        return new JTableHeader(columnModel) {
		            public String getToolTipText(MouseEvent e) {
		                java.awt.Point p = e.getPoint();
		                int index = columnModel.getColumnIndexAtX(p.x);
		                if (index<0 || index>=columnModel.getColumnCount()) return null;
		                int realIndex = columnModel.getColumn(index).getModelIndex();
		                return columnModel.getColumn(realIndex).getHeaderValue().toString();
		            }
		        };
		    }
			// Para tooltips de celdas
			@Override
			public String getToolTipText(MouseEvent e) {
		        String tip = null;
		        try {
			        java.awt.Point p = e.getPoint();
			        int rowIndex = rowAtPoint(p);
			        int colIndex = columnAtPoint(p);
			        int realColumnIndex = convertColumnIndexToModel(colIndex);  // Real column in model, not in view
			        Object o = getValueAt( rowIndex, realColumnIndex );
			        if (o==null) {
			            tip = "NULO";
			        } else if (o instanceof String) {  // Tip for strings
			            tip = (String) o;
			            // } else { tip = super.getToolTipText(e);
			        } else if (o instanceof Integer) {
			            tip = o.toString();
			        } else {
			            tip = o.toString();
			        }
			        // if (tip.length() < 5) tip = "";   // If string too short, don't make tip
		        } catch (Exception e2) {
			    	tip = "";
			    }
		        return tip;
		    }
		};
		tDatos.setShowGrid( true );
		tDatos.setGridColor( Color.LIGHT_GRAY );
		tDatos.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
		lMensaje = new JLabel( " " );
		// Asignación de componentes
		if (horizontalScroll.length>0 && horizontalScroll[0]) {
			spDatos = new JScrollPane( tDatos, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED );
			tDatos.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		} else {
			spDatos = new JScrollPane( tDatos );
		}
		getContentPane().add( spDatos, BorderLayout.CENTER );
		getContentPane().add( pBotonera, BorderLayout.SOUTH );
		getContentPane().add( lMensaje, BorderLayout.NORTH );
		// Eventos
		tDatos.addMouseListener( new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount()>=2) {
					int fila = tDatos.rowAtPoint( e.getPoint() );
					int columna = tDatos.columnAtPoint( e.getPoint() );
					if (dobleClick!=null) dobleClick.evento( VentanaTabla.this, fila, columna );
				}
			}
			// T5 - parte 1
			int fila = -1;
			int columna = -1;
			public void mousePressed(MouseEvent e) {
				fila = tDatos.rowAtPoint( e.getPoint() );
				columna = tDatos.columnAtPoint( e.getPoint() );
			}
			public void mouseReleased(MouseEvent e) {
				int filaDest = tDatos.rowAtPoint( e.getPoint() );
				int columnaDest = tDatos.columnAtPoint( e.getPoint() );
				if (fila>=0 && filaDest>=0 && columna>=0 && columnaDest>=0) {
					// System.out.println( fila + "," + columna + " - " + filaDest + "," + columnaDest );
					Object o1 = tablaDatos.get( fila, columna );
					Object o2 = tablaDatos.get( filaDest, columnaDest );
					if (o1!=null && o2!=null && o1.getClass()==o2.getClass()) {
						tablaDatos.set( fila, columna, o2 );
						tablaDatos.set( filaDest, columnaDest, o1 );
					}
				}
			}
		});
		tDatos.addMouseMotionListener( new MouseMotionAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				int fila = tDatos.rowAtPoint( e.getPoint() );
				int columna = tDatos.columnAtPoint( e.getPoint() );
				if (fila>=0 && columna>=0) {
					Object valor = tDatos.getValueAt( fila, columna );
					if (valor!=null && ventMadre!=null) ventMadre.setMensaje( valor.toString() );
				}
			}
		});
		tDatos.getTableHeader().addMouseListener( new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount()>=2) {
					int columna = tDatos.columnAtPoint( e.getPoint() );
					if (dobleClickHeader!=null) dobleClickHeader.evento( VentanaTabla.this, -1, columna );
				}
			}
		});
		tDatos.getTableHeader().addMouseMotionListener( new MouseMotionAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				int columna = tDatos.columnAtPoint( e.getPoint() );
				if (columna>=0) {
					Object valor = tDatos.getTableHeader().getColumnModel().getColumn(columna).getHeaderValue().toString();
					if (valor!=null && ventMadre!=null) ventMadre.setMensaje( valor.toString() );
				}
			}
		});
		tDatos.addKeyListener( new KeyAdapter() {
			boolean ctrlPulsado = false;
			boolean ultimaBusquedaEnCol = false; // Indica si la última búsqueda ha sido en columna (true) o en tabla (false)
			String ultimaBusqueda = ""; // Texto de la última búsqueda
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					if (enter!=null && tDatos!=null) {
						enter.evento( VentanaTabla.this, tDatos.getSelectedRow(), tDatos.getSelectedColumn() );
						e.consume();
					}
				} else if (e.getKeyCode() == KeyEvent.VK_CONTROL ) {
					ctrlPulsado = true;
				} else if (e.getKeyCode() == KeyEvent.VK_F && ctrlPulsado) {  // Ctrl+B = Buscar en la columna actual
					ultimaBusqueda = JOptionPane.showInputDialog( VentanaTabla.this, "Texto a buscar en la columna actual:", ultimaBusqueda );
					ultimaBusquedaEnCol = true;
					hacerBusqueda();
				} else if (e.getKeyCode() == KeyEvent.VK_B && ctrlPulsado) {  // Ctrl+F = Buscar en toda la tabla (primero hacia abajo luego hacia la derecha)
					ultimaBusqueda = JOptionPane.showInputDialog( VentanaTabla.this, "Texto a buscar en la tabla desde la posición actual:", ultimaBusqueda );
					ultimaBusquedaEnCol = false;
					hacerBusqueda();
				} else if (e.getKeyCode() == KeyEvent.VK_K && ctrlPulsado) {  // Ctrl+K = Buscar de nuevo (repetir la última búsqueda)
					hacerBusqueda();
				}
			}
			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_CONTROL ) {
					ctrlPulsado = false;
				} else if (e.getKeyCode() == KeyEvent.VK_DOWN ) {
					if (ctrlPulsado) {
						// Buscar siguiente dato vacío de la columna actual
						int colActual = tDatos.getSelectedColumn();
						int row = tDatos.getSelectedRow();
						if (colActual!=-1 && row>=0) {
							row++;
							while (row<tDatos.getModel().getRowCount()) {
								if (tDatos.getValueAt( row, colActual ).toString().isEmpty()) {
									hacerScrollHastaFila( row, colActual );  // Seleccionar fila encontrada en la ventana
									return;
								}
								row++;
							}
						}
					}
				} else if (e.getKeyCode()==KeyEvent.VK_C && !e.isControlDown()) { // C copia al portapapeles (sin Control)
					(new Thread() {
						@Override
						public void run() {
							StringBuffer texto = new StringBuffer();
							for (String header : tablaDatos.getCabeceras()) {
								texto.append( header==null ? "" : header + "\t" );
							}
							texto.append( "\n" );
							for (int f=0; f<tablaDatos.size(); f++) {
								for (int c=0; c<tablaDatos.getWidth(); c++) {
									Object o = tablaDatos.get( f, c );
									String s = (o==null) ? "null" : o.toString();
									texto.append( (s==null?"":s) + "\t" );
								}
								texto.append( "\n" );
							}
							Toolkit.getDefaultToolkit().getSystemClipboard().setContents( new StringSelection(texto.toString()), null );
							JOptionPane.showMessageDialog( ventMadre, "Contenido de tabla " + getTitle() + " copiado al portapapeles.", "Copia de " + tablaDatos.size() + " filas", JOptionPane.INFORMATION_MESSAGE );
						}
					}).start();
					// JOptionPane.showMessageDialog( kaw, "Copiándose la tabla de interacciones al portapapeles...", "Puedes seguir trabajando mientras", JOptionPane.INFORMATION_MESSAGE );
				}
			}
			private void hacerScrollHastaFila( int row, int col ) {  // Mostrar en el scrollpane la fila,col indicada
				tDatos.getSelectionModel().setSelectionInterval( row, row );
				Rectangle rect = tDatos.getCellRect( row, col, true );
				rect.setLocation(rect.x, rect.y+25);  // Y un poquito más abajo (para que no quede demasiado justo)
				tDatos.scrollRectToVisible( rect );
			}
			private void hacerBusqueda() {
				if (ultimaBusqueda==null || ultimaBusqueda.isEmpty()) return;
				int col = tDatos.getSelectedColumn();
				int row = tDatos.getSelectedRow();
				if (col<0) col = 0; if (row<0) row = 0;
				String aBuscar = ultimaBusqueda.toUpperCase();
				row++;
				if (ultimaBusquedaEnCol) {
					while (row<tDatos.getModel().getRowCount()) {
						if (tDatos.getValueAt( row, col ).toString().toUpperCase().contains( aBuscar )) {
							hacerScrollHastaFila( row, col );  // Seleccionar fila encontrada en la ventana
							return;
						}
						row++;
					}
				} else {
					while (row<tDatos.getModel().getRowCount() && col<tDatos.getModel().getColumnCount()) {
						if (tDatos.getValueAt( row, col ).toString().toUpperCase().contains( aBuscar )) {
							hacerScrollHastaFila( row, col );  // Seleccionar fila encontrada en la ventana
							return;
						}
						row++;
						if (row>=tDatos.getModel().getRowCount()) {
							row = 0;
							col = col + 1;
						}
					}
				}
			}
		} );
		// T5 - parte 2
		tDatos.setDefaultRenderer( Object.class, new DefaultTableCellRenderer() {
			Font font = new Font( "Arial", Font.PLAIN, 14 );
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
				Component c = super.getTableCellRendererComponent( table, value, isSelected, hasFocus, row, column );
				c.setFont( font );
				if (hasFocus) {
					c.setForeground( Color.cyan );
				} else if (isSelected) {
					c.setForeground( Color.yellow );
				} else {
					c.setForeground( Color.black );
				}
				return c;
			}
		} );
		// T5 - parte 3
		addInternalFrameListener( new InternalFrameAdapter() {
			@Override
			public void internalFrameActivated(InternalFrameEvent e) {
				if (tablaDatos instanceof TablaAnalisis) {
					TablaAnalisis ta = (TablaAnalisis) tablaDatos;
					if (ta.getGrafico() != null) {
						ta.getGrafico().getJFrame().setVisible( true );
					}
				}
			}
		});
	}
	
	public void setMensaje( String mens ) {
		if (mens==null || mens.isEmpty()) mens = " ";
		lMensaje.setText( mens );
	}
	
	/** Asigna una tabla de datos a la JTable principal de la ventana
	 * @param tabla	Tabla de datos a visualizar
	 */
	public void setTabla( Tabla tabla ) {
		tablaDatos = tabla;
		tDatos.setModel( tabla.getTableModel() );
	}
	
	/** Devuelve la tabla de datos asignada a la ventana
	 * @return	tabla de datos asignada, null si no la hay
	 */
	public Tabla getTabla() {
		return tablaDatos;
	}
		
	/** Oculta las columnas indicadas en la visual
	 * @param colD	columna inicial (0 a n-1)
	 * @param colH	columna final (0 a n-1)
	 */
	public void ocultaColumnas( final int colD, final int colH ) {
		Runnable r = new Runnable() {
			@Override
			public void run() {
				for (int i=colH; i>=colD; i--)
					tDatos.removeColumn(tDatos.getColumnModel().getColumn( i ));
			}
		};
		if (SwingUtilities.isEventDispatchThread()) r.run(); else SwingUtilities.invokeLater( r );
	}
	
	/** Asocia escuchador al doble click de la celda
	 * @param evento	Escuchador a asociar
	 */
	public void setDobleClickCelda( EventoEnCelda evento ) {
		dobleClick = evento;
	}
	
	/** Asocia escuchador al doble click de la cabecera
	 * @param evento	Escuchador a asociar
	 */
	public void setDobleClickHeader( EventoEnCelda evento ) {
		dobleClickHeader = evento;
	}
	
	/** Asocia escuchador a la pulsación de enter de la celda
	 * @param evento	Escuchador a asociar
	 */
	public void setEnterCelda( EventoEnCelda evento ) {
		enter = evento;
	}
	
	/** Busca la columna para un nombre de columna dado
	 * @param nomCol	Nombre de columna a buscar
	 * @param nomExacto	true si la coincidencia debe ser exacta, si no se busca coincidencia parcial
	 * @return	Número de la columna, -1 si no se encuentra
	 */
	public int getColumnWithHeader( String nomCol, boolean nomExacto ) {
		TableColumnModel cols = tDatos.getTableHeader().getColumnModel();
		for (int col = 0; col<cols.getColumnCount(); col++) {
			String nom = cols.getColumn(col).getHeaderValue() + "";
			if (nomExacto && nom.equals( nomCol )) return col;
			if (!nomExacto && nom.toUpperCase().startsWith( nomCol.toUpperCase() ) ) return col;
		}
		return -1;
	}
	
	// =================================================
	// Renderer

	/** Pone un renderer de 4 colores a los valores numéricos de la tabla.
	 * @param decimales	true si se quieren sacar los valores con decimales, false en caso contrario (con decimales Intenta sacar 4 dígitos: por debajo de 10 pone 3 decimales, por debajo de 100 pone 2 decimales, por debajo de 1000 un decimal, por encima ninguno)
	 * @param nColD	Nombre columna inicial donde poner el renderer (inclusive)
	 * @param nColH	Nombre columna final donde poner el renderer (inclusive)
	 * @param val1	Valor de corte de primer color (por debajo se pone al primer color, por encima gradiente al segundo)
	 * @param val2	Valor de corte de segundo color (gradientes a primero y tercero)
	 * @param val3	Valor de corte de tercer color (gradientes a segundo y cuarto)
	 * @param val4	Valor de corte de cuarto color (por encima se pone el primer color, por debajo gradiente)
	 * @param col1	Color 1
	 * @param col2	Color 2
	 * @param col3	Color 3
	 * @param col4	Color 4
	 */
	public void setRendererCuatroColores( boolean decimales, String nColD, String nColH, double val1, double val2, double val3, double val4, Color col1, Color col2, Color col3, Color col4 ) {
		int colD = tablaDatos.getCabeceras().indexOf(nColD);
		int colH = tablaDatos.getCabeceras().indexOf(nColH);
		if (colD<0 || colD>colH || tDatos.getColumnCount()<=colH ) return;  // columnas incorrectas
		String formato = null;
		if (decimales) {
			formato = "%.3f";
			if (val1<=-1000.0 || val4>=1000.0) formato = "%.0f";
			else if (val1<=-100.0 || val4>=100.0) formato = "%.1f";
			else if (val1<=-10.0 || val4>=10.0) formato = "%.2f";
		}
		final String formatoDef = formato;
		DefaultTableCellRenderer tcr = new DefaultTableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
				/*Component c =*/super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				setHorizontalAlignment( JLabel.RIGHT );
				if (value==null) {
					setText( "" );
					setBackground( Color.white );
				} else {
					Double d = null;
					if (value instanceof Double) d = (Double) value;
					else if (value instanceof Integer) d = new Double( (Integer) value );
					else if (value instanceof Long) d = new Double( (Long) value );
					if (d!=null) {
						if (formatoDef!=null) setText( String.format(formatoDef, d) );
						Color col = null;
						// System.out.println( "   " + d + "  " + val1 + " - " + val2 + " - " + val3 + " - " + val4 );
						if (d <= val2) {  // franja inferior
							if (d<val1) d = val1;
							if (d==val2 && d==val1) { col = col1; } // Caso especial - val1==val2  // T1 CORREGIDO
							else {
								double porcDif = (d - val1) / (val2-val1);
								double deltaR = col2.getRed() - col1.getRed();
								double deltaG = col2.getGreen() - col1.getGreen();
								double deltaB = col2.getBlue() - col1.getBlue();
								int r = (int) Math.round( col1.getRed() + deltaR*porcDif );
								int g = (int) Math.round( col1.getGreen() + deltaG*porcDif );
								int b = (int) Math.round( col1.getBlue() + deltaB*porcDif );
								// System.out.println( "Dato1 " + d + " en [" + val1 + "," + val2 + "," + val3 + "," + val4 + "] = RGB (" + r + "," + g + "," + b + "   -  sobre " + col1 + " y " + col2 );
								col = new Color( r, g, b );
							}
						} else if (d <= val3) {  // segunda franja
							if (d==val2 && d==val3) { col = col2; } // Caso especial - val2==val3  // T1 CORREGIDO
							else {
								double porcDif = (d - val2) / (val3-val2);
								double deltaR = col3.getRed() - col2.getRed();
								double deltaG = col3.getGreen() - col2.getGreen();
								double deltaB = col3.getBlue() - col2.getBlue();
								int r = (int) Math.round( col2.getRed() + deltaR*porcDif );
								int g = (int) Math.round( col2.getGreen() + deltaG*porcDif );
								int b = (int) Math.round( col2.getBlue() + deltaB*porcDif );
								// System.out.println( "Dato2 " + d + " en [" + val1 + "," + val2 + "," + val3 + "," + val4 + "] = RGB (" + r + "," + g + "," + b + "   -  sobre " + col2 + " y " + col3 );
								col = new Color( r, g, b );
							}
						} else {  // franja superior
							if (d>val4) d = val4;
							if (d==val3 && d==val4) { col = col3; } // Caso especial - val3==val4  // T1 CORREGIDO
							else {
								double porcDif = (d - val3) / (val4-val3);
								double deltaR = col4.getRed() - col3.getRed();
								double deltaG = col4.getGreen() - col3.getGreen();
								double deltaB = col4.getBlue() - col3.getBlue();
								int r = (int) Math.round( col3.getRed() + deltaR*porcDif );
								int g = (int) Math.round( col3.getGreen() + deltaG*porcDif );
								int b = (int) Math.round( col3.getBlue() + deltaB*porcDif );
								// System.out.println( "Dato3 " + d + " en [" + val1 + "," + val2 + "," + val3 + "," + val4 + "] = RGB (" + r + "," + g + "," + b + "   -  sobre " + col3 + " y " + col4 );
								col = new Color( r, g, b );
							}
						}
						setBackground( col );
					} else {
						setBackground( Color.white );
					}
				}
				return this;
			}
		};
		for (int col=colD; col<=colH; col++) {
			tDatos.getColumn( tablaDatos.getCabeceras().get(col) ).setCellRenderer( tcr );
		}
	}
		
}
