package examen.ord202001;

import java.awt.Color;
import java.util.ArrayList;

/** Tabla de datos bidimensional resultado de un análisis estadístico
 * Composición: una serie de cabeceras (columnas) con un tipo por columna, con una serie de datos en filas que responden a los valores estadísticos
 * Permite visualizar gráficos de barras asociados a la tabla de análisis
 * @author andoni.eguiluz @ ingenieria.deusto.es
 */
public class TablaAnalisis extends Tabla {

	GraficoColumnas vg = null;
	
	/** Crea una tabla de estadísticas de datos vacía (sin cabeceras ni datos)
	 */
	public TablaAnalisis() {
		super();
	}
	
	/** Crea una tabla de análisis desde una tabla normal
	 * @param tabla	Tabla de origen
	 */
	public TablaAnalisis( Tabla tabla ) {
		cabeceras = tabla.cabeceras;
		tipos = tabla.tipos;
		dataO = tabla.dataO;
		vg = null;
	}
	
	/** Crea una tabla de estadística de datos vacía con cabeceras y tipos
	 * @param cabeceras	Nombres de las cabeceras de datos
	 */
	public TablaAnalisis( ArrayList<String> cabeceras, ArrayList<Class<?>> tipos ) {
		super( cabeceras, tipos );
	}
	
	/** Devuelve la ventana gráfica de la tabla, si ha sido generada (método {@link #sacaGrafico(String, ArrayList, ArrayList, int, int)})
	 * @return	Ventana ya generada o null si no lo está
	 */
	public GraficoColumnas getGrafico() {
		return vg;
	}
	
	/** Crea y muestra una ventana de columnas acumuladas correspondiente a los datos de la tabla. Se creará una columna por cada fila de datos, y un bloque apilado en vertical por cada una de las columnas de datos
	 * @param titulo	Título de la ventana
	 * @param lColores	Lista de colores de los bloques (en orden de izquierda a derecha de las columnas a mostrar)
	 * @param lColoresBorde	Lista de colores de borde de los bloques (mismo orden)
	 * @param escalaMax	Valor máximo de escala vertical del gráfico
	 * @param marcas	Valor de marca de escala vertical del gráfico
	 */
	public void sacaGrafico( String titulo, ArrayList<Color> lColores, ArrayList<Color> lColoresBorde, int escalaMax, int marcas ) {
		if (vg==null) {
			vg = new GraficoColumnas( titulo );
			vg.importaDatosDesdeTabla( this, lColores, lColoresBorde );
			vg.setEscalaAltura( 0, escalaMax, marcas );
			vg.getJFrame().setLocation( 100, 10 );
			vg.visualiza( 600, 400 );
			// vg.animaGrafico( 500, 10, 2, TipoGrafico.TRAPEZOIDES, true );
			vg.dibuja();
		} else {
			vg.getJFrame().setVisible( true );
		}
	}
	
	
}
