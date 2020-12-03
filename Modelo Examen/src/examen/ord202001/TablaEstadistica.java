package examen.ord202001;

import java.awt.Color;
import java.util.*;

/** Tabla de datos bidimensional de cualquier tipo para utilización estadística, permitiendo filtros de tablas y análisis estadístico de segmentos
 * Composición: una serie de cabeceras (columnas) con una serie de filas de datos (objetos) que tienen un dato para cada columna
 * Dato de cada celda puede ser cualquier objeto
 * Si los datos son numéricos se pueden aplicar estadísticas
 * @author andoni.eguiluz @ ingenieria.deusto.es
 */
public class TablaEstadistica extends Tabla {

	/** Interfaz para métodos que calculan un nuevo valor en una tabla
	 * @author andoni.eguiluz @ ingenieria.deusto.es
	 */
	public static interface CalculadorColumna {
		/** Método de cálculo de nuevo valor
		 * @param tabla	Tabla de la que calcular el valor
		 * @param fila	fila de datos de la que calcular el valor
		 * @return	Valor nuevo calculado
		 */
		public Object calcula( TablaEstadistica tabla, int fila );
	}
	
	/** Tipo enumerado para posibles cálculos estadísticos
	 * @author andoni.eguiluz @ ingenieria.deusto.es
	 */
	public static enum TipoEstad { SUMATORIO, MEDIANA, MEDIA };
	
	
	/** Crea una tabla de estadísticas de datos vacía (sin cabeceras ni datos)
	 */
	public TablaEstadistica() {
		super();
	}
	
	/** Crea una tabla de estadística de datos vacía con cabeceras y tipos
	 * @param cabeceras	Nombres de las cabeceras de datos
	 */
	public TablaEstadistica( ArrayList<String> cabeceras, ArrayList<Class<?>> tipos ) {
		super( cabeceras, tipos );
	}
	
	/** Crea una tabla estadística desde una tabla normal
	 * @param tabla	Tabla de origen
	 */
	public TablaEstadistica( Tabla tabla ) {
		cabeceras = tabla.cabeceras;
		tipos = tabla.tipos;
		dataO = tabla.dataO;
	}
	
	
	// =================================================
	// Métodos estadísticos
	
	/** Calcula estadísticos básicos para los valores de la tabla
	 * @param nomColD	Columna inicial desde la que tomar valores (inclusive). Solo se consideran los valores no nulos Integer, Long o Double
	 * @param nomColH	Columna final hasta la que tomar valores (inclusive). Solo se consideran los valores no nulos Integer, Long o Double
	 * @return	Array de doubles: [ N, media, mediana, desv.típica, mínimo, primerCuartil, segundoCuartil, tercerCuartil, máximo ]<br>
	 * 			Si no hay valores o si algún nombre de columna es incorrecto, se devuelve null<br>
	 * 			Apunte: la desv.típica se calcula dividiendo entre N-1 si N es mayor que 10, entre N si N es 10 o menor
	 */
	public double[] getEstadValores( String nomColD, String nomColH ) {
		if (dataO==null) return null;
		ArrayList<Double> lValores = new ArrayList<>();
		int colD = cabeceras.indexOf( nomColD );
		int colH = cabeceras.indexOf( nomColH );
		if (colD==-1 || colH==-1) return null;
		for (int fila=0; fila<dataO.size(); fila++) {
			for (int col=colD; col<=colH; col++) {
				Object valor = get( fila, col );
				if (valor!=null) {
					if (valor instanceof Integer) lValores.add( new Double((Integer)valor) );
					else if (valor instanceof Long) lValores.add( new Double((Long)valor) );
					else if (valor instanceof Double) lValores.add( (Double)valor );
				}
			}
		}
		if (lValores.size()==0) return null;
		Collections.sort( lValores );
		return getEstadValores( lValores );
	}
	
