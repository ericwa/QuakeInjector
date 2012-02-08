/*
Copyright 2009 Hauke Rehfeld


This file is part of QuakeInjector.

QuakeInjector is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

QuakeInjector is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with QuakeInjector.  If not, see <http://www.gnu.org/licenses/>.
*/
package de.haukerehfeld.quakeinjector;

//import java.awt.*;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import org.jdesktop.swingworker.SwingWorker;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import de.haukerehfeld.quakeinjector.gui.ProgressPopup;
import de.haukerehfeld.quakeinjector.packagelist.model.PackageListModel;

import de.haukerehfeld.quakeinjector.database.InstalledMapsParser;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class QuakeInjector extends JFrame {
	/**
	 * Window title
	 */
	private static final String ICON_URL = "/Inject2_SIZE.png";
	private static final String ICON_SIZE_PLACEHOLDER = "SIZE";
	private static final int[] ICON_SIZES = { 16, 32, 48, 256 };
	
	private static final String applicationName = "Quake Injector";
	private static final int minWidth = 300;
	private static final int minHeight = 300;

	private final static String installedMapsFileName = "installedMaps.xml";
	private final static File installedMapsFile = new File(appDataDirectory(), installedMapsFileName);
	private final SaveInstalled saveInstalled = new SaveInstalled(installedMapsFile);
	
	private final static String zipFilesXml = "zipFiles.xml";

	final static File configFile = new File(appDataDirectory(), "config.properties");




	private EngineStarter starter;

	/**
	 * @todo 2010-02-09 12:11 hrehfeld    member variable seems unnecessary
	 */
	private PackageInteractionPanel interactionPanel;
	private RequirementList maps;
	/**
	 * @todo 2010-02-09 12:11 hrehfeld    member variable seems unnecessary
	 */
	private PackageList packages;
	/**
	 * @todo 2010-02-09 12:11 hrehfeld    member variable seems unnecessary
	 */
	private final PackageListModel maplist;
	private Installer installer;


	private final InstalledPackages installedMaps = new InstalledPackages();

	/** is offline mode enabled? */
	private Configuration.OfflineMode offline;

	private final Configuration config;

	private final Menu menu;

	public QuakeInjector() {
		super(applicationName);

		//load config
		final Future<Configuration> config = new SwingWorker<Configuration,Void>() {
			 public Configuration doInBackground() { return new Configuration(configFile); }
		};
		((SwingWorker<?,?>) config).execute();

		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		setLayout(new BoxLayout(getContentPane(),
								BoxLayout.PAGE_AXIS));

		maps = new RequirementList();
		packages = new PackageList(maps);
		maplist = new PackageListModel(packages);

		{
			setIconImages(createIconList(ICON_SIZES, ICON_URL, ICON_SIZE_PLACEHOLDER));
		}

		menu = createMenuBar();
		setJMenuBar(menu);

		setMinimumSize(new Dimension(minWidth, minHeight));
		
		addMainPane(getContentPane());

		addWindowListener(new QuakeInjectorWindowListener());


		Configuration cfg = null;
		try {
			cfg = config.get();
		}
		catch (ExecutionException e) {
			System.err.println("Couldn't load config: " + e.getCause());
			e.getCause().printStackTrace();
		}
		catch (InterruptedException e) {
			System.err.println("Interrupted: " + e);
		}
		this.config = cfg;

		this.offline = cfg.offlineMode;

		//config needed here
		setWindowSize();
	}

	/**
	 * main menu
	 */
	private Menu createMenuBar() {
		ActionListener parseDatabase = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					doParseInstalled();
					parseDatabaseAndSetList();
				}
			};

		ActionListener checkInstalled = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					checkForInstalledMaps();
				}
			};
		

		ActionListener quit = new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						setVisible(false);
						dispose();
					}
			};

		ActionListener showEngineConfig = new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						showEngineConfig(maps.get("rogue").isInstalled(),
						                 maps.get("hipnotic").isInstalled());
					}};
		ActionListener offlineModeChanged = new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						offline.set(!offline.get());
					}};

		return new Menu(parseDatabase, checkInstalled, quit, showEngineConfig, offlineModeChanged);
	}

	/**
	 * Try setting the saved window size and position
	 */
	private void setWindowSize() {
		Configuration c = getConfig();

		if (c.mainWindowWidth.exists() && c.mainWindowHeight.exists()) {
			int width = c.mainWindowWidth.get();
			int height = c.mainWindowHeight.get();
			if (c.mainWindowPositionX.exists() && c.mainWindowPositionY.exists()) {
				int posX = c.mainWindowPositionX.get();
				int posY = c.mainWindowPositionY.get();
				// System.out.println("Setting window bounds: "
				//                    + posX + ", "
				//                    + posY + ", "
				//                    + width + ", "
				//                    + height);
			
				setBounds(posX, posY, width, height);
			}
			else {
				// System.out.println("Setting window size: " + width + ", " + height);
				setSize(width, height);
			}
		}
		else {
			pack();
		}
	}
		

	/**
	 * Everything that may be run AFTER the initial window is shown should be run here
	 */
	private void init() {
		doParseInstalled();

		final Future<Void> requirementsListUpdater = parseDatabaseAndSetList();

		Configuration.EnginePath enginePath = getConfig().enginePath;
		File engineExe = new File("");
		if (getConfig().engineExecutable.existsOrDefault()) {
			engineExe = new File(enginePath.get()
			                          + File.separator
			                          + getConfig().engineExecutable);
		}
		starter = new EngineStarter(enginePath.get(),
		                            engineExe,
		                            getConfig().engineCommandLine);
		installer = new Installer(enginePath,
		                          getConfig().downloadPath);

		interactionPanel.init(installer,
		                      getConfig().repositoryBasePath,
		                      maps,
		                      starter,
		                      new SaveInstalled(installedMapsFile)
		    );

		if (!installer.checkInstallDirectory()) {
			//wait until database was loaded, then pop up config
			new SwingWorker<Void,Void>() {
				
			    public Void doInBackground() {
					try {
						requirementsListUpdater.get();
					}
					catch (java.lang.InterruptedException e) {}
					catch (java.util.concurrent.ExecutionException e) {}
					return null;
				}
				
			    public void done() {
					enginePathNotSetDialogue();
				}
			}.execute();
		}
	}


	private void doParseInstalled() {
		installedMaps.parse(installedMapsFile);
	}

	private InputStream downloadDatabase(String databaseUrl) throws IOException {
		//get download stream
		Download d = Download.create(databaseUrl);
		d.connect();
		int size = d.getSize();
		InputStream dl;
		// if (size > 0) {
		// 	ProgressListener progress =
		// 	    new SumProgressListener(new PercentageProgressListener(size, this));
		// 	dl = d.getStream(progress);
		// }
		// else {
		dl = d.getStream(null);
		//}
		
		return dl;
	}


	private List<Requirement> parseDatabase(InputStream database)
		throws IOException, org.xml.sax.SAXException {
		final PackageDatabaseParser parser = new PackageDatabaseParser();
		
		List<Requirement> all = parser.parse(XmlUtils.getDocument(database));

		return all;
	}

	/**
	 * Parse the online database
	 */
	private Future<List<Requirement>> doParseDatabase() {
		
		final String databaseUrl = getConfig().repositoryDatabasePath.get();
		
		final SwingWorker<List<Requirement>, Void> dbParse
		    = new SwingWorker<List<Requirement>,Void>() {
			/** we need to try to download the db to a tmp file first so the old one doesn't get overwritten */
			private File tmpFile;
			/** the cached database file */
			private File cache;
			/** the stream to the temporary file */
			private FileOutputStream cacheStream;
			
			
			public List<Requirement> doInBackground() throws IOException, org.xml.sax.SAXException {
				cache = getConfig().localDatabaseFile.get();
				cache = cache.getAbsoluteFile();
				InputStream db;
				try {
					//download database and dump to file
					tmpFile = File.createTempFile(cache.getName(), ".xml", cache.getParentFile());
					cacheStream = new FileOutputStream(tmpFile);
					OutputStream out = new BufferedOutputStream(cacheStream);
					db = new DumpInputStream(new BufferedInputStream(downloadDatabase(databaseUrl)), out);
				}
				catch (IOException e) {
					//try reading the cached version if downloading fails
					System.err.println("Downloading the database failed.");
					if (cache.exists() && cache.canRead()) {
						System.err.println("Using cached database file (" + cache + ") instead.");
						db = new BufferedInputStream(new FileInputStream(cache));
					}
					else {
						System.err.println("using cached database file instead.");
						throw e;
					}
				}
				return parseDatabase(db);
			}

			
			public void done() {
				try {
					cacheStream.close();
				}
				catch (IOException e) {
					System.out.println("Couldn't close tmp cache outputstream!" + e);
				}

				if (cache.exists() && !cache.delete()) {
					System.err.println("Couldn't delete the real cache file!");
				}
				if (tmpFile.renameTo(cache) != true) {
					System.err.println("Couldn't move the temporary cache file to the real cache file!");
				}
			}
		};

		final ProgressPopup dbpopup = new ProgressPopup("Downloading package database",
		                      new ActionListener() {
								  public void actionPerformed(ActionEvent e) {
									  dbParse.cancel(true);
								  }
							  },
		                      QuakeInjector.this);

		dbParse.addPropertyChangeListener(new PropertyChangeListener() {
				
				public void propertyChange(PropertyChangeEvent evt) {
					if (evt.getPropertyName() == "progress") {
						int p = (Integer) evt.getNewValue();
						dbpopup.setProgress(p);
					}
					else if (evt.getPropertyName() == "state"
					    && evt.getNewValue().equals(SwingWorker.StateValue.DONE)) {
						dbpopup.close();
					}
				}
			});
		dbParse.execute();
		dbpopup.pack();
		dbpopup.setVisible(true);

		return dbParse;
	}



	/**
	 * See what maps are installed
	 */
	private Future<List<PackageFileList>> checkForInstalledMaps() {
		final File enginePath = getConfig().enginePath.get();

		final File file = new File(appDataDirectory(), zipFilesXml);

		final CheckInstalled checker
		    = new CheckInstalled(this,
		                         getConfig().zipContentsDatabaseUrl.get(),
		                         getConfig().enginePath.get().toString(),
		                         maps,
		        saveInstalled);

		final ProgressPopup dbpopup =
		    new ProgressPopup("Checking for installed maps",
		                      new ActionListener() {

								  public void actionPerformed(ActionEvent e) {
									  checker.cancel(true);
								  }
							  },
		                      QuakeInjector.this);

		checker.addPropertyChangeListener(new PropertyChangeListener() {
				
				public void propertyChange(PropertyChangeEvent evt) {
					if (evt.getPropertyName() == "progress") {
						int p = (Integer) evt.getNewValue();
						dbpopup.setProgress(p);
					}
					else if (evt.getPropertyName() == "state"
					    && evt.getNewValue().equals(SwingWorker.StateValue.DONE)) {
						dbpopup.close();
					}
				}
			});
		checker.execute();
		dbpopup.pack();
		dbpopup.show();

		return checker;
	}
	

	/**
	 * Tell maps what maps are already installed
	 */
	void setInstalledStatus(final List<PackageFileList> packages) {
		for (PackageFileList l: packages) {
			maps.setInstalled(l);
		}
		for (Requirement r: maps) {
			//System.out.println(r);
		}
		
		maps.notifyChangeListeners();

		
	}

	private Future<Void> parseDatabaseAndSetList() {
		final Future<List<Requirement>> dbParse = doParseDatabase();

		SwingWorker<Void,Void> waitForInstalledMapsAndDb = new SwingWorker<Void,Void>() {
			 public Void doInBackground() throws Exception {
				//just wait
				installedMaps.get();
				dbParse.get();

				return null;
			}

			public void done() {
				List<Requirement> packages = null;
				try {
					packages = dbParse.get();
				}
				catch (InterruptedException e) {
					throw new RuntimeExecutionException("parsing database", e);
				}
				catch (ExecutionException e) {
					String ERROR_MESSAGE = "Database parsing failed!";
					Throwable err = e.getCause();
					String msg = err.getMessage();
					try {
						throw err;
					}
					catch (java.net.UnknownHostException exc) {
						msg = "Couldn't establish connection to the server (" + err.getMessage() + ").";
						offline.set(true);
					}
					catch (Throwable any) { /*do nothing*/; }
					
					JOptionPane.showMessageDialog(QuakeInjector.this,
					                              ERROR_MESSAGE + " " + msg,
					                              ERROR_MESSAGE,
					                              JOptionPane.ERROR_MESSAGE);
					return;
				}

				maps.setRequirements(packages);
				System.out.println("Setting Requirements");

				try {
					setInstalledStatus(installedMaps.get());
				}
				catch (InterruptedException e) {
					System.err.println("Interrupted while getting installed maps" + e);
					e.printStackTrace();
				}
				catch (ExecutionException err) {
					maps.notifyChangeListeners();
					
					try {
						throw err.getCause();
					}
					catch (InstalledPackages.NoInstalledPackagesFileException e) {
						System.err.println(e.getMessage());
					}
					catch (Throwable e) {
						String ERROR_MESSAGE = "Reading installed maps failed!";
						JOptionPane.showMessageDialog(QuakeInjector.this,
						                              ERROR_MESSAGE + " " + e.getMessage(),
						                              ERROR_MESSAGE,
						                              JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		};
		waitForInstalledMapsAndDb.execute();
		return waitForInstalledMapsAndDb;
	}

	private void showEngineConfig(boolean rogueInstalled, boolean hipnoticInstalled) {
		final EngineConfigDialog d
		    = new EngineConfigDialog(QuakeInjector.this,
		                             getConfig().enginePath,
		                             getConfig().engineExecutable,
		                             getConfig().downloadPath,
		                             getConfig().engineCommandLine,
		                             rogueInstalled,
		                             hipnoticInstalled
		        );
		d.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					try {
						saveEngineConfig(d.getEnginePath(),
						                 d.getEngineExecutable(),
						                 d.getDownloadPath(),
						                 d.getCommandline(),
						                 d.getRogueInstalled(),
						                 d.getHipnoticInstalled());
					}
					catch (IOException err) {
						savingFailedDialogue(err);
					}
				}
			});

		d.pack();
		d.setLocationRelativeTo(this);
		d.show();
		
	}


	private void savingFailedDialogue(IOException e) {
		String msg = "Saving the configuration file failed: " + e.getMessage() + "\n"
		    + "The directory is probably read-only and cannot be set writable automatically (Vista/Win7 bug), try to set write permissions manually." ;
		JOptionPane.showMessageDialog(QuakeInjector.this,
		                              msg,
		                              "Saving configuration failed!",
		                              JOptionPane.ERROR_MESSAGE);
	}

	private void saveEngineConfig(File enginePath,
								  File engineExecutable,
	                              File downloadPath,
	                              String commandline,
	                              boolean rogueInstalled,
	                              boolean hipnoticInstalled) throws IOException {
		

		Configuration c = getConfig();
		c.enginePath.set(enginePath);
		c.engineExecutable.set(RelativePath.getRelativePath(enginePath, engineExecutable));
		c.engineCommandLine.set(commandline);

		c.downloadPath.set(downloadPath);

		setEngineConfig(enginePath, engineExecutable, getConfig().engineCommandLine, rogueInstalled, hipnoticInstalled);


		try {
			c.write();
		}
		catch (IOException e) {
			File dir = configFile.getAbsoluteFile().getParentFile();
			System.out.println("Trying to set directory (" + dir + ") writable..");
			try {
				//dir.setWritable(true);
			}
			catch (SecurityException securityError) {
				System.out.println("Couldn't set writable: " + securityError);
			}

			c.write();
		}
	}

	/**
	 * @todo 2010-02-09 12:19 hrehfeld    Let this use configuration values to their full extent
	 */
	private void setEngineConfig(File enginePath,
								 File engineExecutable,
	                             Configuration.EngineCommandLine commandline,
	                             boolean rogueInstalled,
	                             boolean hipnoticInstalled) {
		starter.setQuakeDirectory(enginePath);
		starter.setQuakeExecutable(engineExecutable);
		starter.setQuakeCommandline(commandline);

		maps.get("rogue").setInstalled(rogueInstalled);
		maps.get("hipnotic").setInstalled(hipnoticInstalled);
		try {
			synchronized (maps) {
				saveInstalled.write(maps);
			}
		}
		catch (java.io.IOException e) {}
	}

	private void addMainPane(Container panel) {
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new GridBagLayout());

		
		//create a table
		final PackageTable table =  new PackageTable(maplist);
		maplist.size(table);

		{
			JPanel filterPanel = new JPanel();
			filterPanel.setLayout(new BoxLayout(filterPanel, BoxLayout.LINE_AXIS));
			JLabel filterText = new JLabel("Filter: ", SwingConstants.TRAILING);
			filterPanel.add(filterText);
			filterPanel.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));

			final JTextField filter = new JTextField();
			filter.getDocument().addDocumentListener(
                new DocumentListener() {
                    public void changedUpdate(DocumentEvent e) { filter(); }
                    public void insertUpdate(DocumentEvent e) { filter(); }
                    public void removeUpdate(DocumentEvent e) { filter(); }

					private void filter() {
						table.getRowSorter().setRowFilter(maplist.filter(filter.getText()));
					}
                });
			filterText.setLabelFor(filter);
			filterPanel.add(filter);

			mainPanel.add(filterPanel, new GridBagConstraints() {{
				anchor = LINE_START;
				fill = HORIZONTAL;
				weightx = 1;
				weighty = 0;
			}});

		}
		

		//Create the scroll pane and add the table to it.
		JScrollPane scrollPane = new JScrollPane(table);


		mainPanel.add(scrollPane, new GridBagConstraints() {{
				anchor = CENTER;
				fill = BOTH;
				gridx = 0;
				gridy = 1;
				gridwidth = 1;
				gridheight = 1;
				weightx = 1;
				weighty = 1;
			}});

		final InstallQueuePanel installQueue = new InstallQueuePanel();

		this.interactionPanel = new PackageInteractionPanel(this, installQueue);

		JPanel infoPanel = new JPanel(new GridBagLayout());

		PackageDetailPanel details = new PackageDetailPanel();
		infoPanel.add(details, new GridBagConstraints() {{
			anchor = PAGE_START;
			fill = BOTH;
			weightx = 1;
			weighty = 1;
		}});

		infoPanel.add(interactionPanel, new GridBagConstraints() {{
			gridy = 1;
			fill = BOTH;
			weightx = 1;
		}});

