/**
 * Created by B. Sebastian
 */

import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;

import java.lang.Math;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import java.util.ArrayList;
import java.util.List;

import static org.firstinspires.ftc.robotcore.external.navigation.AngleUnit.DEGREES;
import static org.firstinspires.ftc.robotcore.external.navigation.AxesOrder.XYZ;
import static org.firstinspires.ftc.robotcore.external.navigation.AxesOrder.YZX;
import static org.firstinspires.ftc.robotcore.external.navigation.AxesReference.EXTRINSIC;

@Autonomous(name = "FrontRight",group = "parking")
public class FrontRight extends LinearOpMode {
    //imu
    BNO055IMU imu;
    BNO055IMU.Parameters parameters = new BNO055IMU.Parameters();

    //Creating DcMotor objects
    private DcMotor ridicareStanga;
    private DcMotor ridicareDreapta;
    private DcMotor intakeStanga;
    private DcMotor intakeDreapta;
    private DcMotor motorStangaFata;
    private DcMotor motorStangaSpate;
    private DcMotor motorDreaptaFata;
    private DcMotor motorDreaptaSpate;

    //Creating servo and CRservo objects
    private CRServo extindereServo;
    private Servo capstoneServo;
    private Servo prindere1;
    private Servo prindere2;
    private Servo prindere3;
    private Servo prindere4;
    private Servo platformaStanga;
    private Servo platformaDreapta;

    //Creating sensor objects
    private com.qualcomm.robotcore.hardware.TouchSensor atingere;
    private com.qualcomm.robotcore.hardware.ColorSensor culoare;

    //General global variables for various button logic and automation
    private boolean modRulareTeleop = true;
    private boolean modRulareTeleOpLogica = false;

    private boolean platformaLogica=false;
    private boolean platformaToggle=true;

    private boolean capstoneToggle=true;
    private boolean capstoneLogica=false;

    private boolean prindereToggle=false;
    private boolean prindereLogica=false;

    private boolean retragereExtindere=false;

    private boolean stareCurenta;
    private boolean stareFosta;

    private boolean logicaLoop=false;

    private int inaltimeStone=1;//stone height, remembers the last pozition the main lifting system got up to

    private float targetPosition=0;
    public double globalAngle;

    Orientation lastAngles = new Orientation();

    //Return the angle of the robot relative to the starting pozition
    private double getAngle() {
        Orientation angles = imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);

        double deltaAngle = angles.firstAngle - lastAngles.firstAngle;

        if (deltaAngle < -180)
            deltaAngle += 360;
        else if (deltaAngle > 180)
            deltaAngle -= 360;

        globalAngle += deltaAngle;

        lastAngles = angles;