	/** Calcula estadísticos básicos para los valores de una lista de números
	 * @param lValoresOrdenados	Lista de valores YA ORDENADOS
	 * @return	Array de doubles: [ N, media, mediana, desv.típica, mínimo, primerCuartil, segundoCuartil, tercerCuartil, máximo ]<br>
	 * 			Si no hay valores o si algún nombre de columna es incorrecto, se devuelve null<br>
	 * 			Apunte: la desv.típica se calcula dividiendo entre N-1 si N es mayor que 10, entre N si N es 10 o menor
	 */
	public static double[] getEstadValores( ArrayList<Double> lValoresOrdenados ) {
		if (lValoresOrdenados.size()==0) return null;
		double[] ret = new double[9];
		int n = lValoresOrdenados.size();
		ret[0] = n;  // N
		double suma = 0; for (double d : lValoresOrdenados) suma += d;
		ret[1] = suma / n;  // media
		ret[2] = (n%2==0) ? (lValoresOrdenados.get(n/2) + lValoresOrdenados.get(n/2-1))/2.0 : lValoresOrdenados.get(n/2);  // mediana
		double sumaCuadsDist = 0;
		for (double d : lValoresOrdenados) { double distAMedia = ret[1] - d; sumaCuadsDist += distAMedia * distAMedia; }
		final double DIVISOR= (n>10) ? (n-1) : n;
		ret[3] = Math.sqrt( sumaCuadsDist / DIVISOR );  // desviación típica
		ret[4] = lValoresOrdenados.get(0);  // min
		ret[5] = lValoresOrdenados.get(n/4);  // primer cuartil
		ret[6] = lValoresOrdenados.get(2*n/4);  // segundo cuartil
		ret[7] = lValoresOrdenados.get(3*n/4);  // tercer cuartil
		ret[8] = lValoresOrdenados.get(n-1);  // max
		return ret;
	}
	
	/** Añade a la tabla una nueva columna y realiza los cálculos de sus valores en todas las filas existentes
	 * @param colNueva	Nombre de la columna nueva
	 * @param tipoCol	Tipo de la columna nueva
	 * @param calc	Función de cálculo de los valores de esa columna
	 */
	public void addCalcColumn(String colNueva, Class<?> tipoCol, CalculadorColumna calc) {
		addColumna( colNueva, tipoCol, null );
		for (int fila=0; fila<dataO.size(); fila++) {
			set( fila, colNueva, calc.calcula( this, fila ) );
		}
	}

	/** Crea una nueva tabla filtro de la actual (solo contendrá algunas de las filas de acuerdo al filtro definido)
	 * @param columnaFiltro	Columna por la que se filtra
	 * @param aMayusculas	Si es true considera los strings pasados a mayúsculas (si es false, los diferencia por capitalización)
	 * @param valor1	Valor de filtro. Si es el único, se filtran los valores iguales. Si el siguiente no es null, se filtran los valores mayores o iguales a él
	 * @param valor2	Valor de filtro, por menor o igual a él. Si es nulo solo se considera el anterior
	 * @param colsADejar	Si no se indica, se copian todas las columnas. Si se indica, solo se incluyen esas
	 * @return	tabla filtrada nueva
	 */
	public TablaEstadistica creaTablaFiltro( String columnaFiltro, boolean aMayusculas, Object valor1, Object valor2, String... colsADejar ) {
		if (aMayusculas && valor1!=null && valor1 instanceof String) valor1 = valor1.toString().toUpperCase();
		if (aMayusculas && valor2!=null && valor2 instanceof String) valor2 = valor2.toString().toUpperCase();
		// 1. Crear estructura de tabla
		if (colsADejar.length==0) {
			ArrayList<String> lC = new ArrayList<>( cabeceras );
			colsADejar = new String[ lC.size() ];
			for (int i=0; i<lC.size(); i++) colsADejar[i] = lC.get(i);
		}
		TablaEstadistica to = new TablaEstadistica();
		for (String col : colsADejar) {
			int colN = cabeceras.indexOf( col );
			if (colN==-1) return null;  // Columna incorrecta
			to.addColumna( col, tipos.get(colN), null );
		}
		// 2. Llenar tabla con los valores filtrados
		for (int fila=0; fila<dataO.size(); fila++) {
			Object o = get( fila, columnaFiltro );
			if (aMayusculas && o instanceof String) o = ((String)o).toUpperCase();
			boolean enFiltro = false;
			if (valor2==null) {  // Por valor == con valor1
				enFiltro = (o==null && valor1==null) || (o!=null && o.equals(valor1));
			} else {  // Por valor1 <= valor <= valor2
				if (valor1!=null) {
					if (o!=null) { // Comparar en función del tipo de valor (entero, doble, string)
						if (o instanceof Integer && valor1 instanceof Integer && valor2 instanceof Integer) {
							enFiltro = ((Integer) o >= (Integer) valor1) && ((Integer) o <= (Integer) valor2);
						} else if (o instanceof Double && valor1 instanceof Double && valor2 instanceof Double) {
							enFiltro = ((Double) o >= (Double) valor1) && ((Double) o <= (Double) valor2);
						} else {
							enFiltro = (o.toString().compareTo(valor2.toString())>=0 && o.toString().compareTo(valor2.toString())<=0);
						}
					}
				}
			}
			if (enFiltro) {
				to.addDataLine();
				for (String col : colsADejar) {
					Object valor = get( fila, col );
					to.set( col, valor );
				}
			}
		}
		return to;
	}

