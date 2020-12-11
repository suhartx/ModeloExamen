package examen.ord202001;

public class Contador {
	
	int valor;
	int contador;
	public Contador(int valor) {
		super();
		this.valor = valor;
		this.contador =1;

	}
	public int getValor() {
		return valor;
	}
	public void setValor(int valor) {
		this.valor = valor;
	}
	public int getContador() {
		return contador;
	}
	public void setContador(int contador) {
		this.contador = contador;
	}
	public void incContador() {
		this.contador++;
	}



}
