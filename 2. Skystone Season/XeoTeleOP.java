/**
 * Created by B. Sebastian with help from N. Rares
 */

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;

import java.util.*;

@TeleOp(name="XeoTeleOP",group="TeleOp")
public class XeoTeleOP extends OpMode {
    //Creating DCMotor objects
    private DcMotor ridicareStanga;   private DcMotor ridicareDreapta;
    private DcMotor intakeStanga;     private DcMotor intakeDreapta;
    private DcMotor motorStangaFata;  private DcMotor motorStangaSpate;
    private DcMotor motorDreaptaFata; private DcMotor motorDreaptaSpate;

    //Creating Servo and CRservo objects
    private CRServo extindereServo;
    private Servo capstoneServo;//                      open:  --- |closed:  ---
    private Servo prindere1;//                          open:  0.9 |closed:  0.5
    private Servo prindere2;//                          open:  --- |closed:  ---
    private Servo platformaStanga;//                    open:  0.1 |closed:  1.0
    private Servo platformaDreapta;//                   open:  0.2 |closed:  1.0

    //Cerator sensor objects
    private com.qualcomm.robotcore.hardware.TouchSensor atingere;
    private com.qualcomm.robotcore.hardware.ColorSensor culoare;

    //Global variables for button logic and other functions
    private boolean modRulareTeleop = true; //if true then its running in semi-autonomous mode
    private boolean modRulareTeleOpLogica = false; //button logic (double press prevention)

    private boolean platformaLogica=false;//button logic
    private boolean platformaToggle=true;

    private boolean capstoneToggle=true;//button logic
    private boolean capstoneLogica=false;

    private boolean prindereToggle=false;//button logic
    private boolean prindereLogica=false;

    private boolean retragereExtindere=false;//for extender arm retraction automation

    private boolean stareCurenta;
    private boolean stareFosta;

    private int inaltimeStone=1;//knows the last height a stone was placed on the tower.
    //measured in stone heights, 3 meaning a tower made of 3 stones one on top of another.

    private int targetPosition=0;//targetPosition for main lifting system.

    private long targetMili;
    private int extindereLogica=0;

    //maps the objects to the respective servo and DcMotor controllers
    //sets the initial poz of the servos
    //starts the encoders and calibrates them
    //starts the main lifting system motors
    @Override
    public void init() {
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

        //sensor
        atingere=hardwareMap.touchSensor.get("touchSensor");
        culoare=hardwareMap.colorSensor.get("colorSensor");

        //initial servo pos
        capstoneServo.setPosition(0);
        platformaStanga.setPosition(1);
        platformaDreapta.setPosition(1);
        prindere1.setPosition(0.78);
        prindere2.setPosition(0.22);

        //encodere startup
        motorDreaptaFata.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        motorDreaptaFata.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        motorDreaptaFata.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        motorDreaptaSpate.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        motorDreaptaSpate.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        motorDreaptaSpate.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        motorStangaFata.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        motorStangaFata.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        motorStangaFata.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        motorStangaSpate.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        motorStangaSpate.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        motorStangaSpate.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        ridicareDreapta.setTargetPosition(0);
        ridicareDreapta.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        ridicareDreapta.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        ridicareDreapta.setMode(DcMotor.RunMode.RUN_TO_POSITION);

        ridicareStanga.setTargetPosition(0);
        ridicareStanga.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        ridicareStanga.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        ridicareStanga.setMode(DcMotor.RunMode.RUN_TO_POSITION);

        //starting lifting system motors
        ridicareStanga.setPower(1);
        ridicareDreapta.setPower(1);

        inaltimeStone=1;
        telemetry.addData("Status","Intialized");
        telemetry.update();
    }

    //translates and rotates the robot. can also do both at the same time.
    //simple controller input.
    private void miscare() {
        float m1,m2,m3,m4;
        double a;
        a=0.5;
        m2=-gamepad1.left_stick_y+gamepad1.left_stick_x-gamepad1.right_stick_x;
        m1=gamepad1.left_stick_y+gamepad1.left_stick_x-gamepad1.right_stick_x;
        m4=-gamepad1.left_stick_y-gamepad1.left_stick_x-gamepad1.right_stick_x;
        m3=gamepad1.left_stick_y-gamepad1.left_stick_x-gamepad1.right_stick_x;

        //this line modifies the variable 'a' using the triggers in order to finely tune the speed
        //of the robot.
        a = a * (gamepad1.right_trigger + 1) / (gamepad1.left_trigger + 1);

        motorStangaFata.setPower(m1*a);
        motorDreaptaFata.setPower(m2*a);
        motorStangaSpate.setPower(m3*a);
        motorDreaptaSpate.setPower(m4*a);
    }