	/** Crea una nueva tabla reducida de la actual (solo contendrá algunas columnas de las previas)
	 * @param colsADejar	Columnas que se quieren incluir
	 * @return	tabla filtrada nueva, null si es errónea
	 */
	public TablaEstadistica creaTablaFiltro( String[] colsADejar ) {
		if (colsADejar==null || colsADejar.length==0) return null;
		// 1. Crear estructura de tabla
		TablaEstadistica to = new TablaEstadistica();
		for (String col : colsADejar) {
			int colN = cabeceras.indexOf( col );
			if (colN==-1) return null;  // Columna incorrecta
			to.addColumna( col, tipos.get(colN), null );
		}
		// 2. Llenar tabla con los valores
		for (int fila=0; fila<dataO.size(); fila++) {
			to.addDataLine();
			for (String col : colsADejar) {
				Object valor = get( fila, col );
				to.set( col, valor );
			}
		}
		return to;
	}

	private double[] valoresEstadisticos;
	/** Devuelve los valores estadísticos de la tabla estadística recién creada (tras llamar a {@link #creaTablaEstad(TipoEstad, String, String, boolean, String...)} o {@link #creaTablaPorc(String, String, Object, String[])}) */
	public double[] getValoresEstadisticos() { return valoresEstadisticos; }
	
	/** Crea una nueva tabla de análisis estadístico desde la actual
	 * @param tipo	Tipo de estadística a calcular
	 * @param columnaAgrup	Columna cuyos valores se utilizan como agrupación (define las filas que quedarán en la tabla de análisis retornada). Debe ser string o entera
	 * @param colsCalculadas	Columnas que se quieren calcular con estadística (columnas posteriores), si no se indica se incluyen todas las numéricas (int o double)
	 * @return	tabla de análisis estadístico nueva, null si hay algún error
	 */
	public TablaAnalisis creaTablaEstad( TipoEstad tipo, String columnaAgrup, String... colsCalculadas ) {
		// 1. Crear estructura de tabla
		int colAgrup = cabeceras.indexOf( columnaAgrup );
		if (colAgrup==-1) return null;
		if (colsCalculadas.length==0) { // Si no se indican las columnas a añadir, se incluyen todas las numéricas (excepto la de agrupación)
			ArrayList<String> lC = new ArrayList<>();
			for (int i=0; i<tipos.size(); i++) {
				Class<?> c = tipos.get(i);
				String n = cabeceras.get(i);
				if (!n.equals(columnaAgrup) && (c.equals(Integer.class) || c.equals(Double.class))) {  // Si no es la columna de agrupación y es numérica se añade
					lC.add( n );
				}
			}
			colsCalculadas = new String[ lC.size() ];
			for (int i=0; i<lC.size(); i++) colsCalculadas[i] = lC.get(i);
		}
		TablaAnalisis to = new TablaAnalisis();
		to.addColumna( columnaAgrup, tipos.get(colAgrup), null );
		for (String col : colsCalculadas) {
			to.addColumna( col, Double.class, null );
		}
		// 2. Calcular los valores de segmentación
		ArrayList<Object> valoresSeg = new ArrayList<>();
		for (int fila=0; fila<dataO.size(); fila++) {
			Object o = get(fila,columnaAgrup);
			if (o!=null) {
				if (!valoresSeg.contains(o)) valoresSeg.add( o );
			}
		}
		valoresSeg.sort( new Comparator<Object>() {
			@Override
			public int compare(Object o1, Object o2) {
				if (o1 instanceof Integer) {
					if (o2!=null && o2 instanceof Integer) {
						return ((Integer)o1).compareTo( (Integer)o2 );
					} else {
						if (o2==null) return +1;
						return -1;
					}
				} else {
					if (o2!=null) {
						return o1.toString().compareTo( o2.toString() );
					} else {
						return +1;
					}
				}
			}
		});
		// 3. Calcular los estadísticos para la tabla
		ArrayList<Double> listaValores = new ArrayList<>();
		for (Object valorSeg : valoresSeg) {
			to.addDataLine();
			to.set( columnaAgrup, valorSeg==null ? "null" : valorSeg.toString() );
			for (String col : colsCalculadas) {
				// Calculamos todos los valores de cada columna y segmentación
				ArrayList<Double> vals = new ArrayList<Double>();
				for (int fila=0; fila<dataO.size(); fila++) {
					Object seg = get(fila,columnaAgrup);
					if (valorSeg==null && seg==null || (valorSeg!=null && valorSeg.equals(seg))) {  // Es valor de la segmentación
						Object o = get(fila,col);
						if (o!=null) {
							if (o instanceof Integer) vals.add( new Double(((Integer) o).intValue()) );
							else if (o instanceof Double) vals.add( (Double) o );
						}
					}
				}
				// Se calcula el estadístico
				Double estad = null;
				if (vals.size()>0) {
					if (tipo==TipoEstad.MEDIA) {
						double suma = 0;
						for (double d : vals) suma += d;
						estad = suma / vals.size();
					} else if (tipo==TipoEstad.SUMATORIO) {
						double suma = 0;
						for (double d : vals) suma += d;
						estad = suma;
					} else if (tipo==TipoEstad.MEDIANA) {
						Collections.sort( vals );
						if (vals.size()%2==0) {  // Nº par de valores: media de los internos
							estad = (vals.get( vals.size()/2 ) + vals.get( vals.size()/2 - 1 ))/2.0;
						} else { // Nº impar de valores: mediana pura
							estad = vals.get( vals.size()/2 );
						}
					}
				}
				// Se pone el estadístico en la tabla
				if (estad!=null) {
					to.set( col, estad );
					listaValores.add( estad );
				}
			}
		}
		// 4.- Calcular estadísticos de los valores calculados por si se quieren consultar después
		Collections.sort( listaValores );
		valoresEstadisticos = getEstadValores( listaValores );
		return to;
	}
	