// 		JLabel queueLabel = new JLabel("Install Queue");
// 		infoPanel.add(queueLabel, new GridBagConstraints() {{
// 			anchor = PAGE_END;
// 			fill = BOTH;
// 			gridy = 2;
// 			weightx = 1;
// 		}});

		JScrollPane queueScroll = new JScrollPane(installQueue);
		infoPanel.add(queueScroll, new GridBagConstraints() {{
			anchor = PAGE_END;
			fill = BOTH;
			gridy = 3;
			weightx = 1;
			weighty = 1;
		}});

		JSplitPane infoSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
		                                      infoPanel,
		                                      queueScroll);
		infoSplit.setOneTouchExpandable(true);
		infoSplit.setResizeWeight(1);
		infoSplit.setContinuousLayout(true);
		infoSplit.setDividerLocation(400);
		infoSplit.setMinimumSize(new Dimension(200, 300));
		
		PackageListSelectionHandler selectionHandler
			= new PackageListSelectionHandler(maplist,
											  table);
		table.getSelectionModel().addListSelectionListener(selectionHandler);
		selectionHandler.addSelectionListener(interactionPanel);
		selectionHandler.addSelectionListener(details);


		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
		                                      mainPanel,
		                                      infoSplit);
		splitPane.setOneTouchExpandable(true);
		splitPane.setResizeWeight(1);
		splitPane.setContinuousLayout(true);
		splitPane.setMinimumSize(new Dimension(450, 300));

		panel.add(splitPane);

		
	}

	
	private void display() {
		//pack();
		setVisible(true);
	}

	private Configuration getConfig() {
		if (config == null) {
			throw new RuntimeException("Config not initialised!");
		}
		return config;
	}

	/**
	 * @return false if the user didn't open the config dialog
	 */
	public boolean enginePathNotSetDialogue() {
		String msg = "Quakepath isn't set correctly.\n"
		    + "It  needs to be set before trying to install (or play).";

		Object[] options = {"Open Engine Configuration",
		                    "Cancel"};
		int openEngineConfig =
		    JOptionPane.showOptionDialog(QuakeInjector.this,
		                                 msg,
		                                 "Quakepaths incorrect",
		                                 JOptionPane.YES_NO_OPTION,
		                                 JOptionPane.ERROR_MESSAGE,
		                                 null,
		                                 options,
		                                 options[0]);
		//button for engine config pressed
		if (openEngineConfig == 0) {
			//wait until maps are finished loading
			showEngineConfig(maps.get("rogue").isInstalled(),
			                 maps.get("hipnotic").isInstalled());
			return true;
		}
		else {
			return false;
		}
	}


	public static void main(String[] args) {
		// Use Mac OS menu bar
		if (QuakeInjector.isMacOSX()) {
			try {
				System.setProperty("apple.laf.useScreenMenuBar", "true");
			}
			catch (SecurityException e) {
			}
		}

		try {
        // Set System L&F
			javax.swing.UIManager.setLookAndFeel(
				javax.swing.UIManager.getSystemLookAndFeelClassName());
		} 
		catch (javax.swing.UnsupportedLookAndFeelException e) {
		}
		catch (ClassNotFoundException e) {
		}
		catch (InstantiationException e) {
		}
		catch (IllegalAccessException e) {
		}

		javax.swing.SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					QuakeInjector qs = new QuakeInjector();
					qs.display();
					qs.init();
				}
			});

	}

	private class QuakeInjectorWindowListener extends WindowAdapter
	{
		
		public void windowClosing(WindowEvent e) {
			if (installer.working()) {
				String msg = "There are maps left in the install queue. Wait until they are finished installing?";

				Object[] options = {"Wait",
				                    "Close immediately"};
				int optionDialog =
				    JOptionPane.showOptionDialog(QuakeInjector.this,
				                                 msg,
				                                 "Maps still installing",
				                                 JOptionPane.YES_NO_OPTION,
				                                 JOptionPane.WARNING_MESSAGE,
				                                 null,
				                                 options,
				                                 options[0]);
				if (optionDialog == 0) {
					setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
					return;
				}
				else {
					installer.cancelAll();
					setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				}
			}
			windowClosed(e);
		}
		
		public void windowClosed(WindowEvent e)
		{
			Configuration config = getConfig();
			Rectangle bounds = QuakeInjector.this.getBounds();
			config.mainWindowPositionX.set((int) bounds.getX());
			config.mainWindowPositionY.set((int) bounds.getY());
			config.mainWindowWidth.set((int) bounds.getWidth());
			config.mainWindowHeight.set((int) bounds.getHeight());

			try {
				config.write();
			}
			catch (IOException err) {
				savingFailedDialogue(err);
			}
			//System.out.println("Closing Window: " + (int) bounds.getWidth() + (int) bounds.getHeight());


			System.exit(0);
		}

	}

	private static List<Image> createIconList(int[] iconSizes, String iconUrl, String sizeToken) {
			List<Image> icons = new ArrayList<Image>(iconSizes.length);
			for (int size: iconSizes) {
				String path = iconUrl.replace(sizeToken, Integer.toString(size));
				try {
					javax.swing.ImageIcon icon = Utils.createImageIcon(path, "Icon" + size);
					icons.add(icon.getImage());
				}
				catch (IOException e) {
					System.err.println("WARNING: Couldn't load icon file " + path);
				}
			}
			return icons;
	}

	public static boolean isMacOSX() {
		return System.getProperty("os.name").startsWith("Mac OS X");
	}

	public static boolean isWindows() {
		return System.getProperty("os.name").startsWith("Windows");
	}

	public static File appDataDirectory() {
		// It's rediculous that Java doesn't provide this.
		String path;
		if (isMacOSX()) {
			path = System.getProperty("user.home") + "/Library/Application Support/" + applicationName;
		} else if (isWindows()) {
			path = System.getenv("APPDATA") + File.separator + applicationName;
		} else { // For *nix-like and other systmes, store in ~/.quakeinjector
			path = System.getProperty("user.home") + File.separator + ".quakeinjector";
		}
		
		File dir = new File(path);
		try {
			if (!dir.exists()) {
				dir.mkdirs();
			}
		} catch (SecurityException e) {
			System.err.println("WARNING: Couldn't create app data directory " + path);
		}
		return dir;
	}
}
