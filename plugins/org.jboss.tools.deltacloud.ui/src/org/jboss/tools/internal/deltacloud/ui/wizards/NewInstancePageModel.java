/*******************************************************************************
 * Copyright (c) 2010 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.internal.deltacloud.ui.wizards;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jboss.tools.deltacloud.core.DeltaCloudHardwareProfile;
import org.jboss.tools.deltacloud.core.DeltaCloudImage;
import org.jboss.tools.deltacloud.core.DeltaCloudRealm;
import org.jboss.tools.internal.deltacloud.ui.common.databinding.validator.ObservableUIPojo;

/**
 * @author Jeff Jonhston
 * @author André Dietisheim
 */
public class NewInstancePageModel extends ObservableUIPojo {

	public static final String PROPERTY_URL = "url"; //$NON-NLS-1$
	public static final String PROPERTY_ALIAS = "alias"; //$NON-NLS-1$
	public static final String PROPERTY_IMAGE = "image"; //$NON-NLS-1$
	public static final String PROPERTY_ARCH = "arch"; //$NON-NLS-1$
	public static final String PROPERTY_REALMS = "realms"; //$NON-NLS-1$
	public static final String PROPERTY_SELECTED_REALM_INDEX = "selectedRealmIndex"; //$NON-NLS-1$
	public static final String PROPERTY_KEYID = "keyId"; //$NON-NLS-1$
	public static final String PROP_PROFILE = "profile"; //$NON-NLS-1$
	public static final String PROP_ALL_PROFILES = "allProfiles"; //$NON-NLS-1$
	public static final String PROP_FILTERED_PROFILES = "filteredProfiles"; //$NON-NLS-1$
	public static final String PROP_SELECTED_PROFILE_INDEX = "selectedProfileIndex"; //$NON-NLS-1$

	private String alias;
	private DeltaCloudImage image;
	private String arch;
	private String keyId;
	private DeltaCloudRealm selectedRealm;
	private List<DeltaCloudRealm> realms = new ArrayList<DeltaCloudRealm>();
	private DeltaCloudHardwareProfile selectedProfile;
	private List<DeltaCloudHardwareProfile> allProfiles = new ArrayList<DeltaCloudHardwareProfile>();
	private List<DeltaCloudHardwareProfile> filteredProfiles = new ArrayList<DeltaCloudHardwareProfile>();
	private String cpu;
	private String storage;
	private String memory;

	protected NewInstancePageModel(String keyId, DeltaCloudImage image) {
		this.keyId = keyId;
		this.image = image;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		firePropertyChange(PROPERTY_ALIAS, this.alias, this.alias = alias);
	}

	public DeltaCloudImage getImage() {
		return image;
	}

	public void setImage(DeltaCloudImage image) {
		firePropertyChange(PROPERTY_IMAGE, this.image, this.image = image);
		List<DeltaCloudHardwareProfile> filteredProfiles = filterProfiles(image, allProfiles);
		setFilteredProfiles(filteredProfiles);
		setArch(image.getArchitecture());
	}

	public void setSelectedRealmIndex(int index) {
		if (realms.size() > index) {
			int oldIndex = -1;
			if (selectedRealm != null
					&& realms.size() > 0) {
				oldIndex = realms.indexOf(selectedRealm);
			}
			DeltaCloudRealm deltaCloudRealm = realms.get(index);
			setSelectedRealm(deltaCloudRealm);
			firePropertyChange(PROPERTY_SELECTED_REALM_INDEX, oldIndex, index);
		}
	}

	public int getSelectedRealmIndex() {
		return realms.indexOf(selectedRealm);
	}

	public void setSelectedRealm(DeltaCloudRealm realm) {
		selectedRealm = realm;
	}

	public String getRealmId() {
		if (selectedRealm == null) {
			return null;
		}
		return selectedRealm.getId();
	}

	protected void setRealms(List<DeltaCloudRealm> realms) {
		firePropertyChange(PROPERTY_REALMS, this.realms, this.realms = realms);
		setSelectedRealmIndex(0);
	}

	public List<DeltaCloudRealm> getRealms() {
		return realms;
	}

	protected void setAllProfiles(List<DeltaCloudHardwareProfile> profiles) {
		firePropertyChange(PROP_ALL_PROFILES, this.allProfiles, this.allProfiles = profiles);
		setFilteredProfiles(filterProfiles(image, profiles));
	}

	public List<DeltaCloudHardwareProfile> getAllProfiles() {
		return allProfiles;
	}

	private void setFilteredProfiles(List<DeltaCloudHardwareProfile> profiles) {
		firePropertyChange(PROP_FILTERED_PROFILES, this.filteredProfiles, this.filteredProfiles = profiles);
		setSelectedProfileIndex(0);
	}

	public List<DeltaCloudHardwareProfile> getFilteredProfiles() {
		return filteredProfiles;
	}

	private List<DeltaCloudHardwareProfile> filterProfiles(DeltaCloudImage image,
			Collection<DeltaCloudHardwareProfile> profiles) {
		List<DeltaCloudHardwareProfile> filteredProfiles = new ArrayList<DeltaCloudHardwareProfile>();
		for (DeltaCloudHardwareProfile p : profiles) {
			if (p.getArchitecture() == null
					|| image == null
					|| image.getArchitecture().equals(p.getArchitecture())) {
				filteredProfiles.add(p);
			}
		}

		return filteredProfiles;
	}

	public void setSelectedProfileIndex(int index) {
		if (filteredProfiles.size() > index) {
			int oldIndex = -1;
			if (selectedProfile != null
					&& filteredProfiles.size() > 0) {
				oldIndex = filteredProfiles.indexOf(selectedProfile);
			}
			DeltaCloudHardwareProfile hardwareProfile = filteredProfiles.get(index);
			setSelectedProfile(hardwareProfile);
			firePropertyChange(PROP_SELECTED_PROFILE_INDEX, oldIndex, index);
		}
	}

	public int getSelectedProfileIndex() {
		if (filteredProfiles == null ||
				filteredProfiles.size() <= 0) {
			return -1;
		}
		return filteredProfiles.indexOf(selectedProfile);
	}

	public void setSelectedProfile(DeltaCloudHardwareProfile profile) {
		selectedProfile = profile;
	}

	public String getProfileId() {
		if (selectedProfile == null) {
			return null;
		}
		return selectedProfile.getId();
	}

	public String getKeyId() {
		return keyId;
	}

	public void setKeyId(String keyId) {
		getPropertyChangeSupport().firePropertyChange(PROPERTY_KEYID, this.keyId, this.keyId = keyId);
	}

	public String getArch() {
		return arch;
	}

	public void setArch(String arch) {
		getPropertyChangeSupport().firePropertyChange(PROPERTY_ARCH, this.arch, this.arch = arch);
	}

	public int getSelectedProfile() {
		return allProfiles.indexOf(selectedProfile);
	}

	public void setCpu(String cpu) {
		this.cpu = cpu;
	}

	public String getCpu() {
		return this.cpu;
	}

	public void setMemory(String memory) {
		this.memory = memory;
	}

	public String getMemory() {
		return this.memory;
	}

	public void setStorage(String storage) {
		this.storage = storage;
	}

	public String getStorage() {
		return this.storage;
	}
}