import com.dd.plist.*;
import java.io.File;

public class AppBundle {
	public static File bundleExecutable(File bundle) {
		try {
			File infoPlistFile = new File(new File(bundle, "Contents"), "Info.plist");
			
			NSDictionary infoPlist = (NSDictionary)PropertyListParser.parse(infoPlistFile);
			
			String bundleExecutable = infoPlist.objectForKey("CFBundleExecutable").toString();
			
			File executableFile = new File(new File(new File(bundle, "Contents"), "MacOS"), bundleExecutable);
		
			System.out.println("Got bundle executable: ");
			System.out.println(executableFile.toString());
			
			return executableFile;
			
		} catch (Exception e) {
			System.out.println("Failed to get bundle executable.");
			return null;
		}
	}
}