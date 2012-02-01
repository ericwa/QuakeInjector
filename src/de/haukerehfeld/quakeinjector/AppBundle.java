import com.dd.plist.PropertyListParser;

public class AppBundle {
	AppBundle(File bundle) {
		File infoPlistFile = File(File(bundle, "Contents"), "Info.plist")
		
		NSDictionary infoPlist = (NSDictionary)PropertyListParser.parser(infoPlistFile);
		
		String bundleExecutable = infoPlist.objectForKey("CFBundleExecutable").toString();
		
		File executableFile = File(File(File(bundle, "Contents"), "MacOS"), bundleExecutable);
	
		System.out.println("Got bundle executable: ");
		System.out.println(executableFile.toString());
	}
}