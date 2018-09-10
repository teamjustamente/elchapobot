package TeamJustamente;
import robocode.*;
import java.awt.Color;
import java.awt.geom.*;
import java.util.*;

// API help : http://robocode.sourceforge.net/docs/robocode/robocode/Robot.html

/**
 * ElChapoBot - a robot by TeamJustamente
 */
public class ElChapoBot extends AdvancedRobot {
	/**
	 * run: ElChapoBot default behavior
	 */
	Hashtable targets;				//Hashtable que armazenara todos os inimigos
	Enemy target;					//inimigo atual
	final double PI = Math.PI;
	int direction = 1;				//direcao na qual o bot esta indo... 1 = para frente, -1 = para tras
	double firePower;				//poder do tiro do bot
	double midpointstrength = 0;	//força do ponto de gravidade no meio do campo
	int midpointcount = 0;			//numero de turnos de combate desde que a força foi alterada
	
	public void run() {
		targets = new Hashtable();
		target = new Enemy();
		target.distance = 100000;						//inicializa a distancia para que se possa selecionar o alvo
		setColors(Color.red,Color.blue,Color.green);	//sets the colours of the robot
		setAdjustGunForRobotTurn(true); // determina que nos turnos de combate a arma e idependente
		setAdjustRadarForGunTurn(true); // determina que nos turnos de combate o radar e idependente
		turnRadarRightRadians(2*PI);	//ira girar o radar scaneando o campo 1 vez
		while(true) {
			antiGravMove();					//move o bot
			doFirePower();					//seleciona o poder de fogo a ser usado
			doScanner();					//scanneia
			doGun();						//move a arma para aonde o inimigo estara
			fire(firePower);				//atira
			execute();						//executa todos os comandos
		}
	}
	
	void doFirePower() {
		firePower = 400/target.distance;//seleciona o poder da bala com base na distancia entre o nosso bot e o bot inimigo
		if (firePower > 3) {
			firePower = 3;
		}
	}
	
	void antiGravMove() {
   		double xforce = 0;
	    double yforce = 0;
	    double force;
	    double ang;
	    Point p;
		Enemy en;
    	Enumeration e = targets.elements();
	    //percorrer todos os inimigos. Se eles estão vivos, eles são repelidos. Calcule a força entre todos nos
		while (e.hasMoreElements()) {
    	    en = (Enemy)e.nextElement();
			if (en.live) {
				p = new Point(en.x,en.y, -1000);
		        force = p.power/Math.pow(getRange(getX(),getY(),p.x,p.y),2);
		        //encontra o angulo entre o ponto e nosso bot
		        ang = normaliseBearing(Math.PI/2 - Math.atan2(getY() - p.y, getX() - p.x)); 
		        //adiciona essa força as forças totais em suas respectivas direçoes
		        xforce += Math.sin(ang) * force;
		        yforce += Math.cos(ang) * force;
			}
	    }
	    
		/**A próxima seção adiciona um ponto médio com uma força aleatória (positiva ou negativa).
		A força muda a cada 5 turnos, e vai entre -1000 e 1000. Isto dá uma melhor movimento global.**/
		midpointcount++;
		if (midpointcount > 5) {
			midpointcount = 0;
			midpointstrength = (Math.random() * 2000) - 1000;
		}
		p = new Point(getBattleFieldWidth()/2, getBattleFieldHeight()/2, midpointstrength);
		force = p.power/Math.pow(getRange(getX(),getY(),p.x,p.y),1.5);
	    ang = normaliseBearing(Math.PI/2 - Math.atan2(getY() - p.y, getX() - p.x)); 
	    xforce += Math.sin(ang) * force;
	    yforce += Math.cos(ang) * force;
	   
	    /**As quatro linhas seguintes adicionam evitação de parede. Eles só nos afetarão se o bot estiver perto
		para as paredes devido à força das paredes diminuindo a uma potência 3.**/
	    xforce += 5000/Math.pow(getRange(getX(), getY(), getBattleFieldWidth(), getY()), 3);
	    xforce -= 5000/Math.pow(getRange(getX(), getY(), 0, getY()), 3);
	    yforce += 5000/Math.pow(getRange(getX(), getY(), getX(), getBattleFieldHeight()), 3);
	    yforce -= 5000/Math.pow(getRange(getX(), getY(), getX(), 0), 3);
	    
	    //O bot ira se mover na direcao entao da forca resolvida.
	    goTo(getX()-xforce,getY()-yforce);
	}
	
