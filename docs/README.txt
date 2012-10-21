Hi!

Glad you are taking a look at this GPS parsing library.

As you may know, GPS devices spit information using a standard:
NMEA 0183.

I'm fiddling a little bit with a usb GPS device I got attached to my computer.
Device is USGlobalSat ND-100S. You will get a sample of GPS output from it in the 
docs/samples directory.

Please, don't assume this library is ready to go in production cause it's definitely not. I still 
have a lot of things to research about NMEA 0183 parsing and how messages go along with each other.
I could be as optimistic as to say that the basics are laid down.... and that's about it.

How to use the library:

Suppose you have a file (something like /home/myuser/gps_output.txt) that has output from a
gps device. In order to process it (and for the sakes of processing given that there's no
feedback at the moment about the events found while processing) with the library, you would do something like:

GpsAnalyzer analyzer = new GpsAnalyzer(new FileReader("/home/myuser/gps_output.txt"));

And that's it. It works on its own thread so you could ask it (while processing) if it's got a 
valid reading (from last RMC processed) or what the last Valid reading is at the moment.

if (analyzer.gettingValidReadings()) {
    System.out.println("Last RMC says it's invalid");
} else {
    System.out.println("Last RMC says it's valid");
}

System.out.println("Last valid reading: " + analyzer.getLastValidReading());


If you have an actual GpsDevice, you have to use the Comm or RXTX APIs (I'm testing
with RXTX) to be able to get its inputStream and then:

new GpsAnalyzer(port.getInputStream());

Pretty straight forward.

Most of what I've implemented was based on a rather outdated document (by author's self-admission) from here:
http://www.gpsinformation.org/dale/nmea.htm

Hope the library is kind of useful.

Edmundo Carmona Antoranz
Caracas, Oct 21st 2012