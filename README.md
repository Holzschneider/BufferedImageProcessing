# BufferedImageProcessing
A collection of custom implementations of java.awt.image.BufferedImage with support for image processing and simplified direct access to image data.


Release
-------

The base release 1.0.0 corresponds to the unmodified image processing implementation, as it's extracted from legacy projects.


As usual, releases are deployed automatically to the deploy branch of this github repostory. 
To add a dependency on *BufferedImageProcessing* using maven, modify your *repositories* section to include the git based repository.

	<repositories>
	 ...
	  <repository>
	    <id>git-holzschneider</id>
	    <name>Holzschneider's Git based repo</name>
	    <url>https://raw.githubusercontent.com/Holzschneider/BufferedImageProcessing/deploy/</url>
	  </repository>
	...
	</repositories>
	
and modify your *dependencies* section to include the graphics3d dependency
 
	  <dependencies>
	  ...
	  	<dependency>
	  		<groupId>de.dualuse.commons</groupId>
	  		<artifactId>BufferedImageProcessing</artifactId>
	  		<version>LATEST</version>
	  	</dependency>
	  ...
	  </dependencies>


To add the repository and the dependency using gradle refer to this

	repositories {
	    maven {
	        url "https://raw.githubusercontent.com/Holzschneider/BufferedImageProcessing/deploy/"
	    }
	}

and this

	dependencies {
	  compile 'de.dualuse.commons:BufferedImageProcessing:1.0'
	}