    //simply turns the on and off the intake using the triggers of the second driver
    private void intake() {
        if (gamepad2.left_trigger>0.5) {
            intakeDreapta.setPower(1);
            intakeStanga.setPower(-1);
        }
        else if (gamepad2.right_trigger>0.5) {
            intakeDreapta.setPower(-1);
            intakeStanga.setPower(1);
        }
        else {
            intakeStanga.setPower(0);
            intakeDreapta.setPower(0);
        }
    }

    //button logic for the platform grabber, first driver right bumper opens and closes the platfomrm
    //grabber
    private void platforma() {
        //logica toggle
        if (!gamepad1.right_bumper && platformaLogica) {
            if (platformaToggle) {
                platformaToggle = false;
            }
            else
                platformaToggle = true;
        }
        if (gamepad1.right_bumper) {
            platformaLogica=true;
        }
        else {
            platformaLogica=false;
        }

        if (!platformaToggle) {
            platformaDreapta.setPosition(0.2);
            platformaStanga.setPosition(0.1);
        }
        else {
            platformaDreapta.setPosition(1);
            platformaStanga.setPosition(1);
        }
    }

    //button logic for the capstone holder, second driver 'y' button drops the capstone
    private void capStone() {
        //button logic
        if (!gamepad2.y && capstoneLogica) {
            if (capstoneToggle) {
                capstoneToggle = false;
            }
            else
                capstoneToggle = true;
        }
        if (gamepad2.y) {
            capstoneLogica=true;
        }
        else {
            capstoneLogica=false;
        }

        if (!capstoneToggle) {
            capstoneServo.setPosition(1);
        }
        else {
            capstoneServo.setPosition(0);
        }
    }

    //button logic for the stone grabber, second driver 'x' button closes and opens the grabbers
    private void prindereManual() {
        //button logic
        if (!gamepad2.x && prindereLogica) {
            if (prindereToggle) {
                prindereToggle = false;
            }
            else
                prindereToggle = true;
        }
        if (gamepad2.x) {
            prindereLogica=true;
        }
        else {
            prindereLogica=false;
        }

        if (!prindereToggle) {
            prindere1.setPosition(0.78);
            prindere2.setPosition(0.22);
        }
        else {
            prindere1.setPosition(1);
            prindere2.setPosition(0);
        }
    }

    //the main lifting system is moved using 2 DcMotors who have to be in perfect sync.
    //this creates the problem that if one of the motors fails or gets stuck then the system could
    //break and create safety issues. (shattering and throwing metal pieces everywhere)
    //i created a simple failsafe system to correct in case of something like this.
    //we have two safety controls that the drivers can use to reset or save the lifting system.
    //one is on the left stick button and one on the right stick button.
    //pressing the left stick switches from semi-automated to fully manual mode, disables the
    //lifting system encoders and switches to the 'ridicareFailsafe' function which allows you
    //to move the lifting system manually. once you move the system back to the initial position
    //you press the right stick to reset the encoders to 0 on that position and
    //reset the automations. after the right stick press you press left stick again to
    //re-engage the automations.

    //this is the lifting system fallback function
    private void ridicareFailsafe() {
        ridicareDreapta.setPower(-gamepad2.left_stick_y);
        ridicareStanga.setPower(-gamepad2.left_stick_y);
    }

