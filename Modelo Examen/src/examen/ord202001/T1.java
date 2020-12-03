package examen.ord202001;

import static org.junit.Assert.*;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.JLabel;
import javax.swing.JTable;

import org.junit.Test;

public class T1 {

	// T4 
	@Test
	public void testRenderer4Cuartiles() {
		TablaEstadistica tabla = new TablaEstadistica();
		tabla.addColumna( "A", new Integer(0) );
		tabla.addColumna( "B", new Integer(0) );
		tabla.addColumna( "C", new Integer(0) );
		tabla.addDataLine( new ArrayList<Object>( Arrays.asList( 1, 2, 3 ) ) );
		tabla.addDataLine( new ArrayList<Object>( Arrays.asList( 4, 5, 6 ) ) );
		tabla.addDataLine( new ArrayList<Object>( Arrays.asList( 7, 8, 9 ) ) );
		tabla.addDataLine( new ArrayList<Object>( Arrays.asList( 10, 11, 12 ) ) );
		tabla.addDataLine( new ArrayList<Object>( Arrays.asList( 13, 14, 15 ) ) );
		VentanaTabla v = new VentanaTabla( null, "Test", true );
		v.setTabla( tabla );
		tabla.setRenderer4Cuartiles( v, false, "A", "C", new Color(0,0,0), new Color(0,0,255), new Color(255,0,255), new Color(255,255,255) );
		int cuartil1 = 4;
		int cuartil3 = 12;
		for (int f=0; f<tabla.size(); f++) {
			for (int c=0; c<tabla.getWidth(); c++) {
				// System.out.println( getRenderer( v.getJTable(), f, c).getBackground() + " - " + getRenderer( v.getJTable(), f, c).getText());
				int valor = (Integer) tabla.get( f, c );
				Color colCelda = getRenderer( v.getJTable(), f, c ).getBackground();
				String coordenada = "(" + f + "," + c + ")";
				if (valor<cuartil1) {  // Progresión RGB 0,0,0 a RGB 0,0,255
					assertEquals( coordenada, colCelda.getRed(), 0 );
					assertEquals( coordenada, colCelda.getGreen(), 0 );
				} else if (valor<cuartil3) {  // Progresión RGB 0,0,255 a RGB 255,0,255
					assertEquals( coordenada, colCelda.getGreen(), 0 );
					assertEquals( coordenada, colCelda.getBlue(), 255 );
				} else {  // Progresión RGB 255,0,255 a RGB 255,255,255
					assertEquals( coordenada, colCelda.getRed(), 255 );
					assertEquals( coordenada, colCelda.getBlue(), 255 );
				}
			}
		}
	}

	// Método de utilidad: devuelve el JLabel que se está usando para hacer el render de la celda fila, col de la JTable indicada
	private JLabel getRenderer( JTable jTable, int fila, int col ) {
		return (JLabel) jTable.prepareRenderer( jTable.getCellRenderer( fila, col ), fila, col );
	}
}
