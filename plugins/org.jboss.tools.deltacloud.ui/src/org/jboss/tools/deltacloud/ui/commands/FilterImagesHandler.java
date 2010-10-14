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
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;
import org.jboss.tools.deltacloud.core.DeltaCloud;
import org.jboss.tools.deltacloud.ui.views.CVCloudElement;
import org.jboss.tools.deltacloud.ui.views.CloudViewElement;
import org.jboss.tools.internal.deltacloud.ui.utils.UIUtils;
import org.jboss.tools.internal.deltacloud.ui.wizards.ImageFilter;

/**
 * @author Andre Dietisheim
 */
public class FilterImagesHandler extends AbstractHandler implements IHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		if (selection instanceof IStructuredSelection) {
			CloudViewElement cloudViewElement = UIUtils.getFirstElement(selection, CloudViewElement.class);
			createImagesFilter(cloudViewElement, HandlerUtil.getActiveShell(event));
		}

		return Status.OK_STATUS;
	}

	private void createImagesFilter(CloudViewElement element, final Shell shell) {
		if (element != null) {
			while (element != null && !(element	 instanceof CVCloudElement)) {
				element = (CloudViewElement) element.getParent();
			}
			if (element != null) {
				CVCloudElement cve = (CVCloudElement) element;
				final DeltaCloud cloud = (DeltaCloud) cve.getElement();
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						IWizard wizard = new ImageFilter(cloud);
						WizardDialog dialog = new WizardDialog(shell, wizard);
						dialog.create();
						dialog.open();
					}
				});
			}

		}
	}
}