        return globalAngle;
    }

    //Resets the angle to 0
    private void resetAngle() {
        lastAngles = imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);

        globalAngle = 0;
    }

    //Initializes the robot, sets servo pozitions and starts encoders
    private void init_robot() {
        //imu
        parameters.mode                = BNO055IMU.SensorMode.IMU;
        parameters.angleUnit           = BNO055IMU.AngleUnit.DEGREES;
        parameters.accelUnit           = BNO055IMU.AccelUnit.METERS_PERSEC_PERSEC;
        parameters.loggingEnabled      = false;

        imu = hardwareMap.get(BNO055IMU.class, "imu");

        imu.initialize(parameters);

        while (!isStopRequested() && !imu.isGyroCalibrated())
        {
            sleep(50);
            idle();
        }

        //dc
        ridicareDreapta=hardwareMap.dcMotor.get("ridicareDreapta");
        ridicareStanga=hardwareMap.dcMotor.get("ridicareStanga");
        intakeDreapta=hardwareMap.dcMotor.get("intakeDreapta");
        intakeStanga=hardwareMap.dcMotor.get("intakeStanga");
        motorDreaptaFata=hardwareMap.dcMotor.get("motorDreaptaFata");
        motorDreaptaSpate=hardwareMap.dcMotor.get("motorDreaptaSpate");
        motorStangaFata=hardwareMap.dcMotor.get("motorStangaFata");
        motorStangaSpate=hardwareMap.dcMotor.get("motorStangaSpate");

        //servo
        extindereServo=hardwareMap.crservo.get("extindereServo");
        capstoneServo=hardwareMap.servo.get("capstoneServo");
        platformaStanga=hardwareMap.servo.get("platformaStanga");
        platformaDreapta=hardwareMap.servo.get("platformaDreapta");
        prindere1=hardwareMap.servo.get("prindere1");
        prindere2=hardwareMap.servo.get("prindere2");

        //senzori
        atingere=hardwareMap.touchSensor.get("touchSensor");
        culoare=hardwareMap.colorSensor.get("colorSensor");

        platformaStanga.setPosition(1);
        platformaDreapta.setPosition(1);
        prindere1.setPosition(0.75);
        prindere2.setPosition(0.25);


        //encodere
        motorDreaptaFata.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        motorDreaptaFata.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        motorDreaptaFata.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        motorDreaptaFata.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        motorDreaptaSpate.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        motorDreaptaSpate.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        motorDreaptaSpate.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        motorDreaptaSpate.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        motorStangaFata.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        motorStangaFata.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        motorStangaFata.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        motorStangaFata.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        motorStangaSpate.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        motorStangaSpate.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        motorStangaSpate.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        motorStangaSpate.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        ridicareDreapta.setTargetPosition(0);
        ridicareDreapta.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        ridicareDreapta.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        ridicareDreapta.setMode(DcMotor.RunMode.RUN_TO_POSITION);

        ridicareStanga.setTargetPosition(0);
        ridicareStanga.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        ridicareStanga.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        ridicareStanga.setMode(DcMotor.RunMode.RUN_TO_POSITION);

        ridicareStanga.setPower(1);
        ridicareDreapta.setPower(1);

        inaltimeStone=1;
        telemetry.addData("Status","Intialized");
        telemetry.update();
    }

    //Useful functions witch operate the various systems
    private void prindere(boolean a) {
        if (!a) {
            prindere1.setPosition(0.75);
            prindere2.setPosition(0.25);
        }
        else {
            prindere1.setPosition(1);
            prindere2.setPosition(0);
        }
    }
    private void platforma(boolean a) {
        if (!a) {
            platformaDreapta.setPosition(1);
            platformaStanga.setPosition(1);
        }
        else {
            platformaDreapta.setPosition(0.2);
            platformaStanga.setPosition(0.1);
        }
    }
    private void intake(boolean a) {
        if (a) {
            intakeDreapta.setPower(1);
            intakeStanga.setPower(-1);
        }
        else {
            intakeDreapta.setPower(0);
            intakeStanga.setPower(0);
        }
    }

    //Lifts the main system up to the level specified in stone heights in condition a
    private void ridicareNivel(int a) {
        targetPosition=300+a*630;
        ridicareDreapta.setTargetPosition((int)targetPosition);
        ridicareStanga.setTargetPosition((int)targetPosition);
    }

    //Lifts the main system up to the level specified in ticks in condition a
    private void ridicareTicks(int a) {
        ridicareDreapta.setTargetPosition(a);
        ridicareStanga.setTargetPosition(a);
    }

    //Various automations (in the old build, now its only the automatic closing of the grabbers)
    private void automatizari() {
        if (culoare.red()>(culoare.blue()+15)) {
            prindere(true);
        }
    }

    //Extends arm forward by a certain number of seconds, blocking
    private void extindereSecunde(long a) {
        extindereServo.setPower(1);
        sleep(a);
        extindereServo.setPower(0);
    }

    //Retracts arm backwards by a certain number of seconds, blocking
    private void extindereSecundeInvers(long a) {
        extindereServo.setPower(-1);
        sleep(a);
        extindereServo.setPower(0);
    }

    //Rotates the robot to a certain absolute angle
    void rotatieAbsoluta(int unghi) {
        int eror=11;
        if (getAngle() < unghi) {
            while (getAngle() < unghi - eror && !isStopRequested()) {
                motorStangaFata.setPower(0.7);
                motorDreaptaFata.setPower(0.7);
                motorStangaSpate.setPower(0.7);
                motorDreaptaSpate.setPower(0.7);
            }
        }

        else {
            while (getAngle() > unghi + eror && !isStopRequested()) {
                motorStangaFata.setPower(-0.7);
                motorDreaptaFata.setPower(-0.7);
                motorStangaSpate.setPower(-0.7);
                motorDreaptaSpate.setPower(-0.7);
            }
        }
        motorStangaFata.setPower(0);
        motorDreaptaFata.setPower(0);
        motorStangaSpate.setPower(0);
        motorDreaptaSpate.setPower(0);
    }

    //The bread and butter of the autonomy, unfortunately this function no longer works as intended
    //as the setPower function had a feature in which it stabilazed the voltage using
    //the battery voltage. This is not the case anymore and this logic needs to be created manually
    //Angle of translation(double), distance(long), useIntake? (bool)
    private void miscare360Secunde(double alfa,long distanta,boolean cuIntake) {
        float viteza2=(float)0.6;
        double m1,m2,m3,m4,left=1,right=1,targetAngle2,eroare=1;
        double x=0,y=0;
        viteza2=(float)0.6;
        long milisecunde=1;
        milisecunde=(long)(distanta*14.423);

        if (cuIntake) {
            intake(true);
        }

        if (alfa>360) {
            x=0;
            y=0;
        }

        if (alfa==0 || alfa==360) {
            x=1;
            y=0;
        }

        if (alfa==90) {
            x=0;
            y=1;
        }

        if (alfa==180) {
            x=-1;
            y=0;
        }

        if (alfa==270) {
            x=0;
            y=-1;
        }

        y=Math.sin(Math.toRadians(alfa));
        x=Math.sin(Math.toRadians(90-alfa));

        m1=-y+x;
        m2=y+x;
        m3=-y-x;
        m4=y-x;

        motorStangaFata.setPower(m1*viteza2);
        motorDreaptaFata.setPower(m2*viteza2);
        motorStangaSpate.setPower(m3*viteza2);
        motorDreaptaSpate.setPower(m4*viteza2);

        milisecunde=milisecunde+System.currentTimeMillis();
        targetAngle2 = getAngle();

        if (alfa<180 && alfa>0) {
            while (System.currentTimeMillis() < milisecunde && !isStopRequested()) {
                if ((culoare.red()>(culoare.blue()+15)) && cuIntake==true) {
                    prindere(true);
                    sleep(100);
                    intake(false);
                    ridicareTicks(300);
                }

                if (getAngle() < targetAngle2 - eroare) {
                    left = left - 0.01;
                    right = right + 0.01;
                } else if (getAngle() > targetAngle2 + eroare) {
                    right = right - 0.01;
                    left = left + 0.01;
                } else {
                    right = 1;
                    left = 1;
                }

                motorStangaFata.setPower(m1 * left * viteza2);
                motorDreaptaFata.setPower(m2 * right * viteza2);
                motorStangaSpate.setPower(m3 * left * viteza2);
                motorDreaptaSpate.setPower(m4 * right * viteza2);
                telemetry.addData("mili", milisecunde);
                telemetry.addData("system time", System.currentTimeMillis());
                telemetry.addData("unghi", getAngle());
                telemetry.addData("target", targetAngle2);
                telemetry.update();
            }
        }

        else {
            while (System.currentTimeMillis() < milisecunde && !isStopRequested()) {
                if ((culoare.red()>(culoare.blue()+15)) && cuIntake==true) {
                    prindere(true);
                    sleep(100);
                    intake(false);
                    ridicareTicks(300);
                }

                if (getAngle() < targetAngle2 - eroare) {
                    left = left + 0.01;
                    right = right - 0.01;
                } else if (getAngle() > targetAngle2 + eroare) {
                    right = right + 0.01;
                    left = left - 0.01;
                } else {
                    right = 1;
                    left = 1;
                }

                motorStangaFata.setPower(m1 * left * viteza2);
                motorDreaptaFata.setPower(m2 * right * viteza2);
                motorStangaSpate.setPower(m3 * left * viteza2);
                motorDreaptaSpate.setPower(m4 * right * viteza2);
                telemetry.addData("mili", milisecunde);
                telemetry.addData("system time", System.currentTimeMillis());
                telemetry.addData("unghi", getAngle());
                telemetry.addData("target", targetAngle2);
                telemetry.update();
            }
        }

        motorDreaptaFata.setPower(0);
        motorDreaptaSpate.setPower(0);
        motorStangaFata.setPower(0);
        motorStangaSpate.setPower(0);
    }

    @Override
    public void runOpMode() {
        init_robot();

        waitForStart();

        resetAngle();

        miscare360Secunde(90,70,false);
        miscare360Secunde(0,90,false);
    }
}
