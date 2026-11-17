package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

@Autonomous(name = "Auton")
public class Auton extends LinearOpMode {

    private DcMotor intakeMotor;
    private DcMotorEx flywheel;
    private CRServo crServo;
    private DcMotor leftDrive;
    private DcMotor rightDrive;
    private Servo pollenServo;

    private static final double TARGET_VELOCITY = 1160;   // ticks/sec
    private static final double VELOCITY_TOLERANCE = 50;  // "close enough" to fire
    private static final double SHOOT_TIME_MS = 25000;
    private static final double DRIVE_TIME_MS = 1000;
    private static final double DRIVE_POWER = 0.5;

    private boolean driveMoving = false;
    private final ElapsedTime timer = new ElapsedTime();

    @Override
    public void runOpMode() {
        intakeMotor = hardwareMap.get(DcMotor.class, "intake");
        flywheel    = hardwareMap.get(DcMotorEx.class, "flywheel");
        crServo     = hardwareMap.get(CRServo.class, "blocker");
        leftDrive   = hardwareMap.get(DcMotor.class, "left");
        rightDrive  = hardwareMap.get(DcMotor.class, "right");
        pollenServo = hardwareMap.get(Servo.class, "flower");

        flywheel.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        leftDrive.setDirection(DcMotorSimple.Direction.FORWARD);
        rightDrive.setDirection(DcMotorSimple.Direction.REVERSE);
        leftDrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rightDrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        telemetry.addLine("Ready");
        telemetry.update();
        waitForStart();
        if (isStopRequested()) return;

        pollenServo.setPosition(1);

        // 1. Spin up the flywheel and wait until it reaches speed (max 2 s)
        flywheel.setVelocity(TARGET_VELOCITY);
        timer.reset();
        while (opModeIsActive()
                && Math.abs(flywheel.getVelocity() - TARGET_VELOCITY) > VELOCITY_TOLERANCE
                && timer.milliseconds() < 2000) {
            showTelemetry("Spinning up");
        }

        // 2. Feed rings/artifacts into the flywheel
        intakeMotor.setPower(1);
        crServo.setPower(1);
        timer.reset();
        while (opModeIsActive() && timer.milliseconds() < SHOOT_TIME_MS) {
            showTelemetry("Shooting");
        }

        // 3. Stop shooting
        intakeMotor.setPower(0);
        crServo.setPower(0);
        flywheel.setVelocity(0);

        // 4. Drive forward
        leftDrive.setPower(DRIVE_POWER);
        rightDrive.setPower(DRIVE_POWER);
        driveMoving = true;
        timer.reset();
        while (opModeIsActive() && timer.milliseconds() < DRIVE_TIME_MS) {
            showTelemetry("Driving");
        }

        // 5. Stop everything
        leftDrive.setPower(0);
        rightDrive.setPower(0);
        driveMoving = false;
        showTelemetry("Done");
    }

    private void showTelemetry(String state) {
        telemetry.addData("State", state);
        telemetry.addData("Flywheel Velocity", flywheel.getVelocity());
        telemetry.addData("Is moving", driveMoving);
        telemetry.update();
    }
}