	/** Devuelve la lista de valores de una columna
	 * @param colNom	Nombre de la columna que revisar (debe ser de enteros o strings)
	 * @param aMayusculas	true si son Strings y se quieren convertir a mayúsculas, false en caso contrario (diferencia mayúsculas de minúsculas)
	 * @return	Lista ordenada de los valores no nulos de una columna (solo una vez cada valor único)
	 */
	public Object[] getValoresUnicos( String colNom, boolean aMayusculas ) {
		if (dataO==null) return null;
		ArrayList<Object> valoresSeg = new ArrayList<>();
		for (int fila=0; fila<dataO.size(); fila++) {
			Object o = get(fila, colNom);
			if (o!=null) {
				if (aMayusculas && o instanceof String) o = ((String)o).toUpperCase();
				if (!valoresSeg.contains(o)) valoresSeg.add( o );
			}
		}
		valoresSeg.sort( new Comparator<Object>() {
			@Override
			public int compare(Object o1, Object o2) {
				if (o1==null) if (o2==null) return 0; else return -1;
				else if (o1 instanceof Integer) {
					if (o2!=null && o2 instanceof Integer) {
						return ((Integer)o1).compareTo( (Integer)o2 );
					} else {
						if (o2==null) return +1;
						return -1;
					}
				} else {
					if (o2!=null) {
						return o1.toString().compareTo( o2.toString() );
					} else {
						return +1;
					}
				}
			}
		});
		Object[] ret = new Object[valoresSeg.size()];
		int i = 0;
		for (Object o : valoresSeg) { ret[i] = o; i++; }
		return ret;
	}

	
	/** Pone un renderer de 4 colores según cuartiles 1 y 3 a los valores numéricos de la tabla en la tabla visual de datos asociada a esta tabla.
	 * @param vt	Ventana correspondiente a esta tabla
	 * @param decimales	true si se quieren sacar los valores con decimales, false en caso contrario (con decimales Intenta sacar 4 dígitos: por debajo de 10 pone 3 decimales, por debajo de 100 pone 2 decimales, por debajo de 1000 un decimal, por encima ninguno)
	 * @param nColD	Nombre columna inicial donde poner el renderer (inclusive)
	 * @param nColH	Nombre columna final donde poner el renderer (inclusive)
	 * @param col1	Color 1	Color mínimo a utilizar (valores más pequeños)
	 * @param col2	Color 2	Color correspondiente al cuartil 1
	 * @param col3	Color 3	Color correspondiente al cuartil 3
	 * @param col4	Color 4	Color máximo a utilizar (valores mayores)
	 */
	public void setRenderer4Cuartiles( VentanaTabla vt, boolean decimales, String nColD, String nColH, Color col1, Color col2, Color col3, Color col4 ) {
		double[] est = getEstadValores( nColD, nColH );
		vt.setRendererCuatroColores( decimales, nColD, nColH, est[4], est[5], est[7], est[8], col1, col2, col3, col4 );
	}
	
	
}
