package robot2;

import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.Motor;
import lejos.hardware.port.Port;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3TouchSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.robotics.SampleProvider;
import lejos.robotics.chassis.Chassis;
import lejos.robotics.chassis.Wheel;
import lejos.robotics.chassis.WheeledChassis;
import lejos.robotics.filter.MeanFilter;
import lejos.utility.Delay;

public class Main {
	
	/*variable du chassis*/
	static Wheel wheel1 = WheeledChassis.modelWheel(Motor.A, 56).offset(-61);
	static Wheel wheel2 = WheeledChassis.modelWheel(Motor.C, 56).offset(61);
	static Chassis chassis = new WheeledChassis(new Wheel[] { wheel1, wheel2 }, WheeledChassis.TYPE_DIFFERENTIAL);
	
	/*variable sensor sonar*/
	static EV3UltrasonicSensor sensor = new EV3UltrasonicSensor(SensorPort.S3);
    static SampleProvider distanceMode = sensor.getDistanceMode();
    
    /*variable sensor toucher*/
    static EV3TouchSensor touch= new EV3TouchSensor(SensorPort.S2) ;
	static SampleProvider toucher= touch.getTouchMode();
	
	/*variable global du robot*/
	static int boussol=0;
	static float distmin=(float)1.5;
	static boolean droite=true;
	static int possedepalet=0;
	
	//static int angle=180;

	/*variable sensor color*/
	static Port port = LocalEV3.get().getPort("S4");
	static EV3ColorSensor colorSensor = new EV3ColorSensor(port);
	static SampleProvider average = new MeanFilter(colorSensor.getRGBMode(), 1);
	static float[] blue = new float[average.sampleSize()];
	static float[] red = new float[average.sampleSize()];
	static float[] black = new float[average.sampleSize()];
	static float[] green = new float[average.sampleSize()];
	static float[] yellow= new float[average.sampleSize()];
	static float[] white = new float[average.sampleSize()];
	static float[] grey = new float[average.sampleSize()];
	
	/*utiliser pour la detection de coulore*/
	public static double scalaire(float[] v1, float[] v2) {
		return Math.sqrt(Math.pow(v1[0] - v2[0], 2.0) + Math.pow(v1[1] - v2[1], 2.0) + Math.pow(v1[2] - v2[2], 2.0));
	}
	
	/*calibre les variable des coulor*/
	public static void calibreColor() {
		System.out.println("Press enter to calibrate blue...");
		Button.ENTER.waitForPressAndRelease();
		average.fetchSample(blue, 0);
		System.out.println("Blue calibrated");
	

		System.out.println("Press enter to calibrate red...");
		Button.ENTER.waitForPressAndRelease();
		average.fetchSample(red, 0);
		System.out.println("Red calibrated");
	

		System.out.println("Press enter to calibrate black...");
		Button.ENTER.waitForPressAndRelease();
		average.fetchSample(black, 0);
		System.out.println("Black calibrated");
		
		System.out.println("Press enter to calibrate green...");
		Button.ENTER.waitForPressAndRelease();
		average.fetchSample(green, 0);
		System.out.println("Green calibrated");
		
		System.out.println("Press enter to calibrate yellow...");
		Button.ENTER.waitForPressAndRelease();
		average.fetchSample(yellow, 0);
		System.out.println("Yellow calibrated");

		System.out.println("Press enter to calibrate white...");
		Button.ENTER.waitForPressAndRelease();
		average.fetchSample(white, 0);
		System.out.println("White calibrated");

		System.out.println("Press enter to calibrate grey...");
		Button.ENTER.waitForPressAndRelease();
		average.fetchSample(grey, 0);
		System.out.println("Grey calibrated");

	}
	
	public static boolean toucherPalet() {
		 float[] sample = new float[toucher.sampleSize()];
		 toucher.fetchSample(sample, 0);
		 float range=sample[0];
		 sample = new float[toucher.sampleSize()];
		 toucher.fetchSample(sample, 0);
		 range=sample[0];
		 if(range==1)
			 return true;
		 return false;
	}
	
