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
package org.jboss.tools.internal.deltacloud.ui.preferences;

/**
 * @author Andre Dietisheim
 */
public class StringEntriesPreferenceValue extends AbstractPreferenceValue<String[]> {

	private String delimiter;

	public StringEntriesPreferenceValue(String delimiter, String prefsKey, String pluginId) {
		super(prefsKey, pluginId);
		this.delimiter = delimiter;
	}

	public String[] get() {
		return get(null);
	}

	public String[] get(String[] currentValues) {

		String string = doGet(null);
		String[] prefValues = string.split(delimiter);
		return overrideValues(currentValues, prefValues);
	}

	private String[] overrideValues(String[] newValues, String[] prefValues) {
		if (prefValues == null) {
			return newValues;
		}

		for (int i = 0; i < prefValues.length; i++) {
			if (newValues == null
					|| newValues.length < i) {
				break;
			}
			prefValues[i] = newValues[i];
		}
		return prefValues;
	}

	public void add(String value) {
		StringBuilder builder = new StringBuilder(doGet());
		if (builder.length() > 0) {
			builder.append(delimiter);
		}
		builder.append(value);
		doStore(builder.toString());
	}

	/**
	 * Removes the given values from the strings stored in the preferences and
	 * stores the preferences.
	 * 
	 * @param values
	 *            the values
	 */
	public void remove(String... valuesToRemove) {
		String[] currentValues = get();
		if (valuesToRemove != null) {
			for (int i = 0; i < currentValues.length; i++) {
				for (String valueToRemove : valuesToRemove) {
					if (valueToRemove.equals(currentValues[i])) {
						currentValues[i] = null;
					}
				}
			}
		}
		store(currentValues);
	}

	/**
	 * Overrides the current values in the preferences with the values in the
	 * given array (value in the preferences at index x is overridden with the
	 * value in the given array at index x) and stores the preferences.
	 */
	public void store(String[] newValues) {
		String[] currentValues = get();
		overrideValues(newValues, currentValues);
		doStore(concatenate(currentValues));
	}

	protected String concatenate(String[] values) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < values.length; i++) {
			if (values[i] != null) {
				if (builder.length() > 0) {
					builder.append(delimiter);
				}
				builder.append(values[i]);
			}
		}
		return builder.toString();
	}
}