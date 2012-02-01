package de.haukerehfeld.quakeinjector;

import com.dd.plist.*;
import java.io.File;
import de.haukerehfeld.quakeinjector.QuakeInjector;

public class Application {
	
	public static File applicationExecutable(File applicationFile) {
		if (!QuakeInjector.isMacOSX()) {
			return applicationFile;
		} else {
			if (!applicationFile.getName().endsWith(".app"))
			{
				return null;
			}
			
			try {
				File infoPlistFile = new File(new File(applicationFile, "Contents"), "Info.plist");
				
				NSDictionary infoPlist = (NSDictionary)PropertyListParser.parse(infoPlistFile);
				
				String bundleExecutable = infoPlist.objectForKey("CFBundleExecutable").toString();
				
				File executableFile = new File(new File(new File(applicationFile, "Contents"), "MacOS"), bundleExecutable);
			
				System.out.println("Got bundle executable: ");
				System.out.println(executableFile.toString());
				
				return executableFile;
				
			} catch (Exception e) {
				System.out.println("Failed to get bundle executable.");
				return null;
			}
		}
	}
	
	public static boolean isValidApplication(File applicationFile) {
		File exe = applicationExecutable(applicationFile);
		
		return exe != null
			&& !exe.isDirectory()
			&& exe.canRead()
			&& exe.canExecute();
	}
}