	public static int search(int angle) {
		if(angle<3) {
			if(droite) {
				int comp=0;
				while(comp<5) {
					chassis.rotate(5);
					while(chassis.isMoving()) {
						if(distance()<= distmin+0.01) {
							Sound.beepSequenceUp();  
							boussol+=3;
							chassis.stop();
							return 0;
						}
						/*
						if(distance()<= distmin+0.007) {
							chassis.waitComplete();
							boussol+=2;
							chassis.rotate(-3);
							chassis.waitComplete();
							return;
						}
						*/
					}
					chassis.waitComplete();
					boussol+=5;
					comp++;
				}
				droite=true;
				return 1;

			}
			else {
				int comp=0;
				while(comp<5) {
					chassis.rotate(-5);
					while(chassis.isMoving()) {
						if(distance()<=distmin+0.01) {
							Sound.beepSequenceUp();  
							boussol-=2;
							chassis.stop();
							return 0;
						}
						/*
						if(distance()<= distmin+0.007) {
							chassis.waitComplete();
							boussol-=2;
							chassis.rotate(3);
							//boussol+=2;
							chassis.waitComplete();
							return;
						}
						*/
					}
					chassis.waitComplete();
					boussol-=5;
					comp++;
				}
				droite=true;
				return 1;

			}
			
		}
		if(droite) {
			 chassis.rotate(angle);
			 boussol+=angle;
		 }
		 else {
			 chassis.rotate((-1)*angle);
			 boussol+=(-1)*angle;
		 }
		while(chassis.isMoving()) {
		 if(distance()<= distmin+0.007) {
			 Sound.beepSequenceUp();  
			 droite=(!droite);
			 break;
		 }
		}
		chassis.waitComplete();
		return search(angle/2);
		
	}
	
	public static void rotation(int ang) {
		chassis.rotate(ang);
		boussol+=ang;
		chassis.waitComplete();
	}
	
	
    public static float distance() {
   	 float[] sample = new float[distanceMode.sampleSize()];
	        distanceMode.fetchSample(sample, 0);
	        //System.out.printf("%.5f m\n", sample[0]);
	        float range=sample[0];
	        return range;
   }
	
	public static void searchMin(int n) {
		chassis.rotate(n);
		boussol+=n%360;
		 while(chassis.isMoving()) {
			 if(distance()<=distmin)
				 distmin=distance();	
			 
		 }	
		 if(distmin==1.5) {
			 /*TODO*/
			 /* voir plus loin si on trouve mieux en fonction de la position*/
		 }
	}
	
	public static void ouvrirPince() {
		 Motor.B.setSpeed(1000);
		 Motor.B.forward();
		 Delay.msDelay(750);
		 Motor.B.stop();
	}
	
	public static void fermerPince() {
		 Motor.B.setSpeed(1000);
		 Motor.B.backward();
		 Delay.msDelay(750);
		 Motor.B.stop();
	}
	
	public static void positionLigneBlanche() {
		boussol=boussol%360;
		 if(boussol<180)
			 chassis.rotate(-boussol);
		 else
			 chassis.rotate(360-boussol);
		 chassis.waitComplete();
		 boussol=0;
		 
	}
	
