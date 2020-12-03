package examen.ord202001;

import java.awt.Color;
import java.awt.geom.Point2D;

public class T4 {
	
	private static double RADIO = 15.0;
	private static double INCREMENTO = 0.1;   // Incremento de tiempo entre prueba y prueba para el choque aproximado
	private static double T_MAXIMO = 100.0;   // Tiempo máximo de prueba para el choque aproximado
	private static double DIST_FINA = 0.0001; // Distancia de error que se quiere afinar para el choque exacto

	public static void main(String[] args) {
		dibuja(); // T4 Se podría comentar, no hace falta verlo para calcularlo
		// T4
		double tiempo = buscaChoque( 0 );
		if (tiempo>=0) {
			System.out.println( "Las bolas 1 y 2 chocan aproximadamente en el tiempo " + tiempo );
			tiempo = buscaChoqueExacto( 0, tiempo );
			System.out.println( "Las bolas 1 y 2 chocan exactamente en el tiempo " + tiempo );
		} else {
			System.out.println( "Las bolas 1 y 2 no chocan." );
		}
	}
	
	// Devuelve primer tiempo aproximado en el que se detecta el choque, -1 si no se detecta
	public static double buscaChoque( double tiempo ) {
		if (tiempo>T_MAXIMO) {  // Caso base: tiempo muy grande
			return -1.0;  // No chocan
		} else {
			Point2D.Double b1 = getBola1( tiempo );
			Point2D.Double b2 = getBola2( tiempo );
			if (chocan( b1, b2 )) {  // Caso base: chocan
				return tiempo;
			} else {  // Caso recursivo: seguimos probando
				return buscaChoque( tiempo + INCREMENTO );
			}
		}
	}

	// pre: las bolas no chocan en tIni pero están chocando en tFin
	public static double buscaChoqueExacto( double tIni, double tFin ) {
		double tiempo = (tIni + tFin) / 2.0;  // Calcula el tiempo mitad entre ini y fin
		Point2D.Double b1 = getBola1( tiempo );
		Point2D.Double b2 = getBola2( tiempo );
		if (chocan(b1,b2)) {
			double dist = distancia( b1, b2 );
			if (dist >= 2*RADIO-DIST_FINA) {  // Caso base: chocan solo un poquito
				return tiempo;
			} else {
				return buscaChoqueExacto( tIni, tiempo );
			}
		} else {
			return buscaChoqueExacto( tiempo, tFin );
		}
	}
	
	public static boolean chocan( Point2D.Double b1, Point2D.Double b2 ) {
		return distancia(b1,b2) <= 2*RADIO;
	}

	// Distancia entre dos puntos
	public static double distancia( Point2D.Double b1, Point2D.Double b2 ) {
		return Math.sqrt( (b2.x-b1.x)*(b2.x-b1.x) + (b2.y-b1.y)*(b2.y-b1.y) );
	}

	// Ecuación de bola 1
	public static Point2D.Double getBola1( double tiempoSegs ) {
		Point2D.Double ret = new Point2D.Double( tiempoSegs*50, tiempoSegs*50 );
		return ret;
	}

	// Ecuación de bola 2
	public static Point2D.Double getBola2( double tiempoSegs ) {
		Point2D.Double ret = new Point2D.Double( 290 + 100 * Math.sin(tiempoSegs), 295 + 100 * Math.cos(tiempoSegs) );
		return ret;
	}
	
	// Ecuación de bola 3
	public static Point2D.Double getBola3( double tiempoSegs ) {
		Point2D.Double ret = new Point2D.Double( 260 + 200 * Math.cos(tiempoSegs*0.5), 250 + 200 * Math.sin(tiempoSegs*0.5) );
		return ret;
	}
	
	// Dibuja las bolas de billar hasta que se haga click
	private static void dibuja() {
		VentanaGrafica vg = new VentanaGrafica( 800, 600, "Bolas T4" );
		vg.setDibujadoInmediato( false );
		double tiempo = 0.0;
		while (vg.getRatonPulsado()==null && !vg.estaCerrada()) {
			Point2D.Double bola1 = getBola1( tiempo );
			Point2D.Double bola2 = getBola2( tiempo );
			Point2D.Double bola3 = getBola3( tiempo );
			vg.borra();
			vg.dibujaCirculo( bola1.x, bola1.y, RADIO, 2f, Color.blue );
			vg.dibujaCirculo( bola2.x, bola2.y, RADIO, 2f, Color.green );
			vg.dibujaCirculo( bola3.x, bola3.y, RADIO, 2f, Color.red );
			if (chocan(bola1,bola2)) System.out.println( "Chocan en " + tiempo ); // T4 - Si se quiere ver en directo cuándo chocan
			vg.repaint();
			tiempo += 0.1;
			vg.espera( 100 );
		}
		vg.acaba();
	}
	
}
