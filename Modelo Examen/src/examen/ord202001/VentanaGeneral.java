package examen.ord202001;

import java.awt.BorderLayout;
import java.awt.event.*;
import java.util.ArrayList;
import javax.swing.*;

/** Ventana general contenedora con definición de JDesktopPane para poder introducir ventanas internas (JInternalFrame)<br/>
 * Incluye menú de ventanas internas y menú de acciones configurable
 * @author andoni.eguiluz @ ingenieria.deusto.es
 */
@SuppressWarnings("serial")
public class VentanaGeneral extends JFrame {
	private JDesktopPane desktop;
	private JLabel lMensaje = new JLabel( " " );
	private JPanel pSuperior = new JPanel();
	private JMenu menuVentanas;
	private JMenu menuAcciones;
	private Runnable accionCierre;
	private ArrayList<JInternalFrame> misSubventanas;
	
	/** Construye y devuelve una ventana general
	 */
	public VentanaGeneral() {
		misSubventanas = new ArrayList<>();
		// Configuración general
		setTitle( "Ventana General" );
		setSize( 1200, 800 ); // Tamaño por defecto
		setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
		// Creación de componentes y contenedores
		desktop = new JDesktopPane();
		add( desktop, BorderLayout.CENTER );
		// setContentPane( desktop );
		add( lMensaje, BorderLayout.SOUTH );
		add( pSuperior, BorderLayout.NORTH );
		// Menú y eventos
		addWindowListener( new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				cerrarVentana();
			}
		});
		JMenuBar menuBar = new JMenuBar();
		menuVentanas = new JMenu( "Ventanas" ); menuVentanas.setMnemonic( KeyEvent.VK_V );
		menuBar.add( menuVentanas );
		menuAcciones = new JMenu( "Acciones" ); menuAcciones.setMnemonic( KeyEvent.VK_A );
		menuBar.add( menuAcciones );
		setJMenuBar( menuBar );
	}
	
		private void cerrarVentana() {
			if (accionCierre!=null) accionCierre.run();
		}
	
		private ActionListener alMenu = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String vent = e.getActionCommand();
				for (JInternalFrame vd : misSubventanas) if (vd.getName().equals( vent )) { vd.setVisible( true ); vd.moveToFront(); return; }
			}
		};
		
	/** Añade una ventana interna
	 * @param f	Ventana interna a añadir
	 * @param codVentana	Código de esa ventana
	 */
	public void addVentanaInterna( JInternalFrame f, String codVentana ) {
		desktop.add( f );
		JMenuItem menuItem = new JMenuItem( codVentana ); 
		menuItem.setActionCommand( codVentana );
		menuItem.addActionListener( alMenu );
		menuVentanas.add( menuItem );	
		misSubventanas.add( f );
		f.setName( codVentana );
	}
	
	/** Asigna un evento de cierre a la ventana principal. Se lanzará al cerrarse la ventana
	 * @param runnable
	 */
	public void setAccionCierre( Runnable runnable ) {
		accionCierre = runnable;
	}
	
	/** Devuelve el componente superior
	 * @return	Panel superior
	 */
	public JPanel getPanelSuperior() {
		return pSuperior;
	}
	
	/** Devuelve el componente principal
	 * @return	Panel de desktop principal de la ventana
	 */
	public JDesktopPane getJDesktopPane() {
		return desktop;
	}
	
	/** Cambia el mensaje de la línea inferior de mensajes
	 * @param mens	Texto del mensaje
	 */
	public void setMensaje( String mens ) {
		if (mens==null || mens.isEmpty()) mens = " ";
		lMensaje.setText( mens );
	}
	
	/** Añade una acción al menú de acciones
	 * @param textoMenu	Texto a indicar en el menú y comando de acción del evento
	 * @param accion	Acción a lanzar (recibirá el comando de acción del evento)
	 */
	public void addMenuAccion( String textoMenu, ActionListener accion ) {
		JMenuItem menuItem = new JMenuItem( textoMenu );
		menuItem.setActionCommand( textoMenu );
		menuItem.addActionListener( accion );
		menuAcciones.add( menuItem );
	}
	
}
