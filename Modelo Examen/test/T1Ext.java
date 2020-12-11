import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import examen.ord202001.Tabla;
import examen.ord202001.TablaAnalisis;
import examen.ord202001.TablaEstadistica;
import examen.ord202001.TablaEstadistica.CalculadorColumna;
import examen.ord202001.TablaEstadistica.TipoEstad;




public class T1Ext {
	
TablaEstadistica tabla;
TablaEstadistica tabla2;
TablaAnalisis tabla3;
int posImpar;

	@Before
	 public void setUp() throws Exception {
		tabla = new TablaEstadistica();
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
		
		tabla2 = tabla.creaTablaFiltro("tipo", false, "dataset1", null, new String[]{"datoD", "Par-Impar"});
		tabla3 = tabla2.creaTablaEstad(TipoEstad.MEDIA, "Par-Impar", "datoD");
		
}
	
	
	@After
	 public void tearDown() {
		
		

	 }
	
	// COMPROBACION SI LA COLUMNA ES 7
	@Test
	public void cabeceraParImparCol7() {
		
		assertEquals("Cabecera Columna 7", "Par-Impar", tabla.getCabecera(6));
		
		//System.out.println(tabla.getCabecera(7));

	}
	
	// EN ESTE COMPROBAMOS SI EL TIPO DE DATOS DE LA COLUMNA ES STRING
	@Test
	public void tipoDatosCol7() {
		
		assertEquals("Tipo de dato Columna 7", "String", tabla.getType(6).getSimpleName());

	}
	
	// EN ESTE COMPROBAMOS SI LOS DATOS DE LA TABLA CUMPLEN CIERTOS REQUISITOS
	@Test
	public void CompruebaDatos() {
		
		for (int i = 0; i < tabla.size(); i++) {
			if (tabla.get(i, "datoI")==null) {
				assertEquals("Tipo de dato en Par-impar", "String", tabla.getType(6).getSimpleName());
				assertEquals("La columna es nula?", "nulo", tabla.get(i, "Par-Impar"));
			}else {
				assertEquals("Tipo de dato en datoI", "Integer", tabla.getType(0).getSimpleName());
				assertEquals("Tipo de dato en Par-impar", "String", tabla.getType(6).getSimpleName());

				if(tabla.get(i, 6)=="Par") {
				assertNotEquals("Dato en Par-impar es Par, Impar o Cero", "cero", tabla.get(i,6));
				assertNotEquals("Dato en Par-impar es Par, Impar o Cero", "Impar", tabla.get(i,6));
				assertEquals("Dato en Par-impar es Par, Impar o Cero", "Par", tabla.get(i,6));
				

				}else if(tabla.get(i, 6)=="Impar") {
					assertNotEquals("Dato en Par-impar es Par, Impar o Cero", "cero", tabla.get(i,6));
					assertNotEquals("Dato en Par-impar es Par, Impar o Cero", "Par", tabla.get(i,6));
					assertEquals("Dato en Par-impar es Par, Impar o Cero", "Impar", tabla.get(i,6));
		
				}else if(tabla.get(i, 6)=="Cero") {
					assertNotEquals("Dato en Par-impar es Par, Impar o Cero", "Impar", tabla.get(i,6));
					assertNotEquals("Dato en Par-impar es Par, Impar o Cero", "Par", tabla.get(i,6));
					assertEquals("Dato en Par-impar es Par, Impar o Cero", "Cero", tabla.get(i,6));

				}
			}
		}
	}
	
	// EN ESTE COMPROBAMOS LA LARGURA DE LA TABLA CON LA NUEVA TABLA QUE HEMOS CREADO
	@Test
	public void numFilasDataset1(){
		int contador= 0;
		for (int i = 0; i < tabla.size(); i++) {
			if (tabla.get(i, "tipo")=="dataset1"){
				contador++;
			}
		}
		assertEquals("cantidad de dataset1", contador, tabla2.size());

	}
	
	
	// EN ESTE COMPROBAMOS SI EN LA TERCERA TABLA HAY SOO UNA FILA IMPAR
	@Test
	public void numFilasImpar(){
		int contador= 0;
		
		for (int i = 0; i < tabla3.size(); i++) {
			
			if (tabla3.get(i, "Par-Impar")=="Impar"){
				
				posImpar=i;
				contador++;
			}
		}
		assertEquals("cantidad de impares", 1, contador);
		//assertEquals("cantidad de impares", 2, tabla3.get(1, 1));
		

	}

	// EN ESTE COMPROBAMOS SI LA MEDIA DE LAS FILAS IMPARES ES CORRECTA
	@Test
	public void mediaImpar(){
		int contador= 0;
		double suma=0;
		for (int i = 0; i < tabla2.size(); i++) {
			
			if (tabla2.get(i, "Par-Impar")=="Impar"){
				suma += tabla2.getDouble(i, 0);
				contador++;
			}
			
		}
		suma = suma/contador;
		assertEquals("cantidad de dataset1", suma, tabla3.getDouble(1, 1), 0.0000000001);
		// AQUI HACEMOS LA FUNCION PARA CALCULAR LA MEDIA Y NOS DAMOS CUENTA DE QUE EL DATO DE EMDIA DE IMPAR ES LA FILA 1 Y COLUMNA 1

	}	



}
