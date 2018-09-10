package TeamJustamente;
import java.awt.geom.*;
/**
 * Enemy - Entidade que representa os dados do bot inimigo
 */
public class Enemy {
	String name;
	public double bearing,heading,speed,x,y,distance,changehead;
	public long ctime; 		//tempo do jogo em que o inimigo foi identificado
	public boolean live; 	//e um inimigo vivo?
	public Point2D.Double guessPosition(long when) { // acha a posicao do inimigo
		double diff = when - ctime;
		double newY = y + Math.cos(heading) * speed * diff;
		double newX = x + Math.sin(heading) * speed * diff;
		
		return new Point2D.Double(newX, newY);
	}
}
