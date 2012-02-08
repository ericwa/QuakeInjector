package de.haukerehfeld.quakeinjector;

import javax.swing.event.ChangeListener;

/**
 * A requirement that doesn't have a package available and thus can't be installed (like the quake mission packs)
 * 
 * Immutable.
 */
public class UnavailableRequirement extends SortableRequirement implements Requirement {
	public UnavailableRequirement(String id) {
		super(id);
	}
	
	public void addChangeListener(ChangeListener l) {}
	
	protected void notifyChangeListeners() {}

	public PackageFileList getFileList() {
		return new PackageFileList(getId());
	}
	
	public void removeChangeListener(ChangeListener l) {}
}

