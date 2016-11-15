# BufferedImageProcessing
A collection of custom java.awt.image.BufferedImage implementations with support for image processing and simplified direct access to the image's data.

In Short
--------

Conveniently manipulate images like this ...


	//load an image and convert it into an PixelBufferedImage
	//and set the center pixel to green
	PixelBufferedImage i = new PixelBufferedImage( ImageIO.read( ... ) );
	i.pixels[ i.width/2 + i.height/2 * j.scan ] = 0xFF00FF00; 
	
	//convert it to YUV color space and stretch the Y component a bit
	YUVBufferedImage j = new YUVBufferedImage( i );
	j.Y.sub(25).mul(1.1);
	
	//store it back
	ImageIO.write( j, "png", ... );


Release
-------

The base release 1.0.x corresponds to the unmodified image processing implementation, as it's extracted from legacy projects.


As usual, releases are deployed automatically to the deploy branch of this github repostory. 
To add a dependency on *BufferedImageProcessing* using maven, modify your *repositories* section to include the git based repository.

	<repositories>
	 ...
	  <repository>
	    <id>BufferedImageProcessing-Repository</id>
	    <name>BufferedImageProcessing's Git-based repo</name>
	    <url>https://raw.githubusercontent.com/Holzschneider/BufferedImageProcessing/deploy/</url>
	  </repository>
	...
	</repositories>
	
and modify your *dependencies* section to include the graphics3d dependency
 
	  <dependencies>
	  ...
	  	<dependency>
	  		<groupId>de.dualuse</groupId>
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
	  compile 'de.dualuse:BufferedImageProcessing:1.0.+'
	}

