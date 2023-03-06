// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.PowerDistribution;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.PowerDistribution.ModuleType;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 * The VM is configured to automatically run this class, and to call the functions corresponding to
 * each mode, as described in the TimedRobot documentation. If you change the name of this class or
 * the package after creating this project, you must also update the build.gradle file in the
 * project.
 */

  public class Robot extends TimedRobot {
  private static final String kDefaultAuto = "Default";
  private static final String kCustomAuto = "My Auto";
  private String m_autoSelected;
  private final SendableChooser<String> m_chooser = new SendableChooser<>();

  boolean elManualMode = true;
  boolean zeroed = false;
  Timer autoTimer = new Timer();
  Timer intakeTimer = new Timer();
  Timer balanceTimer = new Timer();

  Integer Abutton = 1;
  Integer Bbutton = 2;
  Integer Xbutton = 3;
  Integer Ybutton = 4;
  Integer Lbumper = 5;
  Integer Rbumper = 6; 
  Integer BackButton = 7;
  Integer StartButton = 8;
  Integer LStickClick = 9;
  Integer RStickClick = 10;
  Integer LStickLeftRightAxis = 0;
  Integer LStickFwdBackAxis = 1; 
  Integer LtriggerAxis = 2;
  Integer RtriggerAxis = 3;
  Integer RStickLeftRightAxis = 4;
  Integer RStickFwdBackAxis = 5;

  double TargetYaw = 0;

  //Joystick
  Joystick stick = new Joystick(0);
  Joystick stick2 = new Joystick(1);

  PowerDistribution PDThing = new PowerDistribution(1,ModuleType.kCTRE);

   //Charge station autonomous
   private static final String kTokyoDrift = "Tokyo Drift";
   private static final String kHitchRoute = "Hitch Route";
   private static final String kFadeAway = "Fade Away";
 
   //Scoring autonomous
   private static final String kFirstScore = "First Score";
   private static final String kSecondScore = "Second Score";
   private static final String kThirdScore = "Third Score"; 

   //Presets for cone scoring
   private static final String kHigh = "High";
   private static final String kMid = "Mid";
   private static final String kLow = "Low";

   private String score_preset_selected;
   private final SendableChooser<String> score_preset_chooser = new SendableChooser<>();

   private DigitalInput zeroedbutton = new DigitalInput(3);
   boolean buttonpressed = false;

   double startingYAW;

  //5464-created classes!
  Drivetrain drivetrain = new Drivetrain();
  Elevator elevator = new Elevator();
  Gyro gyro = new Gyro();
  Leds Leds = new Leds();
  Vacuum intake = new Vacuum();
  Vision vision = new Vision();  
  Pneumatics pneumatics = new Pneumatics();

  /**
   * This function is run when the robot is first started up and should be used for any
   * initialization code.
   */
  @Override
  public void robotInit() {
    m_chooser.setDefaultOption("Default Auto", kDefaultAuto);
    m_chooser.addOption("My Auto", kCustomAuto);
    SmartDashboard.putData("Auto choices", m_chooser);

    elevator.Init();
    Leds.Init();
    drivetrain.Init();
    pneumatics.Init();
    vision.Init();
    intake.Init();

    m_chooser.setDefaultOption("Default Auto", kDefaultAuto);

    m_chooser.addOption("Tokyo Drift", kTokyoDrift);
    m_chooser.addOption("Hitch Route", kHitchRoute);
    m_chooser.addOption("Fade Away", kFadeAway);
    m_chooser.addOption("First Score", kFirstScore);
    m_chooser.addOption("Second Score", kSecondScore);
    m_chooser.addOption("Third Score", kThirdScore);

    SmartDashboard.putData("Auto choices", m_chooser);

    score_preset_chooser.setDefaultOption("High", kHigh);
    score_preset_chooser.addOption("Mid", kMid);
    score_preset_chooser.addOption("Low", kLow);
    SmartDashboard.putData("Score Preset Choices", score_preset_chooser);
    Leds.QuestionError();
  }

  /**
   * This function is called every 20 ms, no matter the mode. Use this for items like diagnostics
   * that you want ran during disabled, autonomous, teleoperated and test.
   *
   * <p>This runs after the mode specific periodic functions, but before LiveWindow and
   * SmartDashboard integrated updating.
   */
  @Override
  public void robotPeriodic() {
    drivetrain.DisplayStats();
    elevator.PeriodicTasks();
    gyro.DisplayStats();
    gyro.UpdateGyro();
    vision.DisplayStats();
    intake.DisplayStats();
    pneumatics.DisplayPressure();
    vision.ReturnBestTargetXY();
    intake.DistanceCheck();
    
    SmartDashboard.putNumber("autoStep", autoStep);
    SmartDashboard.putNumber("autoTimer", autoTimer.get());
    SmartDashboard.putNumber("intakeTimer", intakeTimer.get());
    SmartDashboard.putNumber("balanceTimer", balanceTimer.get());

    // This button switches between manual winch/extender control and automatic.
    if(stick2.getRawButtonPressed(StartButton)){
      if(elManualMode){
        System.out.println("We're in automatic mode!");
        if(zeroed){Leds.Pidmode();}
        else{Leds.QuestionError();}
        elManualMode = false;       
      }
      else{
        if(zeroed){Leds.Manualmode();}        
        System.out.println("We're in manual mode!");
        elManualMode = true;
      }
    }

  }

  // This is step 0 in 'Tokyo Drift' subroutine!
  // Angles the bot so it can score
  public void scorePrep(){

    // The X value we will be homing to
    double targetX = 5;

    // INCREASE this value to make us home faster, but possibly less stable. Decrease if overshooting.
    double divisor = 50;

    // How wide of a range are we going to be looking for when homing? DECREASE to look for smaller window.
    double window = 3;

    // The value we are fetching from Photonvision
    double xcord = vision.USBcamerax;

    drivetrain.Move(0, 0, (vision.USBcamerax-targetX)/divisor); 

    if ((xcord < (targetX + window)) && (xcord > (targetX - window))){
      autoStep++;
      autoTimer.start();
    }
  }

  public void EscapePrep(){
    // What exact X value are we trying to home in on?

    // The X value we will be homing to
    double targetX = 23;

    // INCREASE this value to make us home faster, but possibly less stable. Decrease if overshooting.
    double divisor = 50;

    // How wide of a range are we going to be looking for when homing? DECREASE to look for smaller window.
    double window = 3;

    // The value we are fetching from Photonvision
    double xcord = vision.USBcamerax;

    drivetrain.Move(0, 0, (vision.USBcamerax-targetX)/divisor); 

    if ((xcord < (targetX + window)) && (xcord > (targetX - window))){
      autoStep++;
    }
  }
 
  public void sConeEl(){

    switch (score_preset_selected) {
      
      case kHigh:
        elevator.setElevatorPosition("ScoreHighCone");
        autoStep++;
        break;

      case kMid:
        elevator.setElevatorPosition("ScoreHScoreMidCone");
        autoStep++;
        break;

      case kLow:
        elevator.setElevatorPosition("ScoreLowCone");
         autoStep++;
        break;
    }    

  }

  public void Score(){  
    // TODO: HIGH: Verify that this turns on/off intake correctly. Adjust timer if needed.
    
    // after shortly running the intake, then move on.
    if(autoTimer.get() > 2){
      intake.stoprun();
      autoStep++;
    }
    
    // run the intake (a few seconds into auto) to spit out the cone
    else if(autoTimer.get() > 1){
      intake.outrun();      
    }
  }
  
  public void TokyoEscape(){
    drivetrain.Move(-0.5,0 , 0);
    System.out.println(drivetrain.frontleftrotations);
    if(drivetrain.frontleftrotations < -58.0){
      elevator.setElevatorPosition("AprilTagEncoder");
      TargetYaw = gyro.Yaw;
      autoStep++;
    }
  }

  public void HitchEscape(){   // This one is different than the rest
    drivetrain.Move(-0.5, 0, 0);
    //TODO: find encoder values
      if(drivetrain.frontleftrotations > 0){
        autoStep++;
      }
    
 }

  public void FadeEscape(){
    drivetrain.Move(0.5,0 , 0);
    if(drivetrain.frontleftrotations < -58.0){
      elevator.setElevatorPosition("AprilTagEncoder");
      TargetYaw = gyro.Yaw;
      autoStep++;
    }
  }

  public void FirstEscape(){
    drivetrain.Move(-0.5, 0, 0);
    if(drivetrain.frontleftrotations < -58.0){
      TargetYaw = gyro.Yaw;
      elevator.setElevatorPosition("Drive");
      autoStep++;
    }
  }

  public void SecondEscape(){
    drivetrain.Move(-0.5, 0, 0);
    if(drivetrain.frontleftrotations < -58.0){
      TargetYaw = gyro.Yaw;
      elevator.setElevatorPosition("Drive");
      autoStep++;
    }
  }

  public void ThirdEscape(){
    drivetrain.Move(-0.5, 0, 0);
    if(drivetrain.frontleftrotations < -58.0){
      TargetYaw = gyro.Yaw;
      elevator.setElevatorPosition("Drive");
      autoStep++;
    }
  }

  public void Spin180Gyro(){
    //Gyro will preform a 180
    //TODO: varify degree and roll direction
  drivetrain.Move(0, 0, 0.5);
    if(gyro.Yaw > 180){
      autoStep++;
    }
  }

  public void ConeDetect(){
    //TODO: find distance from a cone
    if(intake.dist > 0){ 
    intake.inrun();
    elevator.setElevatorPosition("ScoreLowConeCube"); 
    intakeTimer.reset();
    intakeTimer.start();
    autoStep++;
    }
  }

  public void IntakeRun(){
    //TODO: varify time
    if(intakeTimer.get() < 2){
      IntakeRun();
    }
    else if(intakeTimer.get() > 2){
      intake.stoprun();
      intakeTimer.stop();
      autoStep++;
    }
  }

  public void FirstFormation(){

  }

  public void SecondFormation(){

  }

  public void ThirdFormation(){

  }

  public void TokyoDrift(){
    drivetrain.Move(0, -0.5, (startingYAW-gyro.Yaw)/100);
    System.out.println(drivetrain.frontleftrotations);
    if(drivetrain.frontleftrotations < -133){  
      autoStep++;
    }
  }
  
  public void FadeDrift(){
    drivetrain.Move(0, 0.5, (startingYAW-gyro.Yaw)/100);
    //TODO: find encoder values
      if(drivetrain.frontleftrotations > 0){ 
        autoStep++;
      }
  }

  public void Arrival(){
    // This is currently instantly skipped because tag7x/y registers 0,0.
    // TODO: low: Get tag 7 to actually read out with the vision system, or switch this to Gyro lineup.

    double x = vision.tag7x;
    double y = vision.tag7y;

    if((x < 2) && (x > -2) && (y < 2) && (y > -2)){
      elevator.setElevatorPosition("Drive");
      autoStep++;
    }

    
  }

  public void Gunit(){
    drivetrain.Move(0.7, 0 , 0);
    System.out.println(drivetrain.frontleftrotations);
    if(drivetrain.frontleftrotations > -54){
      autoStep++;
      balanceTimer.stop();
      balanceTimer.reset();
      balanceTimer.start();
    }
  }

  public void Balance(){
    //When pitch ~ 0 then stop
    if((gyro.Pitch < 1)&&(gyro.Pitch > -1)){
      drivetrain.Move(0,0 ,0 );
      if( balanceTimer.get() > 2.0){
        autoStep++;
        balanceTimer.stop();
       }
    }
    
    if(gyro.Pitch < 0){
      drivetrain.Move(0.2,0 ,0 );
      balanceTimer.reset();
    }
      
    if(gyro.Pitch > 0){
      drivetrain.Move(-0.2,0 ,0 );
      balanceTimer.reset();
    }
  }

  public void Generic_Backup(){
    drivetrain.Move(-0.5, 0, 0);
      if(drivetrain.frontleftrotations < -58.0){
        TargetYaw = gyro.Yaw;
        autoStep++;
      }
  }

  // This autonomous routine is for a start in front of a cone-scoring post
  // It scores a cone, then zooms around the charging station
  // It then drives us onto the charging station, keeping us there with a gyro/brake

  // This autonomous routine starts in the right position, scores a cone,
  // backs up past the charge station, strafes right, and drives back on it
  public void AutoTokyoDrift(){
    switch(autoStep){
      case 0:
        sConeEl();
        break;
      case 1:
        scorePrep();
        break;
      case 2:
        Score();
        break;
      case 3:
        EscapePrep();
        break;
      case 4:
        TokyoEscape();
        break;
      case 5:
        TokyoDrift();
        break;
      case 6: 
        Arrival();
        break;
      case 7:
        Gunit();
        break;
      case 8:
        Balance();
        break;
      case 9:
        break;
      default:
        drivetrain.Move(0, 0, 0);
    }
  }



  // This autonomous routine starts anywhere in front of a cone scoring location
  // It drives forward, scores, backs out of community
  // TODO: HIGH: Finish Default auto steps. Once this works, Tokyo Drift will have a more solid start.
  public void AutoDefault(){
    switch(autoStep){
      case 0:
        scorePrep();
      break;

      case 1:
        sConeEl();
      break;

      case 2:
        Score();
      break;

      case 3:
        Generic_Backup();
      break;

      case 8:
        break;
    }
  }

  /**
   * This autonomous (along with the chooser code above) shows how to select between different
   * autonomous modes using the dashboard. The sendable chooser code works with the Java
   * SmartDashboard. If you prefer the LabVIEW Dashboard, remove all of the chooser code and
   * uncomment the getString line to get the auto name from the text box below the Gyro
   *
   * <p>You can add additional auto modes by adding additional comparisons to the switch structure
   * below with additional strings. If using the SendableChooser make sure to add them to the
   * chooser code above as well.
   */
  public int autoStep=0;

  /**
   * This autonomous (along with the chooser code above) shows how to select between different
   * autonomous modes using the dashboard. The sendable chooser code works with the Java
   * SmartDashboard. If you prefer the LabVIEW Dashboard, remove all of the chooser code and
   * uncomment the getString line to get the auto name from the text box below the Gyro
   *
   * <p>You can add additional auto modes by adding additional comparisons to the switch structure
   * below with additional strings. If using the SendableChooser make sure to add them to the
   * chooser code above as well.
   */
  @Override
  public void autonomousInit() {
    m_autoSelected = m_chooser.getSelected();
    // m_autoSelected = SmartDashboard.getString("Auto Selector", kDefaultAuto);
    System.out.println("Auto selected: " + m_autoSelected);
    m_autoSelected = m_chooser.getSelected();
    score_preset_selected = score_preset_chooser.getSelected(); 

    // m_autoSelected = SmartDashboard.getString("Auto Selector", kDefaultAuto);
    System.out.println("Auto selected: " + m_autoSelected);
    System.out.println("Preset selected: " + score_preset_selected);

    autoTimer.stop();
    autoTimer.reset();

    intakeTimer.stop();
    intakeTimer.reset();

    balanceTimer.stop();
    balanceTimer.reset();

    startingYAW = gyro.Yaw;
  
    autoStep = 0;

    drivetrain.DriveEncodersZeroed();
  }

    /** This function is called periodically during autonomous. */
    @Override
    public void autonomousPeriodic() {
      elevator.pidHoming();
      
      System.out.println(autoStep);
      
      switch (m_autoSelected) {
        
        case kTokyoDrift:
          // If we select 'Tokyo Drift' on Drivers' station, it will run this function!
          AutoTokyoDrift();     
          break;
  
        // case kHitchRoute:
        //   AutoHitchRoute();
        //   break;
  
        // case kFadeAway:
        //   AutoFadeAway();
        //   break;
  
        // case kFirstScore:
        //   AutoFirstScore();
        // break;
  
        // case kSecondScore:
        //   AutoSecondScore();
        // break;
  
        // case kThirdScore:
        //   AutoThirdScore();
        // break;
  
        // case kDefaultAuto:
        //   AutoDefault();
        //   break;
          
        default:
          AutoDefault();
          break;
      }

      SmartDashboard.putNumber("autoSep", autoStep);
    }

  /** This function is called once when teleop is enabled. */
  @Override
  public void teleopInit() {
    elevator.setElevatorToBrake();
    elevator.Init();
    pneumatics.Init();
    drivetrain.DriveEncodersZeroed();
  }

  public void stickControlDrivetrain(){
    // These are the default values of the drivetrain from the Driver controller
    double speed     =  0.8;
    double fwdBack   = -stick.getRawAxis(LStickFwdBackAxis);
    double leftRight =  stick.getRawAxis(LStickLeftRightAxis);
    double rotate    =  stick.getRawAxis(RStickLeftRightAxis);
    
    // Base rotation value off of Apriltag vision
    if(stick.getRawAxis(LtriggerAxis) > 0.1){      
      vision.setUsbPipelineIndex(1);  
      rotate = vision.Picamerax/120; 
    }

    // Slow down while picking up stuff
    else if(stick2.getRawButton(Rbumper)){         
      fwdBack = fwdBack * 0.18;
      leftRight = leftRight * 0.18;
      rotate = rotate * 0.18; 
    }

    // Charge station climb overrides turning vals to be perfectly lined up
    else if(stick.getRawAxis(RtriggerAxis) > 0.1){  
      speed = 1.0;
      rotate = drivetrain.SnapToAngle(gyro.Yaw, 0);
    }

    // Base rotation value off of Reflective vision
    else if(stick.getRawButton(Xbutton)){
        vision.setUsbPipelineIndex(0); 
        rotate = vision.USBcamerax*intake.intdist/120; //intdist multiplied? 
     }

    // Pi-camera lineup (untested)
    else if(stick.getRawButton(Ybutton)){ 
        rotate = vision.Picamerax/120; 
    }

    // use the updated values to move
    drivetrain.Move(fwdBack * speed, rotate, leftRight * speed);

    //drivetrain.MoveFieldOriented(fwdBack * speed, rotate, leftRight * speed, gyro.Angle);
  }

  public void stickControlManualElevator(){
    if(stick2.getRawButton(Xbutton)){
      elevator.Extend();
    }
    else if(stick2.getRawButton(Ybutton)){
      elevator.Retract();
    }
    else{
      elevator.Shutdown();
    }     
    if(stick2.getRawButton(Abutton)){
      elevator.jogWinch(0.7);
    }
    else if(stick2.getRawButton(Bbutton)){
      elevator.jogWinch(-0.7);
    }
    else{
      elevator.jogWinch(0);
    }   
  }

  public void stickControlPidHoming(){
    if(stick2.getRawButtonPressed(BackButton)){
      Leds.PickCone();
    }
      else {
        Leds.PickCube();
      }

    // Elevator encoding homing
    if(stick2.getRawButtonPressed(LStickClick)){
      elevator.setElevatorPosition("Drive");
    }

    if(stick2.getRawButtonPressed(RtriggerAxis)){
      elevator.setElevatorPosition("ConePickupHigh");
      Leds.PickCone();
    }

    if(stick2.getRawButtonPressed(Abutton)){
      elevator.setElevatorPosition("ConeCubePickupLow");
      Leds.PickCone();
    }
    
    if(stick2.getRawButtonPressed(Ybutton)){
      elevator.setElevatorPosition("ScoreHighCone");
      Leds.PickCone();
    }

    if(stick2.getRawButtonPressed(Bbutton)){
      elevator.setElevatorPosition("ScoreMidCone");
      Leds.PickCone();
    }

    if(stick2.getPOV() == 180){ //TODO: does set to the encoder probably do to an issue with the POV
      elevator.setElevatorPosition("CubePickupHigh");
      Leds.PickCube();
    }

    if(stick2.getPOV() == 0){
      elevator.setElevatorPosition("ScoreHighCube");
      Leds.PickCube();
    }

    if(stick2.getPOV() == 90){
      elevator.setElevatorPosition("ScoreMidCube");
      Leds.PickCube();
    }

    if(stick2.getPOV() == 270){ //TODO: does set to the encoder probably do to an issue with the POV
      elevator.setElevatorPosition("ConePickupLowforHighScore");
      Leds.PickCone();
    }

    if(stick2.getRawButtonPressed(RStickClick)){
      elevator.setElevatorPosition("ScoreLowCone/Cube");
      Leds.HybridPickConeCube();

    // LOWER THE INTAKE ONTO A CONE
    if(stick.getRawButtonPressed(Xbutton)){
      elevator.setElevatorPosition("Winch-5");
      }
    }

    // holds the elevator according to an auto-control scheme that is not as cool as PID
    elevator.pidHoming();
  }

  /** This function is called periodically during operator control. */
  @Override
  public void teleopPeriodic() {
    
    stickControlDrivetrain();
    
    if(elManualMode){
      stickControlManualElevator();
    }
    else{
      stickControlPidHoming();
    }

    // THIS INTAKES A CONE OR SPITS OUT A CUBE
    if(stick2.getRawButton(Rbumper)){
      intake.inrun();
    }
    // THIS INTAKES A CUBE OR SPITS OUT A CONE
    else if(stick2.getRawButton(Lbumper)){
      intake.outrun();
    }
    else{
      intake.stoprun();
    }   

    // ENABLE OR DISABLE DA GREEN LIGHT
    if(stick.getRawButtonPressed(BackButton)){
      PDThing.setSwitchableChannel(true);
    }
    else{
      PDThing.setSwitchableChannel(false);
    }

    // TURN ON OR OFF THE COMPRESSOR
    if(stick.getRawButtonPressed(LStickClick)){
      pneumatics.CompOnOffOn();
    }

    // RESET GYROSCOPE FOR FIELD-ORIENTED DRIVE
    if(stick.getRawButtonPressed(Lbumper)){
      gyro.ResetGyro();
    }

    // ENGAGE BRAKE MODE
    if(stick.getRawButtonPressed(Rbumper)){ 
      pneumatics.SolBreak();
    }

  }

  /** This function is called once when the robot is disabled. */
  @Override
  public void disabledInit() {
    elevator.setElevatorToCoast();
    autoTimer.stop();
    intakeTimer.stop();
    balanceTimer.stop();
  }

  /** This function is called periodically when disabled. */
  @Override
  public void disabledPeriodic(){
    
    if(zeroedbutton.get()){
      zeroed = elevator.zeroRotations();

      if(elManualMode){
        Leds.Manualmode();
      }
      else{ 
        Leds.Pidmode();
      }
    }
  } 

  /** This function is called once when test mode is enabled. */
  @Override
  public void testInit() {}

  /** This function is called periodically during test mode. */
  @Override
  public void testPeriodic() {}

  /** This function is called once when the robot is first started up. */
  @Override
  public void simulationInit() {}

  /** This function is called periodically whilst in simulation. */
  @Override
  public void simulationPeriodic() {}
  }