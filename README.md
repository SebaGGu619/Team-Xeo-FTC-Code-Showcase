# Team-Xeo-FTC-Code-Showcase
Heya, I'm Sebi, the lead programmer of Team Xeo and I welcome you to our code Showcase!

Disclaimer: Code may not be up-to-date or working without modification as this is an archive of the code that survived developement. 

I will be updating this repo as soon as we get our hands on older (older than the Rover Ruckus Season) and more complete code (from that period) to showcase. Development is not always easy to document and sometimes code is lost along the way. Also, not all of our competition code makes it here, its usually the same thing but for the other alliance or tests that were unsuccessful.

Feel free to contact us using our contact information for any help related to FTC or other cool projects!

Legend:
1. Misc and useful code
2. Skystone Season Code (2019-2020)
3. Ultimate Goal Season Code (2020-2021)

Skystone Season:
  
  a. FrontRight - This is a simple, short program that simply parked the robot in order to not interfere with the autonomy of the other robots. This program represents the diffrent autonomous paths you need to have ready going into the competition.
  
  b. XeoTeleOp - This is our competition failsafe TeleOp code all commentated and easy to understand! It uses a simple failsafe system in case of motor failure of desynchronization. Our lifting system used two DcMotor who needed to be synchronised, if something happened to these two the whole system would fail and thus we needed the failsafe code. Left stick press stopped all automations and switched to a manual lifting function, after you realigned the motors you pressed right stick to reset the encoders to that position as 0 and reset the automations on that position. After those two you could press left stick again to re-engage the automations and thus save the system and possibly the match!