    //main automated failsafe lifting function
    private void ridicareAutomatizat() {
        //modifing the targetPos variable using the controller
        targetPosition = targetPosition - (int)(gamepad2.left_stick_y * 60);
        targetPosition = targetPosition + (int)(gamepad2.left_stick_x * 200);//x axis moves faster for better control

        //software limit so the system dosen't get over extended or retracted
        if (modRulareTeleop==true) {
            if (targetPosition < (0 - pozitieEncodere.pozitieEncodereRidicareDreapta)) {
                targetPosition = 0 - pozitieEncodere.pozitieEncodereRidicareDreapta;
            }
            if (targetPosition > 4800) {
                targetPosition = 4800;
            }
        }

        //closes the stone grabbers automatically if it detects the stone has fully entered the robot
        if (culoare.red()>(culoare.blue()+15)) {
            if (ridicareDreapta.getCurrentPosition()<100) {
                prindere1.setPosition(1);
                prindere2.setPosition(0);
                prindereToggle = true;
            }
        }

        //pressing driver two button a automatically alligns the lifting and extension systems to
        //receive another stone, also opens to grabbers so they dont hit the stone while retracting
        if (gamepad2.a) {
            prindere1.setPosition(0.78);
            prindere2.setPosition(0.22);
            prindereToggle=false;
            targetPosition=0;
            retragereExtindere=true;
        }

        //pressing driver two dpad_up will lift the system to the neccesay height to place the next
        //stone, remembers last height it got up to.
        stareCurenta=gamepad2.dpad_up;
        if (!stareCurenta && stareFosta) {
            targetPosition=200+inaltimeStone*630;
            inaltimeStone++;
        }
        stareFosta=stareCurenta;

        //driver two dpad_down, reset the remembers height to 0 in case the tower falls over :-(
        if (gamepad2.dpad_down) {
            inaltimeStone=1;
        }

        //third, smaller failsafe function. driver two left_bumper
        //stops the lifting system no matter where it is.
        //this is use in case the driver accidentally gives an erroneous command and wishes to stop
        //the lifting system
        if (gamepad2.left_bumper) {
            ridicareStanga.setTargetPosition(ridicareStanga.getCurrentPosition());
            ridicareDreapta.setTargetPosition(ridicareDreapta.getCurrentPosition());
            targetPosition=ridicareDreapta.getCurrentPosition();
        }

        //driver two dpad_right, extends extension system automatically
        if (gamepad2.dpad_right) {
            targetMili=System.currentTimeMillis()+1500;
            extindereLogica=1;
        }

        //driver two dpad_left, retracts extension system automatically
        if (gamepad2.dpad_left) {
            targetMili=System.currentTimeMillis()+1500;
            extindereLogica=2;
        }

        //driver two right_bumper, extension system failsafe function, if pressed stops the
        //extension system where it currently is.
        if (gamepad2.right_bumper) {
            targetMili=System.currentTimeMillis();
            extindereLogica=0;
        }

        //extension system logic
        if (extindereLogica==1 && System.currentTimeMillis()<targetMili) {
            extindereServo.setPower(1);
        }
        else if (extindereLogica==2 && System.currentTimeMillis()<targetMili) {
            extindereServo.setPower(-1);
        }
        else {
            extindereServo.setPower(-gamepad2.right_stick_y);
        }

        //sets the target positions to the motor controllers
        ridicareDreapta.setTargetPosition(targetPosition);
        ridicareStanga.setTargetPosition(targetPosition);
    }

    @Override
    public void loop() {
        miscare();
        intake();
        platforma();
        prindereManual();
        capStone();
        ridicareAutomatizat();

        //failsafe switching code that we talked about at line 247.
        if (!gamepad2.left_stick_button && modRulareTeleOpLogica==true && modRulareTeleop==true) {
            modRulareTeleop=false;
        }
        else if (!gamepad2.left_stick_button && modRulareTeleOpLogica==true && modRulareTeleop==false) {
            modRulareTeleop=true;
        }
        modRulareTeleOpLogica=gamepad2.left_stick_button;

        //failsafe encoder and automation reset code that was discussed at line 247.
        if (gamepad2.right_stick_button) {
            ridicareStanga.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
            ridicareDreapta.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

            ridicareDreapta.setTargetPosition(0);
            ridicareStanga.setTargetPosition(0);
            ridicareStanga.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            ridicareDreapta.setMode(DcMotor.RunMode.RUN_TO_POSITION);

            targetPosition=0;
        }

        telemetry.addData("Level",inaltimeStone);
        telemetry.addData("ModeOfOperation",modRulareTeleop);
        telemetry.addData("Status","Running");
        telemetry.update();
    }
}
