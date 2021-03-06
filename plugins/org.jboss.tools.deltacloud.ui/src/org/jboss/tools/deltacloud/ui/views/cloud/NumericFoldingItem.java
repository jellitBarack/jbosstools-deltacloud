/*******************************************************************************
 * Copyright (c) 2010 Red Hat Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/
package org.jboss.tools.deltacloud.ui.views.cloud;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.views.properties.IPropertySource;

/**
 * @author Jeff Johnston
 * @author Andre Dietisheim
 */
public class NumericFoldingItem extends DeltaCloudViewItem<Object> {

	public static final int FOLDING_SIZE = 50;
	private Object min;
	private int max;

	protected NumericFoldingItem(int min, int max, DeltaCloudViewItem<?> parent, TreeViewer viewer) {
		super(null, parent, viewer);
		this.min = min;
		this.max = max;
	}

	@Override
	public String getName() {
		return new StringBuilder()
				.append("[")
				.append(min)
				.append("..")
				.append(max - 1)
				.append("]").toString();
	}

	@Override
	public Object[] getChildren() {
		return super.getChildren();
	}

	@Override
	public boolean hasChildren() {
		return true;
	}

	@Override
	public IPropertySource getPropertySource() {
		return null;
	}
}