	/**Move-se para uma coordenada X,Y**/
	void goTo(double x, double y) {
	    double dist = 20; 
	    double angle = Math.toDegrees(absbearing(getX(),getY(),x,y));
	    double r = turnTo(angle);
	    setAhead(dist * r);
	}


	/**metodo auxiliar para calculo do menor angulo**/
	int turnTo(double angle) {
	    double ang;
    	int dir;
	    ang = normaliseBearing(getHeading() - angle);
	    if (ang > 90) {
	        ang -= 180;
	        dir = -1;
	    } else if (ang < -90) {
	        ang += 180;
	        dir = -1;
	    } else {
	        dir = 1;
	    }
	    setTurnLeft(ang);
	    return dir;
	}

	/**realiza o scanner do campo**/
	void doScanner() {
		setTurnRadarLeftRadians(2*PI);
	}
	
	/**move a arma para a proxima posicao do inimigo**/
	void doGun() {
		long time = getTime() + (int)Math.round((getRange(getX(),getY(),target.x,target.y)/(20-(3*firePower))));
		Point2D.Double p = target.guessPosition(time);
		
		//compensa o angulo da arma com base no direcionamento linear fornecido pela classe inimiga
		double gunOffset = getGunHeadingRadians() - (Math.PI/2 - Math.atan2(p.y - getY(), p.x - getX()));
		setTurnGunLeftRadians(normaliseBearing(gunOffset));
	}
	
	
	//se o movimento não estiver dentro da faixa -pi a pi, altera-o para fornecer o ângulo mais curto
	double normaliseBearing(double ang) {
		if (ang > PI)
			ang -= 2*PI;
		if (ang < -PI)
			ang += 2*PI;
		return ang;
	}
	
	//se o angulo da cabeca da arma não estiver dentro do intervalo de 0 a 2*pi, altera-o para fornecer o ângulo mais curto
	double normaliseHeading(double ang) {
		if (ang > 2*PI)
			ang -= 2*PI;
		if (ang < 0)
			ang += 2*PI;
		return ang;
	}
	
	//retorna a distancia entre as coordenadas X,Y
	public double getRange( double x1,double y1, double x2,double y2 ) {
		double xo = x2-x1;
		double yo = y2-y1;
		double h = Math.sqrt( xo*xo + yo*yo );
		return h;	
	}
	
	//obtem o movimento absoluto entre as coordenadas X,Y
	public double absbearing( double x1,double y1, double x2,double y2 ) {
		double xo = x2-x1;
		double yo = y2-y1;
		double h = getRange( x1,y1, x2,y2 );
		if( xo > 0 && yo > 0 ){
			return Math.asin( xo / h );
		}
		
		if( xo > 0 && yo < 0 ){
			return Math.PI - Math.asin( xo / h );
		}
		
		if( xo < 0 && yo < 0 ){
			return Math.PI + Math.asin( -xo / h );
		}
		
		if( xo < 0 && yo > 0 ){
			return 2.0*Math.PI - Math.asin( -xo / h );
		}
		
		return 0;
	}


	/**
	 * onScannedRobot: What to do when you see another robot
	 */
	public void onScannedRobot(ScannedRobotEvent e) {
		Enemy en;
		if (targets.containsKey(e.getName())) {
			en = (Enemy)targets.get(e.getName());
		} else {
			en = new Enemy();
			targets.put(e.getName(),en);
		}
		//obtem o movimento para o raio absoluto do ponto em que o bot esta
		double absbearing_rad = (getHeadingRadians()+e.getBearingRadians())%(2*PI);
		//nessa secao e definida todas as informacoes do nosso alvo
		en.name = e.getName();
		double h = normaliseBearing(e.getHeadingRadians() - en.heading);
		h = h/(getTime() - en.ctime);
		en.changehead = h;
		en.x = getX()+Math.sin(absbearing_rad)*e.getDistance(); //obtem a coordenada X de onde o alvo esta
		en.y = getY()+Math.cos(absbearing_rad)*e.getDistance(); //obtem a coordenada Y de onde o alvo esta
		en.bearing = e.getBearingRadians();
		en.heading = e.getHeadingRadians();
		en.ctime = getTime();				//pega o tempo do turno em que esse scanner ocorreu
		en.speed = e.getVelocity();
		en.distance = e.getDistance();	
		en.live = true;
		if ((en.distance < target.distance)||(target.live == false)) {
			target = en;
		}
	}
		
	public void onRobotDeath(RobotDeathEvent e) {
		Enemy en = (Enemy)targets.get(e.getName());
		en.live = false;		
	}		
}
