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
package org.jboss.tools.deltacloud.ui.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;
import org.jboss.tools.common.ui.WizardUtils;
import org.jboss.tools.deltacloud.core.DeltaCloud;
import org.jboss.tools.internal.deltacloud.ui.utils.WorkbenchUtils;
import org.jboss.tools.internal.deltacloud.ui.wizards.InstanceFilterWizard;

/**
 * @author Andre Dietisheim
 */
public class FilterInstancesHandler extends AbstractHandler implements IHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		if (selection instanceof IStructuredSelection) {
			DeltaCloud deltaCloud = WorkbenchUtils.getFirstAdaptedElement(selection, DeltaCloud.class);
			if (deltaCloud != null) {
				createInstancesFilter(deltaCloud, HandlerUtil.getActiveShell(event));
			} else {
				// no item selected: try active part
				deltaCloud = WorkbenchUtils.adapt(HandlerUtil.getActivePart(event), DeltaCloud.class);
				createInstancesFilter(deltaCloud, HandlerUtil.getActiveShell(event));
			}
		}

		return Status.OK_STATUS;
	}

	private void createInstancesFilter(final DeltaCloud cloud, final Shell shell) {
		if (cloud != null) {
			Display.getDefault().asyncExec(new Runnable() {

				@Override
				public void run() {
					WizardUtils.openWizardDialog(new InstanceFilterWizard(cloud), shell);
				}
			});
		}
	}
}
