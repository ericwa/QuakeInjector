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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.event.ChangeListener;

/**
 * A requirement that has an installation candidate available.
 */
public class Package extends SortableRequirement implements Requirement {

	/**
	 * easily have change listeners
	 */
	private ChangeListenerList listeners = new ChangeListenerList();

	private String author;

	private String title;

	private Rating rating;

	private String description;

	/**
	 * Size in kb?
	 */
	private int size;

	private Date date;

	private String relativeBaseDir;

	private String commandline;

	private List<String> startmaps;
	
	private List<Requirement> requirements;

	private PackageFileList fileList;
	private PackageFileList supposedFileList;

	public Package(String id,
				   String author,
				   String title,
				   int size,
				   Date date,
	               boolean isInstalled,
	               Rating rating,
	               String description) {
		this(id, author, title, size, date, isInstalled, rating, description, null, null, null, null);
	}

	public Package(String id,
				   String author,
				   String title,
				   int size,
				   Date date,
				   boolean isInstalled,
	               Rating rating,
	               String description,
				   String relativeBaseDir,
				   String commandline,
				   List<String> startmaps,
				   List<Requirement> requirements) {
		super(id);
		this.author = author;
		this.title = title;
		this.size = size;
		this.date = date;
		super.setInstalled(isInstalled);
		this.rating = rating;
		this.description = description;
		this.relativeBaseDir = relativeBaseDir;
		this.commandline = commandline;
		this.startmaps = startmaps;
		this.requirements = requirements;
	}
	
	public void addChangeListener(ChangeListener l) {
		listeners.addChangeListener(l);
	}


	public void removeChangeListener(ChangeListener l) {
		listeners.removeChangeListener(l);
	}
	
	public String getAuthor() {
		return author;
	}
	public String getTitle() {
		return title;
	}
	public int getSize() {
		return size;
	}
	public Date getDate() {
		return date;
	}

	/**
	 * get rating
	 */
	public Rating getRating() { return rating; }


	/**
	 * get description
	 */
	public String getDescription() { return description; }
	

	public String getRelativeBaseDir() {
		return relativeBaseDir;
	}

	public String getCommandline() {
		return commandline;
	}

	public List<String> getStartmaps() {
		return startmaps;
	}

	public void setRequirements(List<Requirement> requirements) {
		this.requirements = requirements;
	}

	public List<Requirement> getRequirements() {
		return this.requirements;
	}


	public List<Package> getAvailableRequirements() {
		List<Package> avails = new ArrayList<Package>();
		for (Requirement r: requirements) {
			if (r instanceof Package) {
				avails.add((Package) r);
			}
		}
		return avails;
	}


	public List<Requirement> getUnavailableRequirements() {
		List<Requirement> unavails = new ArrayList<Requirement>();

		for (Requirement r: requirements) {
			if (!r.isInstalled() && !(r instanceof Package)) {
				unavails.add(r);
			}
		}
		return unavails;
	}

	public List<Requirement> getUnmetRequirements() {
		List<Requirement> unmet = new ArrayList<Requirement>();
		for (Requirement requirement: requirements) {
			if (!requirement.isInstalled()) {
				unmet.add(requirement);
			}
		}
		
		return unmet;
	}

	
	protected void notifyChangeListeners() {
		listeners.notifyChangeListeners(this);
	}

	
	public String toString() {
		return getId() + " (" + isInstalled() + ")";
	}

	/**
	 * get fileList
	 */
	public PackageFileList getFileList() { return fileList; }
	
/**
 * set fileList
 */
	public void setFileList(PackageFileList fileList) { this.fileList = fileList; }

	/**
	 * get supposedFileList
	 */
	public PackageFileList getSupposedFileList() { return supposedFileList; }
    
/**
 * set supposedFileList
 */
	public void setSupposedFileList(PackageFileList supposedFileList) { this.supposedFileList = supposedFileList; }

	public static enum Rating {
		Unrated(0),
		    Crap(1),
		    Poor(2),
		    Average(3),
		    Nice(4),
		    Excellent(5);

		private int rating;
		Rating(int rating) {
			this.rating = rating;
		}

		public int getRating() {
			return rating;
		}
	}
}

