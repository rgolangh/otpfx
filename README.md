followme.fx
===========

A better name would be "cloneme.fx". Follow me is the simplest possible afterburner.fx
application demonstrating:

1. CSS and FXML integration
2. Integration with [http://afterburner.adam-bien.com](http://afterburner.adam-bien.com)
3. Dependency Injection of models / services
4. Maven 3 Build

Builder and install afterburner.fx first:

1. git clone https://github.com/AdamBien/afterburner.fx
2. cd afterburner.fx
3. mvn

Then build followme.fx

1. git clone https://github.com/AdamBien/followme.fx
2. cd followme.fx
3. mvn

See also: [http://afterburner.adam-bien.com](http://afterburner.adam-bien.com)
=======
# otpfx


An otp UI written using javafx, supports TOTP and HOTP.

# Prerequisit
- openjdk 8
- openjfx

To get the pre-requisits
```
sudo dnf install java-1.8.0-openjdk java-1.8.0-openjdk-openjfx
```

# Run it
```
java -jar otpfx.jar
```

# Compile it
```
mvn clean package
```