	public static String donneColor() {
		float[] sample = new float[average.sampleSize()];
		// System.out.println("\nPress enter to detect a color...");
		// Button.ENTER.waitForPressAndRelease();
		average.fetchSample(sample, 0);
		double minscal = Double.MAX_VALUE;
		String color = "";

		double scalaire = scalaire(sample, blue);
		// Button.ENTER.waitForPressAndRelease();
		// System.out.println(scalaire);

		if (scalaire < minscal) {
			minscal = scalaire;
			color = "blue";
		}

		scalaire = scalaire(sample, red);
		// System.out.println(scalaire);
		// Button.ENTER.waitForPressAndRelease();
		if (scalaire < minscal) {
			minscal = scalaire;
			color = "red";
		}

		scalaire = scalaire(sample, black);
		// System.out.println(scalaire);
		// Button.ENTER.waitForPressAndRelease();
		if (scalaire < minscal) {
			minscal = scalaire;
			color = "black";
		}
		
		scalaire = scalaire(sample, green);
		// System.out.println(scalaire);
		// Button.ENTER.waitForPressAndRelease();
		if (scalaire < minscal) {
			minscal = scalaire;
			color = "green";
		}

		scalaire = scalaire(sample, yellow);
		// System.out.println(scalaire);
		// Button.ENTER.waitForPressAndRelease();
		if (scalaire < minscal) {
			minscal = scalaire;
			color = "yellow";
		}

		scalaire = scalaire(sample, white);
		// System.out.println(scalaire);
		// Button.ENTER.waitForPressAndRelease();
		if (scalaire < minscal) {
			minscal = scalaire;
			color = "white";
		}

		scalaire = scalaire(sample, grey);
		// System.out.println(scalaire);
		// Button.ENTER.waitForPressAndRelease();
		if (scalaire < minscal) {
			minscal = scalaire;
			color = "grey";
		}
		return color;
	}

	
	public static void vaLigneBlanche() {
		chassis.travel(10000);
		//chassis.setLinearSpeed(1000);
		while(true) {
			if(distance()<0.20) {
				chassis.stop();
				chassis.waitComplete();
				chassis.rotate(45);
				chassis.waitComplete();
				chassis.travel(1000);
				Delay.msDelay(1700);
				chassis.stop();
				chassis.waitComplete();
				chassis.rotate(-45);
				chassis.waitComplete();
				vaLigneBlanche();
				break;
			}
			if(donneColor().equals("white")) {
				//System.out.println("white trouve");
				chassis.stop();
				chassis.waitComplete();
				Sound.beepSequenceUp();
				break;
			}
		}
	}
	
	public static void palet(int angle) {
		 Button.LEDPattern(3);  
		 searchMin(-angle);
		 if(search(angle/2)==0) {
			 Button.LEDPattern(4);  
			 chassis.travel(1000);
			 Delay.msDelay(400);
			 ouvrirPince();
			
			 while((!toucherPalet()) ) {
				 possedepalet=1;
				 if (distance()<0.20) {
					 Button.LEDPattern(2);  
					 chassis.stop();
					 fermerPince();
					 chassis.waitComplete();
					 positionLigneBlanche();
					 vaLigneBlanche();
					 chassis.travel(-150);
					 chassis.waitComplete();
					 return;
				 }
				 if (!chassis.isMoving()) {
					 fermerPince();
					 positionLigneBlanche();
					 return;
				 }
			 }

			 Button.LEDPattern(1);  
			 chassis.stop();
			 fermerPince();
			 positionLigneBlanche();
			 vaLigneBlanche();
			 ouvrirPince();
			 chassis.travel(-1000);
			 Delay.msDelay(1000);
			 chassis.stop();
			 fermerPince();
			 distmin=10;
			 droite=true;
			 chassis.waitComplete();
			 Button.LEDPattern(4);  
		 }
		 else {
			 positionLigneBlanche();
		 }
	}

	public static void traceFirst() {
		 Button.LEDPattern(3);  
		chassis.setLinearSpeed(1000);
		chassis.travel(10000);
		ouvrirPince();
		while(!(toucherPalet()) /* rajouter une condition*/) {
		}
		Button.LEDPattern(1);  
		chassis.stop();
		fermerPince();
		chassis.waitComplete();
		chassis.rotate(45);
		chassis.waitComplete();
		chassis.travel(1000);
		Delay.msDelay(1700);
		chassis.stop();
		chassis.waitComplete();
		chassis.rotate(-45);
		chassis.waitComplete();
		vaLigneBlanche();
		Button.LEDPattern(4);
		chassis.stop();
		ouvrirPince();
		chassis.travel(-1000);
		Delay.msDelay(1000);
		chassis.stop();
		fermerPince();
	}
	
	public static void test() {
		chassis.rotate(180);
		while(true) {
			if (distance()<0.20) {
				chassis.stop();
				System.out.println(distance());
			}
		}
		
	}
	
	public static void main(String [] args) {
		System.out.println("Press any key to start");
		Sound.beepSequenceUp();    // make sound when ready.
		
		/* sequence d'initialisation*/
		//calibreColor();
		System.out.println("ready");
		Button.waitForAnyPress();
		Delay.msDelay(2000);
		
		
		/*sequence d'execution*/
		traceFirst();
		
		/*fin du programme*/
		chassis.stop();
		System.out.println("fini");
		Button.waitForAnyPress();

	}
}