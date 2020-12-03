package examen.ord202001;

// T2
public class Puntuacion implements Comparable<Puntuacion> {
	private String nick;
	private int p;
	private double t;
	public Puntuacion(String nick, int p, double t) {
		this.nick = nick;
		this.p = p;
		this.t = t;
	}
	public String getNick() {
		return nick;
	}
	public void setNick(String nick) {
		this.nick = nick;
	}
	public int getP() {
		return p;
	}
	public void setP(int p) {
		this.p = p;
	}
	public double getT() {
		return t;
	}
	public void setT(double t) {
		this.t = t;
	}
	@Override
	public String toString() {
		return nick + ": " + p + " (" + t + ")";
	}
	@Override
	public int compareTo(Puntuacion o) {
		int dif = o.p - p;  // Primero el mayor (puntos)
		if (dif==0) {
			dif = (int) Math.round( t*1000 - o.t*1000 );  // Primero el menor (tiempos)
		}
		if (dif==0) {
			dif = nick.compareTo( o.nick );
		}
		return dif;
	}	
}
