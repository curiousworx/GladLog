# GladLog

GladLog is a simple helper class implementation for DogLog developed by FRC Team #4398 - Gladiators with the following goals:

* Provide a **simple** logging example in DogLog for beginner programmers
* Log an important item (current) 
* Minimize CAN Bus and CPU usage  
* Support for CTRE devices only (TalonFX)
* Flexibility to be extended in the future 

GladLog makes the following assumptions:

* FRC project with DogLog already installed and configured to log
* Using Phoenix 6 library from CRTE

## Usage

1. Copy the GladLog class to your project
2. In your desired subsystem(s):
    1. Import the GladLog class 
    > import frc.robot.util.GladLog;
    2. In the subsystem constructor, create an instance of the GladLog class 
    > GladLog logger = GladLog.getInstance();
    3. In the subsystem constructor, register motor(s) 
    > logger.registerTalonFX("Intake/Roller", intakeMotor);
3. In Robot.java, within robotPeriodic() make a call like this 
> GladLog.getInstance().logAll();